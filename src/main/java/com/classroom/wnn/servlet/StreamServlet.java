package com.classroom.wnn.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.classroom.wnn.bean.Range;
import com.classroom.wnn.model.BiVideoInfo;
import com.classroom.wnn.model.BiZoneInfo;
import com.classroom.wnn.service.RedisService;
import com.classroom.wnn.service.VideoService;
import com.classroom.wnn.task.RedisThreadPool;
import com.classroom.wnn.task.UploadHDFSTask;
import com.classroom.wnn.util.Convert;
import com.classroom.wnn.util.IoUtil;
import com.classroom.wnn.util.SpringContextHelper;
import com.classroom.wnn.util.TokenUtil;
import com.classroom.wnn.util.constants.Constants;
import com.classroom.wnn.util.lock.RedisLockUtil;


/**
 * 这种切分的办法是错误的 2016年9月1日
 * 必须使用jdk1.7及以上
 * 文件上传
 * part, stored it.
 * 会先调用 doGet 方法进行信息初始化
 * 然后将要上传的数据进行分片，并多次调用 doPost方法进行上传
 * 实现分块存储，可以在存储到hdfs的时候选择不同的namenode，以减轻hdfs压力，以及播放的时候选择减轻压力（因为实在搞不定前端~所以讲分块信息保存到了redis中，最好能保存到req中）
 * 
 * 现在太依赖redis了~~要是redis
 * @author java_speed
 * 修改时间 2016年8月18日
 * 修改人 lgh
 */
public class StreamServlet extends HttpServlet {
	private static final long serialVersionUID = -8619685235661387895L;
	private static Logger logger = Logger.getLogger(StreamServlet.class);
	
	static final int BUFFER_LENGTH = 10240;
	static final String START_FIELD = "start";
	public static final String CONTENT_RANGE_HEADER = "content-range";
	private static final String UPLOAD_ZONE_KEY = "upload-zone-key";
	
	private RedisThreadPool redisThreadPool;
	private VideoService videoService;
	private SpringContextHelper contextHelper;
	private RedisService redisService;

	@Override
	public void init() throws ServletException {
		ServletContext servletContext = this.getServletContext();  
		WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
		redisThreadPool = (RedisThreadPool) ctx.getBean("redisThreadPool");
		videoService = (VideoService) ctx.getBean("videoService");
		contextHelper = (SpringContextHelper) ctx.getBean("contextHelper");
		redisService = (RedisService) ctx.getBean("redisService");
	}
	
	/**
	 * Lookup where's the position of this file?
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		doOptions(req, resp);
		final String token = req.getParameter(Constants.TOKEN_FIELD);
		final String size = req.getParameter(Constants.FILE_SIZE_FIELD);
		String fileName = new String(req.getParameter(Constants.FILE_NAME_FIELD).getBytes("ISO-8859-1"), "UTF-8");
		final PrintWriter writer = resp.getWriter();
		logger.info("开始上传文件-----"+fileName+"---"+req.getParameter("param1"));
		
		/** TODO: validate your token. */
		JSONObject json = new JSONObject();
		long start = 0;
		boolean success = true;
		String message = "";
		try {
			File f = IoUtil.getTokenedFile(token);
			start = f.length();
			/*
			 * 查看有没有分片的文件,无用
			 * */
			String zoneSize = redisService.get(UPLOAD_ZONE_KEY+"-start-"+token);
			if(!StringUtils.isBlank(zoneSize)){
				start = start + Long.parseLong(zoneSize);
			}
			/** file size is 0 bytes. */
			/*如果文件大小为空，则删除token文件，并报错*/
			if (token.endsWith("_0") && "0".equals(size) && 0 == start){
				f.delete();
				success = false;
				message = "Error: 文件大小为空";
			}else if(start == 0){//将文件信息写入数据库,并标记为不可用(未上传完成)
				BiVideoInfo dto = new BiVideoInfo();
				dto.setvName(fileName);
				dto.setvAvailable(2);
				videoService.insertVideo(dto);
				Integer viceoId = dto.getId();//获得视频ID
				redisService.set("file_upload_id"+token, String.valueOf(viceoId));
			}
		} catch (FileNotFoundException fne) {
			message = "Error: " + fne.getMessage();
			success = false;
		} finally {
			try {
				if (success){
					json.put(START_FIELD, start);
				}
				json.put(Constants.SUCCESS, success);
				json.put(Constants.MESSAGE, message);
			} catch (JSONException e) {}
			writer.write(json.toString());
			IoUtil.close(writer);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");//解决乱码问题		
		doOptions(req, resp);
		final String token = req.getParameter(Constants.TOKEN_FIELD);
		final String fileName = new String(req.getParameter(Constants.FILE_NAME_FIELD).getBytes("ISO-8859-1"), "UTF-8");
		Range range = IoUtil.parseRange(req);
		OutputStream out = null;
		InputStream content = null;
		final PrintWriter writer = resp.getWriter();
		JSONObject json = new JSONObject();
		long start = 0;
		boolean success = true;
		String message = "";
		long sizeNow = 0L;
		long zoneSize = 0L;
		File f = IoUtil.getTokenedFile(token);
		
		try {
			sizeNow = f.length();
			String zoneSizeSTR = redisService.get(UPLOAD_ZONE_KEY+"-start-"+token);
			if(!StringUtils.isBlank(zoneSizeSTR)){
				zoneSize = Long.parseLong(zoneSizeSTR);
				sizeNow = sizeNow + zoneSize;
			}
			
			if (sizeNow != range.getFrom()) {//如果当前文件大小和已经上传的文件大小不同，则抛出异常
				/** drop this uploaded data */
				throw new StreamException(StreamException.ERROR_FILE_RANGE_START);//当抛出异常的时候会重新执行doGet方法
			}
			/*
			 * 以输出流的方式写入文件
			 * */
			out = new FileOutputStream(f, true);
			content = req.getInputStream();
			int read = 0;
			final byte[] bytes = new byte[BUFFER_LENGTH];
			while ((read = content.read(bytes)) != -1){
				out.write(bytes, 0, read);
			}

			start = f.length() + zoneSize;
		} catch (StreamException se) {
			success = StreamException.ERROR_FILE_RANGE_START == se.getCode();
			message = "Code: " + se.getCode();
		} catch (FileNotFoundException fne) {
			message = "Code: " + StreamException.ERROR_FILE_NOT_EXIST;
			success = false;
		} catch (IOException io) {
			message = "IO Error: " + io.getMessage();
			success = false;
		} finally {
			IoUtil.close(out);
			IoUtil.close(content);
			
			try {
				
				//start 当前传递的大小
//				if(f.length() >= ZONE_SIZE && range.getSize() != start){//已达到分片大小，但是总上传未完成
//					System.out.println("文件达到分片大小");
//					Integer nextNum = getNextNum(token);
//					rename(f, token,nextNum);//重命名文件
//					redisService.set(UPLOAD_ZONE_KEY+"-num-"+token, String.valueOf(nextNum));//将下个num存储
//					redisService.set(UPLOAD_ZONE_KEY+"-start-"+token, String.valueOf(start));//将分片大小存储
//				}else if (f.length() >= ZONE_SIZE && range.getSize() == start) {//达到分片大小，并且总上传已经完成,
//					rename(f, token, getNextNum(token));
//					//将数据库中的文件信息标记为上传完成
//					BiVideoInfo dto = new BiVideoInfo();
//					dto.setId(Integer.parseInt(redisService.get("file_upload_id"+token)));
//					dto.setvAvailable(1);
//					videoService.updateVideo(dto);
//					redisService.del(new String[]{UPLOAD_ZONE_KEY+"-num-"+token, UPLOAD_ZONE_KEY+"-start-"+token, "file_upload_id"+token});//删除reids中的key
//				}else if(f.length() < ZONE_SIZE && range.getSize() == start){//未达到分片大小，但总上传已经完成
//					rename(f, token, getNextNum(token));
//					//将数据库中的文件信息标记为上传完成
//					BiVideoInfo dto = new BiVideoInfo();
//					dto.setId(Integer.parseInt(redisService.get("file_upload_id"+token)));
//					dto.setvAvailable(1);
//					videoService.updateVideo(dto);
//					redisService.del(new String[]{UPLOAD_ZONE_KEY+"-num-"+token, UPLOAD_ZONE_KEY+"-start-"+token, "file_upload_id"+token});//删除reids中的key
//				}else if(f.length() < ZONE_SIZE && range.getSize() != start){//未达到分片大小，总上传未完成
//					
//				}
				if(range.getSize() == start){//未达到分片大小，但总上传已经完成
					rename(f, token, getNextNum(token), fileName.substring(fileName.lastIndexOf(".")));
					//将数据库中的文件信息标记为上传完成
					BiVideoInfo dto = new BiVideoInfo();
					dto.setId(Integer.parseInt(redisService.get("file_upload_id"+token)));
					dto.setvAvailable(1);
					videoService.updateVideo(dto);
					redisService.del(new String[]{UPLOAD_ZONE_KEY+"-num-"+token, UPLOAD_ZONE_KEY+"-start-"+token, "file_upload_id"+token, token});//删除reids中的key
				}
			} catch (IOException e) {
				success = false;
				message = "Rename file error: " + e.getMessage();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				success = false;
				message = "Rename file error: " + e.getMessage();
			}
			
			try {
				if (success){
					json.put(START_FIELD, start);
				}
				json.put(Constants.SUCCESS, success);
				json.put(Constants.MESSAGE, message);
			} catch (JSONException e) {}
			
			writer.write(json.toString());
			IoUtil.close(writer);
		}
	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("application/json;charset=utf-8");
		resp.setHeader("Access-Control-Allow-Headers", "Content-Range,Content-Type");
		resp.setHeader("Access-Control-Allow-Origin", Constants.streamCrossOrigin);
		resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
	}

	@Override
	public void destroy() {
		System.out.println("容器销毁");
		super.destroy();
	}
	
	private boolean rename(File f, String fileName, Integer num, String fileType) throws Exception{
		boolean flag = true;
		String zone_fileName = fileName+"_"+num+fileType;
		IoUtil.getFile(zone_fileName).delete();
	
		Files.move(f.toPath(), f.toPath().resolveSibling(zone_fileName));
		/** if `STREAM_DELETE_FINISH`, then delete it. */
		if (Constants.streamDeleteFinish.equals("true")) {
			IoUtil.getFile(zone_fileName).delete();
		}
		
		/*
		 * 先将本地磁盘的文件目录写入msql中的视频信息表
		 * 然后开启一个线程，将文件上传到hdfs，上传完成后会对mysql中视频信息表中的文件目录进行修改，改为hdfs中的目录
		 * */
		Integer id = Integer.parseInt(redisService.get("file_upload_id"+fileName));
		BiZoneInfo info = new BiZoneInfo();
		info.setvFileid(id);
		info.setzFile(zone_fileName);
		videoService.insertZoneVider(info);//插入分片信息
		UploadHDFSTask hdfsTask = new UploadHDFSTask(contextHelper, IoUtil.getFile(zone_fileName), zone_fileName, String.valueOf(info.getId()));
		redisThreadPool.pushFromTail(Convert.objectToBytes(hdfsTask));
		return flag;
	}
	
	private Integer getNextNum(String fileName){
		Integer num = 0;
		String numStr = redisService.get(UPLOAD_ZONE_KEY+"-num-"+fileName);
		if(!StringUtils.isBlank(numStr)){
			num =  Integer.parseInt(numStr.replaceAll(" ", "")) + 1;
		}
		return num;
	}
	
}

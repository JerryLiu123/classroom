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
import com.classroom.wnn.service.VideoService;
import com.classroom.wnn.task.RedisThreadPool;
import com.classroom.wnn.task.UploadHDFSTask;
import com.classroom.wnn.util.IoUtil;
import com.classroom.wnn.util.ObjectUtil;
import com.classroom.wnn.util.SpringContextHelper;
import com.classroom.wnn.util.TokenUtil;
import com.classroom.wnn.util.constants.Constants;


/**
 * 必须使用jdk1.7及以上
 * 文件上传
 * part, stored it.
 * 会先调用 doGet 方法进行信息初始化
 * 然后将要上传的数据进行分片，并多次调用 doPost方法进行上传，并会记录每一次上传之后的文件大小(由常量BUFFER_LENGTH控制)，当上传异常时，会根据这个大小进行断点续传
 * Range 对象中的size参数表示当前上传的文件大小
 * 
 * start 表示文件的总大小
 * @author java_speed
 * 修改时间 2016年8月18日
 * 修改人 lgh
 */
public class StreamServlet extends HttpServlet {
	private static final long serialVersionUID = -8619685235661387895L;
	private static Logger logger = Logger.getLogger(StreamServlet.class);
	
	static final int BUFFER_LENGTH = 10240;
	static final long ZONE_SIZE = 30 * 1024 * 1024;//30M
	static final String START_FIELD = "start";
	public static final String CONTENT_RANGE_HEADER = "content-range";
	
	private RedisThreadPool redisThreadPool;
	private VideoService videoService;
	private SpringContextHelper contextHelper;

	@Override
	public void init() throws ServletException {
		ServletContext servletContext = this.getServletContext();  
		WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
		redisThreadPool = (RedisThreadPool) ctx.getBean("redisThreadPool");
		videoService = (VideoService) ctx.getBean("videoService");
		contextHelper = (SpringContextHelper) ctx.getBean("contextHelper");
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
		String fileName = req.getParameter(Constants.FILE_NAME_FIELD);
		final PrintWriter writer = resp.getWriter();
		logger.info("开始上传文件-----"+fileName);
		
		/** TODO: validate your token. */
		
		JSONObject json = new JSONObject();
		long start = 0;
		boolean success = true;
		String message = "";
		try {
			File f = IoUtil.getTokenedFile(token);
			start = f.length();
			/** file size is 0 bytes. */
			/*如果上传文件大小为0将文件重命名*/
			if (token.endsWith("_0") && "0".equals(size) && 0 == start){
				f.renameTo(IoUtil.getFile(fileName));
			}
			//将文件信息写入数据库,并标记为未上传完成
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
		final String fileName = req.getParameter(Constants.FILE_NAME_FIELD);
		Range range = IoUtil.parseRange(req);
		
		OutputStream out = null;
		InputStream content = null;
		final PrintWriter writer = resp.getWriter();
		
		/** TODO: validate your token. */
		
		JSONObject json = new JSONObject();
		String tokenNew = null;
		long start = 0;
		boolean success = true;
		String message = "";
		File f = IoUtil.getTokenedFile(token);
		Integer num = Integer.parseInt(token.substring(token.lastIndexOf("_")));
		try {
			if ((f.length() + (num * ZONE_SIZE)) != range.getFrom()) {//如果当前文件大小和已经上传的文件大小不同，则抛出异常，根据当前是上传的第几个问价内进行判断
				/** drop this uploaded data */
				throw new StreamException(StreamException.ERROR_FILE_RANGE_START);
			}
			/*
			 * 以输出流的方式写入文件
			 * 主要是hdfs不支持同一个文件多次写入啊！！！
			 * */
			out = new FileOutputStream(f, true);
			content = req.getInputStream();
			int read = 0;
			final byte[] bytes = new byte[BUFFER_LENGTH];
			while ((read = content.read(bytes)) != -1)
				out.write(bytes, 0, read);

			start = f.length();
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

			if(start == ZONE_SIZE && range.getSize() != start){//已达到分片大小，但是总上传未完成
				try {
					rename(f, fileName, String.valueOf(num));//重命名文件
					
					String size = req.getParameter(Constants.FILE_SIZE_FIELD);
					if (StringUtils.isBlank(size)){
						logger.error("文件大小为null");
					}
					//获得上一个token的数量加一作为下一个token的数量
					tokenNew = TokenUtil.generateToken(fileName, size, String.valueOf(num+1));
				} catch (IOException e) {
					success = false;
					message = "Rename file error: " + e.getMessage();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					success = false;
					message = "Rename file error: " + e.getMessage();
				}
			}else if (start == ZONE_SIZE && range.getSize() == start) {//达到分片大小，并且总上传已经完成
				try {
					rename(f, fileName, String.valueOf(num));
					//将数据库中的文件信息标记为上传完成
					
				} catch (IOException e) {
					success = false;
					message = "Rename file error: " + e.getMessage();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					success = false;
					message = "Rename file error: " + e.getMessage();
				}
			}else if(start != ZONE_SIZE && range.getSize() == start){//未达到分片大小，但总上传已经完成
				try {
					rename(f, fileName, String.valueOf(num));
					//将数据库中的文件信息标记为上传完成
					
				} catch (IOException e) {
					success = false;
					message = "Rename file error: " + e.getMessage();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					success = false;
					message = "Rename file error: " + e.getMessage();
				}
				
			}else if(start != ZONE_SIZE && range.getSize() != start){//未达到分片大小，总上传未完成
				
			}
			
			
			try {
				if (success){
					if(!StringUtils.isBlank(tokenNew)){
						json.put(Constants.TOKEN_FIELD, tokenNew);
					}
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
	
	private boolean rename(File f, String fileName, String num) throws Exception{
		boolean flag = true;
		String zone_fileName = fileName.substring(0, fileName.lastIndexOf("."))+"_"+num+fileName.substring(fileName.lastIndexOf("."));
		IoUtil.getFile(zone_fileName).delete();
	
		Files.move(f.toPath(), f.toPath().resolveSibling(zone_fileName));
		/** if `STREAM_DELETE_FINISH`, then delete it. */
		if (Constants.streamDeleteFinish.equals("true")) {
			IoUtil.getFile(zone_fileName).delete();
		}
		
		/*
		 * 先将本地磁盘的文件目录写入msql中的视频信息表
		 * 然后开启一个线程，将文件上传到hdfs，上传完成后会对mysql中视频信息表中的文件目录进行修改，改为hdfs中的目录
		 * 因为是一部分一部分传的，所以如果直接存hdfs的话会导致性能下降,这里就先存到本地，然后再存入hdfs
		 * */
		BiVideoInfo info = new BiVideoInfo();
		info.setvName(fileName);
		info.setvFile(IoUtil.getFile(fileName).toString());
		videoService.insertVider(info);
		UploadHDFSTask hdfsTask = new UploadHDFSTask(contextHelper, IoUtil.getFile(zone_fileName), zone_fileName, String.valueOf(info.getId()));
		redisThreadPool.pushFromTail(ObjectUtil.objectToBytes(hdfsTask));
		return flag;
	}
	
}

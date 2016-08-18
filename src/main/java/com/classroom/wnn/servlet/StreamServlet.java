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

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.classroom.wnn.bean.Range;
import com.classroom.wnn.task.RedisThreadPool;
import com.classroom.wnn.task.UploadHDFSTask;
import com.classroom.wnn.util.Configurations;
import com.classroom.wnn.util.IoUtil;
import com.classroom.wnn.util.ObjectUtil;
import com.classroom.wnn.util.constants.Constants;


/**
 * 文件上传
 * part, stored it.
 * 会先调用 doGet 方法进行信息初始化
 * 然后将要上传的数据进行分片，并多次调用 doPost方法进行上传，并会记录每一次上传之后的文件大小(由常量BUFFER_LENGTH控制)，当上传异常时，会根据这个大小进行断点续传
 * Range 对象中的size参数表示当前上传的文件大小
 * start 表示文件的总大小
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
	
	private RedisThreadPool redisThreadPool;

	@Override
	public void init() throws ServletException {
		ServletContext servletContext = this.getServletContext();  
		WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext); 
		redisThreadPool = (RedisThreadPool) ctx.getBean("redisThreadPool");
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
			/*如果是初次上传，将文件重命名*/
			if (token.endsWith("_0") && "0".equals(size) && 0 == start){
				f.renameTo(IoUtil.getFile(fileName));
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
		req.setCharacterEncoding("UTF-8");//解决乱码问题		doOptions(req, resp);
		final String token = req.getParameter(Constants.TOKEN_FIELD);
		final String fileName = req.getParameter(Constants.FILE_NAME_FIELD);
		Range range = IoUtil.parseRange(req);
		
		OutputStream out = null;
		InputStream content = null;
		final PrintWriter writer = resp.getWriter();
		
		/** TODO: validate your token. */
		
		JSONObject json = new JSONObject();
		long start = 0;
		boolean success = true;
		String message = "";
		File f = IoUtil.getTokenedFile(token);
		try {
			/*
			 * 会先把 token 的名字当做文件名
			 * */
			if (f.length() != range.getFrom()) {
				/** drop this uploaded data */
				throw new StreamException(StreamException.ERROR_FILE_RANGE_START);
			}
			/*
			 * 以输出流的方式写入文件
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

			/*
			 * 上传完成后重命名文件
			 * range.getSize() 正在已经上传了 多大
			 * */
			if (range.getSize() == start) {
				/** fix the `renameTo` bug */
//				File dst = IoUtil.getFile(fileName);
//				dst.delete();
				// TODO: f.renameTo(dst); 重命名在Windows平台下可能会失败，stackoverflow建议使用下面这句
				try {
					// 先删除
					IoUtil.getFile(fileName).delete();
				
					Files.move(f.toPath(), f.toPath().resolveSibling(fileName));
					System.out.println("TK: `" + token + "`, NE: `" + fileName + "`");
					
					/** if `STREAM_DELETE_FINISH`, then delete it. */
					if (Configurations.isDeleteFinished()) {
						IoUtil.getFile(fileName).delete();
					}
					logger.info("文件上传成功-----"+fileName);
					/*
					 * 走到这里表示已经上传完成
					 * 可以先将本地磁盘的文件目录写入msql中的视频信息表
					 * 然后开启一个线程，将文件上传到hdfs，上传完成后会对mysql中视频信息表中的文件目录进行修改，改为hdfs中的目录
					 * */
					System.err.println(redisThreadPool);
//					UploadHDFSTask uploadHDFSTask = new UploadHDFSTask(IoUtil.getFile(fileName), fileName);
//					redisThreadPool.pushFromTail(ObjectUtil.objectToBytes(uploadHDFSTask));
					
				} catch (IOException e) {
					success = false;
					message = "Rename file error: " + e.getMessage();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					success = false;
					message = "Rename file error: " + e.getMessage();
				}
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
		resp.setHeader("Access-Control-Allow-Origin", Configurations.getCrossOrigins());
		resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
	}

	@Override
	public void destroy() {
		super.destroy();
	}

}

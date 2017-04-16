package com.classroom.wnn.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.classroom.wnn.aop.annotation.Log;
import com.classroom.wnn.bean.UserBean;
import com.classroom.wnn.service.RedisService;
import com.classroom.wnn.service.VideoService;
import com.classroom.wnn.util.Convert;
import com.classroom.wnn.util.JsonDateValueProcessor;
import com.classroom.wnn.util.JsonUtil;
import com.classroom.wnn.util.lock.RedisLockUtil;

@Controller
@RequestMapping(value = "/video")
public class VideoStreamController extends BaseController{
	private static Logger logger = Logger.getLogger(VideoStreamController.class);

	@Autowired
	private VideoService videoService;
	@Autowired
	private RedisService redisService;
	@Autowired
	private RedisLockUtil redisLockUtil;
	
	@Log(name="11111")
	@RequestMapping(value = "/toupvideo")
	public String toVideo(HttpServletRequest req,HttpServletResponse resp, Map<String, Object> datamap){
		datamap = getBaseMap(datamap);
		try {
			videoService.testException();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}
		return "/index";
	}
	@RequestMapping(value = "/toopvideo")
	public String toOpenVideo(HttpServletRequest req,HttpServletResponse resp, Map<String, Object> datamap){
		datamap = getBaseMap(datamap);
		return "/mystream";
	}
	@ResponseBody
	@RequestMapping(value = "/testjson")
	public UserBean toTestJson(HttpServletResponse response, Map<String, Object> dataMap){
		dataMap = getBaseMap(dataMap);
		UserBean bean = new UserBean();
		bean.setBz("aaaaa");
		bean.setDwdh("1111111");
		//String json = JsonUtil.getJsonString4JavaPOJO(dataMap);
		//ajaxJson(json, response);
		return bean;
	}
	
	/**
	 * 播放视频，将课程名传入经过base64转码传入url
	 * @param cName
	 */
	@RequestMapping(value = "/dovideo/{filename}")
	public void preview(@PathVariable("filename")String filename, HttpServletRequest req,HttpServletResponse resp){
			//将目录进行base64解码
			try {
				filename = new String(Convert.fromBase64String(filename), "UTF-8");
				Pattern pattern = Pattern.compile("^hdfs.*");
				Matcher matcher = pattern.matcher(filename);
				if(matcher.matches()){//如果是hdfs上的文件则直接进行播放
					playHDFS(filename, req, resp);
				}else{//如果是服务器文件则在redis中建立字段表示文件已经被占用，不可删除，修改
					/*
					 * 如果分布式情况下服务器 A 上传的文件，所以本地文件在 A 上
					 * 但是服务器B 收到请求播放文件，如果此时还没有将文件上传到hdfs
					 * B会在本地找，但是因为文件在A上所以会播放失败，这怎么办
					 * 可以使用存储共享，本项目用的是存储共享,但是使用存储共享应该也有ftp服务器的问题，因为都是通过网络传输
					 * 生产环境下可以使用类似于数据共享的软件
					 * 这个问题暂时没有条件验证
					 * */
					
					String range=req.getHeader("Range");
					if(range.equals("bytes=0-")){//如果是第一次播放，才加入使用者
						fileUserAdd(filename);
					}
					playLocal(filename, req, resp);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				logger.error("视频目录解码失败"+e);
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				logger.error("添加视频使用者失败"+e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("视频播放失败---"+e);
			}
	}
	
	/**
	 * 当用户退出是，将此文件使用者减一
	 * */
	@RequestMapping(value = "/defvideouser/{filename}")
	public void defVideoUser(@PathVariable("filename")String filename){
		try {
			//判断是否为本地文件播放
			filename = new String(Convert.fromBase64String(filename), "UTF-8");
			Pattern pattern = Pattern.compile("^hdfs.*");
			Matcher matcher = pattern.matcher(filename);
			if(!matcher.matches()){//如果是本地文件则进行使用量减一
				fileUserDel(filename);
			}
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			logger.error("删除文件使用者异常---"+e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			logger.error("文件解码失败---"+e);
		}
		
	}
	
	/**
	 * 播放hdfs文件
	 * @param filename
	 * @param req
	 * @param resp
	 * @throws IOException 
	 */
	private void playHDFS(String filename, HttpServletRequest req,HttpServletResponse resp) throws IOException{
		Configuration config=new Configuration();
		FileSystem fs = null; 
		FSDataInputStream in=null;
		fs = FileSystem.get(URI.create(filename),config);	
		in=fs.open(new Path(filename));
	    final long fileLen = fs.getFileStatus(new Path(filename)).getLen();     
	    String range=req.getHeader("Range");
	    resp.setHeader("Content-type","video/mp4");
	    OutputStream out=resp.getOutputStream();    
	    if(range==null){
	    	 filename=filename.substring(filename.lastIndexOf("/")+1);
	         resp.setHeader("Content-Disposition", "attachment; filename="+filename);
	         resp.setContentType("application/octet-stream");
	         resp.setContentLength((int)fileLen);
	    	 IOUtils.copyBytes(in, out, fileLen, false);
	     }else{
		    long start=Integer.valueOf(range.substring(range.indexOf("=")+1, range.indexOf("-")));
		    long count=fileLen-start;
		    long end;
		    if(range.endsWith("-")){
		    	  end=fileLen-1;
		    }else{
		    	  end=Integer.valueOf(range.substring(range.indexOf("-")+1));
		    }
		    String ContentRange="bytes "+String.valueOf(start)+"-"+end+"/"+String.valueOf(fileLen);
		    resp.setStatus(206);
		    resp.setContentType("video/mpeg4");
		    resp.setHeader("Content-Range",ContentRange);
		    in.seek(start);
		    IOUtils.copyBytes(in, out, count, false);
	     }
	     in.close();
	     in = null;
	     out.close();
	     out = null;
	}
	
	/**
	 * 播放本地文件,和上面播放hdfs文件逻辑相同，正在寻找更好的方法
	 * @param filename
	 * @param req
	 * @param resp
	 * @throws IOException 
	 */
	private void playLocal(String filename, HttpServletRequest req,HttpServletResponse resp) throws IOException{
			File file = new File(filename);
			InputStream ins = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(ins);
		    final long fileLen = file.length(); 
		    String range=req.getHeader("Range");
		    resp.setHeader("Content-type","video/mp4");
		    OutputStream out=resp.getOutputStream();    
		    if(range==null){
		    	 filename=filename.substring(filename.lastIndexOf("/")+1);
		         resp.setHeader("Content-Disposition", "attachment; filename="+filename);
		         resp.setContentType("application/octet-stream");
		         resp.setContentLength((int)fileLen);
		    	 IOUtils.copyBytes(bis, out, fileLen, false);
		     }else{
			    long start=Integer.valueOf(range.substring(range.indexOf("=")+1, range.indexOf("-")));
			    long count=fileLen-start;
			    long end;
			    if(range.endsWith("-")){
			    	  end=fileLen-1;
			    }else{
			    	  end=Integer.valueOf(range.substring(range.indexOf("-")+1));
			    }
			    String ContentRange="bytes "+String.valueOf(start)+"-"+end+"/"+String.valueOf(fileLen);
			    resp.setStatus(206);
			    resp.setContentType("video/mpeg4");
			    resp.setHeader("Content-Range",ContentRange);
			    //bis.seek(start);
			    bis.skip(start);
			    IOUtils.copyBytes(bis, out, count, false);
		     }
		     bis.close();
		     bis = null;
		     out.close();
		     out = null;
	}
	
	/**
	 * 将本地文件使用者数量加一并返回
	 * @throws TimeoutException 
	 */
	private String fileUserAdd(String filename) throws TimeoutException{
		String value = redisLockUtil.addLock("FileUser"+filename, Long.valueOf(3*60*1000));
		String out = null;
		try {
			if(redisService.exists(filename)){//如果存在使用者，则将使用数量加一
				String i = redisService.get(filename);
				out = redisService.getSet(filename, String.valueOf((Integer.parseInt(i)+1)), Long.valueOf(24*60*60*1000));
			}else{//如果不存在使用者，将此文件标记为使用
				redisService.set(filename, "1");
				out = "1";
			}
		} finally {
			// TODO: handle finally clause
			redisLockUtil.unLock("FileUser"+filename, value);
		}
		
		return out;
	}
	
	/**
	 * 将本地文件使用者数量减一，如果已经为最后一个使用者，则删除此锁
	 * @throws TimeoutException 
	 */
	private void fileUserDel(String filename) throws TimeoutException{
		String value = redisLockUtil.addLock("FileUser"+filename, Long.valueOf(3*60*1000));
		try {
			if(redisService.exists(filename)){//如果存在使用者，则将使用数量减一
				String i = redisService.get(filename);
				if(i.equals("1")){//如果为最后一个使用者，则直接将锁删除
					redisService.del(filename);
				}else{
					redisService.getSet(filename, String.valueOf((Integer.parseInt(i)-1)), 0L);
				}
			}else{//如果不存在使用者，表示为有别人将锁删除，直接抛出异常
				throw new Error("使用者被其余线程删除");
			}
		} finally {
			// TODO: handle finally clause
			redisLockUtil.unLock("FileUser"+filename, value);
		}
	} 
	
}

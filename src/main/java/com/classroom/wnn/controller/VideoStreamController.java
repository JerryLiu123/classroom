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

import com.classroom.wnn.service.RedisService;
import com.classroom.wnn.util.Convert;
import com.classroom.wnn.util.lock.RedisLockUtil;

@Controller
@RequestMapping(value = "/video")
public class VideoStreamController extends BaseController{
	private static Logger logger = Logger.getLogger(VideoStreamController.class);

	@Autowired
	private RedisService redisService;
	@Autowired
	private RedisLockUtil redisLockUtil;
	
	@RequestMapping(value = "/toupvideo")
	public String toVideo(HttpServletRequest req,HttpServletResponse resp, Map<String, Object> datamap){
		datamap = getBaseMap(datamap);
		return "/index";
	}
	@RequestMapping(value = "/toopvideo")
	public String toOpenVideo(HttpServletRequest req,HttpServletResponse resp, Map<String, Object> datamap){
		datamap = getBaseMap(datamap);
		return "/mystream";
	}
	
	/**
	 * 播放视频，将课程名传入经过base64转码传入url
	 * @param cName
	 */
	@RequestMapping(value = "/dovideo/{filename}")
	public void preview(@PathVariable("filename")String filename, HttpServletRequest req,HttpServletResponse resp){
			/*
			 * 这里如果传视频地址的话
			 * 如果清理本地视频的程序在视频播放期间运行的话，清理程序会将本地文件删除，那么清理完
			 * */
			//根据视频id获得视频地址
//			boolean isHDFS = true;
//			String redisKey = "redisFile_"+id;
//			String file = redisService.get(redisKey);
//			if(!StringUtils.isBlank(file)){
//				//如果redis中没有则将从数据库中查找信息
//				BiVideoInfo info = videoService.getVideoById(Integer.parseInt(id));
//				//判断是否由hdfs目录，如果有的话使用hdfs目录，没有使用本地目录
//				if(StringUtils.isBlank(info.getvHdfsfile())){
//					isHDFS = false;
//					file = info.getvFile();
//				}else{
//					file = info.getvHdfsfile();
//				}
//				redisService.set(redisKey, file);//将目录信息放入redis中
//			}
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
					 * 因暂时无法解决在分布式下的这个问题，所以本地视频暂时不支持播放
					 * 暂时想到的就是弄一个ftp服务器作为存储，但是会给ftp服务器造成很大的压力
					 * */
					
//					String range=req.getHeader("Range");
//					if(range.equals("bytes=0-")){//如果是第一次播放，才加入使用者
//						FileUserAdd(filename);
//					}
//					playLocal(filename, req, resp);
					return;
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				logger.error("视频目录解码失败"+e);
			}
	}
	
	/**
	 * 当用户退出是，将此文件使用者减一
	 * */
	@RequestMapping(value = "/defvideouser/{filename}")
	public void defVideoUser(@PathVariable("filename")String filename){
		try {
			FileUserDel(filename);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			logger.error("删除文件使用者异常---"+e);
		}
		
	}
	
	/**
	 * 播放hdfs文件
	 * @param filename
	 * @param req
	 * @param resp
	 */
	private void playHDFS(String filename, HttpServletRequest req,HttpServletResponse resp){
		try {
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
		 } catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("播放视频失败:"+e);
		 }
	}
	
	/**
	 * 播放本地文件
	 * @param filename
	 * @param req
	 * @param resp
	 */
	private void playLocal(String filename, HttpServletRequest req,HttpServletResponse resp){
		try {
			
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
		    
		 } catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("播放视频失败:"+e);
		 }
	}
	
	/**
	 * 将本地文件使用者数量加一并返回
	 * @throws TimeoutException 
	 */
	private String FileUserAdd(String filename) throws TimeoutException{
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
	private void FileUserDel(String filename) throws TimeoutException{
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

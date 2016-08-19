package com.classroom.wnn.controller;

import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
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

import com.classroom.wnn.model.BiVideoInfo;
import com.classroom.wnn.service.RedisService;
import com.classroom.wnn.service.VideoService;
import com.classroom.wnn.util.constants.Constants;

@Controller
@RequestMapping(value = "/video")
public class VideoStreamController extends BaseController{
	private static Logger logger = Logger.getLogger(VideoStreamController.class);

	@Autowired
	private VideoService videoService;
	@Autowired
	private RedisService redisService;
	
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
	@RequestMapping(value = "/dovideo/{id}")
	public void preview(@PathVariable("id")String id, HttpServletRequest req,HttpServletResponse resp){
		try {
//			String curriculumName = new String(Convert.decode(cName), "UTF-8");//将课程名解码
//			byte[] aa = redisService.getByte(curriculumName);//根据课程名获得redis中的课程对象
//			Object object = null;
//			if(aa != null){
//				//获得数据库中的课程对象
//			}else{
//				object = ObjectUtil.bytesToObject(aa);//将字节码反序列化成对象
//			}
//			String path = "";//获得文件的路径
			
//			if(StringUtils.isBlank(path)){
//				return;
//			}
			//根据视频id获得视频地址,现在放入redis的机制还没有想明白
			boolean isHDFS = true;
			String file = redisService.get(id);
			if(!StringUtils.isBlank(file)){
				//如果redis中没有则将从数据库中查找信息
				BiVideoInfo info = videoService.getVideoById(Integer.parseInt(id));
				//判断是否由hdfs目录，如果有的话使用hdfs目录，没有使用本地目录
				if(StringUtils.isBlank(info.getvHdfsfile())){
					isHDFS = false;
					file = info.getvFile();
				}else{
					file = info.getvHdfsfile();
				}
			}
			
			if(isHDFS){
				
			}
			String filename=Constants.hdfsAddress+"/course/"+file+".mp4";
			logger.info("filename:"+filename);
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
	
}

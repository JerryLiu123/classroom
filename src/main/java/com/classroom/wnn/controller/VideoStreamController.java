package com.classroom.wnn.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.disk.DiskFileItem;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.classroom.wnn.aop.annotation.Log;
import com.classroom.wnn.service.RedisService;
import com.classroom.wnn.util.Convert;
import com.classroom.wnn.util.HdfsFileSystem;
import com.classroom.wnn.util.JsonUtil;
import com.classroom.wnn.util.ObjectUtil;
import com.classroom.wnn.util.constants.Constants;

@Controller
@RequestMapping(value = "/video")
public class VideoStreamController extends BaseController{
	private static Logger logger = Logger.getLogger(VideoStreamController.class);
	
	@Autowired
	private RedisService redisService;
	
	@RequestMapping(value = "/toupvideo")
	@Log(name="test")
	public String toVideo(HttpServletRequest req,HttpServletResponse resp, Map<String, Object> datamap){
		datamap = getBaseMap(datamap);
		return "/index";
	}
	
	/**
	 * 播放视频，将课程名传入经过base64转码传入url
	 * @param cName
	 */
	@RequestMapping(value = "/dovideo/{cName}")
	public void preview(@PathVariable("cName")String cName, HttpServletRequest req,HttpServletResponse resp){
		try {
			String curriculumName = new String(Convert.decode(cName), "UTF-8");//将课程名解码
			byte[] aa = redisService.getByte(curriculumName);//根据课程名获得redis中的课程对象
			Object object = null;
			if(aa != null){
				//获得数据库中的课程对象
			}else{
				object = ObjectUtil.bytesToObject(aa);//将字节码反序列化成对象
			}
			String path = "";//获得文件的路径
			
			if(StringUtils.isBlank(path)){
				return;
			}
			String filename=Constants.HDFSAddress+"/"+path;
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
		    	 filename=path.substring(path.lastIndexOf("/")+1);
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
	 * 上传视频
	 */
	@RequestMapping(value="/upload")
	public String opload(MultipartHttpServletRequest request,HttpServletResponse response){
		Map<String, String> modelMap = new HashMap<String, String>(); 
		MultipartFile file = request.getFile("file");
		CommonsMultipartFile cf= (CommonsMultipartFile)file; 
		if(!file.isEmpty()){
			String name = file.getOriginalFilename();
			DiskFileItem fi = (DiskFileItem)cf.getFileItem(); 
			File inputFile = fi.getStoreLocation();
			try {
				HdfsFileSystem.getInstance().createFile(inputFile, "/course/"+name);
				logger.info("上传文件成功");
				modelMap.put("status", "success");
				modelMap.put("message", "上传成功");
				modelMap.put("site", "/course/"+name);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				modelMap.put("status", "false");
				modelMap.put("errorValue", "上传失败");
				logger.error("上传文件失败"+e);
			}
		}else{
			modelMap.put("flag", "false");
			modelMap.put("errorValue", "请选择有效文件");
		}
		return ajaxHtml(JsonUtil.getJsonString4JavaPOJO(modelMap), response);
		//return modelMap;
	}
	
}

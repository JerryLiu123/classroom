package com.classroom.wnn.task;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.classroom.wnn.service.VideoService;
import com.classroom.wnn.util.Convert;
import com.classroom.wnn.util.HdfsFileSystem;
import com.classroom.wnn.util.SpringContextHelper;
import com.classroom.wnn.util.VideoUtil;
import com.classroom.wnn.util.constants.Constants;

/**
 * 上传文件到hdfs
 * @author lgh
 *
 */
public class UploadHDFSTask extends Task {
	private static Logger logger = Logger.getLogger(UploadHDFSTask.class);
	static final long ZONE_SIZE = 60 * 1024;//分片大小

	private File inputFile;
	private String fileName;
	private SpringContextHelper contextHelper;
	private String infoKey;
	
	
	public UploadHDFSTask(SpringContextHelper contextHelper, File inputFile, String fileName, String infoKey) {
		super();
		this.inputFile = inputFile;
		this.fileName = fileName;
		this.contextHelper = contextHelper;
		this.infoKey = infoKey;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//super.run();
		try {
			
			logger.info("新线程写入hdfs开始------");
			//将文件名加入当前时间时间戳，并进行base64编码
//			this.fileName = Convert.toBase64String(this.fileName.substring(0,this.fileName.lastIndexOf("."))+
//													"_"+
//													System.currentTimeMillis())+
//													this.fileName.substring(this.fileName.lastIndexOf("."));
			crteatHDFS();
			
			//将视频切片
//			String videoPath = inputFile.getPath();//视频地址
//			Long videoSize = inputFile.length();//视频总长度
//			System.out.print(videoPath +"----"+ videoSize);
//			if(videoSize > ZONE_SIZE){
//				Map<String, String> info = VideoUtil.getVideoInfo(videoPath);
//				if(info == null || !info.containsKey("time")){
//					throw new IOException();
//				}
//				//计算每一个切片的开始和结束时间
//				Long zoneNum = videoSize/ZONE_SIZE;
//				if(videoSize%ZONE_SIZE > 0){
//					zoneNum = zoneNum + 1;
//				}
//				for(int i=0;i<zoneNum;i++){
//					
//				}
//			}else{
//				crteatHDFS();
//			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("写入hdfs文件出错----"+e);
		}
	}
	
	private boolean crteatHDFS() throws IOException{
		String path = Constants.hdfsAddress+"/course/"+this.fileName;
		logger.info("新写入hdfs地址为------"+path);
		HdfsFileSystem.createFile(inputFile, path);
		/*下面是要将新的地址目录写入 视频信息表 */
		logger.info("线程更新视频信息表开始");
		VideoService videoService = (VideoService) contextHelper.getBean("videoService");
		videoService.updateHDFSFile(this.infoKey, path);
		logger.info("线程更新视频信息表成功");
		return true;
	}

	@Override
	public Task[] taskCore() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean useDb() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean needExecuteImmediate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String info() {
		// TODO Auto-generated method stub
		return null;
	}

}

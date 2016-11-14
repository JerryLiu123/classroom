package com.classroom.wnn.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.classroom.wnn.util.constants.Constants;

/**
 * 视频操作工具类
 * @author xiaoming
 *2016年10月25日
 */
public class VideoUtil {
	static final long ZONE_SIZE = 60 * 1024 * 1024;//分片大小
	
	public static void main(String[] args) {
		Long cc = System.currentTimeMillis();
		//System.out.println(processImg("F:\\电影\\锅盖头3：绝地反击.BD1280超清中英双字.mp4","C:\\Users\\admin\\Desktop\\aa.jpg"));
		//getVideoInfo("F:\\电影\\锅盖头3：绝地反击.BD1280超清中英双字.mp4");
		File inputFile = new File("F:\\电影\\锅盖头3：绝地反击.BD1280超清中英双字.mp4");
		String videoPath = inputFile.getPath();//视频地址
		Long videoSize = inputFile.length();//视频总长度
		System.out.println(videoPath +"----"+ videoSize);
		Map<String, String> info = VideoUtil.getVideoInfo(videoPath);
		if(info == null || !info.containsKey("time")){
			System.out.println("=====error====");
		}
		//计算每一个切片的开始和结束时间
		Long zoneNum = videoSize/ZONE_SIZE;
		if(videoSize%ZONE_SIZE > 0){
			zoneNum = zoneNum + 1;
		}
		System.out.println("zone_num:"+zoneNum);
		Double videoTime = Double.parseDouble(info.get("time"));
		Double stepSize = videoTime/zoneNum;
//		if(videoTime%zoneNum > 0){
//			stepSize = stepSize + 1;
//		}
		System.out.println("stepSize:"+stepSize);
		Double startTime = 0.0;
		String fZonePath = "F:\\1-testvideo\\" + 
							videoPath.substring(videoPath.lastIndexOf("\\")+1, videoPath.lastIndexOf("."));
		//因视频精度的要求，先将所有的帧的编码方式转为帧内编码 太慢了啊
//		String copypath = fZonePath + videoPath.substring(videoPath.lastIndexOf("."));
//		VideoUtil.recoding(videoPath, copypath);
//		for(int i=0;i<zoneNum;i++){
//			String zonepath = fZonePath + 
//							  i + 
//							  videoPath.substring(videoPath.lastIndexOf("."));
//			segmentationVideo(videoPath, zonepath, String.valueOf(startTime), String.valueOf(stepSize));
//			//因为分片的时长会不固定，所以每次都获得视频的时长，以用来计算结束时间
//			Map<String, String> ccd = VideoUtil.getVideoInfo(zonepath);
//			//Double zoneTime =  Double.parseDouble(ccd.get("time"));
//			//System.out.println("startTime:"+Double.parseDouble(ccd.get("startTimer")));
//			startTime = startTime + stepSize + 1;
//		}
		System.err.println("用时---"+(System.currentTimeMillis() - cc)/1000);
		
	}
	
	/**
	 * 截图视频中间位置图片
	 * @param veidoPath
	 * @param outPath
	 * @return
	 */
	public static boolean processImg(String veidoPath, String outPath){
		File file = new File(veidoPath);
		if(!file.exists()){
			return false;
		}
		Map<String, String> info = getVideoInfo(veidoPath);
		if(info == null || !info.containsKey("time")){
			return false;
		}
		String time = String.valueOf((Integer.parseInt(getVideoInfo(veidoPath).get("time"))/2));
		List<String> commands = new ArrayList<String>();  
	    //commands.add(Constants.ffmpegPath);
		commands.add("D:\\ffmpeg-3.1.4-win64-static\\bin\\ffmpeg.exe");
	    commands.add("-ss");  
	    commands.add(time);//这个参数是设置从哪里开始截图
	    commands.add("-i");  
	    commands.add(veidoPath);  
	    commands.add("-y");  
	    commands.add("-f");  
	    commands.add("image2");  
	    commands.add("-vframes");
	    commands.add("1");  
	    commands.add("-s");
	    commands.add("700x525");  
	    commands.add("-an");
	    commands.add(outPath); 
	    
		try {
			return runCommand(commands);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}             
	}
	
	/**
	 * 获得视频信息，包括时长，开始时间，码率
	 * @param veidoPath
	 * @return
	 */
	public static Map<String, String> getVideoInfo(String veidoPath){
		List<String> commands = new ArrayList<String>();
		//commands.add(Constants.ffmpegPath);
		commands.add("D:\\ffmpeg-3.1.4-win64-static\\bin\\ffprobe.exe");
	    //commands.add("-i");  
	    commands.add("-show_packets");//显示每一个包信息
		//commands.add("-show_frames");//显示每一帧信息,太慢，太慢
		//commands.add("-show_format");
	    commands.add(veidoPath);
        Process videoProcess;
        Map<String, String> info = new HashMap<String, String>();
		try {
			videoProcess = new ProcessBuilder(commands).redirectErrorStream(true).start();
			new PrintStream(videoProcess.getErrorStream()).start();
	       //从输入流中读取视频信息  
            BufferedReader br = new BufferedReader(new InputStreamReader(videoProcess.getInputStream()));  
            StringBuffer sb = new StringBuffer();  
            String line = "";  
            while ((line = br.readLine()) != null) {  
                sb.append(line);  
                //System.out.println(line);
            }  
            br.close();

	        videoProcess.waitFor();
            //从视频信息中解析时长  
            String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (\\d*) kb\\/s";  
            Pattern pattern = Pattern.compile(regexDuration);  
            Matcher m = pattern.matcher(sb.toString());  
            if (m.find()) {  
                float time = getTimelen(m.group(1));  
                info.put("time", String.valueOf(time));
                info.put("startTimer", m.group(2));
                info.put("kbps", m.group(3));
                System.out.println(veidoPath+",视频时长："+time+"s, 开始时间："+m.group(2)+",比特率："+m.group(3)+"kb/s");  
                return info;
            }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			return null;
		}
		return null;
	}
	
	/**
	 * 切分视频
	 * @param veidoPath
	 * @param outPath
	 * @param startTime
	 * @param continueTime
	 * @return
	 */
	public static boolean segmentationVideo(String veidoPath, String outPath, String startTime, String continueTime){
		List<String> commands = new ArrayList<String>();  
	    //commands.add(Constants.ffmpegPath);
		commands.add("D:\\ffmpeg-3.1.4-win64-static\\bin\\ffmpeg.exe");
	    commands.add("-ss");  
	    commands.add(startTime);
	    //commands.add("-y");
	    commands.add("-i");  
	    commands.add(veidoPath);  
	    commands.add("-vcodec");  
	    commands.add("copy"); 
	    commands.add("-acodec");  
	    commands.add("copy");
	    commands.add("-t");//持续时间  
	    commands.add(continueTime);   
	    commands.add(outPath); 
	    
		try {
			return runCommand(commands);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}             
	}
	
	/**
	 * 转换为帧内编码
	 * @param veidoPath
	 * @param outPath
	 * @return
	 */
	public static boolean recoding(String veidoPath, String outPath){
		List<String> commands = new ArrayList<String>();
		//commands.add(Constants.ffmpegPath);
		commands.add("D:\\ffmpeg-3.1.4-win64-static\\bin\\ffmpeg.exe");
	    commands.add("-i");  
	    commands.add(veidoPath); 
	    //commands.add("-sameq");
	    commands.add("-qscale");
	    commands.add("0");
	    commands.add("-intra");
	    commands.add(outPath);
		try {
			return runCommand(commands);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}  
	}
	
	private static boolean runCommand(List<String> commands) throws IOException, InterruptedException{
		Process videoProcess;
		videoProcess = new ProcessBuilder(commands).redirectErrorStream(true).start();
        new PrintStream(videoProcess.getErrorStream()).start();
        new PrintStream(videoProcess.getInputStream()).start();
        videoProcess.waitFor();//阻塞等待程序完成
		return true;
	}
	
    //格式:"00:00:10.68"  
    private static float getTimelen(String timelen){  
        float min=0;  
        String strs[] = timelen.split(":");  
        if (strs[0].compareTo("0") > 0) {  
            min+=Integer.valueOf(strs[0])*60*60;//秒  
        }  
        if(strs[1].compareTo("0")>0){  
            min+=Integer.valueOf(strs[1])*60;  
        }  
        if(strs[2].compareTo("0")>0){  
            //min+=Math.round(Float.valueOf(strs[2]));  
        	//System.out.println(strs[2]);
        	min+=Float.valueOf(strs[2]);
        }  
        return min;  
    }
}
//避免ffmpeg的输出流塞满缓存造成死锁
class PrintStream extends Thread {
     java.io.InputStream __is = null;
     public PrintStream(java.io.InputStream is) {
         __is = is;
     } 
     public void run() {
         try {
             while(this != null) {
                 int _ch = __is.read();
                 if(_ch != -1){
                	 //System.out.print((char)_ch); 
                 }else {
                	 break;
                 }
             }
         } 
         catch (Exception e) {
             e.printStackTrace();
         } 
     }
}

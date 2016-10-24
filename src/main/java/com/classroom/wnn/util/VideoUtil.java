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

public class VideoUtil {
	static final long ZONE_SIZE = 60 * 1024 * 1024;//分片大小
	
	public static void main(String[] args) {
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
		Long videoTime = Long.parseLong(info.get("time"));
		Long stepSize = videoTime/zoneNum;
		if(videoTime%zoneNum > 0){
			stepSize = stepSize + 1;
		}
		System.out.println("stepSize:"+stepSize);
		Long startTime = 0L;
		Long cc = System.currentTimeMillis();
		for(int i=0;i<zoneNum;i++){
			String zonepath = "F:\\1-testvideo\\" + 
							  videoPath.substring(videoPath.lastIndexOf("\\")+1, videoPath.lastIndexOf(".")) + 
							  i + 
							  videoPath.substring(videoPath.lastIndexOf("."));
			System.out.println(zonepath);
			segmentationVideo(videoPath, zonepath, String.valueOf(startTime), String.valueOf(stepSize));
			//因为分片的时长会不固定，所以每次都获得视频的时长，以用来计算结束时间
			Long zoneTime =  Long.parseLong(VideoUtil.getVideoInfo(zonepath).get("time"));
			startTime = startTime + zoneTime;
		}
		System.err.println("用时---"+(System.currentTimeMillis() - cc));
		
	}
	
	public static boolean processImg(String veidoPath, String imageRealPath){
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
	    commands.add("-t");//持续时间  
	    commands.add("0.001");  
	    commands.add("-s");  
	    commands.add("700x525");  
	    commands.add(imageRealPath); 
	    
        Process videoProcess;
		try {
			videoProcess = new ProcessBuilder(commands).redirectErrorStream(true).start();
	        new PrintStream(videoProcess.getErrorStream()).start();
	        new PrintStream(videoProcess.getInputStream()).start();
	        videoProcess.waitFor();//阻塞等待程序完成
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}             

		return true;
	}
	
	public static Map<String, String> getVideoInfo(String veidoPath){
		List<String> commands = new ArrayList<String>();
		//commands.add(Constants.ffmpegPath);
		commands.add("D:\\ffmpeg-3.1.4-win64-static\\bin\\ffmpeg.exe");
	    commands.add("-i");  
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
            }  
            br.close();
              
            //从视频信息中解析时长  
            String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (\\d*) kb\\/s";  
            Pattern pattern = Pattern.compile(regexDuration);  
            Matcher m = pattern.matcher(sb.toString());  
            if (m.find()) {  
                int time = getTimelen(m.group(1));  
                info.put("time", String.valueOf(time));
                info.put("startTimer", m.group(2));
                info.put("kbps", m.group(3));
                System.out.println(veidoPath+",视频时长："+time+"s, 开始时间："+m.group(2)+",比特率："+m.group(3)+"kb/s");  
                return info;
            }
			
	        videoProcess.waitFor();
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
	
	public static boolean segmentationVideo(String veidoPath, String zonePath, String startTime, String continueTime){
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
	    commands.add(zonePath); 
	    
        Process videoProcess;
		try {
			videoProcess = new ProcessBuilder(commands).redirectErrorStream(true).start();
	        new PrintStream(videoProcess.getErrorStream()).start();
	        new PrintStream(videoProcess.getInputStream()).start();
	        videoProcess.waitFor();//阻塞等待程序完成
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}             

		return true;
	}
	
    //格式:"00:00:10.68"  
    private static int getTimelen(String timelen){  
        int min=0;  
        String strs[] = timelen.split(":");  
        if (strs[0].compareTo("0") > 0) {  
            min+=Integer.valueOf(strs[0])*60*60;//秒  
        }  
        if(strs[1].compareTo("0")>0){  
            min+=Integer.valueOf(strs[1])*60;  
        }  
        if(strs[2].compareTo("0")>0){  
            min+=Math.round(Float.valueOf(strs[2]));  
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
                	 System.out.print((char)_ch); 
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

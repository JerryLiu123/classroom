package com.classroom.wnn.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.concurrent.TimeoutException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.classroom.wnn.util.constants.Constants;
import com.classroom.wnn.util.lock.RedisLockUtil;

/**
 * hdfs 文件操作
 * @author lgh
 *
 */
public class HdfsFileSystem{
	private static Logger logger = Logger.getLogger(HdfsFileSystem.class);	
	private static final String DEL_LOCK="delLock";
//	private Configuration conf;
//	private FileSystem fs;
//	private DistributedFileSystem hdfs;
	
	//private static HdfsFileSystem instance = HdfsFileSystem.getInstance();
	
	private HdfsFileSystem() throws IOException{
//		conf = new Configuration();
//		System.setProperty("hadoop.home.dir", "F:\\hadoop-2.6.0");
//		//conf.set("dfs.socket.timeout", "180000");
//		fs = FileSystem.get(URI.create(HDFSURL) ,conf);
//		hdfs = (DistributedFileSystem)fs;
	}
//	public static synchronized HdfsFileSystem getInstance(){
//		if(instance == null){
//			try {
//				instance = new HdfsFileSystem();
//				return instance;
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				return null;
//			}
//		}else{
//			return instance;
//		}
//	}
	
    /** 
     * 列出所有DataNode的名字信息 
     * @throws IOException 
     */  
    public static void listDataNodeInfo() throws IOException { 
        Configuration conf = new Configuration();  
        FileSystem fileSystem = FileSystem.get(URI.create(Constants.hdfsAddress), conf); 
        DistributedFileSystem hdfs = (DistributedFileSystem)fileSystem;
        DatanodeInfo[] dataNodeStats = hdfs.getDataNodeStats();  
        String[] names = new String[dataNodeStats.length];  
        System.out.println("List of all the datanode in the HDFS cluster:");  
          
        for (int i=0;i<names.length;i++) {  
            names[i] = dataNodeStats[i].getHostName();  
            System.out.println(names[i]);  
        }  
        System.out.println(hdfs.getUri().toString());  
        fileSystem.close();
    }
    /** 
     * 查看文件是否存在 
     * @throws IOException 
     */  
    public static boolean checkFileExist(String path) throws IOException {  
    	boolean flag = false;
        Configuration conf = new Configuration();  
        FileSystem fileSystem = FileSystem.get(URI.create(path), conf); 
        boolean exist = fileSystem.exists(new Path(path));  
        flag = exist;
        fileSystem.close();
        return flag;
    }
    
    
    /** 
     * 按路径上传文件到hdfs
     * @param local 
     * @param remote 
     * @throws IOException 
     */  
    public static void copyFile(String local, String remote) throws IOException {  
        Configuration conf = new Configuration();  
        FileSystem fileSystem = FileSystem.get(URI.create(remote), conf);  
    	fileSystem.copyFromLocalFile(new Path(local), new Path(remote)); 
        logger.info("上传文件至hdfs---from: " + local + " to " + remote);
        fileSystem.close();  
    } 
    
    /**
     * File对象上传到hdfs 
     * @param localPath
     * @param hdfsPath
     * @throws IOException
     */
    public static void createFile(File localPath, String path) throws IOException {  
        InputStream in = null;  
        try {  
        	logger.info("在hdfs上建立文件----开始:" + path);
            Configuration conf = new Configuration();  
            FileSystem fileSystem = FileSystem.get(URI.create(path), conf);//初始化连接信息,初始化的时候慢，但是还不能用连接池，因为初始化信息都是不同的
            FSDataOutputStream out = fileSystem.create(new Path(path));//建立文件  
            in = new BufferedInputStream(new FileInputStream(localPath));//获得输入流
            IOUtils.copyBytes(in, out, 4096, false);//写入文件
            out.hsync();  
            out.close();  
            fileSystem.close();
            logger.info("在hdfs上建立文件----结束:" + path);
        } finally {  
            IOUtils.closeStream(in);  
        }  
    }
    /**
     * 删除hdfs中的文件
     * @param path
     * @return
     * @throws IOException
     * @throws TimeoutException 
     */
    public static boolean deleteFile(SpringContextHelper sch ,String path) throws IOException, TimeoutException{
    	logger.info("在hdfs上删除文件----开始:" + path);
    	RedisLockUtil redisLockUtil = (RedisLockUtil) sch.getBean("redisLockUtil");
    	boolean flag = false;
    	String value = null;
    	try {
			value = redisLockUtil.addLock(DEL_LOCK, Long.valueOf(3*60*1000));
			if(!(checkFileExist(path))){//检测文件是否存在
				logger.info(path+"文件不存在，请检查目录");
				//throw new NullPointerException(path+"文件不存在，请检查目录");
			}else{
		        Configuration conf = new Configuration(); 
				FileSystem fileSystem = FileSystem.get(URI.create(path), conf);
				fileSystem.delete(new Path(path), true);
				fileSystem.close();
			}
		}finally {
			redisLockUtil.unLock(DEL_LOCK, value);
		}
    	logger.info("在hdfs上删除文件----结束:" + path);
		return flag;
    }
    
    /** 
     * 读取hdfs中的文件内容 
     * 不可用
     */  
//    public static String readFileFromHdfs(String path) { 
//    	StringBuffer value = new StringBuffer();
//        try {  
//        	if(!(checkFileExist(path))){
//        		throw new NullPointerException();
//        	}
//            Path f = new Path(path);  
//            FSDataInputStream dis = fs.open(f);  
//            InputStreamReader isr = new InputStreamReader(dis, "utf-8");  
//            BufferedReader br = new BufferedReader(isr);  
//            String str = null; 
//            while ((str = br.readLine()) != null) {  
//                System.out.println("==="+str); 
//                value.append(str);
//            }  
//            br.close();  
//            isr.close();  
//            dis.close();  
//        } catch (Exception e) {  
//            e.printStackTrace();  
//        }
//        return value.toString();
//    }
    
    private void ma() {
		// TODO Auto-generated method stub

	}

}

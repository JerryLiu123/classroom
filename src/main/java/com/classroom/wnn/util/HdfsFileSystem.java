package com.classroom.wnn.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.classroom.wnn.util.constants.Constants;


public final class HdfsFileSystem {
	private static final String HDFSURL=Constants.HDFSAddress;
	private static Logger logger = Logger.getLogger(HdfsFileSystem.class);	
	private static Configuration conf;
	private static FileSystem fs;
	private static DistributedFileSystem hdfs;
	
	private static HdfsFileSystem instance = HdfsFileSystem.getInstance();
	
	private HdfsFileSystem() throws IOException{
		conf = new Configuration();
		//conf.set("dfs.socket.timeout", "180000");
		fs = FileSystem.get(URI.create(HDFSURL) ,conf);
		hdfs = (DistributedFileSystem)fs;
	}
	public static synchronized HdfsFileSystem getInstance(){
		if(instance == null){
			try {
				instance = new HdfsFileSystem();
				return instance;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}else{
			return instance;
		}
	}
	
    /** 
     * 列出所有DataNode的名字信息 
     */  
    public static void listDataNodeInfo() {          
        try {  
            DatanodeInfo[] dataNodeStats = hdfs.getDataNodeStats();  
            String[] names = new String[dataNodeStats.length];  
            System.out.println("List of all the datanode in the HDFS cluster:");  
              
            for (int i=0;i<names.length;i++) {  
                names[i] = dataNodeStats[i].getHostName();  
                System.out.println(names[i]);  
            }  
            System.out.println(hdfs.getUri().toString());  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }
    /** 
     * 查看文件是否存在 
     */  
    public static boolean checkFileExist(String path) {  
    	boolean flag = false;
        try {  
            Path f = new Path(path);  
            boolean exist = fs.exists(f);  
            flag = exist;
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return flag;
    }
    
    
    /** 
     * 按路径上传文件到hdfs
     * @param local 
     * @param remote 
     * @throws IOException 
     */  
    public static void copyFile(String local, String remote) throws IOException {  
        fs.copyFromLocalFile(new Path(local), new Path(remote)); 
        logger.info("上传文件至hdfs---from: " + local + " to " + remote);
        fs.close();  
    } 
    
    
    /**
     * File对象上传到hdfs 
     * @param localPath
     * @param hdfsPath
     * @throws IOException
     */
    public static void createFile(File localPath, String hdfsPath) throws IOException {  
        InputStream in = null;  
        try {  
//            Configuration conf = new Configuration();  
//            FileSystem fileSystem = FileSystem.get(URI.create(hdfsPath), conf);  
            FSDataOutputStream out = fs.create(new Path(HDFSURL+hdfsPath));  
            in = new BufferedInputStream(new FileInputStream(localPath));  
            IOUtils.copyBytes(in, out, 4096, false);  
            out.hsync();  
            out.close();  
            logger.info("在hdfs上建立文件:" + HDFSURL+hdfsPath);
        } finally {  
            IOUtils.closeStream(in);  
        }  
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
    
//    public static void main(String[] args){
//    	HadoopServer.getInstance().listDataNodeInfo();
//    }
}

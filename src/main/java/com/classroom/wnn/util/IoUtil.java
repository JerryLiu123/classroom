package com.classroom.wnn.util;


import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.classroom.wnn.bean.Range;
import com.classroom.wnn.servlet.StreamServlet;
import com.classroom.wnn.util.constants.Constants;


/**
 * IO 操作，网上找的~~~
 *
 */
public class IoUtil {
	static final Pattern RANGE_PATTERN = Pattern.compile("bytes \\d+-\\d+/\\d+");
	
	/**
	 * 根据键,生成一个文件(如果不存在,则创建一个新文件)。
	 * @param filename
	 * @param fullPath the file relative path(something like `a../bxx/wenjian.txt`)
	 * @return
	 * @throws IOException
	 */
	public static File getFile(String filename) throws IOException {
		if (filename == null || filename.isEmpty())
			return null;
		String name = filename.replaceAll("/", Matcher.quoteReplacement(File.separator));
		File f = new File(Constants.streamFileRepository + File.separator + name);
		if (!f.getParentFile().exists())
			f.getParentFile().mkdirs();
		if (!f.exists())
			f.createNewFile();
		
		return f;
	}

	/**
	 * 获得文件，如果不存在则创建
	 * @param key
	 * @return
	 * @throws IOException 
	 */
	public static File getTokenedFile(String key) throws IOException {
		if (key == null || key.isEmpty())
			return null;

		File f = new File(Constants.streamFileRepository + File.separator + key);
		if (!f.getParentFile().exists())
			f.getParentFile().mkdirs();
		if (!f.exists())
			f.createNewFile();
		
		return f;
	}
	
	public static void storeToken(String key) throws IOException {
		if (key == null || key.isEmpty())
			return;

		File f = new File(Constants.streamFileRepository + File.separator + key);
		if (!f.getParentFile().exists())
			f.getParentFile().mkdirs();
		if (!f.exists())
			f.createNewFile();
	}
	
	/**
	 * 关闭IO
	 * @param stream
	 */
	public static void close(Closeable stream) {
		try {
			if (stream != null)
				stream.close();
		} catch (IOException e) {
		}
	}
	
	/**
	 * 获取Range参数
	 * @param req
	 * @return
	 * @throws IOException
	 */
	public static Range parseRange(HttpServletRequest req) throws IOException {
		String range = req.getHeader(StreamServlet.CONTENT_RANGE_HEADER);
		Matcher m = RANGE_PATTERN.matcher(range);
		if (m.find()) {
			range = m.group().replace("bytes ", "");
			String[] rangeSize = range.split("/");
			String[] fromTo = rangeSize[0].split("-");

			long from = Long.parseLong(fromTo[0]);
			long to = Long.parseLong(fromTo[1]);
			long size = Long.parseLong(rangeSize[1]);

			return new Range(from, to, size);
		}
		throw new IOException("Illegal Access!");
	}

	/**
	 * 将输出流写入执行文件
	 */
	public static long streaming(InputStream in, String key, String fileName) throws IOException {
		OutputStream out = null;
		File f = getTokenedFile(key);
		try {
			out = new FileOutputStream(f);

			int read = 0;
			final byte[] bytes = new byte[Constants.BUFFER_LENGTH];
			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
		} finally {
			close(out);
		}
		/** rename the file * fix the `renameTo` bug */
		File dst = IoUtil.getFile(fileName);
		dst.delete();
		f.renameTo(dst);
		
		long length = getFile(fileName).length();
		/** if `STREAM_DELETE_FINISH`, then delete it. */
		if (Constants.streamDeleteFinish.equals("true")) {
			dst.delete();
		}
		
		return length;
	}
}

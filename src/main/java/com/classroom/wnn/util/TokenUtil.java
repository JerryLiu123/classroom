package com.classroom.wnn.util;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * 用于生成上传问价的token
 * @author lgh
 *
 */
public class TokenUtil {

	/**
	 * 生成Token， A(hashcode>0)|B + |name的Hash值| +_+size的值
	 * @param name
	 * @param size
	 * @param num
	 * @return
	 * @throws Exception
	 */
	public static String generateToken(String name, String size)
			throws IOException {
		if (name == null || size == null){
			return "";
		}
		int code = name.hashCode();
		try {
			String token = (code > 0 ? "A" : "B") + Math.abs(code) + "_" + size.trim();
			/** TODO: store your token, here just create a file */
			IoUtil.storeToken(token);
			
			return token;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}

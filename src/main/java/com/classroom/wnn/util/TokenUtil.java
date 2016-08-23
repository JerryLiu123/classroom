package com.classroom.wnn.util;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

/**
 * Key Util: 1> according file name|size ..., generate a key;
 * 			 2> the key should be unique.
 * 文件名转码
 */
public class TokenUtil {

	/**
	 * 生成Token， A(hashcode>0)|B + |name的Hash值| +_+size的值 +_+num
	 * @param name
	 * @param size
	 * @param num
	 * @return
	 * @throws Exception
	 */
	public static String generateToken(String name, String size, String num)
			throws IOException {
		if (name == null || size == null){
			return "";
		}
		if (StringUtils.isBlank(num)){
			return "";
		}
		int code = name.hashCode();
		try {
			String token = (code > 0 ? "A" : "B") + Math.abs(code) + "_" + size.trim()+"_"+num;
			/** TODO: store your token, here just create a file */
			IoUtil.storeToken(token);
			
			return token;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}

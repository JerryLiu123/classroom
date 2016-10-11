package com.classroom.wnn.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;

import javax.crypto.EncryptedPrivateKeyInfo;



import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/********************************************
 * 数据类型转换类.<br>
 * 用于日常代码中的数据类型转换操作，该类所有方法以静态方法调用<br>
 * CreateDate: 2014年3月3日<br>
 * Copyright: Copyright(c) 2014年3月3日<br>
 * Company: 金弘测控<br>
 * @since v1.0.0
 * @author  巫作坤
 * @version v1.0.0
 *********************************************/
public class Convert {
	private static char[] base64EncodeChars = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

	public static String encode(Byte[] data) {
		StringBuffer sb = new StringBuffer();
		int len = data.length;
		int i = 0;
		int b1, b2, b3;
		while (i < len) {
			b1 = data[i++] & 0xff;
			if (i == len) {
				sb.append(base64EncodeChars[b1 >>> 2]);
				sb.append(base64EncodeChars[(b1 & 0x3) << 4]);
				sb.append("==");
				break;
			}
			b2 = data[i++] & 0xff;
			if (i == len) {
				sb.append(base64EncodeChars[b1 >>> 2]);
				sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
				sb.append(base64EncodeChars[(b2 & 0x0f) << 2]);
				sb.append("=");
				break;
			}
			b3 = data[i++] & 0xff;
			sb.append(base64EncodeChars[b1 >>> 2]);
			sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
			sb.append(base64EncodeChars[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
			sb.append(base64EncodeChars[b3 & 0x3f]);
		}
		return sb.toString();
	}

	public static String encode(byte[] data) {
		StringBuffer sb = new StringBuffer();
		int len = data.length;
		int i = 0;
		int b1, b2, b3;
		while (i < len) {
			b1 = data[i++] & 0xff;
			if (i == len) {
				sb.append(base64EncodeChars[b1 >>> 2]);
				sb.append(base64EncodeChars[(b1 & 0x3) << 4]);
				sb.append("==");
				break;
			}
			b2 = data[i++] & 0xff;
			if (i == len) {
				sb.append(base64EncodeChars[b1 >>> 2]);
				sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
				sb.append(base64EncodeChars[(b2 & 0x0f) << 2]);
				sb.append("=");
				break;
			}
			b3 = data[i++] & 0xff;
			sb.append(base64EncodeChars[b1 >>> 2]);
			sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
			sb.append(base64EncodeChars[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
			sb.append(base64EncodeChars[b3 & 0x3f]);
		}
		return sb.toString();
	}

	public static byte[] decode(String str) throws Exception {
		char[] data = str.toCharArray();
		int len = ((data.length + 3) / 4) * 3;
		if (data.length > 0 && data[data.length - 1] == '=')
			--len;
		if (data.length > 1 && data[data.length - 2] == '=')
			--len;
		byte[] out = new byte[len];
		int shift = 0;
		int accum = 0;
		int index = 0;
		for (int ix = 0; ix < data.length; ix++) {
			int value = codes[data[ix] & 0xFF];
			if (value >= 0) {
				accum <<= 6;
				shift += 6;
				accum |= value;
				if (shift >= 8) {
					shift -= 8;
					out[index++] = (byte) ((accum >> shift) & 0xff);
				}
			}
		}
		if (index != out.length)
			throw new Exception();
		return out;

	}

	static private byte[] codes = new byte[256];
	static {
		for (int i = 0; i < 256; i++)
			codes[i] = -1;
		for (int i = 'A'; i <= 'Z'; i++)
			codes[i] = (byte) (i - 'A');
		for (int i = 'a'; i <= 'z'; i++)
			codes[i] = (byte) (26 + i - 'a');
		for (int i = '0'; i <= '9'; i++)
			codes[i] = (byte) (52 + i - '0');
		codes['+'] = 62;
		codes['/'] = 63;
	}

	/********************************************
	 * 对象转成BASE64字符串.<br>
	 * 方法业务逻辑详细描述……<br>
	 * @since v1.0.0
	 * @param 参数名 参数类型 参数描述
	 * @return 返回类型 返回类型描述
	 * <br>
	 * --------------------------------------<br>
	 * 编辑历史<br>
	 * 2014年3月3日::巫作坤::创建此方法<br>
	 *********************************************/
	@SuppressWarnings("restriction")
	public static String toBase64String(Object obj) {
		BASE64Encoder encoder = new BASE64Encoder();
		String result = null;
		try {
			result = encoder.encode(obj.toString().getBytes());
		} catch (Exception ex) {
			result = null;
		}

		return result;
	}

	/********************************************
	 * BASE64字符串转字节数组.<br>
	 * 方法业务逻辑详细描述……<br>
	 * @since v1.0.0
	 * @param 参数名 参数类型 参数描述
	 * @return 返回类型 返回类型描述
	 * <br>
	 * --------------------------------------<br>
	 * 编辑历史<br>
	 * 2014年3月3日::巫作坤::创建此方法<br>
	 *********************************************/
	@SuppressWarnings("restriction")
	public static byte[] fromBase64String(String source) {
		BASE64Decoder decoder = new BASE64Decoder();
		byte[] result = null;
		try {
			result = decoder.decodeBuffer(source);
		} catch (Exception ex) {
			result = null;
		}

		return result;
	}
	
    /**对象转byte[]
     * @param obj
     * @return
     * @throws IOException
     */
    public static byte[] objectToBytes(Object obj) throws Exception{
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);
        byte[] bytes = bo.toByteArray();
        bo.close();
        oo.close();
        return bytes;
    }
    /**byte[]转对象
     * @param bytes
     * @return
     * @throws Exception
     */
    public static Object bytesToObject(byte[] bytes) throws Exception{
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream sIn = new ObjectInputStream(in);
        return sIn.readObject();
    }
    
    /**
     * long 转 byte[]，用上面的 Object 也行~
     * @param num
     * @return
     */
    public static byte[] long2Bytes(long num) {  
        byte[] byteNum = new byte[8];  
        for (int ix = 0; ix < 8; ++ix) {  
            int offset = 64 - (ix + 1) * 8;  
            byteNum[ix] = (byte) ((num >> offset) & 0xff);  
        }  
        return byteNum;  
    }  
    
    /**
     * byte 转 long
     * @param byteNum
     * @return
     */
    public static long bytes2Long(byte[] byteNum) {  
        long num = 0;  
        for (int ix = 0; ix < 8; ++ix) {  
            num <<= 8;  
            num |= (byteNum[ix] & 0xff);  
        }  
        return num;  
    }

	
}

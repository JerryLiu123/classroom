package com.classroom.wnn.util;

import java.util.Random;

import com.classroom.wnn.util.security.Encryptor;

public class IDCoder {

	static int mixCodeLength = 6;

	/********************************************
	 * Encode<br>
	 * URL ID加密<br> 先把ID  BASE64一下 然后从第二位中间插入一个固定长度的混淆码  最后再base64一下防止id中带有特殊符号导致请求出错
	 * @since v1.0.0
	 * @param IDString
	 * @return
	 * @throws Exception 
	 *********************************************/
	public static String EnCode(String IDString) throws Exception {
		return Convert.toBase64String(Encryptor.encryptUrl(IDString));
		// BASE64Encoder coder = new BASE64Encoder();
		// Random random = new Random();
		// Integer int_random = random.nextInt(240) + 16;// 算出16-255的随机数
		// BigInteger int_result = new BigInteger(IDString).add(new
		// BigInteger(int_random + ""));
		// String str_right =
		// URLEncoder.encode(coder.encode(int_result.toString().getBytes()),
		// "UTF-8");
		//
		// // 将数字放在第3，4位
		// return new StringBuilder(str_right).insert(2,
		// Integer.toHexString(int_random).toUpperCase()).toString();

	}

	/********************************************
	 * DeCode<br>
	 * URL ID 解密<br>
	 * @since v1.0.0
	 * @param string
	 * @return
	 * @throws Exception 
	 *********************************************/
	public static String DeCode(String string) throws Exception {
		string = new String(Convert.fromBase64String(string), "utf8");// 先用base64解密一下
		return Encryptor.decryptUrl(string);
		// BASE64Decoder coder = new BASE64Decoder();
		//
		// String str_left = string.substring(2, 4);
		// String str_right = new StringBuilder(string).delete(2, 4).toString();
		// String id_mix = new
		// String(coder.decodeBuffer(URLDecoder.decode(str_right, "UTF-8")));
		//
		// BigInteger id = new BigInteger(id_mix).subtract(new
		// BigInteger(Integer.parseInt(str_left, 16) + ""));
		// return id.toString();
	}

	/********************************************
	 * getMixCode<br>
	 * 获取混淆码<br>
	 * @since v1.0.0
	 * @param length
	 * @return
	 * String
	 * <br>
	 * --------------------------------------<br>
	 * 编辑历史<br>
	 * 2015年2月15日::x04::创建此方法<br>
	 *********************************************/
	private static String getMixCode(String str) {
		byte[] byte_array = str.getBytes();
		if (byte_array.length == 0) {
			byte_array = new byte[] { 0x05 };
		}
		String mixCode = "";
		for (int i = 0; i < mixCodeLength; i++) {
			Random random = new Random();
			mixCode = mixCode + (char) (random.nextInt(26) + 65);
			// random.next
		}
		return mixCode;
	}
}

package com.classroom.wnn.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtil {

	/**
	 * ******************************************
	 * 设置cookie.默认1年<br>
	 * 方法业务逻辑详细描述……<br>
	 * @since v1.0.0
	 * @param 参数名 参数类型 参数描述
	 * @return 返回类型 返回类型描述
	 * <br>
	 * --------------------------------------<br>
	 * 编辑历史<br>
	 * 2014年10月30日::x05::创建此方法<br>
	 ********************************************
	 */
	public static void addCookie(HttpServletResponse response, String name, String value) {
		addCookie(response, name, value, 365 * 24 * 60 * 60);
	}

	/**
	 * ******************************************
	 * 设置cookie.<br>
	 * 方法业务逻辑详细描述……<br>
	 * @since v1.0.0
	 * @param 参数名 参数类型 参数描述
	 * @return 返回类型 返回类型描述
	 * <br>
	 * --------------------------------------<br>
	 * 编辑历史<br>
	 * 2014年10月30日::x05::创建此方法<br>
	 ********************************************
	 */
	public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
	}

	/**
	 * ******************************************
	 * 根据名称获取值.<br>
	 * 方法业务逻辑详细描述……<br>
	 * @since v1.0.0
	 * @param 参数名 参数类型 参数描述
	 * @return 返回类型 返回类型描述
	 * <br>
	 * --------------------------------------<br>
	 * 编辑历史<br>
	 * 2014年10月30日::x05::创建此方法<br>
	 ********************************************
	 */
	public static String getCookieValueByName(HttpServletRequest request, String name) {
		return getCookieByName(request, name) == null ? null : getCookieByName(request, name).getValue();
	}

	/**
	 * ******************************************
	 * 删除cookie.<br>
	 * 方法业务逻辑详细描述……<br>
	 * @since v1.0.0
	 * @param 参数名 参数类型 参数描述
	 * @return 返回类型 返回类型描述
	 * <br>
	 * --------------------------------------<br>
	 * 编辑历史<br>
	 * 2014年10月30日::x05::创建此方法<br>
	 ********************************************
	 */
	public static void deleteCookie(HttpServletResponse response, String name) {
		addCookie(response, name, null, 0);
	}

	/**
	 * ******************************************
	 * 根据名称获取值.<br>
	 * 方法业务逻辑详细描述……<br>
	 * @since v1.0.0
	 * @param 参数名 参数类型 参数描述
	 * @return 返回类型 返回类型描述
	 * <br>
	 * --------------------------------------<br>
	 * 编辑历史<br>
	 * 2014年10月30日::x05::创建此方法<br>
	 ********************************************
	 */
	public static Cookie getCookieByName(HttpServletRequest request, String name) {
		Map<String, Cookie> cookieMap = ReadCookieMap(request);
		if (cookieMap.containsKey(name)) {
			Cookie cookie = (Cookie) cookieMap.get(name);
			return cookie;
		} else {
			return null;
		}
	}

	/**
	 * ******************************************
	 * 读取cookie到.<br>
	 * 方法业务逻辑详细描述……<br>
	 * @since v1.0.0
	 * @param 参数名 参数类型 参数描述
	 * @return 返回类型 返回类型描述
	 * <br>
	 * --------------------------------------<br>
	 * 编辑历史<br>
	 * 2014年10月30日::x05::创建此方法<br>
	 ********************************************
	 */
	private static Map<String, Cookie> ReadCookieMap(HttpServletRequest request) {
		Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
		Cookie[] cookies = request.getCookies();
		if (null != cookies) {
			for (Cookie cookie : cookies) {
				cookieMap.put(cookie.getName(), cookie);
			}
		}
		return cookieMap;
	}
}

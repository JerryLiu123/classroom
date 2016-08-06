package com.classroom.wnn.util;

import javax.servlet.http.HttpServletRequest;

import com.classroom.wnn.bean.UserBean;
import com.classroom.wnn.util.constants.Constants;


public class SysParamerters {
	public static UserBean userInfo(HttpServletRequest request) {
		return (UserBean) request.getSession().getAttribute(Constants.SESSION_USER_INFO);
	}

	public static void setUserInfo(HttpServletRequest request, UserBean user) {
		request.getSession().removeAttribute(Constants.SESSION_USER_INFO);
		if (user != null) {
			request.getSession().setAttribute(Constants.SESSION_USER_INFO, user);
		}
	}

	public static Integer userId(HttpServletRequest request) {
		UserBean userInfo = userInfo(request);
		if (userInfo != null) {
			return userInfo.getId();
		} else {
			return null;
		}
	}

	public static String yhbh(HttpServletRequest request) {
		UserBean userInfo = userInfo(request);
		if (userInfo != null) {
			return userInfo.getYhbh();
		} else {
			return null;
		}
	}

	public static String yhmc(HttpServletRequest request) {
		UserBean userInfo = userInfo(request);
		if (userInfo != null) {
			return userInfo.getYhmc();
		} else {
			return null;
		}
	}

	public static Object token(HttpServletRequest request) {
		return request.getSession().getAttribute(Constants.SESSION_USER_TOKEN);
	}

}


package com.classroom.wnn.util;

import javax.servlet.http.HttpServletRequest;

import com.classroom.wnn.bean.UserBean;
import com.classroom.wnn.util.constants.Constants;

/**
 * session 用户基本操作
 * @author lgh
 *
 */
public class SysParamerters {
	/**
	 * 从session中获得用户信息
	 * @param request
	 * @return
	 */
	public static UserBean userInfo(HttpServletRequest request) {
		return (UserBean) request.getSession().getAttribute(Constants.SESSION_USER_INFO);
	}
	
	/**
	 * 讲user信息加入 session
	 * @param request
	 * @param user
	 */
	public static void setUserInfo(HttpServletRequest request, UserBean user) {
		request.getSession().removeAttribute(Constants.SESSION_USER_INFO);
		if (user != null) {
			request.getSession().setAttribute(Constants.SESSION_USER_INFO, user);
		}
	}

	/**
	 * 获得用户id
	 * @param request
	 * @return
	 */
	public static Integer userId(HttpServletRequest request) {
		UserBean userInfo = userInfo(request);
		if (userInfo != null) {
			return userInfo.getId();
		} else {
			return null;
		}
	}
	/**
	 * 获得用户编号
	 * @param request
	 * @return
	 */
	public static String yhbh(HttpServletRequest request) {
		UserBean userInfo = userInfo(request);
		if (userInfo != null) {
			return userInfo.getYhbh();
		} else {
			return null;
		}
	}
	/**
	 * 获得用户名称
	 * @param request
	 * @return
	 */
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


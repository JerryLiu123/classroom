package com.classroom.wnn.controller;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.classroom.wnn.bean.UserBean;
import com.classroom.wnn.util.CookieUtil;
import com.classroom.wnn.util.IDCoder;
import com.classroom.wnn.util.SysParamerters;
import com.classroom.wnn.util.constants.Constants;


public class BaseController {

	private static String resource_version = "";
	
	/**
	 * ******************************************
	 * getBaseMap<br>
	 * 向map中添加基础数据<br>
	 * @since v1.0.0
	 * @param 参数名 参数类型 参数描述
	 * @return 返回类型 返回类型描述
	 * <br>
	 * --------------------------------------<br>
	 * 编辑历史<br>
	 * 2015年9月22日::x06::创建此方法<br>
	 ********************************************
	 */
	protected Map<String, Object> getBaseMap(Map<String, Object> map) {
		// 程序目录
		map.put("ap", Constants.applicationPath);
		// 资源目录
		map.put("rs", Constants.staticUrl + "/");
		// 主页url
		map.put("home", Constants.homeUrl);

		// 控制台url
		map.put("businessUrl", Constants.businessUrl);
		String rv = getResource_version();
		// 资源版本
		map.put("rv", rv);
		return map;
	}
	
	/**
	 * ******************************************
	 * getUserMap<br>
	 * 获取带有用户信息的map，包含getBaseMap的基本信息，用户未登录时将检测cookie<br>
	 * @since v1.0.0
	 * @param 参数名 参数类型 参数描述
	 * @return 返回类型 返回类型描述
	 * <br>
	 * --------------------------------------<br>
	 * 编辑历史<br>
	 * 2015年9月22日::x06::创建此方法<br>
	 ********************************************
	 */
	@SuppressWarnings("unused")
	protected Map<String, Object> getUserMap(Map<String, Object> map, HttpServletRequest request) {
		map = getBaseMap(map);
		// 登录用户信息
		UserBean user = SysParamerters.userInfo(request);
		if (user == null) {// 用户未登录，检测cookie自动登录
			String userFlag = CookieUtil.getCookieValueByName(request, "user_flag");// 最后是cookie
			if (userFlag != null && userFlag.trim().length() > 0) {// 登录时已将用户ID加密并存入cookie，此处判断cookie
				try {
					String uid_str = IDCoder.DeCode(userFlag);
					if (uid_str != null && uid_str.trim().length() > 0) {
						Integer uid = Integer.parseInt(uid_str);
						//user = userService.getUserInfo(uid);//根据cookie中的用户信息查找用户
						if (user != null) {
							String user_token = "";
							//String user_token = userService.refreshToken(uid);//根据用户id查找用户完成信息
							user.setDlfs("2");// 2:cookie登录
							if (user.getZt() == 1) {
								// 刷新session中的用户信息
								refreshUserInfo(request, user);
								// 设置user_token，多平台登录使用
								request.getSession().setAttribute(Constants.SESSION_USER_TOKEN, user_token);
							}
						}
					}
				} catch (Exception e) {
					user = null;
				}
			}
		}
		// token...
		map.put("login_token", SysParamerters.token(request));
		map.put("user", user);
		return map;
	}
	
	@Autowired  
	protected  HttpServletRequest request;
	
	protected static final Log LOG = LogFactory.getLog(BaseController.class);
	
	// AJAX输出，返回null
    public String ajax(String content, String type, HttpServletResponse response) {
        try {
            response.setContentType(type + ";charset=UTF-8");
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.getWriter().write(content);
            response.getWriter().flush();
        } catch (IOException e) {
            LOG.error("ajax", e);
        }
        return null;
    }
	
	/**AJAX输出HTML，返回null**/
    public String ajaxHtml(String html, HttpServletResponse response) {
        return ajax(html, "text/html", response);
    }
    
    /**AJAX输出json，返回null**/
    public String ajaxJson(String json, HttpServletResponse response) {
        return ajax(json, "application/json", response);
    }
	
	
	private static String getResource_version() {
		if (resource_version == null || resource_version.trim().length() == 0) {
			UUID uuid = UUID.randomUUID();
			String us = uuid.toString().replace("-", "");
			resource_version = us.substring(0, 5);
		}
		return resource_version;
	}
	
	protected static void refreshResource_version() {
		UUID uuid = UUID.randomUUID();
		String us = uuid.toString().replace("-", "");
		resource_version = us.substring(0, 5);
	}
	
	/**
	 * ******************************************
	 * refreshUserInfo<br>
	 * 刷新session中的用户信息，user为空时将移除session中的用户信息<br>
	 * @since v1.0.0
	 * @param 参数名 参数类型 参数描述
	 * @return 返回类型 返回类型描述
	 * <br>
	 * --------------------------------------<br>
	 * 编辑历史<br>
	 * 2015年9月22日::x06::创建此方法<br>
	 ********************************************
	 */
	protected void refreshUserInfo(HttpServletRequest request, UserBean user) {
		SysParamerters.setUserInfo(request, user);
	}
}

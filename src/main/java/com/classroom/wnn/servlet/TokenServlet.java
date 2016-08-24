package com.classroom.wnn.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.classroom.wnn.util.TokenUtil;
import com.classroom.wnn.util.constants.Constants;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * According the file name and its size, generate a unique token. And this
 * token will be refer to user's file.
 */
public class TokenServlet extends HttpServlet {
	private static final long serialVersionUID = 2650340991003623753L;
	
	@Override
	public void init() throws ServletException {
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String name = new String(req.getParameter(Constants.FILE_NAME_FIELD).getBytes("ISO-8859-1"), "UTF-8");
		boolean flag = true;
		String message = "";
		PrintWriter writer = resp.getWriter();
		JSONObject json = new JSONObject();
		/**/
		String size = req.getParameter(Constants.FILE_SIZE_FIELD);
		if(StringUtils.isBlank(size) || size.equals("0")){
			flag = false;
			message = "文件大小为0，不可上传";
		}else{
			String token = TokenUtil.generateToken(name, size);
			json.put(Constants.TOKEN_FIELD, token);
		}
		
		try {
			if (Constants.streamIsCross.equals("true")){
				json.put(Constants.SERVER_FIELD, Constants.streamCrossServer);
			}
			json.put(Constants.SUCCESS, flag);
			json.put(Constants.MESSAGE, message);
		} catch (JSONException e) {
		}
		/** TODO: save the token. */
		
		writer.write(json.toString());
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doHead(req, resp);
	}

	@Override
	public void destroy() {
		super.destroy();
	}

}

package com.classroom.wnn.bean;

import java.util.Date;


public class UserBean extends BaseUserBean {
	private static final long serialVersionUID = 1L;
	private String yx;// 邮箱
	private String xm;// 用户姓名
	private Integer xb; //性别
	private String sjhm;//
	private String gzdw;
	private String gzzw;
	private String dwdz;
	private String dwdh;
	private String tx;
	private Date zcsj;
	private Integer zt;//状态 1为启用，0为禁用

	private String bz;// 额外信息时使用

	/**
	 * 登录方式，0：未登录；1：用户手动登录；2：cookie登录；
	 */
	private String dlfs;

	public UserBean() {

	}

	public UserBean(BaseUserBean baseUser) {
		this.setId(baseUser.getId());
		this.setLp(baseUser.getLp());
		this.setMm(baseUser.getMm());
		this.setYhbh(baseUser.getYhbh());
		this.setYhlx(baseUser.getYhlx());
		this.setYhmc(baseUser.getYhmc());
	}

	public String getYx() {
		return yx;
	}

	public void setYx(String yx) {
		this.yx = yx;
	}

	public String getXm() {
		return xm;
	}

	public void setXm(String xm) {
		this.xm = xm;
	}

	public Integer getXb() {
		return xb;
	}

	public void setXb(Integer xb) {
		this.xb = xb;
	}

	public String getSjhm() {
		return sjhm;
	}

	public void setSjhm(String sjhm) {
		this.sjhm = sjhm;
	}

	public String getGzdw() {
		return gzdw;
	}

	public void setGzdw(String gzdw) {
		this.gzdw = gzdw;
	}

	public Date getZcsj() {
		return zcsj;
	}

	public String getGzzw() {
		return gzzw;
	}

	public void setGzzw(String gzzw) {
		this.gzzw = gzzw;
	}

	public String getDwdz() {
		return dwdz;
	}

	public void setDwdz(String dwdz) {
		this.dwdz = dwdz;
	}

	public String getDwdh() {
		return dwdh;
	}

	public void setDwdh(String dwdh) {
		this.dwdh = dwdh;
	}

	public String getTx() {
		return tx;
	}

	public void setTx(String tx) {
		this.tx = tx;
	}

	public void setZcsj(Date zcsj) {
		this.zcsj = zcsj;
	}

	public Integer getZt() {
		return zt;
	}

	public void setZt(Integer zt) {
		this.zt = zt;
	}

	public String getBz() {
		return bz;
	}

	public void setBz(String bz) {
		this.bz = bz;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getDlfs() {
		return dlfs;
	}

	public void setDlfs(String dlfs) {
		this.dlfs = dlfs;
	}

}

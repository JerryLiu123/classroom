package com.classroom.wnn.bean;

import java.io.Serializable;
import java.math.BigDecimal;

public class UserInfo implements Serializable {


	private static final long serialVersionUID = 1L;
	private Integer id;
	// 邮箱
	private String yx;
	// 用户名称
	private String yhmc;
	// 用户姓名
	private String xm;
	// 用户头像URL
	private String tx;
	// 余额
	private BigDecimal ye;
	// 用户积分
	private BigDecimal jf;
	private int djsl;// 未完成单据数量

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

	public BigDecimal getYe() {
		return ye;
	}

	public void setYe(BigDecimal ye) {
		this.ye = ye;
	}

	public BigDecimal getJf() {
		return jf;
	}

	public void setJf(BigDecimal jf) {
		this.jf = jf;
	}

	public String getYhmc() {
		return yhmc;
	}

	public void setYhmc(String yhmc) {
		this.yhmc = yhmc;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTx() {
		return tx;
	}

	public void setTx(String tx) {
		this.tx = tx;
	}

	public int getDjsl() {
		return djsl;
	}

	public void setDjsl(int djsl) {
		this.djsl = djsl;
	}

}

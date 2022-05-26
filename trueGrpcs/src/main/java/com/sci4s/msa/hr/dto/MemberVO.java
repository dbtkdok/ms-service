package com.sci4s.msa.hr.dto;

import java.util.Date;

public class MemberVO {	
	
	private String loginID;   
	private String userPW;
	private String aes256PW;   
	private String userNM;	
	private String enabled;  
	private Date regDate;    
	private Date updDate; 	
	private String userUID;
	private String borgUID;
	private String borgNM;
	private String agentID;
	private String pID;
	private String csKey;
	private String token;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public void setBorgUID(String borgUID) {
		this.borgUID = borgUID;
	}
	public String getLoginID() {
		return loginID;
	}
	public void setLoginID(String loginID) {
		this.loginID = loginID;
	}
	public String getUserPW() {
		return userPW;
	}
	public void setUserPW(String userPW) {
		this.userPW = userPW;
	}
	public String getAes256PW() {
		return aes256PW;
	}
	public void setAes256PW(String aes256pw) {
		aes256PW = aes256pw;
	}
	public String getUserNM() {
		return userNM;
	}
	public void setUserNM(String userNM) {
		this.userNM = userNM;
	}
	public String getEnabled() {
		return enabled;
	}
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}
	public Date getRegDate() {
		return regDate;
	}
	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}
	public Date getUpdDate() {
		return updDate;
	}
	public void setUpdDate(Date updDate) {
		this.updDate = updDate;
	}
	public String getUserUID() {
		return userUID;
	}
	public void setUserUID(String userUID) {
		this.userUID = userUID;
	}
	public String getBorgUID() {
		return borgUID;
	}
	public void setBorgID(String borgUID) {
		this.borgUID = borgUID;
	}
	public String getBorgNM() {
		return borgNM;
	}
	public void setBorgNM(String borgNM) {
		this.borgNM = borgNM;
	}
	public String getAgentID() {
		return agentID;
	}
	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}
	public String getpID() {
		return pID;
	}
	public void setpID(String pID) {
		this.pID = pID;
	}
	public String getCsKey() {
		return csKey;
	}
	public void setCsKey(String csKey) {
		this.csKey = csKey;
	}
}

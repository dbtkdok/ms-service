package com.sci4s.msa.tsrc.dto;

public class UserCustDto {
	
	private String userID;
	private String custID;
	private String custNM;
	
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getCustID() {
		return custID;
	}
	public void setCustID(String custID) {
		this.custID = custID;
	}
	public String getCustNM() {
		return custNM;
	}
	public void setCustNM(String custNM) {
		this.custNM = custNM;
	}
}

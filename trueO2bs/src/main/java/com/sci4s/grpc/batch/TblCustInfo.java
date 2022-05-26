package com.sci4s.grpc.batch;

public class TblCustInfo {
	// custID|repCustID|custNM|custGB|bizNO|telNO|faxNO|zipCD|addr1|addr2|repNM|eMail|dbSTS|agentID
	
	private String custID;
	private String repCustID;	
	private String custNM;
	private String custGB;
	private String bizNO;
	private String telNO;
	private String faxNO;
	private String zipCD;
	private String addr1;
	private String addr2;
	private String repNM;
	private String eMail;
	private String dbSTS;
	private String agentID;
	private String createrID;	
	private String SQLMODE;
	
	public String getSQLMODE() {
		return SQLMODE;
	}
	public void setSQLMODE(String sQLMODE) {
		SQLMODE = sQLMODE;
	}
	public String getCreaterID() {
		return createrID;
	}
	public void setCreaterID(String createrID) {
		this.createrID = createrID;
	}
	public String getCustID() {
		return custID;
	}
	public void setCustID(String custID) {
		this.custID = custID;
	}
	public String getRepCustID() {
		return repCustID;
	}
	public void setRepCustID(String repCustID) {
		this.repCustID = repCustID;
	}
	public String getCustNM() {
		return custNM;
	}
	public void setCustNM(String custNM) {
		this.custNM = custNM;
	}
	public String getCustGB() {
		return custGB;
	}
	public void setCustGB(String custGB) {
		this.custGB = custGB;
	}
	public String getBizNO() {
		return bizNO;
	}
	public void setBizNO(String bizNO) {
		this.bizNO = bizNO;
	}
	public String getTelNO() {
		return telNO;
	}
	public void setTelNO(String telNO) {
		this.telNO = telNO;
	}
	public String getFaxNO() {
		return faxNO;
	}
	public void setFaxNO(String faxNO) {
		this.faxNO = faxNO;
	}
	public String getZipCD() {
		return zipCD;
	}
	public void setZipCD(String zipCD) {
		this.zipCD = zipCD;
	}
	public String getAddr1() {
		return addr1;
	}
	public void setAddr1(String addr1) {
		this.addr1 = addr1;
	}
	public String getAddr2() {
		return addr2;
	}
	public void setAddr2(String addr2) {
		this.addr2 = addr2;
	}
	public String getRepNM() {
		return repNM;
	}
	public void setRepNM(String repNM) {
		this.repNM = repNM;
	}
	public String geteMail() {
		return eMail;
	}
	public void seteMail(String eMail) {
		this.eMail = eMail;
	}
	public String getDbSTS() {
		return dbSTS;
	}
	public void setDbSTS(String dbSTS) {
		this.dbSTS = dbSTS;
	}
	public String getAgentID() {
		return agentID;
	}
	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}
}

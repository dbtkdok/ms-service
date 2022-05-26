package com.sci4s.grpc.batch;

public class TblScopes {
	private long scopeUID;
	private String scopeID;
	private String scopeNM;
	private String scopeRemk;
	private String svcTypeID;
	private String svcTypeNM;
	private String dbSTS;
	private String updateDT;
	private String updaterID;
	private String agentID;
	private String SQLMODE;
	
	public long getScopeUID() {
		return scopeUID;
	}
	public void setScopeUID(long scopeUID) {
		this.scopeUID = scopeUID;
	}
	public String getScopeID() {
		return scopeID;
	}
	public void setScopeID(String scopeID) {
		this.scopeID = scopeID;
	}
	public String getScopeNM() {
		return scopeNM;
	}
	public void setScopeNM(String scopeNM) {
		this.scopeNM = scopeNM;
	}
	public String getScopeRemk() {
		return scopeRemk;
	}
	public void setScopeRemk(String scopeRemk) {
		this.scopeRemk = scopeRemk;
	}
	public String getSvcTypeID() {
		return svcTypeID;
	}
	public void setSvcTypeID(String svcTypeID) {
		this.svcTypeID = svcTypeID;
	}
	public String getSvcTypeNM() {
		return svcTypeNM;
	}
	public void setSvcTypeNM(String svcTypeNM) {
		this.svcTypeNM = svcTypeNM;
	}
	public String getDbSTS() {
		return dbSTS;
	}
	public void setDbSTS(String dbSTS) {
		this.dbSTS = dbSTS;
	}
	public String getUpdateDT() {
		return updateDT;
	}
	public void setUpdateDT(String updateDT) {
		this.updateDT = updateDT;
	}
	public String getUpdaterID() {
		return updaterID;
	}
	public void setUpdaterID(String updaterID) {
		this.updaterID = updaterID;
	}
	public String getAgentID() {
		return agentID;
	}
	public void setAgentID(String agentID) {
		this.agentID = agentID;
	}
	public String getSQLMODE() {
		return SQLMODE;
	}
	public void setSQLMODE(String sQLMODE) {
		SQLMODE = sQLMODE;
	}
}

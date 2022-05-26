package com.sci4s.grpc.batch;

public class TblRolescopes {
	private long roleUID;
	private long scopeUID;
	private String roleID;
	private String agentID;
	private String SQLMODE;
	
	public long getRoleUID() {
		return roleUID;
	}
	public void setRoleUID(long roleUID) {
		this.roleUID = roleUID;
	}
	public long getScopeUID() {
		return scopeUID;
	}
	public void setScopeUID(long scopeUID) {
		this.scopeUID = scopeUID;
	}
	public String getRoleID() {
		return roleID;
	}
	public void setRoleID(String roleID) {
		this.roleID = roleID;
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

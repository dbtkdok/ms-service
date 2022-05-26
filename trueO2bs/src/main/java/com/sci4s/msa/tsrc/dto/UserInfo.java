package com.sci4s.msa.tsrc.dto;

public class UserInfo {
	
	private int    userUID       ;
	private String loginID       ;
	private String pwd           ;
	private String userPW        ;
	private String userNM        ;
	private String userNMEng     ;
	private String userNMCN      ;
	private String empNO         ;
	private String regionCD      ; //사용자 조직의 지역 정보
	private String regionNM      ;
	private String zipCD         ;
	private String telNO   	     ;
	private String mobile	     ;
	private String userType      ; //사용장 구분  A="운영사" , V="공급사"
	private int    isActive      ;
	private String roleID        ; //권한코드
	private String grade         ; //직급/직책
	private String faxNO 	     ;
	private int    totAdminYn    ; //1:대표(전체)관리자여부, 0:아님
	private String addr1	     ;
	private String addr2	     ;
	private String email 	     ;
	private String custID 	     ; //회사코드=vendorID
	private String custNM    	 ;
	private String custNMEng     ;
	private String custNMCN      ;
	private String obuID         ; //사업장코드=vendorID
	private String obuNM         ;
	private int    borgUID    	 ; //부서UID
	private String borgID        ; //부서코드=vendorID
	private String borgNM	     ;
	private String agentID       ; //대행사ID	
	private String userActFile   ; //사용자권한 XML파일경로
	private String dbsts         ; //Y:사용, N:삭제
	
	private String userIP        ;	
	private String csKey         ; //로그인 인증키
	private String errCode       ;
	private String errMsg        ;	
	private String pID           ; //프로그램ID 
	private int gridColor        ; //그리드 색상
	//private String userGB        ; //사용장 구분  A="운영사" , V="공급사"
	
	public int getUserUID() {
		return userUID;
	}
	public void setUserUID(int userUID) {
		this.userUID = userUID;
	}
	public String getLoginID() {
		return loginID;
	}
	public void setLoginID(String loginID) {
		this.loginID = loginID;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getUserPW() {
		return userPW;
	}
	public void setUserPW(String userPW) {
		this.userPW = userPW;
	}
	public String getUserNM() {
		return userNM;
	}
	public void setUserNM(String userNM) {
		this.userNM = userNM;
	}
	public String getUserNMEng() {
		return userNMEng;
	}
	public void setUserNMEng(String userNMEng) {
		this.userNMEng = userNMEng;
	}
	public String getEmpNO() {
		return empNO;
	}
	public void setEmpNO(String empNO) {
		this.empNO = empNO;
	}
	public String getRegionCD() {
		return regionCD;
	}
	public void setRegionCD(String regionCD) {
		this.regionCD = regionCD;
	}
	public String getRegionNM() {
		return regionNM;
	}
	public void setRegionNM(String regionNM) {
		this.regionNM = regionNM;
	}
	public String getZipCD() {
		return zipCD;
	}
	public void setZipCD(String zipCD) {
		this.zipCD = zipCD;
	}
	public String getTelNO() {
		return telNO;
	}
	public void setTelNO(String telNO) {
		this.telNO = telNO;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public int getIsActive() {
		return isActive;
	}
	public void setIsActive(int isActive) {
		this.isActive = isActive;
	}
	public String getRoleID() {
		return roleID;
	}
	public void setRoleID(String roleID) {
		this.roleID = roleID;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	public String getFaxNO() {
		return faxNO;
	}
	public void setFaxNO(String faxNO) {
		this.faxNO = faxNO;
	}
	public int getTotAdminYn() {
		return totAdminYn;
	}
	public void setTotAdminYn(int totAdminYn) {
		this.totAdminYn = totAdminYn;
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
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
	public String getObuID() {
		return obuID;
	}
	public void setObuID(String obuID) {
		this.obuID = obuID;
	}
	public String getObuNM() {
		return obuNM;
	}
	public void setObuNM(String obuNM) {
		this.obuNM = obuNM;
	}
	public int getBorgUID() {
		return borgUID;
	}
	public void setBorgUID(int borgUID) {
		this.borgUID = borgUID;
	}
	public String getBorgID() {
		return borgID;
	}
	public void setBorgID(String borgID) {
		this.borgID = borgID;
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
	public String getUserActFile() {
		return userActFile;
	}
	public void setUserActFile(String userActFile) {
		this.userActFile = userActFile;
	}
	public String getDbsts() {
		return dbsts;
	}
	public void setDbsts(String dbsts) {
		this.dbsts = dbsts;
	}
	public String getUserIP() {
		return userIP;
	}
	public void setUserIP(String userIP) {
		this.userIP = userIP;
	}
	public String getCsKey() {
		return csKey;
	}
	public void setCsKey(String csKey) {
		this.csKey = csKey;
	}
	public String getErrCode() {
		return errCode;
	}
	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}
	public String getErrMsg() {
		return errMsg;
	}
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
	public String getpID() {
		return pID;
	}
	public void setpID(String pID) {
		this.pID = pID;
	}
	public int getGridColor() {
		return gridColor;
	}
	public void setGridColor(int gridColor) {
		this.gridColor = gridColor;
	}
	public String getUserNMCN() {
		return userNMCN;
	}
	public void setUserNMCN(String userNMCN) {
		this.userNMCN = userNMCN;
	}
	public String getCustNMEng() {
		return custNMEng;
	}
	public void setCustNMEng(String custNMEng) {
		this.custNMEng = custNMEng;
	}
	public String getCustNMCN() {
		return custNMCN;
	}
	public void setCustNMCN(String custNMCN) {
		this.custNMCN = custNMCN;
	}
	//public String getUserGB() {
	//	return userGB;
	//}
	//public void setUserGB(String userGB) {
	//	this.userGB = userGB;
	//}
}

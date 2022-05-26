package com.sci4s.msa.tsys.dto;

public class BaseDictionaryDto {

	private String UUID;           //유일ID
	private String srcCD;          //소스코드
	private String objCD;          //객체코드
	private String objType;        //객체타입
	private String objName;        //객체명
	private String nullAble;       //널허용여부(1:허용, 0:미허용)
	private String dataDefault;    //데이터기본값
	private String dataDefaultOrcl;//오라클데이터기본값
	private String objComment;     //커맨트
	private String dataType;       //데이터타입
	private String dataSize;       //데이터사이즈(기본값=20)
	private String dataPrecision;  //데이터정밀도
	
	public String getUUID() {
		return UUID;
	}
	public void setUUID(String uUID) {
		UUID = uUID;
	}
	public String getSrcCD() {
		return srcCD;
	}
	public void setSrcCD(String srcCD) {
		this.srcCD = srcCD;
	}
	public String getObjCD() {
		return objCD;
	}
	public void setObjCD(String objCD) {
		this.objCD = objCD;
	}
	public String getObjType() {
		return objType;
	}
	public void setObjType(String objType) {
		this.objType = objType;
	}
	public String getObjName() {
		return objName;
	}
	public void setObjName(String objName) {
		this.objName = objName;
	}
	public String getNullAble() {
		return nullAble;
	}
	public void setNullAble(String nullAble) {
		this.nullAble = nullAble;
	}
	public String getDataDefault() {
		return dataDefault;
	}
	public void setDataDefault(String dataDefault) {
		this.dataDefault = dataDefault;
	}
	public String getDataDefaultOrcl() {
		return dataDefaultOrcl;
	}
	public void setDataDefaultOrcl(String dataDefaultOrcl) {
		this.dataDefaultOrcl = dataDefaultOrcl;
	}
	public String getObjComment() {
		return objComment;
	}
	public void setObjComment(String objComment) {
		this.objComment = objComment;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getDataSize() {
		return dataSize;
	}
	public void setDataSize(String dataSize) {
		this.dataSize = dataSize;
	}
	public String getDataPrecision() {
		return dataPrecision;
	}
	public void setDataPrecision(String dataPrecision) {
		this.dataPrecision = dataPrecision;
	}
}


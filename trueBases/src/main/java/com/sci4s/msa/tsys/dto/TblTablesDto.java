package com.sci4s.msa.tsys.dto;

public class TblTablesDto {

	private String tblName;       //테이블명
	private String tblComment;    //테이블코멘트
	private String colName;       //컬럼명
	private String colComment;    //컬럼코멘트
	private String dataType;      //데이터유형
	private int dataSize;         //데이터길이
	private int dataPrecision;    //데이터정밀도
	private String dataDefault;   //데이터기본값
	private int nullAble;         //널허용여부(1:허용,0:허용않함)
	private String oldColName;    //기존컬럼명
	
	public String getTblName() {
		return tblName;
	}
	public void setTblName(String tblName) {
		this.tblName = tblName;
	}
	public String getTblComment() {
		return tblComment;
	}
	public void setTblComment(String tblComment) {
		this.tblComment = tblComment;
	}
	public String getColName() {
		return colName;
	}
	public void setColName(String colName) {
		this.colName = colName;
	}
	public String getColComment() {
		return colComment;
	}
	public void setColComment(String colComment) {
		this.colComment = colComment;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public int getDataSize() {
		return dataSize;
	}
	public void setDataSize(int dataSize) {
		this.dataSize = dataSize;
	}
	public int getDataPrecision() {
		return dataPrecision;
	}
	public void setDataPrecision(int dataPrecision) {
		this.dataPrecision = dataPrecision;
	}
	public String getDataDefault() {
		return dataDefault;
	}
	public void setDataDefault(String dataDefault) {
		this.dataDefault = dataDefault;
	}
	public int getNullAble() {
		return nullAble;
	}
	public void setNullAble(int nullAble) {
		this.nullAble = nullAble;
	}
	public String getOldColName() {
		return oldColName;
	}
	public void setOldColName(String oldColName) {
		this.oldColName = oldColName;
	}
}

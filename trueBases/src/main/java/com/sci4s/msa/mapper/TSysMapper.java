package com.sci4s.msa.mapper;

import java.util.List;
import java.util.Map;

public interface TSysMapper {
	public List<Map<String,Object>> getTblServiceDtl(Map<String, String> params) throws Exception;
	
	public int delTblCaseDoBystepUID(Map<String, Object> params) throws Exception;
	public int updTblAttach4DocNO(Map<String, String> params) throws Exception;	
}

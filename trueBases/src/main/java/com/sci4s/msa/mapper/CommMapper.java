package com.sci4s.msa.mapper;

import java.util.List;
import java.util.Map;

public interface CommMapper {
	public List<Map<String,Object>> getMstInfoList(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> getSyncMstData(String query) throws Exception;
	public int insInfInfoHist(Map<String,Object> param) throws Exception;
	
	public int updMstInfoList(Map<String,Object> param) throws Exception;
	
}

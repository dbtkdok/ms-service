package com.sci4s.msa.mapper;

import java.util.List;
import java.util.Map;

public interface TO2oUserMapper {

	public Map<String,Object> getLogin4UserID(Map<String,Object> params) throws Exception;
	public List<Map<String,Object>> getConfig4Channel(Map<String,Object> params) throws Exception;
	public List<Map<String,Object>> getBizTimes4Channel(Map<String,Object> params) throws Exception;
	public String getTotalShopInfo(Map<String,Object> params) throws Exception;
}

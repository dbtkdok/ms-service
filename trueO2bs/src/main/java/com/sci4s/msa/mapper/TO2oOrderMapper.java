package com.sci4s.msa.mapper;

import java.util.List;
import java.util.Map;

public interface TO2oOrderMapper {
	
	public String getUUID(String uuid) throws Exception;
	
	public Map<String,Object> getOrderInfo(Map<String,Object> params) throws Exception;
	public List<Object> getOrderItems(Map<String,Object> params) throws Exception;
	public List<Object> getOrderOptions(Map<String,Object> params) throws Exception;
	public String getLocalGrpcIP(Map<String,Object> params) throws Exception;
	
}

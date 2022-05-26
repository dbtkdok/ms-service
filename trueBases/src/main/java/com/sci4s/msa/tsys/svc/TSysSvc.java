package com.sci4s.msa.tsys.svc;

import java.util.Map;

public interface TSysSvc {
	public String getServiceXml(Map<String,String> paramMap) throws Exception;
	public String updTblAttach4DocNO(Map<String, String> params) throws Exception;	
}

package com.sci4s.msa.mapper;

import java.util.List;
import java.util.Map;

import com.sci4s.grpc.batch.TblRolescopes;
import com.sci4s.grpc.batch.TblScopes;

public interface BorgMapper {
	public List<Map<String,Object>> getUserCustAll4Combo(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> getBorgsPopList(Map<String,Object> param) throws Exception;
	public int existsTblRolescopes(TblRolescopes param) throws Exception;
	public int insTblRolescopes(TblRolescopes param) throws Exception;
	public int updTblRolescopes(TblRolescopes param) throws Exception;
	public int existsTblScopes(TblScopes param) throws Exception;
	public int insTblScopes(TblScopes param) throws Exception;
	public int updTblScopes(TblScopes param) throws Exception;
}

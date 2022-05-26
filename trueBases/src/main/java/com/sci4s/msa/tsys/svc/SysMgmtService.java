package com.sci4s.msa.tsys.svc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sci4s.grpc.ErrConstance;
import com.sci4s.grpc.SciRIO;
import com.sci4s.grpc.SciRIO.RetMsg;
import com.sci4s.grpc.dao.DataDao;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.utils.GrpcDataUtil;
import com.sci4s.msa.tsys.dto.UserPrivDto;

@Service
public class SysMgmtService {
	@Value("${db.dbtype}")
	String SQLMODE;
	
	Logger logger = LoggerFactory.getLogger(SysMgmtService.class);	
	
	private DataDao dataDao;
	public SysMgmtService(DataDao dataDao) {
		this.dataDao = dataDao;
	}
	
	/**
	 * 로그인 사용자의 화면 권한 및 공통 코드 데이터를 조회하는 서비스 메서드
	 * 
	 * @param  Map<String,String> paramMap
	 * @return SciRIO.RetMsg
	 */
	public GrpcResp getEnableJob(Map<String, Object> paramMap) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		GrpcResp grpcResp = new GrpcResp();
		StringBuilder retSb = new StringBuilder();
		
		Map<String,Object> sqlMap = new HashMap<String,Object>();
		try {
			
			String privCDs  = (String)(paramMap.get("privIDs")==null?"":paramMap.get("privIDs"));
			String codeGrps = (String) paramMap.get("codeTypes");			
			
			logger.info("privIDs   ::: "+ privCDs +"//"+ privCDs.indexOf("|"));
			// 쿼리 공통 부분 저장(agentID, userUID, borgUID, menuID)
			sqlMap.put("userUID",  paramMap.get("userUID"));
			sqlMap.put("borgUID",  paramMap.get("borgUID"));
			sqlMap.put("agentID",  paramMap.get("agentID"));
			sqlMap.put("userIP",   paramMap.get("userIP"));
			sqlMap.put("serverIP", paramMap.get("serverIP"));
			sqlMap.put("pMenuID",  paramMap.get("pMenuID"));
			sqlMap.put("dbSTS",    paramMap.get("dbSTS"));
			if (!paramMap.containsKey("clang")) {
				sqlMap.put("clang", "KR");
			} else {
				sqlMap.put("clang", paramMap.get("clang"));
			}
			
			logger.info("privIDs   ::: "+ privCDs +"//"+ privCDs.indexOf("|"));
			logger.info("codeTypes ::: "+ codeGrps);

			if (privCDs != null && privCDs.length() > 0) {
				
				String[] privIDs = null;
				if (privCDs.indexOf("|") >= 0) {
					privIDs = privCDs.split("[|]");
				} else {
					privIDs = new String[]{ privCDs };
				}
				logger.info("privIDs ::: "+ privIDs);
				sqlMap.put("privIDs", privIDs);
				
				List<UserPrivDto> privList = (List<UserPrivDto>)dataDao.query4List4("getEnableJob", sqlMap);  
				retSb.append("{\"results\":[");
				if (privList == null || privList.size() == 0) {
					retSb.append("{\"privIDs\":[{\"activeImg\":\"fa fa-search\",\"privID\":\"COMM_READ\"}]},{\"privNMs\":\"공통 조회\"}");
		        } else {            
		        	List<Map<String,Object>> privIDList = new ArrayList<Map<String,Object>>();
		        	String privNMstr = "";
		        	for (int ii=0; ii<privList.size(); ii++) {
		        		UserPrivDto privDto = privList.get(ii);		        		
		        		Map<String,Object> privIDmap = new HashMap<String,Object>();
		        		privIDmap.put("privID", ""+ privDto.getPrivID());
		        		privIDmap.put("activeImg", ""+ privDto.getActiveImg());
		        		if (ii == 0) {
		        			privNMstr  = privDto.getPrivNM();
		        		} else {
		        			privNMstr += "|"+ privDto.getPrivNM();
		        		}
		        		privIDList.add(privIDmap);
		        	}
		        	
					retSb.append("{\"privIDs\":"+ GrpcDataUtil.getJsonStringFromList(privIDList) +"}");					
					retSb.append(",{\"privNMs\":\""+ privNMstr +"\"}");
					logger.info("retSb.toString() >>>>>>>>>>>>>>>> "+ retSb.toString());
		        }
				
				sqlMap.remove("privIDs");
				sqlMap.put("pID", paramMap.get("pID"));
				List<Map<String,Object>> searchList = (List<Map<String,Object>>)dataDao.query4List4("getSearchBoxList", sqlMap);
				if (searchList != null || searchList.size() > 0) {
					retSb.append(",{\"searchBox\":"+ GrpcDataUtil.getJsonStringFromList(searchList) +"}");	
		        }
			}else {
				retSb.append("{\"results\":[");
			}
			if (codeGrps != null) {
				String[] codeTypes = null;
				if (codeGrps.indexOf("|") >= 0) {
					codeTypes = codeGrps.split("[|]");
				} else {
					codeTypes = new String[]{ codeGrps };
				}
				for (String codeType : codeTypes) {						
					sqlMap.put("codeType", codeType);
					List<Map<String, Object>> codeList = dataDao.query4List1("getCodesByTypeCode", sqlMap);	
					if (codeList != null && codeList.size() > 0) {
						jsonRet = ",{\""+ codeType +"\":"+ GrpcDataUtil.getJsonStringFromList(codeList) +"}";
						retSb.append(jsonRet);
					}
				}
			}
			retSb.append("]}");
			
			logger.debug("results:::::::::::::::"+ retSb.toString());
			
		} catch(Exception ex) {
			ex.printStackTrace();
			errCode = "ERR_9999";
			errMsg  = ex.toString();
			jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, errMsg, null);
        }

		grpcResp.setErrCode(errCode);
		grpcResp.setErrMsg(errMsg);
		grpcResp.setResults(retSb.toString());
        
        return grpcResp;
	}
	
	public GrpcResp getFileData(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		GrpcResp grpcResp = new GrpcResp();
		Map<String,Object> paramMap = null;
		
		try {
			paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
			jsonRet = "{\"results\":[";
			
			if(paramMap.get("menu") != null) {
				List<Map<String, Object>> menuTopList = dataDao.query4List1("getTblTopMenuList", paramMap);	
				List<Map<String, Object>> menuLeftList = dataDao.query4List1("getTblLeftMenuList", paramMap);
				
				if (menuTopList != null && menuTopList.size() > 0) {
					jsonRet += "{\"menuTop\":"+ GrpcDataUtil.getJsonStringFromList(menuTopList) +"}";
				}
				if (menuLeftList != null && menuLeftList.size() > 0) {
					jsonRet += ",{\"menuLeft\":"+ GrpcDataUtil.getJsonStringFromList(menuLeftList) +"}";
				}
			}
			
			if(paramMap.get("role") != null) {
				List<Map<String, Object>> roleList = dataDao.query4List1("getEnableJobCache", paramMap);
				
				if (roleList != null && roleList.size() > 0) {
					if(paramMap.get("menu") != null) {
						jsonRet += ",";
					}
					jsonRet += "{\"roleList\":"+ GrpcDataUtil.getJsonStringFromList(roleList) +"}";
				}
			}
			
			if(paramMap.get("dict") != null) {
				List<Map<String, Object>> dictList = dataDao.query4List1("getTblDicList", paramMap);
				
				if (dictList != null && dictList.size() > 0) {
					if(paramMap.get("menu") != null || paramMap.get("role") != null) {
						jsonRet += ",";
					}
					jsonRet += "{\"dictList\":"+ GrpcDataUtil.getJsonStringFromList(dictList) +"}";
				}
			}
			
			jsonRet += "]}";
			
		} catch(Exception ex) {
			ex.printStackTrace();
			errCode = "ERR_9999";
			errMsg  = ex.toString();
			jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, errMsg, null);
        }

		grpcResp.setErrCode(errCode);
		grpcResp.setErrMsg(errMsg);
		grpcResp.setResults(jsonRet);
        
        return grpcResp;
	}
	
	/**
	 * 메뉴 상세 정보를 조회하는 서비스 메서드
	 * 
	 * @param  Map<String,String> paramMap
	 * @return SciRIO.RetMsg
	 */
	public GrpcResp getMenuDtlInfo(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		StringBuilder retSb = new StringBuilder();
		GrpcResp grpcResp = new GrpcResp();
		
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		
		Map<String,Object> sqlMap = new HashMap<String,Object>();
		try {	
			logger.debug("paramMap:::::::::::::::"+ paramMap);
	
			Map<String, Object> menuInfo = (Map<String, Object>)dataDao.query4Object1("getTblMenuList", paramMap);           
	        if (menuInfo == null) {
	        	errCode = "0";
	    		errMsg  = ErrConstance.NO_DATA;
	    		jsonRet = GrpcDataUtil.getGrpcResults(errCode, errMsg, null);
	        } else {            
	        	retSb.append("{\"results\":[");
	        	//retSb.append("{\"menuInfo\":"+ GrpcDataUtil.getJsonStringFromMap(menuInfo).toJSONString() + "}");
	        	
	        	int menuLVL   = Integer.parseInt(""+ menuInfo.get("menuLVL"));     	
	        	int topMenuID = Integer.parseInt(menuInfo.get("topMenuID") == null?"0":""+ menuInfo.get("topMenuID"));
	        	int parMenuID = Integer.parseInt(menuInfo.get("parMenuID") == null?"0":""+ menuInfo.get("parMenuID"));
	        	int pprMenuID = Integer.parseInt(menuInfo.get("pprMenuID") == null?"0":""+ menuInfo.get("pprMenuID"));
	        	String svcTypeID = ""+ menuInfo.get("svcTypeID");
	
	        	// 출력순서 조회
	        	sqlMap.putAll(paramMap);
	        	sqlMap.put("svcTypeID", svcTypeID);
	        	sqlMap.put("menuLVL", menuLVL);
	        	if (parMenuID > 0) {
	        		sqlMap.put("parMenuID", parMenuID);
	        	}
	        	List<Map<String,Object>> sortORD = (List<Map<String,Object>>)dataDao.query4List1("getMenuSortORD", sqlMap); 
	        	
	        	if (sortORD != null && sortORD.size() > 0) { // 출력순서만 전송 -> sortORD
	        		retSb.append("{\"sortORD\":"+ GrpcDataUtil.getJsonStringFromList(sortORD) +"}");
	        	}
	        	// 상위메뉴리스트 조회
	        	boolean isTop = false;
	        	boolean isPpr = false;
	        	boolean isPar = false;
	        	if (menuLVL == 0) {// 없음.
	        		
	        	} else if (menuLVL == 1) {// 대메뉴리스트 조회(menuLVL0)
	        		isTop = true;
	        	} else if (menuLVL == 2) {// 대-중메뉴리스트 조회(menuLVL0-menuLVL1)
	        		isTop = true;
	        		isPpr = true;
	        	} else if (menuLVL == 3) {// 대-중-소메뉴리스트 조회(menuLVL0-menuLVL1-menuLVL2)
	        		isTop = true;
	        		isPpr = true;
	        		isPar = true;
	        	}
	        	
	        	if (isTop) {
	        		sqlMap.remove("parMenuID");
	        		sqlMap.put("menuLVL", 0);
	        		List<Map<String,Object>> menuLVL0 = (List<Map<String,Object>>)dataDao.query4List1("getMenuByLevel", sqlMap);
	        		if (menuLVL0 != null && menuLVL0.size() > 0) { // 출력순서만 전송 -> sortORD
		        		retSb.append(",{\"menuLVL0\":"+ GrpcDataUtil.getJsonStringFromList(menuLVL0) +"}");
		        	}
	        	} 
	        	if (isPpr) {
	        		sqlMap.put("menuLVL", 1);
	        		if (topMenuID > 0) {
		        		sqlMap.put("parMenuID", topMenuID);
		        	}	        		
	        		List<Map<String,Object>> menuLVL1 = (List<Map<String,Object>>)dataDao.query4List1("getMenuByLevel", sqlMap);
	        		if (menuLVL1 != null && menuLVL1.size() > 0) { // 출력순서만 전송 -> sortORD
		        		retSb.append(",{\"menuLVL1\":"+ GrpcDataUtil.getJsonStringFromList(menuLVL1) +"}");
		        	}
	        	}	        	
	        	if (isPar) {
	        		sqlMap.put("menuLVL", 2);
	        		if (pprMenuID > 0) {
		        		sqlMap.put("parMenuID", pprMenuID);
		        	}	        		
	        		List<Map<String,Object>> menuLVL2 = (List<Map<String,Object>>)dataDao.query4List1("getMenuByLevel", sqlMap);
	        		if (menuLVL2 != null && menuLVL2.size() > 0) { // 출력순서만 전송 -> sortORD
		        		retSb.append(",{\"menuLVL2\":"+ GrpcDataUtil.getJsonStringFromList(menuLVL2) +"}");
		        	}
	        	}
	        	retSb.append("]}");
	        }
	        jsonRet = retSb.toString();
		} catch(Exception ex) {
			ex.printStackTrace();
			errCode = "ERR_9999";
			errMsg  = ex.toString();
			jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, errMsg, null);
	    }
	
		grpcResp.setErrCode(errCode);
		grpcResp.setErrMsg(errMsg);
		grpcResp.setResults(jsonRet);
	    
	    return grpcResp;
	}
	
	
	/**
	 * 로그인 사용자 최상위 메뉴를 조회하는 서비스 메서드
	 * 
	 * @param  GrpcParams grpcPrms
	 * @return SciRIO.RetMsg
	 */
	public SciRIO.RetMsg getTopMenuList(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = ErrConstance.NO_ERROR;
		String   jsonRet  = "";		
		Map<String, Object> paramMap = null;		
		try {
			paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());	
			logger.debug("paramMap:::::::::::::::"+ paramMap);
			
	        // Test Parameters : agentID = 13, memberID = 1, borgID = 15, userType = 'SYS'
			List<Map<String, Object>> menuList = dataDao.query4List1("getTopMenuList", paramMap);           
	        
	        if (menuList == null || menuList.size() == 0) {
	        	errCode = "NO_DATA";
	    		errMsg  = "조회된 데이터가 없습니다.";
	    		jsonRet = GrpcDataUtil.getGrpcResults("0", ErrConstance.NO_DATA, null);
	        } else {            
	        	jsonRet = "{\"results\":"+ GrpcDataUtil.getJsonStringFromList(menuList) + "}";
	        }
	        //logger.debug("response.errCode() ::: "+ errCode);
		} catch(Exception ex) {
			ex.printStackTrace();
			errCode = "ERR_9999";
			errMsg  = ex.toString();
			jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, errMsg, null);
        }

		// Data 처리 후 Json 형식으로 변환하여 리턴하면 됨.
        SciRIO.RetMsg response = SciRIO.RetMsg.newBuilder()
			.setResults(jsonRet)
			.setErrCode(errCode)
			.setErrMsg(errMsg)
			.build();
        
        return response;
	}
	
	
	/**
	 * 화면권한 정보를 생성/수정/삭제 처리하는 서비스 메서드
	 * 
	 * @param  GrpcParams grpcPrms
	 * @return SciRIO.RetMsg
	 */
	@Transactional
	public RetMsg procActPrivileges(GrpcParams grpcPrms) throws Exception {
		String retText = GrpcDataUtil.getGrpcResults("0", ErrConstance.NO_ERROR, null);
		String errCode = "0";
		String errMsg  = "";	
		
		Map<String,Object> retMap = new HashMap<String,Object>();			
		//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		// 2. GRPC.Data를 파싱하여 paramMap으로 설정한다.
		//params = 
		//{SERVERIP=192.168.219.6				
		//, ACTIMAGE=[/images/bt182.gif, -, /images/bt32.gif, /images/bt012.gif, /images/bt139.gif]
		//, NOACTIMAGE=[-, -, -, , ]
		//, PRIVILEGEID=[80, 51, 24, 100, 60]
		//, PRIVILEGECD=[AUC_ADM001, BG_ACCGENLIST, COMM_LIST, CR_AD003, AUC_VEN003]
		//, ISREAD=[1, 1, 1, 0, 1]
		//, DBSTS=[A, A, A, A, A]
		//, PRIVILEGENM=[계산서출력, 계정조회, 공통 목록, 등록, 마일리지조회]
		//, USERIP=192.168.219.6
		//, AGENTID=13
		//, MEMBERID=1
		//} => Map<String,List<String>> or Map<String,Object>으로 변환되어 리턴됨.		
		System.out.println("procActPrivileges@@@@@@@@@@@@@@@@@@");				
		System.out.println("procActPrivileges@@@@@@@@@@@@@@@@@@");
		System.out.println(grpcPrms.getData());
		System.out.println("procActPrivileges@@@@@@@@@@@@@@@@@@");
		System.out.println("procActPrivileges@@@@@@@@@@@@@@@@@@");
		
		Map<String, Object> paramMap = null;
		try {
			paramMap = GrpcDataUtil.getParams("params", grpcPrms.getData(), true);
		} catch (Exception ee) {
			ee.printStackTrace();
			throw ee;
		}
		List<Map<String,Object>> reqData  = (List<Map<String,Object>>)paramMap.get("PARAMS");
		List<String> reqKeys = (List<String>)paramMap.get("KEYS");

		if (reqData != null && reqData.size() > 0) {

			for (int kk=0; kk<reqData.size(); kk++){
				boolean isDel = false;			
				Map<String,Object>  dataMap = reqData.get(kk);	
				Map<String,Object> sqlParam = null;						
				String dtSTS = "";
				List<Map<String,Object>> paramList = new ArrayList<Map<String,Object>>();
				if (dataMap.get("privID") instanceof List) {
					List<String> privIDs = (List<String>)dataMap.get("privID");
					for (int ll=0; ll<privIDs.size(); ll++) {
						isDel = false;
						
						sqlParam = new HashMap<String, Object>();
						dtSTS = GrpcDataUtil.getVal4MapList(dataMap,"dbSTS", ll);
						if ("D".equals(dtSTS)) {
							isDel = true;
						}
						sqlParam.put("privID",      privIDs.get(ll));
						sqlParam.put("privUID",     GrpcDataUtil.getVal4MapList(dataMap,"privUID", ll));
						sqlParam.put("privNM",      GrpcDataUtil.getVal4MapList(dataMap,"privNM", ll));
						sqlParam.put("activeImg",   GrpcDataUtil.getVal4MapList(dataMap,"activeImg", ll));
						sqlParam.put("noActiveImg", GrpcDataUtil.getVal4MapList(dataMap,"noActiveImg", ll));
						sqlParam.put("isRead",      GrpcDataUtil.getVal4MapList(dataMap,"isRead", ll));
						sqlParam.put("dtSTS",       dtSTS);
						sqlParam.put("agentID",     grpcPrms.getAgentID());
						sqlParam.put("userIP",      grpcPrms.getUserIP());
						sqlParam.put("serverIP",    grpcPrms.getServerIP());
						sqlParam.put("userUID",     grpcPrms.getUserUID());
						sqlParam.put("borgUID",     grpcPrms.getBorgUID());						
						
						int cnt = (Integer)dataDao.query4Object1("chkTblActivities", sqlParam);
						logger.debug(sqlParam.get("privID") +":::::::::::::::"+ cnt);
						if (cnt == 0) {
							sqlParam.put("sqlMode", "0");	
						} else {
							sqlParam.put("sqlMode", "1");	
						}						
						paramList.add(sqlParam);
					}						
				} else {
					sqlParam = new HashMap<String, Object>();
					sqlParam.putAll(dataMap);
					dtSTS = (sqlParam.get("dbSTS")==null?"":""+sqlParam.get("dbSTS"));
					if ("D".equals(dtSTS)) {
						isDel = true;
					}	
					sqlParam.put("agentID",  grpcPrms.getAgentID());
					sqlParam.put("userIP",   grpcPrms.getUserIP());
					sqlParam.put("serverIP", grpcPrms.getServerIP());
					sqlParam.put("userUID",  grpcPrms.getUserUID());//BORGEID
					
					int cnt = (Integer)dataDao.query4Object1("chkTblActivities", sqlParam);
					logger.debug(sqlParam.get("privCD") +":::::::::::::::"+ cnt);
					if (cnt == 0) {
						sqlParam.put("sqlMode", "0");	
					} else {
						sqlParam.put("sqlMode", "1");	
					}	
					paramList.add(sqlParam);
				}		
				logger.debug("paramList:::::::::::::::"+ paramList);
				if (Integer.parseInt("0"+ sqlParam.get("sqlMode")) == 0) {
					dataDao.query4Update3("insTblActivities", paramList);
				} else {
					dataDao.query4Update3("updTblActivities", paramList);					
					//if (isDel) {
						// 삭제의 경우에 사용자 메뉴 테이블에 해당 화면권한에 해당하는 
						// 메뉴에 정보를 수정해야 함.
						// 예) ebdusermenus.dbsts = 'D'로 업데이트해야 함. 
						// dataDao.query4Update3("updEbdUserMenus2Dbsts", paramList);
					//}
				}				
			}	
		}
		//logger.debug("retText ::: "+ retText);
		//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		// 7. 업무 처리 후, Json 형식으로 결과를 리턴한다.
        SciRIO.RetMsg response = SciRIO.RetMsg.newBuilder()
    			.setResults(retText)
    			.setErrCode(errCode)
    			.setErrMsg(errMsg)
    			.build();
        
        return response;
	}
	
	
	/**
	 * 스케줄러로 화면권한 확인 했는지 확인하는 서비스
	 * 
	 * @param  Map<String,String> paramMap
	 * @return SciRIO.RetMsg
	 */
	public GrpcResp getRoleMap(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
//		StringBuilder retSb = new StringBuilder();
		GrpcResp grpcResp = new GrpcResp();
		
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
		try {	
//			System.out.println("SQLMODE    "+SQLMODE);
			paramMap.put("SQLMODE", SQLMODE);
			
//			logger.debug("paramMap:::::::::::::::"+ paramMap);
			retList = dataDao.query4Object0("chkUserRoleMap", paramMap);
	        if (retList.size() <= 0) {
	        	errCode = "NO_DATA";
	    		errMsg  = "조회된 데이터가 없습니다.";
	    		jsonRet = GrpcDataUtil.getGrpcResults("0", ErrConstance.NO_DATA, null);
	    		
	        } else {
	        	//retSb.append("{\"menuInfo\":"+ GrpcDataUtil.getJsonStringFromMap(menuInfo).toJSONString() + "}");
	        	if(retList.size() > 0) {
	        		dataDao.query4Update3("updUserRoleMap", retList);
	        	}
	        }
	        jsonRet = "{\"results\":"+ retList.size() + "}";
	        
		} catch(Exception ex) {
			ex.printStackTrace();
			errCode = "ERR_9999";
			errMsg  = ex.toString();
			jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, errMsg, null);
	    }
	
		grpcResp.setErrCode(errCode);
		grpcResp.setErrMsg(errMsg);
		grpcResp.setResults(jsonRet);
	    
	    return grpcResp;
	}
}

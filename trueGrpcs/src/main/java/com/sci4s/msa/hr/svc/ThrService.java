package com.sci4s.msa.hr.svc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sci4s.grpc.ErrConstance;
import com.sci4s.grpc.batch.TblRolescopes;
import com.sci4s.grpc.batch.TblScopes;
import com.sci4s.grpc.dao.DataDao;
import com.sci4s.grpc.dao.IDataDao;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.svc.CommService;
import com.sci4s.grpc.svc.CommonService;
import com.sci4s.grpc.svc.DataService;
import com.sci4s.grpc.utils.GrpcDataUtil;
import com.sci4s.msa.mapper.BorgMapper;
import com.sci4s.utils.AES256Util;

@Service
public class ThrService {
	Logger logger = LoggerFactory.getLogger(ThrService.class);

	@Value("${db.dbtype}")
	String SQLMODE;
	
	@Value("${buffer.type}")
	String BUFFER_TYPE;
	
	@Value("${default.lang}")
	String CLANG;
	
	@Value("${msa.pids.uri}")
	String TSYS_URI;
	
	DataDao dataDao;
	BorgMapper borgMapper;
	
	
	@Autowired
	CommService commService; 
	
	@Autowired
	public  ThrService(BorgMapper borgMapper, DataDao dataDao){
	    this.borgMapper = borgMapper;
	    this.dataDao = dataDao;
	}
	
	@Transactional
	public void saveTblRolescopesAll(List<TblRolescopes> rsList) throws Exception {		
		logger.debug("saveTblRolescopesAll run");
		for (TblRolescopes rsInfo :rsList) {	
			rsInfo.setSQLMODE(this.SQLMODE);
			logger.debug("saveTblRolescopesAll.rsInfo ::: "+ rsInfo.getRoleUID());
			if (borgMapper.existsTblRolescopes(rsInfo) == 0) {
				borgMapper.insTblRolescopes(rsInfo);
			} else {
				borgMapper.updTblRolescopes(rsInfo); 
			}
		}
	}
	
	@Transactional
	public void saveTblScopesAll(List<TblScopes> rsList) throws Exception {		
		logger.debug("saveTblScopesAll run");
		for (TblScopes rsInfo :rsList) {	
			rsInfo.setSQLMODE(this.SQLMODE);
			logger.debug("saveTblScopesAll.rsInfo ::: "+ rsInfo.getScopeUID());
			if (borgMapper.existsTblScopes(rsInfo) == 0) {
				borgMapper.insTblScopes(rsInfo);
			} else {
				borgMapper.updTblScopes(rsInfo); 
			}
		}
	}
	
	/**
	 * ???????????? > ??????????????? > ??????????????? > ??????/??????
	 * @param grpcPrms
	 * @return GrpcResp
	 * @throws Exception
	 */
	@Transactional(rollbackFor = {Exception.class})
	public GrpcResp saveUserInfo(GrpcParams grpcPrms) throws Exception {
		/**
		 * 1. pid: THR0013 (insUserInfoDtl) / THR0014 (updUserInfo)
      	   1-1.???????????? ?????? ???????????? ?????????
    	   1-2.????????? ?????? ?????? ??? ??????
		   1-3.subPId ????????? ?????????????????? ????????? ?????? ?????? ?????? 
		 * **/
		String errCode  = "0";
		String errMsg   = "SUCCESS";
		String jsonData = null;
		AES256Util aes256 = new AES256Util();
		
		GrpcResp grpcResp = new GrpcResp();	
		String sqlID = "";
		String pid = grpcPrms.getpID();
		logger.info("@!!@!@!@!@!@! THR getData @!!@!@!@!@!@!"+grpcPrms.getData());
		
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		String clangT = grpcPrms.getClang();
		if(clangT == null) {
			paramMap.put("clang", this.CLANG);
		}
		/**
		 * 1.???????????? ?????? ???????????? ?????????
		 *  - BCryptPasswordEncoder encode ???????????? ???????????? ????????? ????????? ???
		 *    login??? bcryptPasswordEncoder.matches()??? ?????? ?????? ??????
		 * */
		String pwd = "";
		if(paramMap.get("pwd") != null) {
			pwd = ""+paramMap.get("pwd");
		    String encKey = aes256.encryptII(pwd);
			paramMap.put("pwd", encKey);
		}
		
		
		/**
		 * 2.????????? ???????????? [THR0013=??????, THR0014=??????]
		 * */
		paramMap.put("SQLMODE", this.SQLMODE);
		paramMap.put("userUID", grpcPrms.getUserUID());
		paramMap.put("agentID", grpcPrms.getAgentID());

		if("THR0013".equals(pid)) {
			//??????????????? ??????
			sqlID = "insUserInfoDtl";
		}else {
			//??????????????? ??????
			sqlID= "updUserInfo";
		}
		
		try {
			int results = dataDao.query4Update1(sqlID, paramMap);
			logger.info("==========ssss  query4Update1 return ========    :::: "+results);
		} catch (Exception ex) {
			logger.info("Exception ============ "+ex);
			errCode = "THR0013 saveUserInfo query4Update1 ERROR";
			errMsg = ex.toString();
		}
		
		/**
		 * 3.????????? ?????? ?????? ????????? ?????? ??? (subPid:THR0013_01)
		 * ???(trueApp)?????? ????????? ?????? ???????????? 
		 * ????????? ?????? ????????? ??????
		 * */
		String subPid =  ""+paramMap.get("subPID");
		String userUID = (String) dataDao.query4Object1("selUserUID", paramMap);
		
		
		String[] subPids = null;
		if (subPid.indexOf("|") >= 0) { // ?????? ???(SYS0026_01|SYS0026_02 ), (SYS0044_01|SYS0044_02|SYS0044_03|SYS0044_04)
			subPids = subPid.split("\\|");
		} else { // ?????? //SYS0026_01
			subPids = new String[] {subPid};
		}
		
		HashMap<String, Object> jsonMap2 = new HashMap<String, Object>();
		jsonMap2.put("loginID", ""+paramMap.get("loginID"));
		jsonMap2.put("puserUID", userUID);
		
		dataDao.query4Update1("delTblCustUsers", jsonMap2);
		
//		if("THR0013_01".equals(subPid)) {
		for (int ii=0; ii<subPids.length; ii++) {
			String subid = subPids[ii];
		    if("THR0013_01".equals(subid) ||"THR0014_01".equals(subid)) {
				sqlID = "updTblAttach4DocNO";
				logger.info("paramMap.get(\"THR0013_01\")  ====== "+paramMap.get(subid));
				/** "THR0013_01":{"docGB":"THR","docKey":"puserUID","attachID":"8c7dcedf-123f-424e-b4ca-1fad010ec807"} * **/
				String json = ""+paramMap.get(subid);
				HashMap<String, Object> jsonMap = new ObjectMapper().readValue(json, HashMap.class) ;
			    logger.info("docGB === "+jsonMap.get("docGB") + "  :::: docKey  =="+jsonMap.get("docKey")+"  ::: attachID == "+jsonMap.get("attachID"));
	
			    /**
			     * 3-1.attachID ??? userUID select ????????? docKey set ?????? ?????????
			     * **/
			    //???????????? ?????????????????? ????????? ???...
	//		    jsonMap.put("TSYS_URI", "192.168.219.10:18997");
	//		    jsonMap.put("TSYS_URI", "127.0.0.1:18997");
			    jsonMap.put("loginID", ""+paramMap.get("loginID"));
			    jsonMap.put("TSYS_URI", this.TSYS_URI);
			    jsonMap.put("BUFFERTYPE", BUFFER_TYPE);
			    jsonMap.put("SQLMODE", SQLMODE);
			    jsonMap.put("agentID", grpcPrms.getAgentID());
			    jsonMap.put("puserUID", userUID);
			    //docKey
			   
			    
			    try {
			    	logger.info("3-1 testestsetset jsonMap "+jsonMap);
			    	String result = new CommonService().updTblAttach4DocNO(jsonMap);
				    logger.info("@@@@@@@ THR-SERVICE THR0013_01Map updTblAttach4DocNO results ======= "+result);
				} catch (Exception ex) {
					logger.info("Exception ============ "+ex);
					errCode = "saveUserInfo subPID query4Update1 ERROR";
					errMsg = ex.toString();
				}
			} else if("THR0013_02".equals(subid) ||"THR0014_02".equals(subid)) {
				String json = ""+paramMap.get(subid);
				HashMap<String, Object> jsonMap = new ObjectMapper().readValue(json, HashMap.class) ;
				
				List<Object> obj = (List<Object>) jsonMap.get("custID");
				List<Map<String, Object>> objLists = new ArrayList<Map<String, Object>>();
				
				for (int i=0; i<obj.size(); i++) {
					Map<String, Object> objMap = new HashMap<String, Object>();
					objMap.put("loginID", ""+paramMap.get("loginID"));
					objMap.put("puserUID", userUID);
					objMap.put("SQLMODE", SQLMODE);
					objMap.put("custID", obj.get(i));
					objMap.put("userUID", ""+paramMap.get("userUID"));
					objLists.add(objMap);
				}
			    
			    dataDao.query4Update3("insCustUserInfos", objLists);
			}
		}	
		jsonData = GrpcDataUtil.getGrpcResults(errCode, errMsg, null);
		grpcResp.setErrCode(errCode);
		grpcResp.setErrMsg(errMsg);
		grpcResp.setResults(jsonData);
		return grpcResp;
	}
	
	@Transactional(rollbackFor = {Exception.class})
	public GrpcResp getUserInfos(GrpcParams grpcPrms) throws Exception {
		String errCode  = "0";
		String errMsg   = "SUCCESS";
		String jsonRet = null;
		
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		Map<String, Object> commInfoMap = new DataService().getCommInfoMap(grpcPrms);
		commInfoMap.put("SQLMODE",  this.SQLMODE);
		commInfoMap.put("clang",    (grpcPrms.getClang()==null?this.CLANG:grpcPrms.getClang()));
		commInfoMap.put("TSYS_URI", this.TSYS_URI);
		commInfoMap.put("BUFFERTYPE",  this.BUFFER_TYPE);
		
		GrpcResp grpcResp = new GrpcResp();
		paramMap.putAll(commInfoMap);
		
		List<Map<String, Object>> rsList = dataDao.query4List1("getUserInfos", paramMap);
        if (rsList == null || rsList.size() == 0) {
        	errMsg   = "????????? ???????????? ????????????.";
        	errCode  = ErrConstance.NO_DATA;
    		jsonRet = GrpcDataUtil.getGrpcResults("NO_DATA", errMsg, null);
        } else {    
        	jsonRet = "{\"results\":"+ new ObjectMapper().writeValueAsString(rsList) + "}";
        	
        	 dataDao.query4Update3("updSyncUserInfos", rsList);
        }		
		
		
		grpcResp.setErrCode(errCode);
		grpcResp.setErrMsg(errMsg);
		grpcResp.setResults(jsonRet);
		
		return grpcResp;
	}
	
	@Transactional(rollbackFor = {Exception.class})
	public GrpcResp getCustInfos(GrpcParams grpcPrms) throws Exception {
		String errCode  = "0";
		String errMsg   = "SUCCESS";
		String jsonRet = null;
		
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		Map<String, Object> commInfoMap = new DataService().getCommInfoMap(grpcPrms);
		commInfoMap.put("SQLMODE",  this.SQLMODE);
		commInfoMap.put("clang",    (grpcPrms.getClang()==null?this.CLANG:grpcPrms.getClang()));
		commInfoMap.put("TSYS_URI", this.TSYS_URI);
		commInfoMap.put("BUFFERTYPE",  this.BUFFER_TYPE);
		
		GrpcResp grpcResp = new GrpcResp();
		paramMap.putAll(commInfoMap);
		
		List<Map<String, Object>> rsList = dataDao.query4List1("getCustInfos", paramMap);
        if (rsList == null || rsList.size() == 0) {
        	errMsg   = "????????? ???????????? ????????????.";
        	errCode  = ErrConstance.NO_DATA;
    		jsonRet = GrpcDataUtil.getGrpcResults("NO_DATA", errMsg, null);
        } else {    
        	jsonRet = "{\"results\":"+ new ObjectMapper().writeValueAsString(rsList) + "}";
        	
        	 dataDao.query4Update3("updSyncCustInfos", rsList);
        }		
		
		
		grpcResp.setErrCode(errCode);
		grpcResp.setErrMsg(errMsg);
		grpcResp.setResults(jsonRet);
		
		return grpcResp;
	}
	
	@Transactional(rollbackFor = {Exception.class})
	public GrpcResp getUserCustInfos(GrpcParams grpcPrms) throws Exception {
		String errCode  = "0";
		String errMsg   = "SUCCESS";
		String jsonRet = null;
		
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		Map<String, Object> commInfoMap = new DataService().getCommInfoMap(grpcPrms);
		commInfoMap.put("SQLMODE",  this.SQLMODE);
		commInfoMap.put("clang",    (grpcPrms.getClang()==null?this.CLANG:grpcPrms.getClang()));
		commInfoMap.put("TSYS_URI", this.TSYS_URI);
		commInfoMap.put("BUFFERTYPE",  this.BUFFER_TYPE);
		
		GrpcResp grpcResp = new GrpcResp();
		paramMap.putAll(commInfoMap);
		
		List<Map<String, Object>> rsList = dataDao.query4List1("getUserCustInfos", paramMap);
        if (rsList == null || rsList.size() == 0) {
        	errMsg   = "????????? ???????????? ????????????.";
        	errCode  = ErrConstance.NO_DATA;
    		jsonRet = GrpcDataUtil.getGrpcResults("NO_DATA", errMsg, null);
        } else {    
        	jsonRet = "{\"results\":"+ new ObjectMapper().writeValueAsString(rsList) + "}";
        	
        	 dataDao.query4Update3("updSyncUserCustInfos", rsList);
        }		
		
		
		grpcResp.setErrCode(errCode);
		grpcResp.setErrMsg(errMsg);
		grpcResp.setResults(jsonRet);
		
		return grpcResp;
	}

	public String bcryptPwdEncode(String pwd) {
//		BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder(10);
		BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
		String encKey = bcryptPasswordEncoder.encode(pwd);
		return encKey;
	}
	
}

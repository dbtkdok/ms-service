package com.sci4s.msa.hr.svc;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sci4s.grpc.ErrConstance;
import com.sci4s.grpc.dao.DataDao;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.utils.GrpcDataUtil;
import com.sci4s.msa.hr.dto.MemberVO;
import com.sci4s.msa.hr.dto.UserInfo;
import com.sci4s.msa.mapper.LoginMapper;
import com.sci4s.utils.AES256Util;
import com.sci4s.utils.DateUtil;

@Service
public class LoginService {
	Logger logger = LoggerFactory.getLogger(LoginService.class);

	@Value("${db.dbtype}")
	String SQLMODE;
	
	@Value("${buffer.type}")
	String BUFFER_TYPE;
	
	DataDao dataDao;	
	LoginMapper loginMapper;
	
	@Autowired
	public  LoginService(LoginMapper loginMapper, DataDao dataDao){
	    this.loginMapper = loginMapper;
	    this.dataDao = dataDao;
	}
	
	/**
	 * 실제 로그인 처리용 정보 조회 서비스임. 
	 * 
	 * @param  GrpcParams grpcPrms
	 * @return HRGIO.UserInfo
	 */
	@Transactional
	public UserInfo checkLogin(Map<String, Object> paramMap) throws Exception {
		int      isLogin  = 0;
		String   errCode  = "0";
		String   errMsg   = "";
		UserInfo userInfo = null;
		boolean  isAlert_ = true;
		
		if (paramMap.get("ISLOGIN") != null) {
			isLogin = Integer.parseInt(""+ paramMap.get("ISLOGIN"));
		}	
		if (!paramMap.containsKey("SQLMODE")) {
			paramMap.put("SQLMODE", this.SQLMODE);
		}	
		logger.debug("paramMap:::::::::::::::"+ paramMap);
        // Test Parameters : agentID = 13, memberID = 1, borgID = 15, userType = 'SYS'
        userInfo = (UserInfo) loginMapper.getLoginUserInfoForAll(paramMap);  
        if (userInfo == null) {
        	errCode = ""+ paramMap.get("PID");
    		errMsg  = "your id is wrong or there is no your organization!";
    		new Exception("["+ errCode +"]"+ errMsg);
        } else {       
        	List<Map<String, Object>> custList = dataDao.query4List1("getUserCustInfoList", paramMap);
        	
        	if(custList != null) {
        		userInfo.setCustList(custList);
        	}
        	
        	// PID 설정
        	userInfo.setpID(""+ paramMap.get("PID"));

        	int julianDate    = DateUtil.toJulian(new Date());        	
        	AES256Util aes256 = new AES256Util();
        	// csKey=julianDate|agentID|userUID|borgUID|userIP|serverIP|roleID|consignGB|loginID
        	// loginKey=julianDate|PID|agentID|clientID|loginID|userPwd|LOGIN|userIP|serverIP|roleID|consignGB        	
        	String tokenStr = julianDate +"|"+ userInfo.getAgentID() +"|"+ userInfo.getUserUID();
        	tokenStr += "|"+ userInfo.getBorgUID();
        	tokenStr += "|"+ paramMap.get("userIP") +"|"+ paramMap.get("serverIP");
        	
        	// 로그인 처리 후, 권하코드와 위탁여부를 csKey에 설정함.
        	// 권한 설정이 완료되기 전까지 임시로 설정함.
    		if ("S".equals(userInfo.getUserType())) {
    			tokenStr += "|AU0000|Y|";
    		} else if ("A".equals(userInfo.getUserType())) {
    			tokenStr += "|AU0001|Y|";
    		} else if ("C".equals(userInfo.getUserType())) {
    			tokenStr += "|AU0100|N|";
    		} else if ("V".equals(userInfo.getUserType())) {
    			tokenStr += "|AU0800|N|";
    		} else if ("F".equals(userInfo.getUserType())) {
    			tokenStr += "|" + userInfo.getRoleID()  +"|N|";
    		}
    		tokenStr += userInfo.getLoginID();
    		
        	String encKey = aes256.encryptII(tokenStr);
        	String desKey = aes256.decryptII(encKey);
        	
        	//logger.debug("converted encKey ::: " + encKey);
        	//logger.debug("converted desKey ::: " + desKey); 

        	userInfo.setCsKey(encKey);            	
            // 로그인 이력 저장
            if (isLogin == 0 || isLogin == 5) { // auto loing == 5
            	String pwd = ""+ paramMap.get("pwd");
            
            	logger.debug("userInfo.getAgentID() ::::::::::::"+ userInfo.getAgentID());
            	logger.debug("userInfo.getUserUID() ::::::::::::"+ userInfo.getUserUID());
            	logger.debug("userInfo.getBorgID()  ::::::::::::"+ userInfo.getBorgID());  
            	logger.debug("userInfo.getPwd()     ::::::::::::"+ userInfo.getPwd()+"=pwd :::"+pwd);
            	logger.debug("userInfo.getToken()   ::::::::::::"+ userInfo.getToken());  
            	            	
            	if (userInfo.getPwd().equals(pwd)) {             		
            		isAlert_ = false;
            	} else {
            		if (isLogin == 5) {
            			if (paramMap.get("token") != null && paramMap.get("series") != null && !userInfo.getToken().equals("N")) {
        		        	isAlert_ = false; // 자동로그인인 경우는 비밀번호 비교는 스킵함.
        	        	} 
    	        	}
            	}
	        	if (isAlert_) {
		        	errCode = ""+ paramMap.get("PID");
		    		errMsg  = "your password is wrong!";
		    		throw new Exception("###[8001]"+ errMsg +"###");
	        	} else { // 로그를 저장한다.
            		Map<String, Object> dataMap = new HashMap<String, Object>();
            		dataMap.put("SQLMODE",     this.SQLMODE);
            		dataMap.put("agentID",     userInfo.getAgentID());
            		dataMap.put("userUID",     userInfo.getUserUID());
            		dataMap.put("borgUID",     userInfo.getBorgUID());
            		dataMap.put("userIP",      ""+ paramMap.get("userIP"));
            		dataMap.put("serverIP",    ""+ paramMap.get("serverIP"));
            		dataMap.put("userMacAddr", null);
            		dataMap.put("csKey",       encKey);
	            	
            		loginMapper.insUserLoginHistory(dataMap);    
	        	} 
            } 
        }
        return userInfo;
	}
	
	/**
	 * 스프린 시큐리팅 로그인 처리용 정보 조회 서비스임. 
	 * 
	 * @param  Map<String,Object> paramMap
	 * @return UserInfo
	 */
	@Transactional
	public UserInfo loginSecurityInfo(Map<String,Object> reqMap) throws Exception {
		
		String errCode = "0";
		String errMsg  = "";
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.putAll(reqMap);	
		paramMap.put("SQLMODE", this.SQLMODE);
		
		//logger.debug(paramMap);
		MemberVO memberVO = null;
		try {
			memberVO = (MemberVO) loginMapper.loginSecurityInfo(paramMap);
		} catch(NullPointerException nex ) {
			memberVO = null;
		}
		if (memberVO == null) {
			errCode = ""+ paramMap.get("PID");
			errMsg  = "your id is wrong or there is no your organization!";
			throw new Exception("###[8000]"+ errMsg +"###");
		} else {
	        String reqPwd = ""+reqMap.get("pwd");
	        logger.info("reqPwd == "+reqPwd+" :::: memberVO.getUserPW() === "+memberVO.getUserPW());
	        logger.info("token  == "+ reqMap.get("token") +" :::: memberVO.getToken() === "+memberVO.getToken());
	        if (!reqPwd.equals(memberVO.getUserPW())) {
	        	boolean isAlert_ = true;
	        	if (reqMap.get("token") != null && reqMap.get("series") != null && !memberVO.getToken().equals("N")) {
		        	isAlert_ = false; // 자동로그인인 경우는 비밀번호 비교는 스킵함.
	        	}    	
	        	if (isAlert_) {
		        	errCode = ""+ paramMap.get("PID");
		    		errMsg  = "your password is wrong!";
		    		throw new Exception("###[8001]"+ errMsg +"###");
	        	}
	        } else {
	        	logger.info("reqPwd == "+reqPwd+"  :::: memberVO.getUserPW()  === "+memberVO.getUserPW());
	        }
	        UserInfo userInfo = this.checkLogin(paramMap);
	//      userInfo.setPwd(userInfo.getUserPW());//스프링 인코딩 암호를 전달함.
	        userInfo.setPwd(userInfo.getPwd());//스프링 인코딩 암호를 전달함. -> tbl_userinfo 의 pwd로 암호 비교...2022.02.14
	        
	        return userInfo;
		}
	}
	
	/* DataDao 사용 예
	 * MemberVO memberVO = (MemberVO)dataDao.query4Object1("loginSecurityInfo", paramMap); 
	 * 
	@Transactional
	public UserInfo loginSecurityInfo(Map<String,String> reqMap) throws Exception {
		
		String errCode = "0";
		String errMsg  = "";
		
		//logger.debug("reqMapreqMapreqMapreqMapreqMapreqMapreqMapreqMapreqMapreqMap"+ reqMap);
		
		Map<String,Object> paramMap = new HashMap<String,Object>();
		paramMap.putAll(reqMap);	
		
		//logger.debug(paramMap);
		
		MemberVO memberVO = (MemberVO)dataDao.query4Object1("loginSecurityInfo", paramMap); 
        if (memberVO == null) {
        	errCode = ""+ paramMap.get("PID");
    		errMsg  = "your id is wrong or there is no your organization!";
    		new Exception("["+ errCode +"]"+ errMsg);
        }
        // Aes256 암호를 추가해 줘서 checkLogin에서 암호 비교시 사용함.
        paramMap.put("aes256PW", memberVO.getAes256PW());
        
        UserInfo userInfo = checkLogin(paramMap);
        userInfo.setPwd(memberVO.getUserPW());//스프링 인코딩 암호를 전달함.
         
        return userInfo;
	}
	*/
	
	/**
	 * 임시 로그인 목록 조회 - 삭제예정
	 * @param grpcMap
	 * @return
	 * @throws Exception
	 */
	public GrpcResp getTempLoginUserList(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";	
		GrpcResp grpcResp = new GrpcResp();
		
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		try {
			//logger.debug("query4Data.SQLMODE ::: "+ this.SQLMODE);
			paramMap.put("SQLMODE", this.SQLMODE);
			//logger.debug("paramMap"+ paramMap);
			List<Object> rsList = dataDao.query4List2("getTempLoginUserList", paramMap);
	        if (rsList == null || rsList.size() == 0) {
	        	errCode = ErrConstance.NO_DATA;
	    		errMsg  = "조회된 데이터가 없습니다.";
	    		jsonRet = GrpcDataUtil.getGrpcResults("NO_DATA", errMsg, null);
	        } else {    
	        	jsonRet = "{\"results\":"+ new ObjectMapper().writeValueAsString(rsList) + "}";
	        }			
		} catch(Exception ex) {
			ex.printStackTrace();
			errCode = ErrConstance.ERR_9999;
			errMsg  = ex.toString();
			jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, errMsg, null);
        } finally {
        	if (paramMap != null) {
        		try { paramMap = null; } catch(Exception ex) { }
        	}
        }
//		logger.debug("231 jsonRet ::: "+ jsonRet);

		grpcResp.setErrCode(errCode);
		grpcResp.setErrMsg(errMsg);
		grpcResp.setResults(jsonRet);

		// Data 처리 후 Json 형식으로 변환하여 리턴하면 됨.
		return grpcResp;
	}

	/**
	 * 매장 어플리케이션 실행 시, 필요한 인증서 정보를 리턴한다.
	 * 
	 * @param grpcMap
	 * @return
	 * @throws Exception
	 */
	public GrpcResp getFirstAuthData(GrpcParams grpcPrms) throws Exception {
		String     errCode   = "0";
		String     errMsg    = "";
		String     jsonRet   = "";		
		GrpcResp   grpcResp  = new GrpcResp();
		//JSONObject jsonObj   = null;
				
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@ getFirstAuthData @@@@@@@@@@@@@@@@@@@@@@@"+ grpcPrms.getData());
		try {
			//jsonObj = (JSONObject) new JSONParser().parse(grpcPrms.getData());		
			//logger.info("localIP ::: "+ jsonObj.get("localIP").toString());
			
			Map<String, Object> rsMap = loginMapper.getFirstAuthData();
	        if (rsMap == null || rsMap.size() == 0) {
	        	errCode = ErrConstance.NO_DATA;
	    		errMsg  = "조회된 데이터가 없습니다.";
	    		jsonRet = GrpcDataUtil.getGrpcResults("NO_DATA", errMsg, null);
	        } else {    
	        	jsonRet = "{\"results\":"+ new ObjectMapper().writeValueAsString(rsMap) + "}";
	        }			
		} catch(Exception ex) {
			ex.printStackTrace();
			errCode = ErrConstance.ERR_9999;
			errMsg  = ex.toString();
			jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, errMsg, null);
        } finally {
	        //if (jsonObj != null) jsonObj = null; 
    	}
		grpcResp.setErrCode(errCode);
		grpcResp.setErrMsg(errMsg);
		grpcResp.setResults(jsonRet);
		// Data 처리 후 Json 형식으로 변환하여 리턴하면 됨.
		return grpcResp;
	}
}

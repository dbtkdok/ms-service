package com.sci4s.grpc;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.util.GrpcDataUtil;
import com.sci4s.grpc.utils.GrpcReflectUtil;
import com.sci4s.msa.hr.dto.UserInfo;
import com.sci4s.msa.hr.svc.LoginService;
import com.sci4s.utils.AES256Util;
import com.sci4s.utils.ErrUtil;

import io.grpc.BindableService;
import io.grpc.stub.StreamObserver;

@Service
public class ThrMainProcessor extends MsaHRGrpc.MsaHRImplBase  implements BindableService {
	
	Logger logger = LoggerFactory.getLogger(ThrMainProcessor.class);

	LoginService loginService;	
	@Autowired
	public  ThrMainProcessor(LoginService loginService){
	    this.loginService = loginService;
	}

	@Override
	public void getLoginUserInfo(SciRIO.Data request, StreamObserver<HRGIO.UserInfo> responseObserver) {
		///////////////////////////////////////////
		// GRPC 요청 파라미터 셋팅	
		///////////////////////////////////////////
		String errCode = "0";
		String errMsg  = "SUCCESS";
		GrpcParams grpcPrms = null;
		HRGIO.UserInfo grpcResp = null;
		//LoginService loginService = new LoginService();
		try {
			//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// 1. GRPC로 전송된 데이터를 Parsing하여 Map에 저장함.
			//    - 전송된 모든 파라미터 명은 대문자로 변경하여 Map에 저장됨.
			// 2. 카프카로 전송할 Topic을 생성하고, 작업이 완료되면 카프카로 Topic을 전송함.
			//grpcPrms = GrpcCommUtil.parseGrpcData(request);
			
			Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
			//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// 3. 로그인 서비스 호출
        	UserInfo userInfo = null;
        	try {
	        	userInfo = loginService.checkLogin(paramMap);
			} catch (Exception e1) {
				logger.error(e1.toString());
				errCode = ""+ paramMap.get("PID");
				errMsg  = e1.toString();
			} 
        	
	        if (!"0".equals(errCode)) {
	        	grpcResp = HRGIO.UserInfo.newBuilder()
	        			.setAgentID(userInfo.getAgentID())
	    				.setPID(userInfo.getpID())
	        			.setCsKey(userInfo.getCsKey()) 
	        			.setErrCode(errCode)
	        			.setErrMsg("[NOT FOUND USER]"+ errMsg)
	        			.build();		
	        } else {
	        	// UserInfo 셋팅 후 리턴하면 됨.   
	        	grpcResp = (HRGIO.UserInfo) GrpcReflectUtil.copyMessageBuilder(userInfo, HRGIO.UserInfo.newBuilder(), userInfo.getCsKey(), errCode, errMsg);
	        }

			responseObserver.onNext(grpcResp);		
			responseObserver.onCompleted();
			
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			//if (loginService != null) { loginService = null; }
			if (grpcPrms != null) { grpcPrms = null; }
			if (grpcResp != null) { grpcResp = null; }
		}		
	}
	
	@Override
	public void loginProcess(HRGIO.Login request, StreamObserver<SciRIO.Data> responseObserver) {
		///////////////////////////////////////////
		// GRPC 요청 파라미터 셋팅	
		///////////////////////////////////////////
		String[] loginData = request.getLoginKey().split("[|]");
		String PID = ""+ loginData[1];
		String errCode = "0";
		String errMsg  = "SUCCESS";
		String jsonRet = null;
		AES256Util aes256 = null;
		SciRIO.Data grpcResp = null;
		Map<String,Object> paramMap = new HashMap<String,Object>();
//		System.out.println("paramsMap :: " + request.getLoginKey());
		
		paramMap.put("jDate",   loginData[0]);
		paramMap.put("PID",     PID);
		paramMap.put("agentID", loginData[2]);
		paramMap.put("custID",  loginData[3]);
		paramMap.put("loginID", loginData[4]);
		paramMap.put("userPwd", loginData[5]);
		paramMap.put("pwd",     loginData[5]);		            		
		paramMap.put("ISLOGIN", loginData[6]);
		paramMap.put("userIP",  loginData[7]);
		paramMap.put("serverIP",loginData[8]);
		
		int ISLOGIN = Integer.parseInt(loginData[6]); //0:로그인 시도, 5:자동로그인
		if (ISLOGIN == 5) {
			paramMap.put("series", loginData[9]);
			paramMap.put("token",  loginData[5]);
		}
		try {
			//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// 3. 로그인 서비스 호출
        	UserInfo userInfo = null;
        	try {
	        	userInfo = loginService.loginSecurityInfo(paramMap);
			} catch (Exception e1) {
				errMsg  = ErrUtil.getErrorResults(e1);
				errCode = PID;
			}   
        	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    		// 4. 업무 처리 후, Json 형식으로 결과를 저장한다.
        	if (userInfo != null) {
            	jsonRet = new ObjectMapper().writeValueAsString(userInfo);
            	jsonRet = "{\"results\":"+ jsonRet + "}";
        	} 
        	
        	if (!errCode.equals("0")) {
    			jsonRet = GrpcDataUtil.getGrpcResults(errCode, errMsg, null);
    		}
    		//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    		// 5. 업무 처리 후, Json 형식으로 결과를 리턴한다.
        	
        	if (userInfo != null) {
            	grpcResp = SciRIO.Data.newBuilder()
            			.setPID(PID)
            			.setAgentID(userInfo.getAgentID())
            			.setData(jsonRet)
            			.setCsKey(userInfo.getCsKey())
            			.setUserIP(""+ paramMap.get("userIP"))
            			.setServerIP(""+ paramMap.get("serverIP"))
            			.setUserUID(""+ userInfo.getUserUID())
            			.setBorgUID(""+ userInfo.getBorgID())
            			.setErrCode(errCode)
            			.setErrMsg(errMsg)
            			.build();
        	} else {
            	grpcResp = SciRIO.Data.newBuilder()
            			.setPID(PID)
            			.setAgentID(""+paramMap.get("agentID"))
            			.setData(jsonRet)
            			.setCsKey("")
            			.setUserIP(""+ paramMap.get("userIP"))
            			.setServerIP(""+ paramMap.get("serverIP"))
            			.setUserUID("0")
            			.setBorgUID("0")
            			.setErrCode(errCode)
            			.setErrMsg(errMsg)
            			.build();
        	}
		} catch (Exception e) {
			logger.error("callRMsg() TRY CATCH");
			e.printStackTrace();
			//logger.error(e.getMessage());			
			if (grpcResp == null) {				
				grpcResp = SciRIO.Data.newBuilder()
	        			.setErrCode(ErrConstance.ERR_9999)
	        			.setErrMsg(e.getMessage())
	        			.build();
			}			
		} finally {			
			try {
				responseObserver.onNext(grpcResp);
				responseObserver.onCompleted();			
			} catch (Exception e1) {
				logger.error("onCompleted() TRY CATCH");
				e1.printStackTrace();
			}
			//if (loginService != null) { loginService = null; }
			if (aes256   != null) { aes256 = null;   }
			if (paramMap != null) { paramMap = null; }
			if (grpcResp != null) { grpcResp = null; }
		}		
	}
}
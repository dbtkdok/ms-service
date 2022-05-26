package com.sci4s.grpc;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sci4s.fbs.Data;
import com.sci4s.fbs.hr.FlatJsonGrpc;
import com.sci4s.fbs.hr.Login;
import com.sci4s.fbs.hr.UserInfo;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.utils.FlatDataUtil;
import com.sci4s.msa.hr.svc.LoginService;
import com.sci4s.utils.AES256Util;

import io.grpc.BindableService;
import io.grpc.stub.StreamObserver;

@Service
public class FlatThrMainProcessor extends FlatJsonGrpc.FlatJsonImplBase implements BindableService {
	
	Logger logger = LoggerFactory.getLogger(FlatThrMainProcessor.class);

	@Value("${default.lang}")
	String CLANG;
	
	LoginService loginService;	
	@Autowired
	public  FlatThrMainProcessor(LoginService loginService){
	    this.loginService = loginService;
	}

	@Override
	public void getLoginUserInfo(Data request, StreamObserver<UserInfo> responseObserver) {
		///////////////////////////////////////////
		// GRPC 요청 파라미터 셋팅	
		///////////////////////////////////////////
		String errCode = "0";
		String errMsg  = "SUCCESS";
		GrpcParams grpcPrms = null;
		UserInfo flatResp = null;
		FlatBufferBuilder builder = new FlatBufferBuilder();
		//LoginService loginService = new LoginService();
		try {
			//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// 1. GRPC로 전송된 데이터를 Parsing하여 Map에 저장함.
			//    - 전송된 모든 파라미터 명은 대문자로 변경하여 Map에 저장됨.
			// 2. 카프카로 전송할 Topic을 생성하고, 작업이 완료되면 카프카로 Topic을 전송함.
			//grpcPrms = GrpcCommUtil.parseGrpcData(request);
			
			Map<String, Object> paramMap = FlatDataUtil.getParams4Map("params", grpcPrms.getData());
			//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// 3. 로그인 서비스 호출
			com.sci4s.msa.hr.dto.UserInfo userInfo = null;
        	try {
	        	userInfo = loginService.checkLogin(paramMap);
			} catch (Exception e1) {
				logger.error(e1.toString());
				errCode = ""+ paramMap.get("PID");
				errMsg  = e1.toString();
			} 
        	
        	int dataOffset = UserInfo.createUserInfo(builder
        			, builder.createString("" + userInfo.getUserUID())
        			, builder.createString("" + userInfo.getLoginID())
        			, builder.createString("" + userInfo.getPwd())
        			, builder.createString("" + userInfo.getUserNM())
        			, builder.createString("" + userInfo.getUserNMEng())
        			, builder.createString("" + userInfo.getEmpNO())
        			, builder.createString("" + userInfo.getRegionCD())
        			, builder.createString("" + userInfo.getRegionNM())
        			, builder.createString("" + userInfo.getZipCD())
        			, builder.createString("" + userInfo.getTelNO())
        			, builder.createString("" + userInfo.getMobile())
        			, builder.createString("" + userInfo.getUserType())
        			, builder.createString("" + userInfo.getIsActive())
        			, builder.createString("" + userInfo.getRoleID())
        			, builder.createString("" + userInfo.getGrade())
        			, builder.createString("" + userInfo.getFaxNO())
        			, builder.createString("" + userInfo.getTotAdminYn())
        			, builder.createString("" + userInfo.getAddr1())
        			, builder.createString("" + userInfo.getAddr2())
        			, builder.createString("" + userInfo.getEmail())
        			, builder.createString("" + userInfo.getCustID())
        			, builder.createString("" + userInfo.getCustNM())
        			, builder.createString("" + userInfo.getObuID())
        			, builder.createString("" + userInfo.getObuNM())
        			, builder.createString("" + userInfo.getBorgUID())
        			, builder.createString("" + userInfo.getBorgID())
        			, builder.createString("" + userInfo.getBorgNM())
        			, builder.createString("" + userInfo.getAgentID())
        			, builder.createString("" + userInfo.getUserActFile())
        			, builder.createString("" + userInfo.getDbsts())
        			, builder.createString("" + userInfo.getUserIP())
        			, builder.createString("" + userInfo.getCsKey())
        			, builder.createString("" + userInfo.getpID())
        			, builder.createString("" + userInfo.getErrCode())
        			, builder.createString("" + userInfo.getErrMsg()));
        	
			builder.finish(dataOffset); 
			flatResp = UserInfo.getRootAsUserInfo(builder.dataBuffer());

			responseObserver.onNext(flatResp);		
			responseObserver.onCompleted();
			
		} catch (Exception e) {
			logger.error(e.toString());
		} finally {
			//if (loginService != null) { loginService = null; }
			if (grpcPrms != null) { grpcPrms = null; }
			if (flatResp != null) { flatResp = null; }
		}		
	}
	
	@Override
	public void loginProcess(Login request, StreamObserver<Data> responseObserver) {
		///////////////////////////////////////////
		// GRPC 요청 파라미터 셋팅	
		///////////////////////////////////////////
		String[] loginData = request.loginKey().split("[|]");
		String PID = ""+ loginData[1];
		FlatBufferBuilder builder = new FlatBufferBuilder();
		String errCode = "0";
		String errMsg  = "SUCCESS";
		String jsonRet = null;
		AES256Util aes256 = null;
		Data flatResp = null;
		Map<String,Object> paramMap = new HashMap<String,Object>();
		
		String agentID = "";
		String csKey = "";
		String userUID = "";
		String borgUID = "";
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
		
		//LoginService loginService = new LoginService();
		try {
			//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
			// 3. 로그인 서비스 호출
			com.sci4s.msa.hr.dto.UserInfo userInfo = null;
        	try {
//        		System.out.println(request.getParamsMap());
	        	userInfo = loginService.loginSecurityInfo(paramMap);
			} catch (Exception e1) {
				e1.printStackTrace();
				errCode = PID;
				errMsg  = e1.toString();
			}   
        	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    		// 4. 업무 처리 후, Json 형식으로 결과를 저장한다.
        	jsonRet = new ObjectMapper().writeValueAsString(userInfo);
        	jsonRet = "{\"results\":"+ jsonRet + "}";
        	
//        	logger.debug("jsonRet ::::::::::::"+ jsonRet);
        	if (!errCode.equals("0")) {
    			jsonRet = FlatDataUtil.getGrpcResults(errCode, errMsg, null);
    		}
        	
        	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    		// 5. 업무 처리 후, Json 형식으로 결과를 리턴한다.
        	agentID = userInfo.getAgentID();
    		csKey = "" + userInfo.getCsKey();
    		userUID = "" + userInfo.getUserUID();
    		borgUID = "" + userInfo.getBorgID();
    		
		} catch (Exception e) {
			logger.error("callRMsg() TRY CATCH");
			e.printStackTrace();
					
			errCode = ErrConstance.ERR_9999;
			errMsg  = e.getMessage();
		} finally {			
			try {
				int dataOffset = Data.createData(builder
	        			, builder.createString("" + PID)
	        			, builder.createString("" + agentID)
	        			, builder.createString("" + csKey)
	        			, builder.createString(""+ paramMap.get("userIP"))
	        			, builder.createString(""+ paramMap.get("serverIP"))
	        			, builder.createString(""+ userUID)
	        			, builder.createString(""+ borgUID)
	        			, builder.createString("" + this.CLANG)
	        			, builder.createString("" + jsonRet)
	        			, builder.createString("" + errCode)
	        			, builder.createString("" + errMsg)
						);
	        	
				builder.finish(dataOffset); 
				flatResp = Data.getRootAsData(builder.dataBuffer());
				
				responseObserver.onNext(flatResp);
				responseObserver.onCompleted();			
			} catch (Exception e1) {
				logger.error("onCompleted() TRY CATCH");
				e1.printStackTrace();
			}
			//if (loginService != null) { loginService = null; }
			if (aes256   != null) { aes256 = null;   }
			if (paramMap != null) { paramMap = null; }
			if (flatResp != null) { flatResp = null; }
		}		
	}
}
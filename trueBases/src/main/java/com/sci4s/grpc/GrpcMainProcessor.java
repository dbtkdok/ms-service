package com.sci4s.grpc;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.sci4s.cnf.PIDSLoader;
import com.sci4s.grpc.SciRIO.Data;
import com.sci4s.grpc.SciRIO.ReqMsg;
import com.sci4s.grpc.SciRIO.RetMsg;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.svc.CommService;
import com.sci4s.grpc.utils.GrpcDataUtil;
import com.sci4s.grpc.utils.GrpcReflectUtil;
import com.sci4s.msa.tsys.svc.SysMgmtService;
import com.sci4s.msa.tsys.svc.TSysService;
import com.sci4s.utils.ErrUtil;

import io.grpc.BindableService;
import io.grpc.stub.StreamObserver;

@Service
public class GrpcMainProcessor extends MsaApiGrpc.MsaApiImplBase implements BindableService {
	
	Logger logger = LoggerFactory.getLogger(GrpcMainProcessor.class);
	
	@Value("${db.dbtype}")
	String SQLMODE;
	
	@Value("${default.lang}")
	String CLANG;

	@Value("${msa.agentid}")
	String MSA_AGENTID;
	
	@Value("${msa.id}")
	String MSA_ID;

	@Value("${msa.pids.uri}")
	String TSYS_URI;
	
	@Value("${mst.chk.time}")
	String MST_CHKTIME;
	
	@Value("${buffer.type}")
	String BUFFER_TYPE;
	
	String xmlUri;
	
	private PIDSLoader pids;
	private ApplicationContext context;	
	private CommService commService;
	private SysMgmtService sysMgmtService;
	private TSysService tSysService;
	@Autowired
	public  GrpcMainProcessor(ApplicationContext context
			, CommService commService
			,SysMgmtService sysMgmtService
			,TSysService tSysService){
	    this.context = context;
	    this.commService = commService;
	    this.sysMgmtService = sysMgmtService;
	    this.tSysService = tSysService;
	}
	
	@PostConstruct
    private void init() {
		this.xmlUri = TSYS_URI +"|"+ MSA_AGENTID +"|"+ MSA_ID;	    
	    if (pids == null) {
			try { pids = PIDSLoader.getInstance(this.xmlUri, this.BUFFER_TYPE); } catch(Exception ex) {}
		}
    }
	
	@Override
	public void callRMsg(SciRIO.Data request, StreamObserver<SciRIO.RetMsg> responseObserver) {
		logger.info("CALL "+ request.getPID() +" METHOD ###################################### START");
		String results = "";
		String errMsg = null;
		GrpcParams  grpcPrms = null;
		SciRIO.RetMsg grpcResp = null;
		String PID = request.getPID();
		PIDSLoader pids = null;
		try {
			if (pids == null) {
				try { pids = PIDSLoader.getInstance(this.xmlUri, this.BUFFER_TYPE); } catch(Exception ex) {}
			}
			grpcPrms = GrpcDataUtil.parseGrpcData(request);		
			String svcName  = pids.getPIDSteps(PID).getService(); // 서비스명
			String queryId  = pids.getPIDSteps(PID).getQuery();   // SqlID
			String methName = pids.getPIDSteps(PID).getMethod();  // 호출할 메서드명
			
			logger.info("service ::: " + svcName +", queryId ::: " + queryId +", method ::: " + methName);
			
			Object beanObj = context.getBean(svcName);
			if (request.getParamsMap() != null && request.getParamsMap().size() > 0) {					
				grpcResp = GrpcReflectUtil.callServiceForMap(beanObj, queryId, methName, request.getParamsMap());
				
			} else {
				grpcResp = GrpcReflectUtil.callServiceForGrpcParams(beanObj, queryId, methName, grpcPrms);
			}
		} catch (Exception e) {
			logger.error("callRMsg() TRY CATCH");
			errMsg = e.getMessage();
			logger.error("113    ::::::"+ errMsg);
			results = ErrUtil.getErrorResults(e);
			logger.error("results::::::"+ results);
		} finally {				
			try {
				if (errMsg != null) {
					grpcResp = SciRIO.RetMsg.newBuilder()
							.setResults(results)
							.setErrCode(ErrConstance.ERR_9999)
							.setErrMsg(errMsg)
							.build();
				}
				responseObserver.onNext(grpcResp);
				responseObserver.onCompleted();			
			} catch (Exception e1) {
				logger.error("onCompleted() TRY CATCH");
				e1.printStackTrace();
			}			
			if (grpcPrms != null) { try { grpcPrms = null; } catch (Exception e1) {} }
			if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }
		}	
		logger.info("CALL "+ PID +" METHOD ###################################### END");
	}
	
	/**
	 * 화면 진입 전에 접근가능 Activities와 공통 코드 데이터를 조회하여 리턴함.
	 * 
	 */
	@Override
	public void getEnableJob(Data request, StreamObserver<RetMsg> responseObserver) {
		SciRIO.RetMsg grpcResp = null;
		Map<String,String> params = new HashMap<String, String>();
		params.putAll(request.getParamsMap());
		params.put("pID", request.getPID());
		
		try {
			String privIDs   = params.get("privIDs");
			String codeTypes = params.get("codeTypes");
			
			logger.info("privIDs   ::: "+ privIDs);
			logger.info("codeTypes ::: "+ codeTypes);
			
//			grpcResp = sysMgmtService.getEnableJob(params);
			
		} catch (Exception e1) {
			logger.error("getEnableJob.onCompleted() TRY CATCH");
			e1.printStackTrace();
			String results = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, e1.getMessage(), null);
			grpcResp = SciRIO.RetMsg.newBuilder()
					.setResults(results)
					.setErrCode(ErrConstance.ERR_9999)
					.setErrMsg(e1.getMessage())
					.build();
		} finally {			
			try {
				responseObserver.onNext(grpcResp);
				responseObserver.onCompleted();			
			} catch (Exception e1) {
				logger.error("getEnableJob.onCompleted() TRY CATCH");
				e1.printStackTrace();
			}		
			if (params   != null) { try { params   = null; } catch (Exception e1) {} }
			if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }
		}
	}
	
	
	/**
	 * 서비스별 Master 데이터 싱크 서비스
	 * 
	 */
	@Override
	public void getMstInfo(ReqMsg request, StreamObserver<RetMsg> responseObserver) {
		SciRIO.RetMsg retMsg = null;
		String jsonData = null;		
		GrpcResp grpcResp = new GrpcResp();
		Map<String,Object> params = new HashMap<String, Object>();
		try {
			logger.info("agentID ::: "+ request.getAgentID());
			logger.info("tblName ::: "+ request.getMsg());
			String[] vals = null;
			if (request.getMsg().indexOf("|") >= 0) {
				vals = request.getMsg().split("\\|");
			}			
			params.put("agentID", request.getAgentID());
			params.put("tblName", vals[0]); // 예) "tbl_custInfo" 테이블명으로 요청해야 함.
			params.put("svcKey",  vals[1]); // 
			params.put("syncType",  vals[2]); // 
			params.put("chkTime", Integer.parseInt((MST_CHKTIME==null?"5":MST_CHKTIME)));// 예) 5분전까지 데이터 조회
			params.put("SQLMODE", this.SQLMODE); 
			if (!params.containsKey("clang")) {
				params.put("clang", this.CLANG);
			}

			grpcResp = commService.getMstInfo(params);
			
			retMsg = SciRIO.RetMsg.newBuilder()
					.setResults(grpcResp.getResults())
					.setErrCode(grpcResp.getErrCode())
					.setErrMsg(grpcResp.getErrMsg())
					.build();
			
		} catch (Exception e1) {
			logger.error("getMstInfo.onCompleted() TRY CATCH");
			e1.printStackTrace();
			String results = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, e1.getMessage(), null);
			retMsg = SciRIO.RetMsg.newBuilder()
					.setResults(results)
					.setErrCode(ErrConstance.ERR_9999)
					.setErrMsg(e1.getMessage())
					.build();
		} finally {			
			try {
				responseObserver.onNext(retMsg);
				responseObserver.onCompleted();			
			} catch (Exception e1) {
				logger.error("getMstInfo.onCompleted() TRY CATCH");
				e1.printStackTrace();
			}		
			if (params   != null) { try { params   = null; } catch (Exception e1) {} }
			if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }
		}
	}
	
	/**
	 * PIDS-XML 파일 내용을 조회하여 XML로 리턴함.
	 * 
	 */
	@Override
	public void getServiceXml(ReqMsg request, StreamObserver<RetMsg> responseObserver) {
		SciRIO.RetMsg grpcResp = null;
		
		Map<String,String> params = new HashMap<String, String>();
		try {
			logger.info("agentID ::: "+ request.getAgentID());
			logger.info("pidsXml ::: "+ request.getMsg());
			
			params.put("agentID", request.getAgentID());
			params.put("pidsXml", request.getMsg()); // "tsys|sysmgmt" 형식의 여러건일 경우 처리해야 함.
			
			String xmlContent = tSysService.getServiceXml(params);
			
			grpcResp = SciRIO.RetMsg.newBuilder()
					.setResults(xmlContent)
					.setErrCode("0")
					.setErrMsg(ErrConstance.NO_ERROR)
					.build();			
		} catch (Exception e1) {
			logger.error("getServiceXml.onCompleted() TRY CATCH");
			e1.printStackTrace();
			String results = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, e1.getMessage(), null);
			grpcResp = SciRIO.RetMsg.newBuilder()
					.setResults(results)
					.setErrCode(ErrConstance.ERR_9999)
					.setErrMsg(e1.getMessage())
					.build();
		} finally {			
			try {
				responseObserver.onNext(grpcResp);
				responseObserver.onCompleted();			
			} catch (Exception e1) {
				logger.error("getServiceXml.onCompleted() TRY CATCH");
				e1.printStackTrace();
			}		
			if (params   != null) { try { params   = null; } catch (Exception e1) {} }
			if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }
		}
	}
	
	/**
	 * tbl_attach 내용을 업데이트함.
	 * 
	 * java.lang.ClassCastException: com.sci4s.grpc.SciRIO$ReqMsg cannot be cast to com.sci4s.grpc.SciRIO$Data
	 * 
	 */
	@Override
	public void updTblAttach4DocNO(ReqMsg request, StreamObserver<RetMsg> responseObserver) {
		SciRIO.RetMsg grpcResp = null;
		
		Map<String,String> paramMap = new HashMap<String, String>();
		String msg = request.getMsg();
		try {
			logger.info("request.getMsg() ::: "+ request.getMsg());
			logger.info("msg ::: "+ msg);// "attachID|docNO|donGB" 형식의 여러건일 경우 처리해야 함.
			
			String[] params = msg.split("\\|");
			
			paramMap.put("attachID", params[0]);
			paramMap.put("docNO",    params[1]);
			paramMap.put("docGB",    params[2]);
			String xmlContent = tSysService.updTblAttach4DocNO(paramMap);
			
			grpcResp = SciRIO.RetMsg.newBuilder()
					.setResults(xmlContent)
					.setErrCode("0")
					.setErrMsg(ErrConstance.NO_ERROR)
					.build();	
		} catch (Exception e1) {
			logger.error("updTblAttach4DocNO.onCompleted() TRY CATCH");
			e1.printStackTrace();
			String results = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, e1.getMessage(), null);
			grpcResp = SciRIO.RetMsg.newBuilder()
					.setResults(results)
					.setErrCode(ErrConstance.ERR_9999)
					.setErrMsg(e1.getMessage())
					.build();
		} finally {			
			try {
				responseObserver.onNext(grpcResp);
				responseObserver.onCompleted();			
			} catch (Exception e1) {
				logger.error("updTblAttach4DocNO.onCompleted() TRY CATCH");
				e1.printStackTrace();
			}		
			if (paramMap != null) { try { paramMap   = null; } catch (Exception e1) {} }
			if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }
		}
	}
}
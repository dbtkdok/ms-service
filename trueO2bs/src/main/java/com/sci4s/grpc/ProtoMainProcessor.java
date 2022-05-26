package com.sci4s.grpc;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sci4s.grpc.SciRIO.ReqMsg;
import com.sci4s.grpc.SciRIO.RetMsg;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.svc.MainProcessor;
import com.sci4s.grpc.utils.GrpcDataUtil;

import io.grpc.BindableService;
import io.grpc.stub.StreamObserver;

@Service
public class ProtoMainProcessor extends MsaApiGrpc.MsaApiImplBase implements BindableService {
	
	Logger logger = LoggerFactory.getLogger(ProtoMainProcessor.class);
	
	private MainProcessor mainProcessor;
	
	@Autowired
	public  ProtoMainProcessor(MainProcessor mainProcessor){
	    this.mainProcessor = mainProcessor;
	}
	
	@Override
	public void callRMsg(SciRIO.Data request, StreamObserver<SciRIO.RetMsg> responseObserver) {
		GrpcParams  grpcPrms = null;
		SciRIO.RetMsg retMsg = null;
		GrpcResp grpcResp = null;
		try {
			grpcPrms = GrpcDataUtil.parseGrpcData(request);
			
			grpcResp = mainProcessor.callRMsg(grpcPrms);
			
			retMsg = SciRIO.RetMsg.newBuilder()
						.setResults(grpcResp.getResults())
						.setErrCode(grpcResp.getErrCode())
						.setErrMsg(grpcResp.getErrMsg())
						.build();
			
			responseObserver.onNext(retMsg);
			responseObserver.onCompleted();	
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (grpcPrms != null) { try { grpcPrms = null; } catch (Exception e1) {} }
			if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }
			if (retMsg != null) { try { retMsg = null; } catch (Exception e1) {} }
		}
	}
	
	/**
	 * 서비스별 Master 데이터 싱크 서비스
	 * 
	 */
	@Override
	public void getMstInfo(ReqMsg request, StreamObserver<RetMsg> responseObserver) {
		SciRIO.RetMsg retMsg = null;
		GrpcResp grpcResp = null;
		String jsonData = null;		
		Map<String,Object> params = new HashMap<String, Object>();
		logger.info("agentID ::: "+ request.getAgentID());
		logger.info("tblName ::: "+ request.getMsg());
		String[] vals = null;
		if (request.getMsg().indexOf("|") >= 0) {
			vals = request.getMsg().split("\\|");
		}			
		params.put("agentID", request.getAgentID());
		params.put("tblName", vals[0]); // 예) "tbl_custInfo" 테이블명으로 요청해야 함.
		params.put("svcKey",  vals[1]); // 
		
		grpcResp = mainProcessor.getMstInfo(params);
		
		retMsg = SciRIO.RetMsg.newBuilder()
				.setResults(grpcResp.getResults())
				.setErrCode(grpcResp.getErrCode())
				.setErrMsg(grpcResp.getErrMsg())
				.build();
		
		responseObserver.onNext(retMsg);
		responseObserver.onCompleted();	
	}
}
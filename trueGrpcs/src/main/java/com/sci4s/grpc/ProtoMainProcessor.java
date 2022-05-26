package com.sci4s.grpc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sci4s.err.NotFoundBeanException;
import com.sci4s.grpc.SciRIO.ReqMsg;
import com.sci4s.grpc.SciRIO.RetMsg;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.svc.MainProcessorImpl;
import com.sci4s.grpc.svc.TopMainProcessor;

import io.grpc.stub.StreamObserver;

@Service
public class ProtoMainProcessor extends TopProtoMainProcessor {
	
//	Logger logger = LoggerFactory.getLogger(ProtoMainProcessor.class);
	
	private MainProcessorImpl mainProcessor;
	
	@Autowired
	public  ProtoMainProcessor(MainProcessorImpl mainProcessor){
	    this.mainProcessor = mainProcessor;
	}
	@Override
	protected TopMainProcessor getMainProcessor() throws NotFoundBeanException { return this.mainProcessor; }
	
	/**
	 * 신규 고객협력사 정보 연계함.
	 * 파라미터 : uuID|custVdID|custID|userUID|agentID|vdUID
	 */
	@Override
	public void insNewCustVdInfo(ReqMsg request, StreamObserver<RetMsg> responseObserver) {
		SciRIO.RetMsg retMsg = null;
		GrpcResp grpcResp = null;
		Map<String,Object> paramMap = new HashMap<String,Object>();
		logger.debug("insNewCustVdInfo.msg() ::: "+ request.getMsg());
		
		String[] vals = null;
		if (request.getMsg().indexOf("|") >= 0) {
			vals = request.getMsg().split("\\|");
		}			
		paramMap.put("uuID",     vals[0]);
		paramMap.put("custVdID", vals[1]);
		paramMap.put("custID",   vals[2]);
		paramMap.put("userUID",  vals[3]);
		paramMap.put("agentID",  vals[4]);
		paramMap.put("vdUID",    vals[5]);		
		
		grpcResp = mainProcessor.insNewCustVdInfo(paramMap);
		
		retMsg = SciRIO.RetMsg.newBuilder()
				.setResults(grpcResp.getResults())
				.setErrCode(grpcResp.getErrCode())
				.setErrMsg(grpcResp.getErrMsg())
				.build();
		
		responseObserver.onNext(retMsg);
		responseObserver.onCompleted();	
	}
	/**
	 * 기존 결재상신 정보를 백업하고 삭제함.
	 * 파라미터 : agentID|userUID|uuID|apvUID
	 */
	@Override
	public void delApvInfo4ApvUID(ReqMsg request, StreamObserver<RetMsg> responseObserver) {
		SciRIO.RetMsg retMsg = null;
		GrpcResp grpcResp = null;
		Map<String,Object> paramMap = new HashMap<String,Object>();
		
		logger.debug("delApvInfo4ApvUID.msg() ::: "+ request.getMsg());
		
		String[] vals = null;
		if (request.getMsg().indexOf("|") >= 0) {
			vals = request.getMsg().split("\\|");
		}		
		
		paramMap.put("agentID", vals[0]);
		paramMap.put("userUID", vals[1]);
		paramMap.put("uuID",    vals[2]);
		paramMap.put("apvUID",  vals[3]);	
		
		grpcResp = mainProcessor.delApvInfo4ApvUID(paramMap);
		
		retMsg = SciRIO.RetMsg.newBuilder()
				.setResults(grpcResp.getResults())
				.setErrCode(grpcResp.getErrCode())
				.setErrMsg(grpcResp.getErrMsg())
				.build();
		
		responseObserver.onNext(retMsg);
		responseObserver.onCompleted();	
	}
}
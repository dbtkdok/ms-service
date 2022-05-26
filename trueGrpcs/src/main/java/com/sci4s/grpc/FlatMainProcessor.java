package com.sci4s.grpc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.flatbuffers.FlatBufferBuilder;
import com.sci4s.err.NotFoundBeanException;
import com.sci4s.fbs.ReqMsg;
import com.sci4s.fbs.RetMsg;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.svc.MainProcessorImpl;
import com.sci4s.grpc.svc.TopMainProcessor;
import com.sci4s.msa.hr.svc.LoginService;

import io.grpc.stub.StreamObserver;

@Service
public class FlatMainProcessor extends TopFlatMainProcessor {
	
//	Logger logger = LoggerFactory.getLogger(FlatMainProcessor.class);
	
	@Value("${default.lang}")
	String CLANG;
	
	private MainProcessorImpl mainProcessor;	
	private LoginService loginService;
	
	@Autowired
	public  FlatMainProcessor(MainProcessorImpl mainProcessor
			, LoginService loginService){
	    this.mainProcessor = mainProcessor;
	    this.loginService  = loginService;
	}
	@Override
	protected TopMainProcessor getMainProcessor() throws NotFoundBeanException { return this.mainProcessor; }
	
	/**
	 * 신규 고객협력사 정보 연계함.
	 * 파라미터 : uuID|custVdID|custID|userUID|agentID|vdUID
	 */
	@Override
	public void insNewCustVdInfo(ReqMsg request, StreamObserver<RetMsg> responseObserver) {
		RetMsg retMsg = null;
		GrpcResp grpcResp = null;
		FlatBufferBuilder builder = new FlatBufferBuilder();
		Map<String,Object> paramMap = new HashMap<String,Object>();
		logger.debug("insNewCustVdInfo.msg() ::: "+ request.msg());
		
		String[] vals = null;
		if (request.msg().indexOf("|") >= 0) {
			vals = request.msg().split("\\|");
		}			
		paramMap.put("uuID",     vals[0]);
		paramMap.put("custVdID", vals[1]);
		paramMap.put("custID",   vals[2]);
		paramMap.put("userUID",  vals[3]);
		paramMap.put("agentID",  vals[4]);
		paramMap.put("vdUID",    vals[5]);
		
		grpcResp = mainProcessor.insNewCustVdInfo(paramMap);
		
		int dataOffset = RetMsg.createRetMsg(builder
				, builder.createString(grpcResp.getErrCode())
				, builder.createString(grpcResp.getErrMsg())
				, builder.createString(grpcResp.getResults()));
		
		builder.finish(dataOffset); 
		
		retMsg =  RetMsg.getRootAsRetMsg(builder.dataBuffer());
		
		responseObserver.onNext(retMsg);
		responseObserver.onCompleted();			

		if (paramMap   != null) { try { paramMap   = null; } catch (Exception e1) {} }
		if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }	
		if (retMsg != null) { try { retMsg = null; } catch (Exception e1) {} }
			
	}
	/**
	 * 기존 결재상신 정보를 백업하고 삭제함.
	 * 파라미터 : agentID|userUID|uuID|apvUID
	 */
	@Override
	public void delApvInfo4ApvUID(ReqMsg request, StreamObserver<RetMsg> responseObserver) {
		RetMsg retMsg = null;
		GrpcResp grpcResp = null;
		FlatBufferBuilder builder = new FlatBufferBuilder();
		Map<String,Object> paramMap = new HashMap<String,Object>();
		
		logger.debug("delApvInfo4ApvUID.msg() ::: "+ request.msg());
		
		String[] vals = null;
		if (request.msg().indexOf("|") >= 0) {
			vals = request.msg().split("\\|");
		}			
		paramMap.put("agentID", vals[0]);
		paramMap.put("userUID", vals[1]);
		paramMap.put("uuID",    vals[2]);
		paramMap.put("apvUID",  vals[3]);	
		
		grpcResp = mainProcessor.delApvInfo4ApvUID(paramMap);
		
		int dataOffset = RetMsg.createRetMsg(builder
				, builder.createString(grpcResp.getErrCode())
				, builder.createString(grpcResp.getErrMsg())
				, builder.createString(grpcResp.getResults()));
		
		builder.finish(dataOffset); 
		
		retMsg =  RetMsg.getRootAsRetMsg(builder.dataBuffer());
		
		responseObserver.onNext(retMsg);
		responseObserver.onCompleted();			

		if (paramMap   != null) { try { paramMap   = null; } catch (Exception e1) {} }
		if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }	
		if (retMsg != null) { try { retMsg = null; } catch (Exception e1) {} }
	}
}

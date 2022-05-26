package com.sci4s.grpc;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.flatbuffers.FlatBufferBuilder;
import com.sci4s.fbs.Data;
import com.sci4s.fbs.FlatJsonGrpc;
import com.sci4s.fbs.ReqMsg;
import com.sci4s.fbs.RetMsg;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.svc.MainProcessor;
import com.sci4s.grpc.utils.FlatDataUtil;

import io.grpc.BindableService;
import io.grpc.stub.StreamObserver;

@Service
public class FlatMainProcessor extends FlatJsonGrpc.FlatJsonImplBase implements BindableService {
	
	Logger logger = LoggerFactory.getLogger(FlatMainProcessor.class);
	
	@Value("${default.lang}")
	String CLANG;
	
	private MainProcessor mainProcessor;
	
	@Autowired
	public  FlatMainProcessor(MainProcessor mainProcessor){
	    this.mainProcessor = mainProcessor;
	}
	
	@Override
	public void callRMsg(Data request, StreamObserver<RetMsg> responseObserver) {
		GrpcParams  grpcPrms = null;
		FlatBufferBuilder builder = new FlatBufferBuilder();
		RetMsg retMsg = null;
		GrpcResp grpcResp = null;
		try {
			grpcPrms = FlatDataUtil.parseGrpcData(request);
			
			grpcResp = mainProcessor.callRMsg(grpcPrms);
			
			int dataOffset = RetMsg.createRetMsg(builder
					, builder.createString(grpcResp.getErrCode())
					, builder.createString(grpcResp.getErrMsg())
					, builder.createString(grpcResp.getResults()));
			
			builder.finish(dataOffset); 
			
			retMsg = RetMsg.getRootAsRetMsg(builder.dataBuffer());
			
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
		RetMsg retMsg = null;
		GrpcResp grpcResp = null;
		FlatBufferBuilder builder = new FlatBufferBuilder();
		Map<String,Object> params = new HashMap<String, Object>();
		
		logger.info("agentID ::: "+ request.agentID());
		logger.info("tblName ::: "+ request.msg());
		String[] vals = null;
		
		if (request.msg().indexOf("|") >= 0) {
			vals = request.msg().split("\\|");
		}			
		params.put("agentID", request.agentID());
		params.put("tblName", vals[0]); // 예) "tbl_custInfo" 테이블명으로 요청해야 함.
		params.put("svcKey",  vals[1]); // 
		params.put("syncType",  vals[2]); // 
		
		grpcResp = mainProcessor.getMstInfo(params);
		
		int dataOffset = RetMsg.createRetMsg(builder
				, builder.createString(grpcResp.getErrCode())
				, builder.createString(grpcResp.getErrMsg())
				, builder.createString(grpcResp.getResults()));
		
		builder.finish(dataOffset); 
		
		retMsg =  RetMsg.getRootAsRetMsg(builder.dataBuffer());
		
		responseObserver.onNext(retMsg);
		responseObserver.onCompleted();			

		if (params   != null) { try { params   = null; } catch (Exception e1) {} }
		if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }
		if (retMsg != null) { try { retMsg = null; } catch (Exception e1) {} }
	}
}

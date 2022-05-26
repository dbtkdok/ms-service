package com.sci4s.grpc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.flatbuffers.FlatBufferBuilder;
import com.sci4s.err.NotFoundBeanException;
import com.sci4s.fbs.Data;
import com.sci4s.fbs.ReqMsg;
import com.sci4s.fbs.RetMsg;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.svc.MainProcessorImpl;
import com.sci4s.grpc.svc.TopMainProcessor;
import com.sci4s.grpc.utils.FlatDataUtil;
import com.sci4s.grpc.utils.GrpcDataUtil;

import io.grpc.stub.StreamObserver;

@Service
public class FlatMainProcessor extends TopFlatMainProcessor {
	
//	Logger logger = LoggerFactory.getLogger(FlatMainProcessor.class);
	
	@Value("${default.lang}")
	String CLANG;
	
	private MainProcessorImpl mainProcessor;
	
	@Autowired
	public  FlatMainProcessor(MainProcessorImpl mainProcessor){
	    this.mainProcessor = mainProcessor;
	}
	@Override
	protected TopMainProcessor getMainProcessor() throws NotFoundBeanException { return this.mainProcessor; }
	
	@Override
	public void getEnableJob(Data request, StreamObserver<RetMsg> responseObserver) {
		FlatBufferBuilder builder = new FlatBufferBuilder();
		RetMsg retMsg = null;
		GrpcResp grpcResp = null;
		GrpcParams  grpcPrms = null;
		Map<String, Object> params = null;
		try {
			grpcPrms = FlatDataUtil.parseGrpcData(request);
			params = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
			
			params.put("pID", request.pID());
			
			grpcResp = mainProcessor.getEnableJob(params);
	
			int dataOffset = RetMsg.createRetMsg(builder
					, builder.createString(grpcResp.getErrCode())
					, builder.createString(grpcResp.getErrMsg())
					, builder.createString(grpcResp.getResults()));
			
			builder.finish(dataOffset); 
			
			retMsg = RetMsg.getRootAsRetMsg(builder.dataBuffer());
			
			responseObserver.onNext(retMsg);
			responseObserver.onCompleted();	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {		
			if (retMsg   != null) { try { retMsg   = null; } catch (Exception e1) {} }
			if (builder  != null) { try { builder  = null; } catch (Exception e1) {} }
			if (params   != null) { try { params   = null; } catch (Exception e1) {} }
			if (grpcPrms != null) { try { grpcPrms = null; } catch (Exception e1) {} }
			if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }
		}
	}

	@Override
	public void getServiceXml(ReqMsg request, StreamObserver<RetMsg> responseObserver) {
		RetMsg retMsg = null;
		GrpcResp grpcResp = null;
		FlatBufferBuilder builder = new FlatBufferBuilder();
		Map<String,String> params = new HashMap<String, String>();
		try {
			logger.info("agentID ::: "+ request.agentID());
			logger.info("pidsXml ::: "+ request.msg());
			
			params.put("agentID", request.agentID());
			params.put("pidsXml", request.msg()); // "tsys|sysmgmt" 형식의 여러건일 경우 처리해야 함.
				
			grpcResp = mainProcessor.getServiceXml(params);
			
			int dataOffset = RetMsg.createRetMsg(builder
					, builder.createString(grpcResp.getErrCode())
					, builder.createString(grpcResp.getErrMsg())
					, builder.createString(grpcResp.getResults()));
			
			builder.finish(dataOffset); 
			
			retMsg =  RetMsg.getRootAsRetMsg(builder.dataBuffer());
				
			responseObserver.onNext(retMsg);
			responseObserver.onCompleted();	
			
		} catch(Exception ex) {
			
		} finally {		
			if (retMsg   != null) { try { retMsg   = null; } catch (Exception e1) {} }
			if (builder  != null) { try { builder  = null; } catch (Exception e1) {} }
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
		RetMsg retMsg = null;
		GrpcResp grpcResp = null;
		FlatBufferBuilder builder = new FlatBufferBuilder();
		Map<String,String> paramMap = new HashMap<String, String>();		
		String msg = request.msg();		
		String[] params = msg.split("\\|");
		try {
			paramMap.put("attachID", params[0]);
			paramMap.put("docNO",    params[1]);
			paramMap.put("docGB",    params[2]);
			
			grpcResp = mainProcessor.updTblAttach4DocNO(paramMap);
			
			int dataOffset = RetMsg.createRetMsg(builder
					, builder.createString(grpcResp.getErrCode())
					, builder.createString(grpcResp.getErrMsg())
					, builder.createString(grpcResp.getResults()));
			
			builder.finish(dataOffset); 
			
			retMsg =  RetMsg.getRootAsRetMsg(builder.dataBuffer());
			
			responseObserver.onNext(retMsg);
			responseObserver.onCompleted();	
		} catch(Exception ex) {
			
		} finally {		
			if (retMsg   != null) { try { retMsg   = null; } catch (Exception e1) {} }
			if (builder  != null) { try { builder  = null; } catch (Exception e1) {} }
			if (paramMap != null) { try { paramMap = null; } catch (Exception e1) {} }
			if (msg      != null) { try { msg      = null; } catch (Exception e1) {} }
			if (params   != null) { try { params   = null; } catch (Exception e1) {} }
			if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }
		}
	}
	
}

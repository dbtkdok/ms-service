package com.sci4s.grpc;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sci4s.err.NotFoundBeanException;
import com.sci4s.grpc.SciRIO.Data;
import com.sci4s.grpc.SciRIO.ReqMsg;
import com.sci4s.grpc.SciRIO.RetMsg;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.svc.MainProcessorImpl;
import com.sci4s.grpc.svc.TopMainProcessor;
import com.sci4s.grpc.utils.GrpcDataUtil;

import io.grpc.stub.StreamObserver;

@Service
public class ProtoMainProcessor extends TopProtoMainProcessor {
	
	private MainProcessorImpl mainProcessor;
	
	@Autowired
	public  ProtoMainProcessor(MainProcessorImpl mainProcessor){
	    this.mainProcessor = mainProcessor;
	}
	@Override
	protected TopMainProcessor getMainProcessor() throws NotFoundBeanException { return this.mainProcessor; }
	
	@Override
	public void getEnableJob(Data request, StreamObserver<RetMsg> responseObserver) {
		GrpcParams  grpcPrms = null;
		GrpcResp grpcResp = null;
		Map<String, Object> params = null;
		try {
			grpcPrms = GrpcDataUtil.parseGrpcData(request);
			params = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
			
			params.put("pID", request.getPID());
			
			System.out.println("params :: " + params);
			grpcResp = mainProcessor.getEnableJob(params);

			SciRIO.RetMsg retMsg = SciRIO.RetMsg.newBuilder()
									.setResults(grpcResp.getResults())
									.setErrCode(grpcResp.getErrCode())
									.setErrMsg(grpcResp.getErrMsg())
									.build();
			
			responseObserver.onNext(retMsg);
			responseObserver.onCompleted();		
		} catch (Exception e) {

		} finally {
			if (grpcPrms != null) { try { grpcPrms = null; } catch (Exception e1) {} }
			if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }
			if (params   != null) { try { params   = null; } catch (Exception e1) {} }
		}
	}
	
	@Override
	public void getServiceXml(ReqMsg request, StreamObserver<RetMsg> responseObserver) {
		GrpcResp grpcResp = null;
		Map<String, String> params = new HashMap<String, String>();
		SciRIO.RetMsg retMsg = null;
		try {
			logger.info("agentID ::: "+ request.getAgentID());
			logger.info("pidsXml ::: "+ request.getMsg());
			
			params.put("agentID", request.getAgentID());
			params.put("pidsXml", request.getMsg()); // "tsys|sysmgmt" 형식의 여러건일 경우 처리해야 함.
				
			grpcResp = mainProcessor.getServiceXml(params);
			
			retMsg = SciRIO.RetMsg.newBuilder()
					.setResults(grpcResp.getResults())
					.setErrCode(grpcResp.getErrCode())
					.setErrMsg(grpcResp.getErrMsg())
					.build();
				
			responseObserver.onNext(retMsg);
			responseObserver.onCompleted();	
		} catch (Exception e) {

		} finally {
			if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }
			if (retMsg   != null) { try { retMsg   = null; } catch (Exception e1) {} }
			if (params   != null) { try { params   = null; } catch (Exception e1) {} }
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
		GrpcResp grpcResp = null;
		Map<String, String> paramMap = new HashMap<String, String>();
		SciRIO.RetMsg retMsg = null;
		String[] params = null;
		try {
			String msg = request.getMsg();
			
			params = msg.split("\\|");
			paramMap.put("attachID", params[0]);
			paramMap.put("docNO",    params[1]);
			paramMap.put("docGB",    params[2]);
			
			grpcResp = mainProcessor.getServiceXml(paramMap);
			
			retMsg = SciRIO.RetMsg.newBuilder()
					.setResults(grpcResp.getResults())
					.setErrCode(grpcResp.getErrCode())
					.setErrMsg(grpcResp.getErrMsg())
					.build();
			
			responseObserver.onNext(retMsg);
			responseObserver.onCompleted();	
		} catch (Exception e) {

		} finally {
			if (grpcResp != null) { try { grpcResp = null; } catch (Exception e1) {} }
			if (retMsg   != null) { try { retMsg   = null; } catch (Exception e1) {} }
			if (params   != null) { try { params   = null; } catch (Exception e1) {} }
			if (paramMap != null) { try { paramMap = null; } catch (Exception e1) {} }
		}
	}
	
}
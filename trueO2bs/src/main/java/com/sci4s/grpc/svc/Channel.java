package com.sci4s.grpc.svc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sci4s.fbs.Data;
import com.sci4s.fbs.FlatJsonGrpc;
import com.sci4s.fbs.RetMsg;
import com.sci4s.grpc.MsaApiGrpc;
import com.sci4s.grpc.SciRIO;
import com.sci4s.grpc.dto.GrpcParams;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

@Service
public class Channel {
	
	private Logger logger = LoggerFactory.getLogger(Channel.class);
	
	private ManagedChannel channel  = null;
	private long TIME_OUT = 5000;
	
	/**
	 * GRPC 채널 오픈
	 * 
	 * @return ManagedChannel
	 */
	public ManagedChannel openChannel(String chnl) throws Exception {
		String GRPC_URI = chnl;		
		logger.info("Channel["+ GRPC_URI +"]");
		try {
			this.channel = ManagedChannelBuilder.forTarget(GRPC_URI).usePlaintext().maxInboundMessageSize(2147483647).build();
			return this.channel;
		} catch (Exception e){
		    e.printStackTrace();
		    throw e;
		}
	}
	/**
	 * GRPC 채널 종료
	 * 
	 */
	public void closeChannel() throws Exception {
		try {
			channel.shutdown();
		    if(!channel.awaitTermination(this.TIME_OUT, TimeUnit.MICROSECONDS)) {
		    	channel.shutdownNow();
		    }
		} catch (InterruptedException e){
		    e.printStackTrace();
		    throw e;
		}
	}
	
	public Object callRPC(GrpcParams gprms, String type ,String resp) throws Exception {		
		if ("PROTO".equals(type)) {
			return callRPCProto(gprms, resp);
		} else {		
			return callRPCFlat(gprms, resp);
		}
	}
	public Object callRPCFlat(GrpcParams gprms, String resp) throws Exception {
		FlatBufferBuilder builder = new FlatBufferBuilder();
		try {			
			logger.debug("GrpcChannel.callRPCFlat RPC Method :::: "+ gprms.getMethodNM());
			int dataOffset = Data.createData(builder
					, builder.createString("" + gprms.getpID())
					, builder.createString("" + gprms.getAgentID())
					, builder.createString("" + gprms.getCsKey())
					, builder.createString("" + gprms.getUserIP())
					, builder.createString("" + gprms.getServerIP())
					, builder.createString("" + gprms.getUserUID())
					, builder.createString("" + gprms.getBorgUID())
					, builder.createString("" + (gprms.getClang()==null?"KR":gprms.getClang()))
					, builder.createString("" + gprms.getData())
					, builder.createString("0")
					, builder.createString(""));
		
			builder.finish(dataOffset);        
		    Data request = Data.getRootAsData(builder.dataBuffer());
		    
		    logger.debug("callRPCFlat.io.grpc.ConnectivityState ::::::::: "+ this.channel.getState(true));
		    FlatJsonGrpc.FlatJsonBlockingStub stub = FlatJsonGrpc.newBlockingStub(this.channel);
			//logger.debug("FLATConnector.stub.callRMsg -> gprms.getData() :::: "+ gprms.getData());
			RetMsg response = stub.callRMsg(request);
			if (response.results() != null && response.results().length() > 0) {
				if ("getResponse".equals(resp)) {
					return response;
				} else if ("getResults".equals(resp)) {
					return response.results();
				} else {
					return this.getData(response.results(), gprms.getType(), response.errCode(), response.errMsg());
				}	
			} else {
				return null;
			}
		} catch(StatusRuntimeException ex) {
			logger.error("%%%%%%%%%%GrpcChannel.callRPCFlat.StatusRuntimeException.S%%%%%%%%%%%");
			ex.printStackTrace();
			logger.error("%%%%%%%%%%GrpcChannel.callRPCFlat.StatusRuntimeException.E%%%%%%%%%%%");
			throw ex;
		} catch(Exception ex) {
			logger.error("%%%%%%%%%%GrpcChannel.callRPCFlat.Exception.S%%%%%%%%%%%");
			ex.printStackTrace();
			logger.error("%%%%%%%%%%GrpcChannel.callRPCFlat.Exception.E%%%%%%%%%%%");
			throw ex;
		}
	}
	
	public Object callRPCProto(GrpcParams gprms, String resp) throws Exception {
		try {
			logger.debug("GrpcChannel.callRPCProto RPC Method :::: "+ gprms.getMethodNM());
			SciRIO.Data request = SciRIO.Data.newBuilder()
				.setPID(gprms.getpID())
				.setData(gprms.getData())
				.setCsKey(gprms.getCsKey())
				.setUserIP(gprms.getUserIP())
				.setServerIP(gprms.getServerIP())
				.setUserUID(gprms.getUserUID())
				.setBorgUID(gprms.getBorgUID())
				.setAgentID(gprms.getAgentID())
				.setClang("" + (gprms.getClang()==null?"KR":gprms.getClang()))
				.build();
		
			logger.debug("callRPCProto.io.grpc.ConnectivityState ::::::::: "+ this.channel.getState(true));
			MsaApiGrpc.MsaApiBlockingStub stub = MsaApiGrpc.newBlockingStub(this.channel);
			
			//logger.debug("PROTOConnector.stub.callRMsg -> gprms.getData() :::: "+ gprms.getData());
			SciRIO.RetMsg response = stub.callRMsg(request);
			if (response.getResults() != null && response.getResults().length() > 0) {
				if ("getResponse".equals(resp)) {
					return response;
				} else if ("getResults".equals(resp)) {
					return response.getResults();
				} else {
					return this.getData(response.getResults(), gprms.getType(), response.getErrCode(), response.getErrMsg());
				}
			} else {
				return null;
			}		
		} catch(StatusRuntimeException ex) {
			logger.error("%%%%%%%%%%GrpcChannel.callRPCProto.StatusRuntimeException.S%%%%%%%%%%%");
			ex.printStackTrace();
			logger.error("%%%%%%%%%%GrpcChannel.callRPCProto.StatusRuntimeException.E%%%%%%%%%%%");
			throw ex;
		} catch(Exception ex) {
			logger.error("%%%%%%%%%%GrpcChannel.callRPCProto.Exception.S%%%%%%%%%%%");
			ex.printStackTrace();
			logger.error("%%%%%%%%%%GrpcChannel.callRPCProto.Exception.E%%%%%%%%%%%");
			throw ex;
		}
	}
	
	protected Object getData(String retData, String type, String errCd, String errMsg) throws Exception {
		Object retObj  = null;
		try {
			logger.debug("GrpcChannel."+ type +" :::: "+ errCd +"["+ errMsg +"]");
			//logger.debug(type +"->errCode :::: "+ errCd);
			//logger.debug(type +"->retData :::: "+ retData);			
			if ("LIST".equals(type.toUpperCase())) {	
		        JSONArray jsonArr = (JSONArray)((JSONObject)new JSONParser().parse(retData)).get("results");
		        //logger.debug("jsonArr.toJSONString() :::: "+ jsonArr.toJSONString());
		        
		        List<Map<String, Object>> retList = null;		        
	        	if (jsonArr != null && jsonArr.size() > 0) {	
//	        		logger.debug("jsonArr.toJSONString() :::: "+ jsonArr.toJSONString());
	        		ObjectMapper objMapper = new ObjectMapper();
	        		retList = objMapper.readValue(jsonArr.toJSONString(), objMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
	        	} else {
	        		retList = new ArrayList<Map<String, Object>>();
	        	}
	        	//logger.debug(type +"->retList.get(0) :::: "+ retList.get(0));
	        	//logger.debug(type +"->retList.get(1) :::: "+ retList.get(1));
		        return retList;
			} else if ("GRID".equals(type.toUpperCase())) {	
				//logger.debug(type +"->errCode :::: "+ errCd);
				//logger.debug(type +"->retData :::: "+ retData);
				if (retData == null) {// 시스템 오류
					return this.getErrMsg(errCd, errMsg);
				} else {
					return retData;
				}
			} else if ("JSON".equals(type.toUpperCase())) {
				//logger.debug(type +"->retData :::: "+ retData);
				if (retData == null) {// 시스템 오류
					return this.getErrMsg(errCd, errMsg);
				} else {
					return retData;
				}
			} else if ("BEAN".equals(type.toUpperCase())) {
				return retObj;
			} else {
				return null;
			}
		} catch(Exception ex) {
			logger.error("%%%%%%%%%%GrpcChannel.getData.S%%%%%%%%%%%");
			ex.printStackTrace();
			logger.error("%%%%%%%%%%GrpcChannel.getData.E%%%%%%%%%%%");
			throw ex;
		}
	}
	
	protected String getErrMsg(String errCd, String errMsg) throws Exception {
		if (errCd != null) {
			if ("0".equals(errCd) || "NO_DATA".equals(errCd)) {
				return "{errCode:\"NO_DATA\",errMsg:\"Not found\"}";
			} else {
				return "{errCode:\"ERR_9999\",errMsg:\""+ errMsg +"\"}";
			}
		} else {					
			return "{errCode:\"NO_DATA\",errMsg:\"Not found\"}";
		}
	}
}

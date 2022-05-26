package com.sci4s.grpc.aop;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.flatbuffers.FlatBufferBuilder;
import com.sci4s.fbs.Login;
import com.sci4s.grpc.dto.KafkaMsg;
import com.sci4s.grpc.svc.TrueKafkaProducer;
import com.sci4s.utils.AES256Util;
import com.sci4s.utils.DateUtil;

import io.grpc.Context;
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

@Service
public class TrueApiInteceptor implements ServerInterceptor {	
	
	private Logger logger = LoggerFactory.getLogger(TrueApiInteceptor.class);
	
	private TrueKafkaProducer kafkaProducer;
	
	@Value("${kafka.log}")
	private String KAFKA_LOG;
	
	@Autowired
	public TrueApiInteceptor(TrueKafkaProducer kafkaProducer){
	    this.kafkaProducer = kafkaProducer;
	}
	
	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
			ServerCallHandler<ReqT, RespT> next) {
		
		String callName = call.getMethodDescriptor().getFullMethodName();
		
		//System.out.println("서버 인터셉터 1 실행, 토큰 가져 오기");
		logger.info("++++++++++++++++++++++++++++++++");
		logger.info("[$$$$$$$-INTERCEPTOR START-$$$$$]");
		logger.info("++++++++++++++++++++++++++++++++");
		logger.info("callName  ::: "+ callName);
		logger.info("call      ::: "+ call);
		logger.info("call.getAttributes() ::: "+ call.getAttributes());
		logger.info("headers   ::: "+ headers);
		logger.info("next      ::: "+ next);
		logger.info("KAFKA_LOG ::: "+ KAFKA_LOG);
		
//		if(callName.indexOf("Flat") > 0) {
//			return flatApiInteceptor.interceptCall(call, headers, next);
//		} else {
//			return protoApiInteceptor.interceptCall(call, headers, next);
//		}
		ServerCall.Listener listener = next.startCall(call, headers);
		return new SimpleForwardingServerCallListener<ReqT>(listener) {
	    	
	    	private String PID = null;
	    	private KafkaMsg kafkaMsg = null;
	    	private long startTime = 0; 
	    	
	        @Override
			public void onComplete() {
                final Context previous = Context.current().attach();  
                long runedTime = System.currentTimeMillis();
                try {
                	super.onComplete();
                } catch (Exception ex) {
                	ex.printStackTrace();
                } finally {
                	Context.current().detach(previous);
                	try {
	                	//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@			
	        			// PID 처리 후 카프카 Topic를 카프카로 전송한다.
	        			if (kafkaMsg != null && Boolean.parseBoolean(KAFKA_LOG)) {
	                    	String endTime = DateUtil.getDateFormat(new Date(), "yyyy-MM-dd HH:mm:ss");
	
	                    	kafkaMsg.setErrCode("0");
	                    	kafkaMsg.setErrMsg("SUCCESS");
	                    	kafkaMsg.setEndTime(endTime);
	                    	kafkaMsg.setRunTime(runedTime); 
	                    	
	                    	//kafkaProducer.send(kafkaMsg);
	                    	
	                    	logger.info("kafkaMsg.getStartTime() ::::: "+ kafkaMsg.getStartTime());
	                    	logger.info("kafkaMsg.getAgentID()   ::::: "+ kafkaMsg.getAgentID());
	                    	logger.info("kafkaMsg.getpID()       ::::: "+ kafkaMsg.getpID());
	            			logger.info("kafkaMsg.getSqlID()     ::::: "+ kafkaMsg.getSqlID());
	            			logger.info("kafkaMsg.getEndTime     ::::: "+ kafkaMsg.getEndTime());  
	        			}
                	} catch (Exception ex) {
                		ex.printStackTrace();
                	}
                	if (kafkaMsg != null) {
                		kafkaMsg = null;
                	}
        			//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        			// PID 처리 시간을 출력한다
        			logger.info("++++++++++++++++++++++++++++++++");
        			logger.info("["+ this.PID +"]실행소요시간 ::: " + (runedTime-this.startTime) +"ms");
        			logger.info("++++++++++++++++++++++++++++++++");
                	logger.info("[$$$$$$$-INTERCEPTOR END-$$$$$]");
                	this.PID = null;
                	this.startTime = 0;
                }
			}

			@Override 
	        public void onMessage(ReqT req) {
				this.startTime = System.currentTimeMillis();
				String beginTime = DateUtil.getDateFormat(new Date(), "yyyy-MM-dd HH:mm:ss");
				
				Map<String,Object> dataMap = null;
				String agentID  = null;
	        	String userUID  = null;
	        	String borgUID  = null;
	        	String csKey    = null;
	        	String serverIP = null;
	        	String userIP   = null;
	        	
	        	this.kafkaMsg   = new KafkaMsg();
	        	
	        	if ("com.sci4s.grpc.MsaApi/getMstInfo".equals(callName) || "com.sci4s.fbs.FlatJson/getMstInfo".equals(callName)) {
	        		this.PID = "getMstInfo";
	        		super.onMessage(req);
	        	} else if ("com.sci4s.grpc.MsaApi/insNewCustVdInfo".equals(callName) || "com.sci4s.fbs.FlatJson/insNewCustVdInfo".equals(callName)) {
	        		this.PID = "insNewCustVdInfo";
	        		super.onMessage(req);
	        	} else if ("com.sci4s.grpc.MsaApi/delApvInfo4ApvUID".equals(callName) || "com.sci4s.fbs.FlatJson/delApvInfo4ApvUID".equals(callName)) {
	        		this.PID = "delApvInfo4ApvUID";
	        		super.onMessage(req);
	        	} else if ("com.sci4s.grpc.MsaHR/loginProcess".equals(callName) || "com.sci4s.fbs.hr.FlatJson/loginProcess".equals(callName)) {
	        		
	        		AES256Util aes256  = null;
	        		String[] loginData = null;
	        		String loginKey    = null;
	            	String desKey      = null;
	        		try {
	        			// loginKey=julianDate|PID|agentID|custID|loginID|userPwd|LOGIN|userIP|serverIP
	        			aes256 = new AES256Util();
	        			
	        			if("com.sci4s.fbs.hr.FlatJson/loginProcess".equals(callName)) {
	        				loginKey = ((com.sci4s.fbs.hr.Login)req).loginKey();
	            		} else {
	            			loginKey = ((com.sci4s.grpc.HRGIO.Login)req).getLoginKey();
	            		}
	        			
		            	desKey   = aes256.decryptII(loginKey);
		            	csKey    = desKey;
		            	logger.info("csKey ::::: "+ csKey);
		            	
		            	if (desKey.indexOf("|") >= 0) {
		            		
		            		loginData = desKey.split("[|]");
		            		this.PID  = loginData[1];
		            		agentID   = loginData[2];        		      		
		            		userIP    = loginData[7];
		            		serverIP  = loginData[8];
		            		
		            		borgUID = loginData[3];
		            		userUID = loginData[4];

		            		kafkaMsg.setStartTime(beginTime);
				        	kafkaMsg.setUserUID(userUID);
				        	kafkaMsg.setAgentID(agentID);
				        	kafkaMsg.setBorgUID(borgUID);
				        	kafkaMsg.setCsKey(csKey);	        	
				        	kafkaMsg.setpID(this.PID);
				        	kafkaMsg.setServerIP(serverIP);
				        	kafkaMsg.setUserIP(userIP);	
		            		
		            		if("com.sci4s.fbs.hr.FlatJson/loginProcess".equals(callName)) {
		            			FlatBufferBuilder builder = new FlatBufferBuilder();
		            			int dataOffset = Login.createLogin(builder, builder.createString("" + desKey));
					        	builder.finish(dataOffset);        
					        	
					        	com.sci4s.fbs.hr.Login login = com.sci4s.fbs.hr.Login.getRootAsLogin(builder.dataBuffer());
					        	
					        	super.onMessage((ReqT)login);
		            		} else {
		            			com.sci4s.grpc.HRGIO.Login.Builder loginBuilder = com.sci4s.grpc.HRGIO.Login.newBuilder();
		            			
		            			loginBuilder.setLoginKey(desKey);
		            			
		            			com.sci4s.grpc.HRGIO.Login login = loginBuilder.build();
		            			
		            			super.onMessage((ReqT)login);
		            		}
		            	} else {
		            		super.onMessage(req);
		            	}
	        		} catch (Exception e0) {
	        			e0.printStackTrace();
	        		} finally {
	        			try { loginKey  = null; } catch (Exception ex) { }
	        			try { desKey    = null; } catch (Exception ex) { }
	        			try { loginData = null; } catch (Exception ex) { }
	        			try { aes256    = null; } catch (Exception ex) { }
	        		}
	        	} else {

	        		if(callName.indexOf("com.sci4s.fbs") >= 0) {
	        			this.PID = ((com.sci4s.fbs.Data) req).pID();
	        			csKey    = ((com.sci4s.fbs.Data) req).csKey();
			        	serverIP = ((com.sci4s.fbs.Data) req).serverIP();
			        	userIP   = ((com.sci4s.fbs.Data) req).userIP();      	
			        	agentID  = ((com.sci4s.fbs.Data) req).agentID();
			        	userUID  = ((com.sci4s.fbs.Data) req).userUID();
			        	borgUID  = ((com.sci4s.fbs.Data) req).borgUID();
	        		} else {
	        			this.PID = ((com.sci4s.grpc.SciRIO.Data) req).getPID();
	        			csKey    = ((com.sci4s.grpc.SciRIO.Data) req).getCsKey();
			        	serverIP = ((com.sci4s.grpc.SciRIO.Data) req).getServerIP();
			        	userIP   = ((com.sci4s.grpc.SciRIO.Data) req).getUserIP();      	
			        	agentID  = ((com.sci4s.grpc.SciRIO.Data) req).getAgentID();
			        	userUID  = ((com.sci4s.grpc.SciRIO.Data) req).getUserUID();
			        	borgUID  = ((com.sci4s.grpc.SciRIO.Data) req).getBorgUID();
	        		}
	        		
		        	kafkaMsg.setStartTime(beginTime);
		        	kafkaMsg.setUserUID(userUID);
		        	kafkaMsg.setAgentID(agentID);
		        	kafkaMsg.setBorgUID(borgUID);
		        	kafkaMsg.setCsKey(csKey);	        	
		        	kafkaMsg.setpID(this.PID);
		        	kafkaMsg.setServerIP(serverIP);
		        	kafkaMsg.setUserIP(userIP);	 
	        	
		        	super.onMessage(req);
	        	}
	        }
	    };	
		
	}
}
package com.sci4s.grpc;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.sci4s.grpc.aop.TrueApiInteceptor;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

@Component
public class GrpcRunner implements ApplicationRunner {
	
	public static Logger logger = LoggerFactory.getLogger(GrpcRunner.class);
	
	@Value("${msa.cls}")
	String MSA_CLS;
	
	@Value("${msa.port}")
	int MSA_PORT;
	
	@Value("${msa.id}")
	String MSA_ID;
	
	@Value("${buffer.type}")
	String BUFFER_TYPE;
	
	private int GRPC_MAX_SIZE = ErrConstance.GRPC_MAX_SIZE;
	
	private Server server;   
	@Autowired
	TrueApiInteceptor apiInteceptor;
	ProtoMainProcessor mainProcessor;
	FlatMainProcessor flatProcessor;
	
	@Autowired
	public GrpcRunner(TrueApiInteceptor apiInteceptor
			, ProtoMainProcessor mainProcessor
			, FlatMainProcessor flatProcessor){
	    this.apiInteceptor = apiInteceptor;
	    this.mainProcessor = mainProcessor;
	    this.flatProcessor = flatProcessor;
	}
 
    @Override
    public void run(ApplicationArguments args) throws Exception {
    	TimeZone.setDefault(TimeZone.getTimeZone("UTC"));    	
    	
        try {
        	//PIDSInfo appProps = PIDSInfo.getInstance();
        	
        	//String MSA_CLS = appProps.getProperty("msa.cls");
        	logger.info("MSA_CLS :::::::::: "+ MSA_CLS);        	
    		//int MSA_PORT   = Integer.parseInt(appProps.getProperty("msa.port"));
    		logger.info("MSA_PORT :::::::::: "+ MSA_PORT);
    		logger.info("BUFFER_TYPE ::::::: "+ BUFFER_TYPE);

    		if("FLAT".equals(BUFFER_TYPE)) {
    			this.server = ServerBuilder.forPort(MSA_PORT)
    	        		.addService((BindableService) flatProcessor)
    	        		.intercept(apiInteceptor)
    	        		.directExecutor()
    	        		.maxInboundMessageSize(GRPC_MAX_SIZE)
    	        		.build().start();
    		}else {
    			this.server = ServerBuilder.forPort(MSA_PORT)
        				.addService((BindableService) mainProcessor)
        				.intercept(apiInteceptor)
        				.directExecutor()
        				.maxInboundMessageSize(GRPC_MAX_SIZE)
        	            .build()
        	            .start();
    		}

    		logger.info("GrpcMainProcessor started : "+ MSA_PORT);
    		logger.info("ThrMainProcessor  started : "+ MSA_PORT);
    	    
    	    Runtime.getRuntime().addShutdownHook(new Thread() {
    			@Override
    			public void run() {
    				// Use stderr here since the logger may have been reset by its JVM shutdown hook.
    				//logger.error("*** shutting down msa-"+ appProps.getProperty("msa.id") +"-server since JVM is shutting down");
    				logger.error("*** shutting down msa-"+ MSA_ID +"-server since JVM is shutting down");
    	            stopGrpc();
    	            logger.error("*** server shut down");
    			}
    		});
    	    blockUntilShutdown();
    		
        } catch(Exception ex) {
        	ex.printStackTrace();
        }
    }
    
    public void stopGrpc() {
	    if (this.server != null) {
	        this.server.shutdown();
	    }
	}
	
	/**
	 * Wait for main method. the gprc services uses daemon threads
	 * @throws InterruptedException
	 */
	public void blockUntilShutdown() throws InterruptedException {
	    if (this.server != null) {
	        this.server.awaitTermination();
	    }
	}
}
package com.sci4s.grpc;

import java.io.File;
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
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;;

@Component
public class GrpcRunner implements ApplicationRunner {
	
	public static Logger logger = LoggerFactory.getLogger(GrpcRunner.class);
	
	
	@Value("${msa.port}")
	int MSA_PORT;
	
	@Value("${msa.id}")
	String MSA_ID;
	
	@Value("${buffer.type}")
	String BUFFER_TYPE;
	
	@Value("${private.key.file}")
	String PRI_KEY;
		
	private int GRPC_MAX_SIZE = ErrConstance.GRPC_MAX_SIZE;
	
	private Server server;   
	
	TrueApiInteceptor apiInteceptor;
	ProtoMainProcessor mainProcessor;
	FlatMainProcessor flatProcessor;
	ThrMainProcessor  thrProcessor;
	FlatThrMainProcessor  flatthrProcessor;
	
	@Autowired
	public GrpcRunner(TrueApiInteceptor apiInteceptor
			, ProtoMainProcessor mainProcessor
			, ThrMainProcessor thrProcessor
			, FlatMainProcessor flatProcessor
			, FlatThrMainProcessor  flatthrProcessor
			){
	    this.apiInteceptor = apiInteceptor;
	    this.mainProcessor = mainProcessor;	   
	    this.thrProcessor  = thrProcessor;	   
	    this.flatProcessor = flatProcessor;	   
	    this.flatthrProcessor = flatthrProcessor;
	}	
	
	private SslContextBuilder getSslContextBuilder(String trustCertCollectionFilePath, String certChainFilePath, String privateKeyFilePath) {
        SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(new File(certChainFilePath),
                new File(privateKeyFilePath));
        if (trustCertCollectionFilePath != null) {
            sslClientContextBuilder.trustManager(new File(trustCertCollectionFilePath));
            sslClientContextBuilder.clientAuth(ClientAuth.REQUIRE);
        }
        return GrpcSslContexts.configure(sslClientContextBuilder);
    }

	private Server getGrpcServer() throws Exception {
		if("FLAT".equals(BUFFER_TYPE)) {
			return ServerBuilder.forPort(MSA_PORT)
	        		.addService((BindableService) flatProcessor)
	        		.addService((BindableService) flatthrProcessor)
	        		.intercept(apiInteceptor)
	        		.maxInboundMessageSize(GRPC_MAX_SIZE)
	        		.build();
		} else {
			return ServerBuilder.forPort(MSA_PORT)
    				.addService((BindableService) mainProcessor)
    				.addService((BindableService) thrProcessor)
    				.intercept(apiInteceptor)
    				.maxInboundMessageSize(GRPC_MAX_SIZE)
    	            .build();
		}
	}
	
    @Override
    public void run(ApplicationArguments args) throws Exception {
    	TimeZone.setDefault(TimeZone.getTimeZone("UTC"));    	
    	
        try {    	
    		logger.info("MSA_PORT    :::::: "+ MSA_PORT);
    		logger.info("BUFFER_TYPE :::::: "+ BUFFER_TYPE);
    		
   			this.server = this.getGrpcServer().start();
   			logger.info("GrpcMainProcessor started : "+ MSA_PORT);
    	    
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
package com.sci4s.grpc;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
//import org.springframework.cloud.commons.util.InetUtils;
//import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.sci4s.grpc.aop.TrueApiInteceptor;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

@Component
public class GrpcRunner implements ApplicationRunner {
	
//	private EurekaInstanceConfigBean eurekaInstanceConfig;
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
	
//	@Value("${eureka.instance.preferIpAddress}")
//	String preferIpAddress;	
//	@Value("${eureka.client.serviceUrl.defaultZone}")
//	String defaultZone;	
//	@Value("${eureka.instance.lease-renewal-interval-in-seconds}")
//	String leaseRenewalInterval;//20	
//	@Value("${eureka.instance.lease-expiration-duration-in-seconds}")
//	String leaseExpirationDuration;//25
	
	private Server server;   
	
	TrueApiInteceptor apiInteceptor;
	ThrMainProcessor  thrProcessor;
	FlatThrMainProcessor  flatthrProcessor;
	ProtoMainProcessor mainProcessor;
	FlatMainProcessor flatProcessor;
	
	@Autowired
	public GrpcRunner(TrueApiInteceptor apiInteceptor
			, ProtoMainProcessor mainProcessor
			, ThrMainProcessor thrProcessor
			, FlatMainProcessor flatProcessor
			, FlatThrMainProcessor  flatthrProcessor){
	    this.apiInteceptor = apiInteceptor;
	    this.mainProcessor = mainProcessor;
	    this.thrProcessor  = thrProcessor;	   
	    this.flatProcessor = flatProcessor;	   
	    this.flatthrProcessor = flatthrProcessor;
	}
	
//	@Bean
//  @Primary
//  @Autowired
//	@Profile("docker")
//	public EurekaInstanceConfigBean DockerSwarm_EurekaClient(InetUtils inetUtils) {
//		try {
//			eurekaInstanceConfig = new EurekaInstanceConfigBean(inetUtils);
//
//			String hostName = System.getenv("HOSTNAME"); // container hostname 가져옴, container_id 일 것임.
//			if (hostName == null) {
//				hostName = "VM192-168-219-44";
//			}
//			logger.info("HOSTNAME : " + hostName); // VM192_168_219_44
//			String hostIP = hostName.substring(2).replace("-", ".");
//			logger.info("MSA_PORT : " + MSA_PORT);
//			logger.info("leaseRenewalInterval : " + leaseRenewalInterval);
//			logger.info("leaseExpirationDuration : " + leaseExpirationDuration);
//			logger.info("preferIpAddress : " + preferIpAddress);
//			logger.info("virtualHostName : " + eurekaInstanceConfig.getVirtualHostName());			
//			
//			eurekaInstanceConfig.setInstanceId(hostIP +":"+ MSA_PORT);
//			eurekaInstanceConfig.setHostname(hostIP);
//			eurekaInstanceConfig.setPreferIpAddress(Boolean.parseBoolean(preferIpAddress));
//			eurekaInstanceConfig.setIpAddress(hostIP);
//			eurekaInstanceConfig.setNonSecurePort(MSA_PORT);
//			
//			eurekaInstanceConfig.setLeaseRenewalIntervalInSeconds(Integer.parseInt(leaseRenewalInterval));
//			eurekaInstanceConfig.setLeaseExpirationDurationInSeconds(Integer.parseInt(leaseExpirationDuration));

			/*
			 * Optional<NetworkInterface> net =
			 * Optional.of(NetworkInterface.getByName("eth0"));
			 * 
			 * logger.info("Network instance inetaddress: " + net.get().getInetAddresses());
			 * logger.info("Network instance name: " + net.get().getName());
			 * 
			 * Enumeration<InetAddress> inetAddress = net.get().getInetAddresses();
			 * 
			 * InetAddress current = inetAddress.nextElement();
			 * 
			 * logger.info("Get Current Address : " + current.toString());
			 * 
			 * String address = current.toString().split("/")[1];
			 * 
			 * // String address = null;
			 * 
			 * while(inetAddress.hasMoreElements()) { current = inetAddress.nextElement();
			 * logger.info("current address 1 : " + current.toString());
			 * if(!current.isLoopbackAddress()){ address = current.toString();
			 * logger.info("current address2: ", address); break; } }
			 * 
			 * logger.info(" HostName : " + HostName); logger.info(" Address : " + address);
			 * 
			 * eurekaInstanceConfig.setHostname(HostName);
			 * eurekaInstanceConfig.setPreferIpAddress(true);
			 * eurekaInstanceConfig.setIpAddress(address);
			 * eurekaInstanceConfig.setNonSecurePort(18999);
			 */
			
//			logger.info("Eureka Config : "+ eurekaInstanceConfig.toString());
//			return eurekaInstanceConfig;
//		} catch(Exception e) {
//			logger.error("EurekaInstanceConfigBean:::"+ e.getStackTrace());
//            return null;
//		}
//	}

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
    	        		.addService((BindableService) flatthrProcessor)
    	        		.intercept(apiInteceptor)
    	        		.maxInboundMessageSize(GRPC_MAX_SIZE)
    	        		.build().start();
    		} else {
    			this.server = ServerBuilder.forPort(MSA_PORT)
        				.addService((BindableService) mainProcessor)
        				.addService((BindableService) thrProcessor)
        				.intercept(apiInteceptor)
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
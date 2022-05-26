package com.sci4s.grpc.svc;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

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
}

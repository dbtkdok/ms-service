package com.sci4s.grpc.util;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
//import com.google.flatbuffers.FlatBufferBuilder;
import com.sci4s.fbs.Chunk;
import com.sci4s.fbs.Data;
import com.sci4s.fbs.FlatJsonGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ClientFileDown {
	
    private static final Logger logger = LoggerFactory.getLogger(ClientFileDown.class);
    private static final int PORT = 3500;

    private final ManagedChannel mChannel;
    private final FlatJsonGrpc.FlatJsonBlockingStub mBlockingStub;

    public ClientFileDown(String host, int port) {    	
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext().maxInboundMessageSize(2147483647).build());
    }
    
    public ClientFileDown(ManagedChannel channel) {
    	
        this.mChannel = channel;
        mBlockingStub = FlatJsonGrpc.newBlockingStub(channel);        
        logger.info("mAsyncStub created!!!");
    }

    public void shutdown() throws InterruptedException {
        mChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void downFile(Data request, String savepath) {
        logger.info("tid: " +  Thread.currentThread().getId() + ", Will try to download");

	    try {
	        File localTmpFile = new File(savepath);	        
	        logger.info("save: " +  savepath);
	        
	        ByteSink byteSink = Files.asByteSink(localTmpFile, FileWriteMode.APPEND);
			Iterator<Chunk> response;

			response = mBlockingStub.download(request);
	    	
	    	while (response.hasNext()) { 		
	    		byteSink.write(response.next().getByteBuffer().array());
	    	}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
    }
/*
    public static void main(String[] args) throws Exception {
    	ClientFileDown client = new ClientFileDown("localhost", PORT);
    	long start = System.currentTimeMillis();
        try {
        	String downFile = "/mlearn/output/c889b35e-913d-4f28-8a5d-a0deb1811b47.png";
        	String PID      = "PYR0001";// 파이썬 파일 다운로드 PID
        	String jsonReq  = "";
        	String saveFile = "D:/dev/c889b35e-913d-4f28-8a5d-a0deb1811b47.png";
        	
    		jsonReq  = "{\"params\": [";
    		jsonReq += "{\"agentID\":\"13\", \"userUID\":\"1\", ";
    		jsonReq += "\"filePath\":\""+ downFile +"\", \"islogin\":\"0\", \"userType\":\"SYS\"}";
    		jsonReq += "]}";
    		
    		FlatBufferBuilder builder = new FlatBufferBuilder();                
            int dataOffset = Data.createData(builder
            		, builder.createString(PID)
            		, builder.createString("13")
            		, builder.createString("E74F67AB47763F38003E1C45F2E3FA71")
            		, builder.createString("192.168.219.180")
            		, builder.createString("192.168.219.195")
            		, builder.createString("1")
            		, builder.createString("15")
            		, builder.createString("KR")
            		, builder.createString(jsonReq));
            builder.finish(dataOffset);        
            Data reqData = Data.getRootAsData(builder.dataBuffer());

            client.downFile(reqData, saveFile);
            logger.info("Done with Download!");
            
            long end = System.currentTimeMillis();
            logger.info("Running Time : " + (end - start) +" milliseconds");
        } finally {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client.shutdown();
        }
    }
*/
}
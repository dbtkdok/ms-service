package com.sci4s.grpc.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.protobuf.ByteString;
import com.sci4s.fbs.Chunk;
import com.sci4s.fbs.FlatJsonGrpc;
import com.sci4s.fbs.Reply;
import com.sci4s.grpc.dto.ResInfo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class ClientFileUp {
	private static final Logger logger = LoggerFactory.getLogger(ClientFileUp.class);
    private static final int PORT = 19005; //19005; //18999;

    private final ManagedChannel mChannel;
    private final FlatJsonGrpc.FlatJsonBlockingStub mBlockingStub;
    private final FlatJsonGrpc.FlatJsonStub mAsyncStub;

    public ClientFileUp(String host, int port) {
    	
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .maxInboundMessageSize(2147483647)
                .maxRetryAttempts(3)
                .build());
        		//options = [('grpc.max_message_length', 100 * 1024 * 1024)]
    }

    public ClientFileUp(ManagedChannel channel) {
        this.mChannel = channel;
        mBlockingStub = FlatJsonGrpc.newBlockingStub(channel);
        mAsyncStub    = FlatJsonGrpc.newStub(channel);
        
        logger.info("mAsyncStub created!!!");
    }    

    public void shutdown() throws InterruptedException {
        mChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public ResInfo mLearning(String reqData, String filepath) {
        logger.info("tid: " +  Thread.currentThread().getId() + ", Will try to uploadFile");
        
        final CountDownLatch finishLatch = new CountDownLatch(1);
    	// logger.info("finishLatch.getCount() ::::::::::"+ finishLatch.getCount());
        ResInfo replay = new ResInfo();
        replay.setName("");
        replay.setLength(0);
        
        /*
    	rpc m_learning(stream Files) returns (Data) {}
    	rpc upload1(stream Chunk) returns (Reply) {}
    	*/        
        StreamObserver<com.sci4s.fbs.Files> requestObserver = mAsyncStub.withDeadlineAfter(60000, TimeUnit.MILLISECONDS).mLearning(new StreamObserver<Reply>() {
            @Override
            public void onNext(Reply value) {
            	// TODO Auto-generated method stub
				logger.info("responseObserver response onNext!!!"+ value.name() +" :::: "+ value.length()); 
				if (value.name() == null) {					
				} else {
					replay.setName(value.name());
					replay.setLength(value.length());
				}
				
				logger.info("replay.getLength() ::::::::::"+ replay.getLength());
            }
            @Override
            public void onError(Throwable t) {
            	logger.info("responseObserver response onError!!!"+ t.toString());
            }
            @Override
            public void onCompleted() {
            	logger.info("responseObserver response onCompleted!!!");
            	finishLatch.countDown();
            }
        });
        try {
        	File file = new File(filepath);
            if (file.exists() == false) {
                logger.info("File does not exist");
                return null;
            }
            logger.info(filepath +" does exist");        	
            
            BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
            int bufferSize = 1024 * 1024;
            byte[] buffer = new byte[bufferSize];
            int data = 0;
            int size = 0;

            ByteArrayOutputStream out = new ByteArrayOutputStream();            
            while ((data = fin.read(buffer)) > 0) {
            	out.write(data); 
            	size += data;            	
            	ByteString byteString = ByteString.copyFrom(out.toByteArray());
            	logger.info("ByteString ::::::: "+ byteString);       
                
            	// ProtoBuffer
                //requestObserver.onNext(com.sci4s.fbs.Files.newBuilder().setFileName(reqData).setFileBytes(byteString).build());
            }         
            logger.info("ByteString size ::: "+ size); 

            requestObserver.onCompleted();
            
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        } catch (Exception e) {
            requestObserver.onError(e);
            logger.error(e.toString());
        }  finally {
            logger.info("finishLatch.getCount() ::::::::::"+ finishLatch.getCount());
            try {
    			finishLatch.await();
    		} catch (InterruptedException e1) {
    			e1.printStackTrace();
    		}
        }
        return replay;
    }
    
    public void upload1(final String filepath) {
        logger.info("tid: "+  Thread.currentThread().getId() + ", Will try to uploadFile");
        
        final CountDownLatch finishLatch = new CountDownLatch(1);        
        // logger.info("finishLatch.getCount() ::::::::::"+ finishLatch.getCount());
        ResInfo replay = new ResInfo();
        replay.setName("");
        replay.setLength(0);
        
        StreamObserver<Reply> responseObserver = new StreamObserver<Reply>() {
        	@Override
			public void onNext(Reply value) {
				logger.info("responseObserver response onNext!!!"+ value.name() +" :::: "+ value.length()); 
				if (value.name() == null) {
					
				} else {
					replay.setName(value.name());
					replay.setLength(value.length());
				}
				logger.info("replay.getLength() ::::::::::"+ replay.getLength());
			}
        	@Override
			public void onError(Throwable t) {
				logger.info("responseObserver response onError!!!"+ t.getMessage());
			}
        	@Override
			public void onCompleted() {
				logger.info("responseObserver response onCompleted!!!");
				finishLatch.countDown();
			}        	
        };
        
        int attachSize = 0;
        StreamObserver<Chunk> requestObserver = mAsyncStub.upload1(responseObserver);
        try {

            File file = new File(filepath);
            if (file.exists() == false) {
                logger.info("File does not exist");
                return;
            }
            logger.info(filepath +" does exist");
            BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
            int bufferSize = 1024 * 1024; // 4194304
            byte[] buffer = new byte[bufferSize];
            int length  = 0;
            while ((length = inStream.read(buffer)) > 0) {
            	attachSize += length;
                ByteString byteString = ByteString.copyFrom(buffer);
                logger.info("byteString : "+ byteString);                
                // ProtoBuffer
                //Chunk req = Chunk.newBuilder().setBuffer(byteString).build();
                //requestObserver.onNext(req);
                
                FlatBufferBuilder builder = new FlatBufferBuilder();
	        	int dataOffset = Chunk.createChunk(builder, builder.createByteVector(byteString.toByteArray()));
                //int dataOffset = Chunk.createChunk(builder, builder.createByteVector(buffer, 0, length));
	        	builder.finish(dataOffset); 
	        	Chunk req = Chunk.getRootAsChunk(builder.dataBuffer());

	        	requestObserver.onNext(req);
            }
            requestObserver.onCompleted();
            logger.info("attachSize ::::::::::"+ attachSize);

        } catch (Exception e) {
            requestObserver.onError(e);
            e.printStackTrace();
        } finally {
            logger.info("finishLatch.getCount() ::::::::::"+ finishLatch.getCount());
    		try {
    			finishLatch.await();
    		} catch (InterruptedException e1) {
    			e1.printStackTrace();
    		}
        }
	}    
/*
    public static void main(String[] args) throws Exception {
    	ClientFileUp client = new ClientFileUp("localhost", PORT);
    	String filePath = "D:/dev/82KIM_JI_YOUNG.mp4";
    	long start = System.currentTimeMillis();
        try {
            client.upload1(filePath);
        	
            logger.info("Done with sendFileStream");
            
            // 머신러닝 샘플
    		//String jsonReq = "{\"PID\":\"MLR0001\", \"fileName\":\"breast_cancer.csv\"}";  
    		//filePath = "D:/dev/breast_cancer.csv";
    		//ResInfo rep = client.mLearning(jsonReq, filePath);

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

package com.sci4s.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GrpcMainProcessor {
	Logger logger = LoggerFactory.getLogger(GrpcMainProcessor.class);
	
	private ProtoMainProcessor protoProcessor;
	private FlatMainProcessor  flatProcessor;
	
	@Autowired
	public  GrpcMainProcessor(ProtoMainProcessor protoProcessor
			,FlatMainProcessor  flatProcessor){
	    this.protoProcessor = protoProcessor;
	    this.flatProcessor = flatProcessor;
	}
	
	public void callRMsg(Object obj) {
		System.out.println("obj :: " + obj);
	}
}
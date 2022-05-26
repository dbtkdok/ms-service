package com.sci4s.grpc.svc;

import java.util.Map;

import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;

public interface MainProcessor {
	public GrpcResp callRMsg(GrpcParams  grpcPrms);
	public GrpcResp getMstInfo(Map<String,Object> params);
}

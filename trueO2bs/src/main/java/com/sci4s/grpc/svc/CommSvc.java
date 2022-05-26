package com.sci4s.grpc.svc;

import java.util.Map;

import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;

public interface CommSvc {
	/**
	 * 단순 데이터 조회 처리
	 * @param  String sqlID
	 * @param  Map<String,String> grpcPrms
	 * @return
	 * @throws Exception
	 */
	public GrpcResp query4Data(String sqlID, Map<String,String> grpcMap) throws Exception;
	public GrpcResp query4Data(String sqlID, GrpcParams grpcPrms) throws Exception;
	
	/**
	 * 단일 건으로 넘어오는 데이터를 생성/수정/삭제 처리하는 서비스 메서드
	 * 
	 * @param  String sqlID
	 * @param  GrpcParams grpcPrms
	 * @return SciRIO.RetMsg
	 */
	public GrpcResp query4Update(String sqlID, GrpcParams grpcPrms) throws Exception;
	public GrpcResp query4Update1(String sqlID, GrpcParams grpcPrms) throws Exception;
	/**
	 * 리스트로 넘어오는 데이터를 생성/수정/삭제 처리하는 서비스 메서드
	 * 
	 * @param  String sqlID
	 * @param  GrpcParams grpcPrms
	 * @return SciRIO.RetMsg
	 */
	public GrpcResp query4Updates(String sqlID, GrpcParams grpcPrms) throws Exception;
	
	public GrpcResp getMstInfo(GrpcParams grpcPrms) throws Exception;
	public GrpcResp getMstInfo(Map<String,Object> paramMap) throws Exception;
}

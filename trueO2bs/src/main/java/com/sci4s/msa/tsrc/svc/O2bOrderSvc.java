package com.sci4s.msa.tsrc.svc;

import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;

public interface O2bOrderSvc {
	public GrpcResp getTblOrderList(GrpcParams grpcPrms) throws Exception;
	public GrpcResp getOrderDetail(GrpcParams grpcPrms) throws Exception;
	
	public GrpcResp saveCategoryMenuList(GrpcParams grpcPrms) throws Exception;
	public GrpcResp updCategoryMenuList(GrpcParams grpcPrms) throws Exception;
	
	public GrpcResp getOrderChannelList(GrpcParams grpcPrms) throws Exception;
	
	public GrpcResp saveBorgChannelList(GrpcParams grpcPrms) throws Exception;
	public GrpcResp updBorgChannelList(GrpcParams grpcPrms) throws Exception;
	
	public GrpcResp saveOrderChannelList(GrpcParams grpcPrms) throws Exception;
	
	/*주문수정*/
	public GrpcResp updOrder(GrpcParams grpcPrms) throws Exception;
	
	// 영수증 출력 서비스
	public GrpcResp getPrintOrderInfo(GrpcParams grpcPrms) throws Exception;
	
	// 주문등록 서비스
	public GrpcResp saveOrderRegistList(GrpcParams grpcPrms) throws Exception;
}

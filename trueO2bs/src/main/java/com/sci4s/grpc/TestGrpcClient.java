package com.sci4s.grpc;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sci4s.grpc.SciRIO.RetMsg;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class TestGrpcClient {
	private static final Logger logger = LoggerFactory.getLogger(TestGrpcClient.class);
	/*
	 * 유효한 인증서를 자바 keystore 명령어로 등록하고 사용해야 함.
	 * 
	 * hosts 파일에 127.0.0.1 www.sci4s.com
	 * 
	 * D:\msa\ssl>keytool -import -file "D:\msa\ssl\sci4s_com_crt.pem" -keystore "E:\Java\jdk1.8.0_144\lib\security\cacerts" -storepass "!qwer1234!"
	 */
	private static String caPemFile      = "D:/msa/ssl/sci4s_com_ca.pem";
	private static String crtPemFile     = "D:/msa/ssl/sci4s_com_crt.pem";
	private static String privateKeyFile = "D:/msa/ssl/sci4s_com.key";
	
	private static SslContext getBuildSslContext(String caPemFile, String crtPemFile,
			String privateKeyFile) throws SSLException {
		SslContextBuilder builder = GrpcSslContexts.forClient();
		if (caPemFile != null) {
			builder.trustManager(new File(caPemFile));
		}
		if (crtPemFile != null && privateKeyFile != null) {
			builder.keyManager(new File(crtPemFile), new File(privateKeyFile));
		}
		return builder.build();
	}
	
	private static ManagedChannel getManagedChannel(String addr, int port, String MSA_TLS) throws SSLException {
		if ("REQUIRE".equals(MSA_TLS)) {
		return NettyChannelBuilder.forAddress(addr, port)
    		    .sslContext(getBuildSslContext(caPemFile,crtPemFile,privateKeyFile))
    		    .negotiationType(NegotiationType.TLS)
    		    .build();
		} else {			
			return ManagedChannelBuilder.forTarget(addr +":"+ port)
	    			.usePlaintext()
	    			.build();
		}
	}

	public static void main(String[] args) throws Exception {
		
		String jsonReq  = "";
		String setPID   = "O2O0001";
    	String addr     = "192.168.219.195";//인증서 사용시 도메인으로 테스트해야 함.(www.sci4s.com)
    	addr = "www.sci4s.com";
    	int    port     = 27999;
    	String borgUID  = "BAM";// BAM : 배달의민족 / BM1 : 배민원 / YOG : 요기요 / COP : 쿠팡 / WMO : 위메프오 
    	String MSA_TLS  = "REQUIRE";//NONE OR REQUIRE
    	
    	final ManagedChannel channel = getManagedChannel(addr, port, MSA_TLS);
    	    	
    	MsaApiGrpc.MsaApiBlockingStub stub = MsaApiGrpc.newBlockingStub(channel);
    	
    	jsonReq = "{\"params\":{\"bbsid\":\"1\",\"bno\":\"1769687\",\"updater\":\"flacom@sci4s.com\",\"title\":\"1111111GRPC 수정작업 테스트\",\"content\":\"1111111GRPC 수정작업 테스트 내용\"}}";   	
    	// 배달 접수 데이터 
    	jsonReq = "{\"channel\":\"BAM\",\"shopNumber\":\"shopNumber\",\"deliveryType\":\"DELIVERY\",\"orders\":[{\"status\":\"ACCEPTED\"," + 
    	" \"deliveryStatus\":\"0\",\"orderNumber\":\"AOrderNo\"," + 
    	" \"roadNameAddress\":\"ARoadNameAddress\",\"address\":\"AAddress\",\"addressDetail\":\"AAddressDetail\"," + 
    	" \"pickupAddress\":\"ARoadNameAddress or AAddress + AAddressDetail\"," + 
    	" \"phoneNo\":\"APhoneNo\",\"latitude\":\"ALatitude\",\"longitude\":\"ALongitude\"," + 
    	" \"itemsSummary\":\"ATitle\",\"orderQty\":\"2\",\"payAmount\":\"20000\"," + 
    	" \"paymentType\":\"1\",\"storeMemo\":\"AMemo\"" + 
    	"}]}";    	
    	setPID = "O2O0001";//배달정보(C#)
    	
    	// 배민 접수 데이터 
    	jsonReq = "{\"totalSize\":2,\"totalPayAmount\":44100,\"contents\":[{\"order\":{\"orderNumber\":\"B16I007ZET\",\"status\":\"ACCEPTED\",\"deliveryType\":\"DELIVERY\",\"payType\":\"MEET\",\"payAmount\":32700,\"orderDateTime\":\"2022-03-11T11:04:39\",\"shopNumber\":13250580,\"itemsSummary\":\"돼지불백도시락 외 1\",\"items\":[{\"name\":\"돼지불백도시락\",\"totalPrice\":20800,\"quantity\":2,\"discountPrice\":\"null\",\"options\":[{\"name\":\"2찬+조미김 ( O )\",\"price\":1000},{\"name\":\"고추장\",\"price\":0},{\"name\":\"1인\",\"price\":9400}]},{\"name\":\"[가성비짱]제주흑돼지김치찌개 도시락\",\"totalPrice\":8900,\"quantity\":1,\"discountPrice\":\"null\",\"options\":[{\"name\":\"2찬+조미김 ( O )\",\"price\":1000},{\"name\":\"\",\"price\":7900}]}],\"deliveryTip\":3000,\"smallOrderFee\":\"null\",\"takeOutDiscountAmount\":\"null\",\"employeeDiscountAmount\":\"null\",\"ownerChargeCouponDiscountAmount\":\"null\",\"baeminChargeCouponDiscountAmount\":\"null\",\"adCampaign\":{\"id\":\"2846369\",\"key\":\"ULTRA_CALL\"}},\"settle\":{\"notDisplayReason\":\"MEET_ORDER\",\"salesAmount\":\"null\",\"discountAmount\":\"null\",\"subtractAmount\":{\"total\":\"null\",\"advertiseFee\":\"null\",\"riderServiceFee\":\"null\",\"deliverySupplyPrice\":\"null\",\"serviceFee\":\"null\",\"deliveryFeeDiscount\":\"null\"},\"vat\":\"null\",\"meetAmount\":\"null\",\"depositDueAmount\":\"null\",\"depositDueDate\":\"null\"}},{\"order\":{\"orderNumber\":\"B16I007IPS\",\"status\":\"ACCEPTED\",\"deliveryType\":\"DELIVERY\",\"payType\":\"BARO\",\"payAmount\":11400,\"orderDateTime\":\"2022-03-11T11:01:56\",\"shopNumber\":13250580,\"itemsSummary\":\"[가성비짱]제주흑돼지김치찌개 도시락\",\"items\":[{\"name\":\"[가성비짱]제주흑돼지김치찌개 도시락\",\"totalPrice\":7900,\"quantity\":1,\"discountPrice\":\"null\",\"options\":[{\"name\":\"\",\"price\":7900},{\"name\":\"2찬+조미김 ( X )\",\"price\":0}]}],\"deliveryTip\":3500,\"smallOrderFee\":\"null\",\"takeOutDiscountAmount\":\"null\",\"employeeDiscountAmount\":\"null\",\"ownerChargeCouponDiscountAmount\":\"null\",\"baeminChargeCouponDiscountAmount\":\"null\",\"adCampaign\":{\"id\":\"2846363\",\"key\":\"OPEN_LIST\"}},\"settle\":{\"notDisplayReason\":\"NOT_READY\",\"salesAmount\":\"null\",\"discountAmount\":\"null\",\"subtractAmount\":{\"total\":\"null\",\"advertiseFee\":\"null\",\"riderServiceFee\":\"null\",\"deliverySupplyPrice\":\"null\",\"serviceFee\":\"null\",\"deliveryFeeDiscount\":\"null\"},\"vat\":\"null\",\"meetAmount\":\"null\",\"depositDueAmount\":\"null\",\"depositDueDate\":\"null\"}}]}";
    	setPID = "O2O0002";//주문 상세(python)
    	
    	//배민 주문 주소 데이터
    	jsonReq = "{\"ownerId\":202010270501,\"orderNumber\":\"B16I007ZET\",\"orderData\":{\"paymentMethod\":{\"purchaseType\":\"MEET\",\"method\":\"만나서 카드결제\",\"amount\":32700},\"subPaymentMethods\":[],\"pickupAddress\":\"인천 중구 영종대로196번길 15-25 운서역반도유보라아파트 101동 2103호\",\"storeMemo\":\"수저젓가락 챙겨주세요(수저포크 O)\",\"riderMemo\":\"조심히 안전하게 와주세요 :)\",\"progressMessage\":\"-\",\"orderDateTime\":\"2022-03-11T11:04:39\",\"acceptDateTime\":\"2022-03-11T11:04:47\",\"deliveryDateTime\":\"2022-03-11T12:04:47\",\"pickupDateTime\":\"None\",\"reservationDateTime\":\"None\",\"cancelDateTime\":\"None\"}}";
    	
    	setPID = "O2O0003";//주문 주소(python)
    	jsonReq = "{\"ownerId\":202010270501,\"orderNumber\":\"B16M015PDN\",\"orderData\":{\"paymentMethod\":{\"purchaseType\":\"BARO\",\"method\":\"네이버페이\",\"amount\":27900},\"subPaymentMethods\":[{\"method\":\"할인쿠폰\"}],\"pickupAddress\":\"인천 중구 신도시북로 68 626-505\",\"storeMemo\":\"(수저포크 X)\",\"riderMemo\":\"문앞에 두고 벨 눌러주세요\",\"progressMessage\":\"-\",\"orderDateTime\":\"2022-03-15T18:12:28\",\"acceptDateTime\":\"2022-03-15T18:12:36\",\"deliveryDateTime\":\"2022-03-15T19:12:36\",\"pickupDateTime\":\"None\",\"reservationDateTime\":\"None\",\"cancelDateTime\":\"None\"}}";
    	// 202010270501_B16M015PDN
    	
    	// GRPC 요청을 위한 파라미터 설정
		SciRIO.Data request = SciRIO.Data.newBuilder()
				.setPID(setPID)
				.setData(jsonReq)
				.setCsKey("123FFTYTHAFW34345464EFWS")
				.setUserIP("192.168.219.100")
				.setServerIP("192.168.219.195")
				.setUserUID("0")
				.setBorgUID(borgUID)
				.setAgentID("13")
				.build();
		
		RetMsg retMsg = stub.callRMsg(request);
		if (!retMsg.getErrCode().equals("0")) {
			System.out.print(retMsg.getErrMsg());
		}
		System.out.println("retMsg.getErrCode() ::: "+ retMsg.getErrCode());
		System.out.println("retMsg.getResults() ::: "+ retMsg.getResults());
    	
    	channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
	}
}

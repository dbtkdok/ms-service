package com.sci4s.grpc.batch;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.msa.tsrc.svc.O2bOrderService;
import com.sci4s.util.JsonUtil;
import com.sci4s.utils.AES256Util;
import com.sci4s.utils.DateUtil;

@RefreshScope
@Component
public class BatchSyncScheduler {
	
	Logger logger = LoggerFactory.getLogger(BatchSyncScheduler.class);
	
	@Value("${mst.sync.running}")
	String SYNC_RUNNING;
	
	boolean IS_SYNC_RUNNING = true;
	
	private O2bOrderService o2oOrderService;
	
	@Autowired
	public BatchSyncScheduler(O2bOrderService o2oOrderService) {
		this.o2oOrderService = o2oOrderService;
	}
	
	
//	@Scheduled(fixedDelayString="60000")
    public void runner4DaumMap() {
    	long start = System.currentTimeMillis();
    	long end   = 0;
    	
    	try {
    		if (this.IS_SYNC_RUNNING) {
    			o2oOrderService.getDaumMapAddress();
    		} else {
    			logger.info("IS_SYNC_RUNNING ::: "+ this.SYNC_RUNNING);
    		}
    		end = System.currentTimeMillis();
    	} catch(Exception ex) {
         	ex.printStackTrace();
        } finally {
        }
    	logger.info("runner4DaumMap Call Finish============="+ ( end - start ) +"ms");
    }
	
//	@Scheduled(cron = "0 0 0/1 * * *") 매일 1시간 마다 실행
    @Scheduled(fixedDelayString="60000")
    public void runner4UserInfo() {
    	long start = System.currentTimeMillis();
    	long end   = 0;
    	GrpcParams gprms = new GrpcParams();
    	AES256Util aes256 = null;
		String csKey = "";
		String encKey = "";
		String data = "";
    	Map<String, Object> rpcPrms = new HashMap<String, Object>();
    	rpcPrms.put("dbSTS", "Y");
		int julianDate  = DateUtil.toJulian(new Date());
		csKey += julianDate+"|"+"14"+"|"+"0";
    	csKey += "|"+"0"+"|"+"127.0.0.1"+"|"+"127.0.0.1";
    	csKey += "|"+"FA0000"+"|"+"N";
    	
    	try {
    		data = JsonUtil.getJsonStringFromMapHead("params", rpcPrms);
    		aes256 = new AES256Util();	
    		encKey = aes256.encryptII(csKey);
    		gprms.setAgentID("14");
    		gprms.setpID("THR001");
    		gprms.setCsKey(encKey);
    		gprms.setUserIP("127.0.0.1");
    		gprms.setServerIP("127.0.0.1");
    		gprms.setUserUID("0");
    		gprms.setBorgUID("0");
    		gprms.setClang("KR");
    		gprms.setData(data);
    		gprms.setType("LIST");
    		
    		if (this.IS_SYNC_RUNNING) {
    			o2oOrderService.getGrpcChannels(gprms, "saveUserInfoUsers");
    		} else {
    			logger.info("IS_SYNC_RUNNING ::: "+ this.SYNC_RUNNING);
    		}
    		end = System.currentTimeMillis();
    	} catch(Exception ex) {
         	ex.printStackTrace();
        } finally {
        }
    	logger.info("runner4UserInfo Call Finish============="+ ( end - start ) +"ms");
    }
	
//	@Scheduled(cron = "0 0 0/1 * * *") 매일 1시간 마다 실행
    @Scheduled(fixedDelayString="60000")
    public void runner4CustInfo() {
    	long start = System.currentTimeMillis();
    	long end   = 0;
    	GrpcParams gprms = new GrpcParams();
    	AES256Util aes256 = null;
		String csKey = "";
		String encKey = "";
		String data = "";
    	Map<String, Object> rpcPrms = new HashMap<String, Object>();
    	rpcPrms.put("dbSTS", "Y");
		int julianDate  = DateUtil.toJulian(new Date());
		csKey += julianDate+"|"+"14"+"|"+"0";
    	csKey += "|"+"0"+"|"+"127.0.0.1"+"|"+"127.0.0.1";
    	csKey += "|"+"FA0000"+"|"+"N";
    	
    	try {
    		data = JsonUtil.getJsonStringFromMapHead("params", rpcPrms);
    		aes256 = new AES256Util();	
    		encKey = aes256.encryptII(csKey);
    		gprms.setAgentID("14");
    		gprms.setpID("THR002");
    		gprms.setCsKey(encKey);
    		gprms.setUserIP("127.0.0.1");
    		gprms.setServerIP("127.0.0.1");
    		gprms.setUserUID("0");
    		gprms.setBorgUID("0");
    		gprms.setClang("KR");
    		gprms.setData(data);
    		gprms.setType("LIST");
    		
    		if (this.IS_SYNC_RUNNING) {
    			o2oOrderService.getGrpcChannels(gprms, "saveCustInfoBorgs");
    		} else {
    			logger.info("IS_SYNC_RUNNING ::: "+ this.SYNC_RUNNING);
    		}
    		end = System.currentTimeMillis();
    	} catch(Exception ex) {
         	ex.printStackTrace();
        } finally {
        }
    	logger.info("runner4CustInfo Call Finish============="+ ( end - start ) +"ms");
    }
    
//	@Scheduled(cron = "0 0 0/1 * * *") 매일 1시간 마다 실행
    @Scheduled(fixedDelayString="60000")
    public void runner4UserCustInfo() {
    	long start = System.currentTimeMillis();
    	long end   = 0;
    	GrpcParams gprms = new GrpcParams();
    	AES256Util aes256 = null;
		String csKey = "";
		String encKey = "";
		String data = "";
    	Map<String, Object> rpcPrms = new HashMap<String, Object>();
    	rpcPrms.put("dbSTS", "Y");
		int julianDate  = DateUtil.toJulian(new Date());
		csKey += julianDate+"|"+"14"+"|"+"0";
    	csKey += "|"+"0"+"|"+"127.0.0.1"+"|"+"127.0.0.1";
    	csKey += "|"+"FA0000"+"|"+"N";
    	
    	try {
    		data = JsonUtil.getJsonStringFromMapHead("params", rpcPrms);
    		aes256 = new AES256Util();	
    		encKey = aes256.encryptII(csKey);
    		gprms.setAgentID("14");
    		gprms.setpID("THR003");
    		gprms.setCsKey(encKey);
    		gprms.setUserIP("127.0.0.1");
    		gprms.setServerIP("127.0.0.1");
    		gprms.setUserUID("0");
    		gprms.setBorgUID("0");
    		gprms.setClang("KR");
    		gprms.setData(data);
    		gprms.setType("LIST");
    		
    		if (this.IS_SYNC_RUNNING) {
    			o2oOrderService.getGrpcChannels(gprms, "saveUserCustInfoUserBorgs");
    		} else {
    			logger.info("IS_SYNC_RUNNING ::: "+ this.SYNC_RUNNING);
    		}
    		end = System.currentTimeMillis();
    	} catch(Exception ex) {
         	ex.printStackTrace();
        } finally {
        }
    	logger.info("runner4UserCustInfo Call Finish============="+ ( end - start ) +"ms");
    }
}

package com.sci4s.grpc.batch;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sci4s.grpc.MsaApiGrpc;
import com.sci4s.grpc.SciRIO;
import com.sci4s.grpc.SciRIO.RetMsg;
import com.sci4s.grpc.svc.Channel;
import com.sci4s.msa.hr.svc.ThrService;
//import com.sci4s.msa.tsrc.svc.TSrcService;
import com.sci4s.utils.AES256Util;
import com.sci4s.utils.DateUtil;

//@RefreshScope
//@Component
public class MstSyncScheduler {

	Logger logger = LoggerFactory.getLogger(MstSyncScheduler.class);
	
	//@Value("${mst.sync.running}")
	String SYNC_RUNNING;
	boolean IS_SYNC_RUNNING = true;
	
	//@Value("${msa.pids.uri}")
	String TSYS_URI;
	
	//@Value("${msa.agentid}")
	String AGENT_ID;
	
	//@Value("${spring.application.name}")
	String APPLICATION_NAME;
	
	//@Value("${msa.ip}")
	String MSA_IP;
	
	private String CS_KEY;
	
	private ThrService thrService;
	private Channel channel;
	//@Autowired
	public MstSyncScheduler(ThrService thrService, Channel channel) {
		this.channel = channel;
		this.thrService = thrService;
	}
	/**
	 * 
	 * 
	 */
	//@PostConstruct
    private void init() {
		try { 
    		int julianDate  = DateUtil.toJulian(new Date());				
    		AES256Util aes256 = new AES256Util();				
			// csKey=julianDate|agentID|userUID|borgUID|userIP|serverIP
	    	String csKey  = julianDate+"|"+ this.AGENT_ID +"|"+ this.APPLICATION_NAME +"_SyncScheduler|15|"+ MSA_IP +"|"+ MSA_IP;
	    	this.CS_KEY = aes256.encrypt(csKey);	    	
	    	this.IS_SYNC_RUNNING = Boolean.parseBoolean(this.SYNC_RUNNING);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
    }
	

    
    /**
     * 권한별 영역정보 조회용 스케줄러
     *  
    @Scheduled(fixedDelayString="${mst.rolescopes.fixedDelay}")
    public void runner4Rolescopes() {
    	long start = System.currentTimeMillis();
    	long end   = 0;
    	logger.info("==========Remote "+ this.TSYS_URI +" Rolescopes Call Begin============="+ new Date());    	
    	String svcKey = com.sci4s.util.Config.SVC_KEY.toUpperCase();
    	List<TblRolescopes> rsList = null;
    	try {
    		if (this.IS_SYNC_RUNNING) {
	    		String jsonRet = syncMstInfoTsys(this.TSYS_URI, "tbl_rolescopes", this.APPLICATION_NAME, svcKey);
	    		if (jsonRet != null) { // 데이터를 저장함
	        		JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonRet);	
	        		//System.out.println("jsonObj.get(\"results\").toString() ::: "+ jsonObj.get("results"));
	        		rsList = new ObjectMapper().readValue(jsonObj.get("results").toString(), new TypeReference<List<TblRolescopes>>(){});
	        		if (rsList != null) { 
	        			thrService.saveTblRolescopesAll(rsList); 
	        		}
		    	}
    		} else {
    			logger.info("IS_SYNC_RUNNING ::: "+ this.SYNC_RUNNING);
    		}
    		end = System.currentTimeMillis();
    	} catch(Exception ex) {
         	ex.printStackTrace();
        } finally {
         	if (rsList != null) { rsList = null; }
        }
    	logger.info("==========Remote "+ this.TSYS_URI +" Rolescopes Call Finish============="+ ( end - start ) +"ms");
    }
     */
    /**
     * 권한별 영역정보 조회용 스케줄러
     *  

    @Scheduled(fixedDelayString="${mst.scopes.fixedDelay}")
    public void runner4Scopes() {
    	long start = System.currentTimeMillis();
    	long end   = 0;
    	logger.info("==========Remote "+ this.TSYS_URI +" Scopes Call Begin============="+ new Date());    	
    	String svcKey = com.sci4s.util.Config.SVC_KEY.toUpperCase();
    	List<TblScopes> rsList = null;
    	try {
    		if (this.IS_SYNC_RUNNING) {
	    		String jsonRet = syncMstInfoTsys(this.TSYS_URI, "tbl_scopes", this.APPLICATION_NAME, svcKey);
	    		if (jsonRet != null) { // 데이터를 저장함
	        		JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonRet);	
	        		//System.out.println("jsonObj.get(\"results\").toString() ::: "+ jsonObj.get("results"));
	        		rsList = new ObjectMapper().readValue(jsonObj.get("results").toString(), new TypeReference<List<TblScopes>>(){});
	        		if (rsList != null) { 
	        			thrService.saveTblScopesAll(rsList); 
	        		}
		    	}
    		} else {
    			logger.info("IS_SYNC_RUNNING ::: "+ this.SYNC_RUNNING);
    		}
    		end = System.currentTimeMillis();
    	} catch(Exception ex) {
         	ex.printStackTrace();
        } finally {
         	if (rsList != null) { rsList = null; }
        }
    	logger.info("==========Remote "+ this.TSYS_URI +" Scopes Call Finish============="+ ( end - start ) +"ms");
    }
     */    
    /**
     * tsys 서비스에 commonService로 구현한 getMstInfo를 호출하여 싱크할 마스터 정보를 조회하는데 사용함.
     * 싱크할 마스터 Data를 관리하는 서비스는  tbl_infinfo와 tbl_infinfohist 테이블를 생성해야 하며,
     * 싱크할 데이블에 trg_ins_attach, trg_upd_attach 트리거를 구혀해야 함.
     *  
     * @param String MST_URL
     * @param String tblNM
     * @param String appNM
     * @param String svcKey
     * @return String results

    private String syncMstInfoTsys(String MST_URL, String tblNM, String appNM, String svcKey) {    	
    	try {
        	MsaApiGrpc.MsaApiBlockingStub stub = MsaApiGrpc.newBlockingStub(this.channel.openChannel(MST_URL));
        	
        	Map<String,String> grpcMap = new HashMap<String,String>();	
        	grpcMap.put("agentID", this.AGENT_ID);
    		grpcMap.put("tblName", tblNM);
    		grpcMap.put("svcKey",  svcKey);
    		
    		SciRIO.Data reqMap = SciRIO.Data.newBuilder()
    				.setPID("TSY0100")
    				.setCsKey(this.CS_KEY)
    				.setUserIP(MSA_IP)
    				.setServerIP(MSA_IP)
    				.setUserUID("1")
    				.setBorgUID("15")
    				.setAgentID(this.AGENT_ID)
    				.putAllParams(grpcMap)
    				.build();

    		RetMsg retMsg = stub.callRMsg(reqMap);
    		String jsonRet = null;
    		if (retMsg.getErrCode().equals("0")) {
    			if (retMsg.getResults().indexOf("NO_DATA") >= 0) {
    				jsonRet = null;
    			} else {
    				jsonRet = retMsg.getResults();
    			}
    		}
    		this.channel.closeChannel();
        	return jsonRet;
        } catch(Exception ex) {
        	ex.printStackTrace();
        	return null;
        }
    }
     */
}

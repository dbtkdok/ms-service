package com.sci4s.grpc.svc;

import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.sci4s.cnf.PIDSLoader;
import com.sci4s.grpc.ErrConstance;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.utils.GrpcDataUtil;
import com.sci4s.utils.ErrUtil;

@Service
public class MainProcessorImpl implements MainProcessor {

	Logger logger = LoggerFactory.getLogger(MainProcessorImpl.class);
	
	@Value("${db.dbtype}")
	String SQLMODE;
	
	@Value("${default.lang}")
	String CLANG;

	@Value("${msa.agentid}")
	String MSA_AGENTID;
	
	@Value("${msa.id}")
	String MSA_ID;

	@Value("${msa.pids.uri}")
	String TSYS_URI;
	
	@Value("${mst.chk.time}")
	String MST_CHKTIME;
	
	@Value("${buffer.type}")
	String BUFFER_TYPE;
	
	String xmlUri;
	
	private PIDSLoader pids;
	private ApplicationContext context;	
	private CommService commService;
	
	@Autowired
	public  MainProcessorImpl(ApplicationContext context
			, CommService commService){
	    this.context = context;
	    this.commService = commService;
	}
	
	@PostConstruct
    private void init() {
		this.xmlUri = TSYS_URI +"|"+ MSA_AGENTID +"|"+ MSA_ID;	    
	    if (pids == null) {
			try { pids = PIDSLoader.getInstance(this.xmlUri, this.BUFFER_TYPE); } catch(Exception ex) {}
		}
    }
	
	@Override
	public GrpcResp callRMsg(GrpcParams grpcPrms) {
		String results = "";
		String errMsg  = null;
		GrpcResp grpcResp = new GrpcResp();
		
		String PID = grpcPrms.getpID();
		PIDSLoader pids = null;
		try {
			if (pids == null) {
				try { pids = PIDSLoader.getInstance(this.xmlUri, this.BUFFER_TYPE); } catch(Exception ex) {}
			}
			String svcName  = pids.getPIDSteps(PID).getService(); // 서비스명
			String queryId  = pids.getPIDSteps(PID).getQuery();   // SqlID
			String methName = pids.getPIDSteps(PID).getMethod();  // 호출할 메서드명
			
			logger.info("service ::: " + svcName +", queryId ::: " + queryId +", method ::: " + methName);
			
			Object beanObj = context.getBean(svcName);
			if ("N".equals(queryId)) {
				Class prmTypes[] = { GrpcParams.class };
				Method method = beanObj.getClass().getDeclaredMethod(methName, prmTypes);
				grpcResp = (GrpcResp) method.invoke(beanObj, new Object[] { grpcPrms });
			} else {
				Class prmTypes[] = { String.class, GrpcParams.class };
				Method method = beanObj.getClass().getDeclaredMethod(methName, prmTypes);
				grpcResp = (GrpcResp) method.invoke(beanObj, new Object[] { queryId, grpcPrms });
			}
		} catch (Exception e) {			
			errMsg = e.getMessage();
			logger.error("99 callRMsg() TRY CATCH -> ::::::"+ errMsg);
			results = ErrUtil.getErrorResults(e);
			logger.error("results::::::"+ results);
		} finally {				
			try {
				if (errMsg != null) {
					grpcResp.setErrCode(ErrConstance.ERR_9999);
					grpcResp.setErrMsg(errMsg);
					grpcResp.setResults(results);
				}	
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if (grpcPrms != null) { try { grpcPrms = null; } catch (Exception e1) {} }
		}
		return grpcResp;
	}

	@Override
	public GrpcResp getMstInfo(Map<String, Object> params) {
		String jsonData = null;	
		String errCode = "0";	
		String errMsg = "";	
		GrpcResp grpcResp = new GrpcResp();
		
		params.put("chkTime", Integer.parseInt((MST_CHKTIME==null?"5":MST_CHKTIME)));// 예) 5분전까지 데이터 조회
		params.put("SQLMODE", this.SQLMODE); 
		
		if (!params.containsKey("clang")) {
			params.put("clang", this.CLANG);
		}
		try {
//			jsonData = commService.getMstInfo(params);
		} catch (Exception e) {
			jsonData = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, e.getMessage(), null);
			errMsg = e.getMessage();
			errCode = ErrConstance.ERR_9999;
		}
		
		grpcResp.setErrCode(errCode);
		grpcResp.setErrMsg(errMsg);
		grpcResp.setResults(jsonData);

		return grpcResp;
	}
}

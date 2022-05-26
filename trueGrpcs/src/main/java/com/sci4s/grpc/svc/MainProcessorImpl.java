package com.sci4s.grpc.svc;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.sci4s.err.NotFoundBeanException;
import com.sci4s.grpc.ErrConstance;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.utils.GrpcDataUtil;
import com.sci4s.msa.hr.svc.LoginService;
import com.sci4s.utils.ErrUtil;

@Service
public class MainProcessorImpl extends TopMainProcessor {

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
	
//	String xmlUri;
	
//	private PIDSLoader pids;
	private ApplicationContext context;	
	private LoginService loginService;
	private CommService commService;
	
	@Autowired
	public  MainProcessorImpl(ApplicationContext context
			, LoginService loginService, CommService commService){
	    this.context = context;
	    this.loginService = loginService;
	    this.commService = commService;
	}
	
	@PostConstruct
    private void init() {
	    super.setMasBufferType(this.BUFFER_TYPE);
	    super.setXmlUri(TSYS_URI +"|"+ MSA_AGENTID +"|"+ MSA_ID);	    
	    super.setClang(this.CLANG);
	    super.setMstChkTime(this.MST_CHKTIME);
	    super.setSqlMode(this.SQLMODE);
    }
	
	/**
	 * TopMainProcessor.getServiceBean를 반드시 오버라이딩해야 함.
	 */
	@Override
	protected Object getServiceBean(String svcName) throws NotFoundBeanException { 
		try {
			return context.getBean(svcName);
		} catch(Exception ex) {
			throw new NotFoundBeanException(ErrUtil.getPrintStackTrace(ex), "8887");
		}
    }
	/**
	 * TopMainProcessor.getMstInfo를 위해 반드시 오버라이딩해야 함.
	 */
	@Override
	protected GrpcResp getMstInfoSvc(Map<String, Object> paramMap) throws Exception {
		return commService.getMstInfo(paramMap);
	}
	
	public GrpcResp insNewCustVdInfo(Map<String, Object> paramMap) {
		String jsonData = null;	
		String errCode = "0";	
		String errMsg = "";	
		GrpcResp grpcResp = new GrpcResp();
		
		paramMap.put("SQLMODE", this.SQLMODE); 
		if (!paramMap.containsKey("clang")) {
			paramMap.put("clang", this.CLANG);
		}
		
		try {
			jsonData = commService.insNewCustVdInfo(paramMap);
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

	public GrpcResp delApvInfo4ApvUID(Map<String, Object> paramMap) {
		String jsonData = null;	
		String errCode = "0";	
		String errMsg = "";	
		GrpcResp grpcResp = new GrpcResp();
		
		paramMap.put("SQLMODE", this.SQLMODE); 
		if (!paramMap.containsKey("clang")) {
			paramMap.put("clang", this.CLANG);
		}
		
		try {
			jsonData = commService.delApvInfo4ApvUID(paramMap);
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

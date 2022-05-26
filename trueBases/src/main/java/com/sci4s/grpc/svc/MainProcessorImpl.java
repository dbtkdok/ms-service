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
import com.sci4s.grpc.utils.FlatDataUtil;
import com.sci4s.msa.tsys.svc.SysMgmtService;
import com.sci4s.msa.tsys.svc.TSysService;
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
	
	private ApplicationContext context;
	private CommService commService;
	private SysMgmtService sysMgmtService;
	private TSysService tSysService;
	
	@Autowired
	public MainProcessorImpl(ApplicationContext context
			, CommService commService
			, SysMgmtService sysMgmtService
			, TSysService tSysService){
	    this.context = context;
	    this.commService = commService;
	    this.sysMgmtService = sysMgmtService;
	    this.tSysService = tSysService;
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
	@Override
	protected GrpcResp getMstInfoSvc(Map<String, Object> paramMap) throws Exception {
		return commService.getMstInfo(paramMap);
	}

	public GrpcResp getEnableJob(Map<String, Object> params) {
		String results = null;
		GrpcResp grpcResp = null;
		try {
			return sysMgmtService.getEnableJob(params);
		} catch (Exception e1) {
			String errMsg = ErrUtil.getPrintStackTrace(e1);
			logger.error("getEnableJob() TRY CATCH Exception ::: "+ errMsg);
			grpcResp = FlatDataUtil.getErrGrpcResp("Exception", ErrConstance.ERR_9999, errMsg);	
			logger.error("getEnableJob() TRY CATCH Exception results ::: "+ results);
			return grpcResp;
		} finally {
			if (grpcResp != null) grpcResp = null;
		}
		
	}
	
	/**
	 * PIDS-XML 파일 내용을 조회하여 XML로 리턴함.
	 * 
	 */
	public GrpcResp getServiceXml(Map<String,String> params) {
		String   jsonData = null;	
		GrpcResp grpcResp = null;
		try {			
			String xmlContent = tSysService.getServiceXml(params);			
			jsonData = xmlContent;
			
			grpcResp = new GrpcResp();
			grpcResp.setErrCode("0");
			grpcResp.setErrMsg("");
			grpcResp.setResults(jsonData);
			return grpcResp;
		} catch (Exception e1) {
			String errMsg = ErrUtil.getPrintStackTrace(e1);
			logger.error("getServiceXml() TRY CATCH Exception ::: "+ errMsg);
			grpcResp = FlatDataUtil.getErrGrpcResp("Exception", ErrConstance.ERR_9999, errMsg);	
			logger.error("getServiceXml() TRY CATCH Exception results ::: "+ grpcResp.getResults());
			return grpcResp;
		} finally {
			if (jsonData != null) jsonData = null;
			if (grpcResp != null) grpcResp = null;
		}
	}
	
	/**
	 * tbl_attach 내용을 업데이트함.
	 * 
	 * java.lang.ClassCastException: com.sci4s.grpc.SciRIO$ReqMsg cannot be cast to com.sci4s.grpc.SciRIO$Data
	 * 
	 */
	public GrpcResp updTblAttach4DocNO(Map<String,String> paramMap) {
		String   jsonData = null;	
		GrpcResp grpcResp = null;
		try {
			String xmlContent = tSysService.updTblAttach4DocNO(paramMap);
			jsonData = xmlContent;
			
			grpcResp = new GrpcResp();
			grpcResp.setErrCode("0");
			grpcResp.setErrMsg("");
			grpcResp.setResults(jsonData);
			return grpcResp;
		} catch (Exception e1) {
			String errMsg = ErrUtil.getPrintStackTrace(e1);
			logger.error("updTblAttach4DocNO() TRY CATCH Exception ::: "+ errMsg);
			grpcResp = FlatDataUtil.getErrGrpcResp("Exception", ErrConstance.ERR_9999, errMsg);	
			logger.error("updTblAttach4DocNO() TRY CATCH Exception results ::: "+ grpcResp.getResults());
			return grpcResp;
		} finally {
			if (jsonData != null) jsonData = null;
			if (grpcResp != null) grpcResp = null;
		}
	}
}

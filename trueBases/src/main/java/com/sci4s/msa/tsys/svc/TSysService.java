package com.sci4s.msa.tsys.svc;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sci4s.grpc.ErrConstance;
import com.sci4s.grpc.utils.GrpcDataUtil;
import com.sci4s.msa.mapper.TSysMapper;


@Service
public class TSysService implements TSysSvc{
	Logger logger = LoggerFactory.getLogger(TSysService.class);
	@Autowired
	public TSysMapper tsysMapper;
	
	/**
	 * 프로시져를 XML로 생성하여 리턴함.
	 */
	public String getServiceXml(Map<String,String> paramMap) throws Exception {
	
		StringBuffer sbXml = new StringBuffer();
		logger.debug("paramMap:::::::::::::::"+ paramMap);
		List<Map<String,Object>> lst = tsysMapper.getTblServiceDtl(paramMap);           

        if (lst == null || lst.size() == 0) {
        	sbXml.append(GrpcDataUtil.getGrpcResults(ErrConstance.NO_DATA, ErrConstance.NO_DATA, null));
        } else {
        	int zz = 0;
        	String prevPID = "";
        	sbXml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        	sbXml.append("<services>\n");	        	
        	for (Map<String,Object> rsMap : lst) {
        		
        		String currPID   = ""+ rsMap.get("procID");        		
        		String prevSql   = (rsMap.get("prevQuery")==null?"":""+rsMap.get("prevQuery"));
        		String prevMeth  = (rsMap.get("prevMethod")==null?"":""+rsMap.get("prevMethod"));
        		
        		String stepSVC   = (rsMap.get("stepSVC")==null?"":""+rsMap.get("stepSVC"));
        		String stepSql   = (rsMap.get("stepSql")==null?"":""+rsMap.get("stepSql"));
        		String stepMeth  = (rsMap.get("stepMeth")==null?"":""+rsMap.get("stepMeth"));
        		
        		String postSql   = (rsMap.get("postQuery")==null?"":""+rsMap.get("postQuery"));
        		String postMeth  = (rsMap.get("postMethod")==null?"":""+rsMap.get("postMethod"));
        		
        		String caseDoRun = (rsMap.get("caseDoRun")==null?"":""+rsMap.get("caseDoRun"));
        		String getKey    = (rsMap.get("getKey")==null?"":""+rsMap.get("getKey"));
        		String chkKey    = (rsMap.get("chkKey")==null?"":""+rsMap.get("chkKey"));
        		String getVar    = (rsMap.get("getVar")==null?"":""+rsMap.get("getVar"));
        		String caseRET   = (rsMap.get("caseRET")==null?"":""+rsMap.get("caseRET"));	        		
        		String getParams = (rsMap.get("getParams")==null?"":""+rsMap.get("getParams"));

        		if (!currPID.equals(prevPID)) {
        			if (zz > 0) {
        				sbXml.append("</proc>\n");
        			}
        			sbXml.append("<proc>");
        		}
        		sbXml.append("<step PID=\""+ rsMap.get("stepPID") +"\" ");
        		
        		if (prevSql.length() > 0) {
    				sbXml.append("prevQuery=\""+ prevSql +"\" ");
    			}
        		if (prevMeth.length() > 0) {
    				sbXml.append("prevMethod=\""+ prevMeth +"\" ");
    			}
        		sbXml.append("service=\""+ stepSVC +"\" query=\""+ stepSql +"\" method=\""+ stepMeth +"\" ");
        		if (postSql.length() > 0) {
    				sbXml.append("postQuery=\""+ postSql +"\" ");
    			}
        		if (postMeth.length() > 0) {
    				sbXml.append("postMethod=\""+ postMeth +"\" ");
    			}
        		
        		if (getKey.length() > 0) { // 부모 데이터에서 가져오는 데이터들
    				sbXml.append("getKey=\""+ getKey +"\" ");
    			}
        		
        		if (getParams.length() > 0) { // 부모파라미터에서 하위로 전달할 파라미터들
    				sbXml.append("getParams=\""+ getParams +"\" ");
    			}
        		
        		if (caseDoRun.length() > 0) { // 하위조건식이 있는 경우
        			sbXml.append("caseDoRun=\""+ caseDoRun +"\" ");
        			
        			
        			if (chkKey.length() > 0) { // 하위 데이터 처리할 때, 체크해야할 변수들 또는 제한할 값의 변수들
        				sbXml.append("chkKey=\""+ chkKey +"\" ");
        			}
        			if (getVar.length() > 0) { // 조건식에 대체할 변수명
        				sbXml.append("getVar=\""+ getVar +"\" ");
        			}
        			if (caseRET.length() > 0) { // 조건식의 결과(Boolean,Choice)
        				sbXml.append("caseRET=\""+ caseRET +"\" ");
        			}
        			sbXml.append(">\n");
        			
        			String subSVC        = (rsMap.get("subSVC")==null?"commService":""+rsMap.get("subSVC"));
        			String execMode      = (rsMap.get("execMode")==null?"Auto":""+rsMap.get("execMode"));
        			String subPrevQuery  = (rsMap.get("subPrevQuery")==null?"":""+rsMap.get("subPrevQuery"));
        			String subPrevMethod = (rsMap.get("subPrevMethod")==null?"":""+rsMap.get("subPrevMethod"));
        			String subQuery1     = (rsMap.get("subQuery1")==null?"":""+rsMap.get("subQuery1"));
        			String subQuery2     = (rsMap.get("subQuery2")==null?"":""+rsMap.get("subQuery2"));
        			String subMethod     = (rsMap.get("subMethod")==null?"":""+rsMap.get("subMethod"));        			
        			String subPostQuery  = (rsMap.get("subPostQuery")==null?"":""+rsMap.get("subPostQuery"));
        			String subPostMethod = (rsMap.get("subPostMethod")==null?"":""+rsMap.get("subPostMethod"));
        			String caseIF        = (rsMap.get("caseIF")==null?"":""+rsMap.get("caseIF"));
        			String throwMsg      = (rsMap.get("throwMsg")==null?"":""+rsMap.get("throwMsg"));        			
        			
        			if (subQuery1.length() > 0 || subQuery2.length() > 0) {
        				sbXml.append("<caseDo ");
        				sbXml.append("service=\""+ subSVC +"\" ");
        				
        				if (subPrevQuery.length() > 0) {
            				sbXml.append("subPrevQuery=\""+ subPrevQuery +"\" ");
            			}
                		if (subPrevMethod.length() > 0) {
            				sbXml.append("subPrevMethod=\""+ subPrevMethod +"\" ");
            			}
        				if (subQuery1.length() > 0) {
	        				sbXml.append("subQuery1=\""+ subQuery1 +"\" ");
	        			}
        				if (subQuery2.length() > 0) {
	        				sbXml.append("subQuery2=\""+ subQuery2 +"\" ");
	        			}
        				if (subMethod.length() > 0) {
	        				sbXml.append("subMethod=\""+ subMethod +"\" ");
	        			}
        				if (subPostQuery.length() > 0) {
            				sbXml.append("subPostQuery=\""+ subPostQuery +"\" ");
            			}
                		if (subPostMethod.length() > 0) {
            				sbXml.append("subPostMethod=\""+ subPostMethod +"\" ");
            			}
                		if (execMode.length() > 0) {
            				sbXml.append("execMode=\""+ execMode +"\" ");
            			}
                		if (throwMsg.length() > 0) {
            				sbXml.append("throwMsg=\""+ throwMsg +"\" ");
            			}
        				sbXml.append(">\n");
        				if (caseIF.length() > 0) {
	        				sbXml.append("<caseIF><![CDATA["+ caseIF +"]]></caseIF>\n");
	        			}	   
        				sbXml.append("</caseDo>\n");
        			}   			
        			sbXml.append("</step>\n");
        		} else {
        			sbXml.append("/>\n");
        		}
        		prevPID = ""+ rsMap.get("procID");
        		zz++;
        	}
        	sbXml.append("</proc>\n");
        	sbXml.append("</services>\n");
        }
        logger.debug(sbXml.toString());
        return sbXml.toString();
	}
	
	public String updTblAttach4DocNO(Map<String, String> params) throws Exception {
		tsysMapper.updTblAttach4DocNO(params);    
		return GrpcDataUtil.getGrpcResults("0", "SUCCESS", null);
	}

}

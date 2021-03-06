package com.sci4s.grpc.svc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sci4s.cnf.PIDSLoader;
import com.sci4s.grpc.SciRIO;
import com.sci4s.grpc.SciRIO.RetMsg;
import com.sci4s.grpc.dao.IDataDao;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.utils.GrpcDataUtil;
import com.sci4s.msa.mapper.CommMapper;
import com.sci4s.utils.AES256Util;

@Service
public class CommService implements CommSvc {
	
	Logger logger = LoggerFactory.getLogger(CommService.class);		

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
	
	@Value("${pid.xml.mode}")
	String PIDS_MODE;
	
	@Value("${mst.chk.time}")
	String MST_CHKTIME;
	
	@Value("${buffer.type}")
	String BUFFER_TYPE;
	
	String xmlUri;
	
	private PIDSLoader pids;
	private IDataDao dataDao;
	private CommMapper commMapper;
	@Autowired
	public CommService(IDataDao dataDao, CommMapper commMapper) {
		this.dataDao = dataDao;		
		this.commMapper = commMapper;
	}
	
	@PostConstruct
    private void init() {
		this.xmlUri = TSYS_URI +"|"+ MSA_AGENTID +"|"+ MSA_ID;	    
	    if (pids == null) {
	    	if ("XML".equals(this.PIDS_MODE)) {
	    		try { pids = PIDSLoader.getInstance(null, null); } catch(Exception ex) {}
	    	} else {
	    		try { pids = PIDSLoader.getInstance(this.xmlUri, this.BUFFER_TYPE); } catch(Exception ex) {}
	    	}
		}
    }
	
	public GrpcResp query4Data(String sqlID, Map<String,String> grpcMap) throws Exception {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		GrpcResp grpcResp = new GrpcResp();
		paramMap.putAll(grpcMap);
		paramMap.put("SQLMODE", this.SQLMODE);
		if (!paramMap.containsKey("clang")) {
			paramMap.put("clang", this.CLANG);
		}

		grpcResp = new DataService().query4Data(BUFFER_TYPE, dataDao, sqlID, paramMap);

		return grpcResp;
	}
	
	/**
	 * ????????? ????????? ?????? ????????? ?????????
	 * 
	 * @param  String sqlID
	 * @param  GrpcParams grpcPrms
	 * @return SciRIO.Data
	 */
	public GrpcResp query4Data(String sqlID, GrpcParams grpcPrms) throws Exception {
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		Map<String, Object> commInfoMap = this.getCommInfoMap(grpcPrms);
		GrpcResp grpcResp = new GrpcResp();
		paramMap.putAll(commInfoMap);
		
		if (!paramMap.containsKey("clang")) {
			paramMap.put("clang", this.CLANG);
		}

		grpcResp = new DataService().query4Data(BUFFER_TYPE, dataDao, sqlID, paramMap);
		
		return grpcResp;
	}
	
	/**
	 * pid-resolver.xml?????? ????????? ??????????????? ?????????.
	 * ???) ???????????? ???????????? ?????? ?????? ????????????????????? ???????????????.
	 * 
	 * @param sqlID
	 * @param grpcMap
	 * @return
	 * @throws Exception
	 */
	public GrpcResp query4Update(String sqlID, Map<String,String> grpcMap) throws Exception {	
		GrpcResp grpcResp = new GrpcResp();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.putAll(grpcMap);
		paramMap.put("SQLMODE", this.SQLMODE);
		if (!paramMap.containsKey("clang")) {
			paramMap.put("clang", this.CLANG);
		}
			
		grpcResp = new DataService().query4Update(BUFFER_TYPE, dataDao, sqlID, paramMap);
		
		return grpcResp;
	}
	
	@Transactional(rollbackFor = {Exception.class})
	public GrpcResp query4Update(String sqlID, GrpcParams grpcPrms) throws Exception {
		return query4Execute("query4Update", sqlID, grpcPrms);
	}
		
	@Transactional(rollbackFor = {Exception.class})
	public GrpcResp query4Update1(String sqlID, GrpcParams grpcPrms) throws Exception {	
		return query4Execute("query4Data", sqlID, grpcPrms);
	}
	
	private Map<String,Object> getCommInfoMap(GrpcParams grpcPrms) throws Exception {
		Map<String,Object> commInfoMap = null;
		try {
			commInfoMap = new DataService().getCommInfoMap(grpcPrms);
			commInfoMap.put("SQLMODE",  this.SQLMODE);
			commInfoMap.put("clang",    (grpcPrms.getClang()==null?this.CLANG:grpcPrms.getClang()));
			commInfoMap.put("TSYS_URI", this.TSYS_URI);
			commInfoMap.put("BUFFERTYPE",  this.BUFFER_TYPE);
			return commInfoMap;			
		} catch(Exception ex) {
			throw new Exception("@FAIL@Not have access rights.@FAIL@");
		}  finally {
        	if (commInfoMap != null) {
        		try { commInfoMap = null; } catch(Exception ex) { }
        	}
        }
	}
	
	@Transactional
	private GrpcResp query4Execute(String flag, String sqlID, GrpcParams grpcPrms) throws Exception {
		if (pids == null) {
			if ("XML".equals(this.PIDS_MODE)) {
	    		try { pids = PIDSLoader.getInstance(null, null); } catch(Exception ex) {}
	    	} else {
	    		try { pids = PIDSLoader.getInstance(this.xmlUri, this.BUFFER_TYPE); } catch(Exception ex) {}
	    	}
		}
		GrpcResp grpcResp = new GrpcResp();
		String PID = (grpcPrms.getpID()==null?"":grpcPrms.getpID());
		Map<String, Object> paramMap = null;
		Map<String,Object> commInfoMap = this.getCommInfoMap(grpcPrms);
		
		paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());	
		paramMap.putAll(commInfoMap);
		logger.info("367 query4Execute.grpcPrms."+ flag +".paramMap():::"+ paramMap);
		
		grpcResp = new DataService().query4Execute(BUFFER_TYPE, dataDao, pids, PID, flag, sqlID, paramMap, commInfoMap);
		
		return grpcResp;
	}
	
	@Transactional(rollbackFor = {Exception.class})
	public GrpcResp query4Updates(String sqlID, GrpcParams grpcPrms) throws Exception {	
		String PID     = grpcPrms.getpID();
		GrpcResp grpcResp = new GrpcResp();
		Map<String,Object> commInfoMap = this.getCommInfoMap(grpcPrms);
		Map<String, Object> paramMap = null;
		paramMap = GrpcDataUtil.getParams("params", grpcPrms.getData(), true);
		
		grpcResp = new DataService().query4Updates(BUFFER_TYPE, dataDao, pids, sqlID, PID, paramMap, commInfoMap);

		
		return grpcResp;
	}
	
	/**
	 * ???????????? ???????????? ???????????? ??????/??????/?????? ???????????? ????????? ?????????
	 * 
	 * @param  String sqlID
	 * @param  GrpcParams grpcPrms
	 * @return SciRIO.RetMsg
	 */
	@Transactional
	public GrpcResp query4Selects(String sqlID, GrpcParams grpcPrms) throws Exception {
		return query4Updates(sqlID, grpcPrms);
	}
	
	/**
	 * Master ????????? ????????? ??????????????? ??????, ???????????? ?????? ?????????
	 * 
	 * @param  Map<String,Object> paramMap
	 * @return String results
	 */
	public GrpcResp getMstInfo(GrpcParams grpcPrms) throws Exception {
		Map<String, Object> paramMap = null;
		paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		paramMap.put("chkTime",  this.MST_CHKTIME);
		paramMap.put("SQLMODE",  this.SQLMODE);
//		logger.info("paramMap :::: " + paramMap);
		return new DataService().getMstInfo(dataDao, paramMap);
	}
	
	public GrpcResp getMstInfo(Map<String, Object> paramMap) throws Exception {
		paramMap.put("chkTime",  this.MST_CHKTIME);
		paramMap.put("SQLMODE",  this.SQLMODE);
		
		return new DataService().getMstInfo(dataDao, paramMap);
	}
	
    /**
	 * ???????????? ????????? ????????? ??????????????? ??????, ???????????? ?????? ?????????
	 * 
	 * @param  Map<String,Object> paramMap
	 * @return String results
	 */
	public String getTblContReqDetail(Map<String,Object> paramMap) throws Exception {		
		
		paramMap.put("SQLMODE", this.SQLMODE);	
		if (!paramMap.containsKey("clang")) {
			paramMap.put("clang", this.CLANG);
		}	
		dataDao.query4Update1("updContReqDetail4IfUID", paramMap);
		
		return new DataService().query4Json(dataDao, "getContReqDetail4NotIF", paramMap);
	}
	
	/**
	 * ?????????????????? ???????????? ?????? ?????????
	 * 
	 * @param  Map<String,Object> paramMap
	 * @return String results
	 */
	public String updLastPrProcSTS(Map<String, Object> params) throws Exception {
		
		String prDtlUIDs = ""+ params.get("prDtlUIDs");
		List<Map<String,Object>> paramList = new ArrayList<Map<String,Object>>();
		if (prDtlUIDs.indexOf(",") >= 0) {
			String[] arrs = prDtlUIDs.split(",");
			for (int ii=0; ii<arrs.length; ii++) {
				Map<String,Object> pmap = new HashMap<String,Object>();
				pmap.put("prDtlUID", arrs[ii]);
				pmap.put("procSTS", params.get("procSTS"));
				pmap.put("userUID", "0");
				pmap.put("SQLMODE", this.SQLMODE);
				if (!params.containsKey("clang")) {
					params.put("clang", this.CLANG);
				} else {
					pmap.put("clang", params.get("clang"));
				}				
				paramList.add(pmap);
			}
		} else {
			Map<String,Object> pmap = new HashMap<String,Object>();
			pmap.put("prDtlUID", prDtlUIDs);
			pmap.put("procSTS", params.get("procSTS"));
			pmap.put("userUID", "0");
			pmap.put("SQLMODE", this.SQLMODE);
			if (!params.containsKey("clang")) {
				params.put("clang", this.CLANG);
			} else {
				pmap.put("clang", params.get("clang"));
			}
			paramList.add(pmap);
		}
		
		/* ?????????????????? ?????? ????????? ?????? prDtlUID, prUID, procSTS, userUID */
		dataDao.query4Update3("syncTblPrDetail4ProcSTS", paramList);
		
		params.remove("prDtlUIDs");
		params.put("SQLMODE", this.SQLMODE);	
		params.put("userUID", "0");
		if (!params.containsKey("clang")) {
			params.put("clang", this.CLANG);
		}
		/* ???????????? ?????? ????????? ?????? prUID, userUID */
		dataDao.query4Update1("syncTblPrMaster4ProcSTS", params);
		
		return GrpcDataUtil.getGrpcResults("0", "SUCCESS", null);
	}
	
	/**
	 * ?????? ??????????????? ?????? ???????????? ?????? ?????????
	 * 
	 * @param  Map<String,Object> paramMap
	 * @return String results
	 */
	public String insNewCustVdInfo(Map<String, Object> params) throws Exception {
		
		dataDao.query4Update1("insNewCustVdInfo", params);
		
		return GrpcDataUtil.getGrpcResults("0", "SUCCESS", null);
	}

	/**
	 * ?????? ???????????? ????????? ???????????? ?????????.
	 * ???????????? : agentID|userUID|uuID|apvUID
	 * 
	 * @param  Map<String,Object> paramMap
	 * @return String results
	 */
	@Transactional
	public String delApvInfo4ApvUID(Map<String, Object> params) throws Exception {		
		
		dataDao.query4Update1("insApvDetailHist", params);		
		dataDao.query4Update1("delApvDetail4apvUID", params);
		
		dataDao.query4Update1("insApvMasterHist", params);
		dataDao.query4Update1("delApvMaster4apvUID", params);		
		
		return GrpcDataUtil.getGrpcResults("0", "SUCCESS", null);
	}	
	
	/**
  	 * ??????????????? ????????? ??????.
  	 * ???????????? : queryType|queryText
  	 * 
  	 * @param  Map<String,Object> paramMap
  	 * @return String results
  	 */
	@Transactional(rollbackFor = {Exception.class})
  	public GrpcResp query4Test(GrpcParams grpcPrms) throws Exception {
		Map<String, Object> paramMap = null;	
  		Map<String, Object> commInfoMap = this.getCommInfoMap(grpcPrms);
  		GrpcResp grpcResp = new GrpcResp();
  		
  		paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());	
  		logger.info("query4Data.SQLMODE ::: "+ this.SQLMODE);
		paramMap.putAll(commInfoMap);
		if (!paramMap.containsKey("clang")) {
			paramMap.put("clang", this.CLANG);
		}
		logger.info("query4Data.paramMap ::: "+ paramMap);
		
		grpcResp = new DataService().query4Test(BUFFER_TYPE, dataDao, paramMap);

		return grpcResp;
  	}
}
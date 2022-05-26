package com.sci4s.msa.tsrc.svc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sci4s.grpc.ErrConstance;
import com.sci4s.grpc.MsaApiGrpc;
import com.sci4s.grpc.SciRIO;
import com.sci4s.grpc.SciRIO.RetMsg;
import com.sci4s.grpc.dao.DataDao;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.dto.GrpcResp;
import com.sci4s.grpc.svc.Channel;
import com.sci4s.grpc.svc.CommService;
import com.sci4s.grpc.svc.CommonService;
import com.sci4s.grpc.utils.GrpcDataUtil;
import com.sci4s.msa.mapper.TO2oOrderMapper;
import com.sci4s.utils.AES256Util;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

@Service
public class O2bOrderService implements O2bOrderSvc {
	
	Logger logger = LoggerFactory.getLogger(O2bOrderService.class);	

	@Value("${db.dbtype}")
	String SQLMODE;
	
	@Value("${buffer.type}")
	String BUFFER_TYPE;
	
	@Value("${thr.buffer.type}")
	String THR_BUFFER_TYPE;
	
//	@Value("${mst.thr.uri}")
//	String THR_URL;
	String THR_URL = "127.0.0.1:18999";
	
	@Value("${daum.api.key}")
	String DAUM_APIKEY;
	
	@Value("${msa.tls}")
	String msa_tls;
	
	@Value("${ca.pem.file}")
	String caPemFile;
	
	@Value("${crt.pem.file}")
	String crtPemFile;
	
	@Value("${private.key.file}")
	String privateKeyFile;	
	
	DataDao dataDao;

	TO2oOrderMapper to2oOrderMapper;
	
	@Autowired
	public  O2bOrderService(TO2oOrderMapper to2oOrderMapper, DataDao dataDao){
	    this.to2oOrderMapper = to2oOrderMapper;
	    this.dataDao = dataDao;
	}
	
	@Override
	public GrpcResp getTblOrderList(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		GrpcResp grpcResp = new GrpcResp();
		Map<String, Object> paramMap = GrpcDataUtil.getParams("params", grpcPrms.getData(), true);
		Map<String,Object> commInfoMap = new CommService(dataDao).getCommInfoMap(grpcPrms);
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@ getTblOrderList paramMap ===== " + paramMap + "  @@@@@@@@@@@@@@@@@@@@@@@ commInfoMap ===== " + commInfoMap);		
		Map<String, Object> sqlParam = new HashMap<>();
		String menuUid = (String) dataDao.query4Object1("getUUID", paramMap);
		paramMap.put("menuUID", menuUid);
		paramMap.putAll(commInfoMap);
		paramMap.put("SQLMODE",  this.SQLMODE);
		
    	try {
    		List<Object> rsList = dataDao.query4List2("getOrderList", paramMap);
    		
//    		Map<String,Object> rsMap = to2oOrderMapper.getOrderList(param);
    		if (rsList == null || rsList.size() == 0) {
   	        	errMsg   = "null";
   	        	errCode  = ErrConstance.NO_DATA;
   	    		jsonRet = GrpcDataUtil.getGrpcResults("NO_DATA", errMsg, null);
   	        } else {    
   	        	jsonRet = "{\"results\":"+ new ObjectMapper().writeValueAsString(rsList) + "}";
   	        }
    		
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		errCode = ErrConstance.ERR_9999;
    		errMsg  = ex.toString();
    		jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, ex.toString(), null);
    	} finally {
	       	if (paramMap != null) {
	       		try { paramMap = null; } catch(Exception ex) { }
	       	}
    	}
    	grpcResp.setResults(jsonRet);
    	grpcResp.setErrCode(errMsg);
    	grpcResp.setErrMsg(errCode);
		
		return grpcResp;
	}	
	
	
	@Override
	public GrpcResp getOrderDetail(GrpcParams grpcPrms) throws Exception{
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		GrpcResp grpcResp = new GrpcResp();

		logger.info("@@@@@@@@@@@@@@@@@@@@@@@ getOrderDetail @@@@@@@@@@@@@@@@@@@@@@@");		
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());

		
		try {
    		List<Object> rsList = dataDao.query4List2("getOrderDetail", paramMap);
    		
//    		Map<String,Object> rsMap = to2oOrderMapper.getOrderList(param);
    		if (rsList == null || rsList.size() == 0) {
   	        	errMsg   = "null";
   	        	errCode  = ErrConstance.NO_DATA;
   	    		jsonRet = GrpcDataUtil.getGrpcResults("NO_DATA", errMsg, null);
   	        } else {  
   	        	jsonRet = "{\"results\":"+ new ObjectMapper().writeValueAsString(rsList) + "}";
   	        }
    		
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		errCode = ErrConstance.ERR_9999;
    		errMsg  = ex.toString();
    		jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, ex.toString(), null);
    	} finally {
	       	if (paramMap != null) {
	       		try { paramMap = null; } catch(Exception ex) { }
	       	}
    	}
    	grpcResp.setResults(jsonRet);
    	grpcResp.setErrCode(errMsg);
    	grpcResp.setErrMsg(errCode);
		
		return grpcResp;
	}

	@Override
	public GrpcResp saveCategoryMenuList(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		GrpcResp grpcResp = new GrpcResp();
		Map<String, Object> paramMap = GrpcDataUtil.getParams("params", grpcPrms.getData(), true);
		Map<String,Object> commInfoMap = new CommService(dataDao).getCommInfoMap(grpcPrms);
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@ paramMap ===== " + paramMap + "  @@@@@@@@@@@@@@@@@@@@@@@ commInfoMap ===== " + commInfoMap);		
		
		List<String> reqKeys = null;
		String firstKey = null;
		List<Map<String, Object>> params = null;
		List<Map<String, Object>> subParams = null;
		Map<String, Object> finalMap = null;
		reqKeys  = (List<String>)paramMap.get("KEYS");	
		params   = ((List<Map<String, Object>>)paramMap.get("params"));	

		finalMap = params.get(0);
		firstKey = new CommonService().getMapFirstKey4List(finalMap);
		if (firstKey == null) {
			firstKey = this.getFirstKey(reqKeys);
		}
//		logger.info("reqKeys ==== " + reqKeys + " / params ==== " + params  + " / subParams ==== " + subParams   + " / firstKey ==== " + firstKey);		
		
		try {
			List<Map<String,Object>> paramList = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> subParamList = new ArrayList<Map<String,Object>>();
			for(int ii=0; ii<params.size(); ii++) {
				Map<String,Object> sqlParam = null;
				Map<String,Object> sqlParam2 = null;
				Map<String,Object> param = params.get(ii);
				if (param.get(firstKey) instanceof List) {
					List<String> firstLst = (List<String>)param.get(firstKey);
					for (int ll=0; ll<firstLst.size(); ll++) {
						sqlParam = new HashMap<String, Object>();	
						sqlParam2 = new HashMap<String, Object>();	
						if (firstLst.get(ll) != null && !firstLst.get(ll).isEmpty()) {
							sqlParam.put(firstKey, firstLst.get(ll));
						}

						String menuUid = (String) dataDao.query4Object1("getUUID", param);
						sqlParam.put("menuUID", menuUid);
						sqlParam.putAll(commInfoMap);
						sqlParam.put("SQLMODE",  this.SQLMODE);
						
						for (String colID : reqKeys) {
							if (!colID.equals(firstKey)) {
								// 리스트가 아닌 데이터는 여기서는 저장하지 않음.
								if("bogOptUID".equals(colID)) {
									String bogOptUid = GrpcDataUtil.getVal4MapList(param,colID, ll);
									String[] bogOptUids = null;
									if(bogOptUid != null && !"".equals(bogOptUid)) {
										if(bogOptUid.indexOf("|") >= 0) {
											bogOptUids = bogOptUid.split("\\|");
											for(int i=0; i<bogOptUids.length; i++) {
												sqlParam2 = new HashMap<String, Object>();	
												sqlParam2.put(colID, bogOptUids[i]);
												sqlParam2.put("menuUID", menuUid);
												sqlParam2.putAll(commInfoMap);
												sqlParam2.put("SQLMODE",  this.SQLMODE);
												subParamList.add(sqlParam2);
											}
										} else {
											sqlParam2.put(colID, bogOptUid);
											sqlParam2.put("menuUID", menuUid);
											sqlParam2.putAll(commInfoMap);
											sqlParam2.put("SQLMODE",  this.SQLMODE);
											
											subParamList.add(sqlParam2);
										}
									}
									
								} else {
									if (param.get(colID) instanceof List) {
										sqlParam.put(colID, GrpcDataUtil.getVal4MapList(param,colID, ll));
									}
								}
							}
						}
						//logger.debug("944 sqlParam -> "+ sqlParam);
						paramList.add(sqlParam);
					}	
				}
			}
			
			if(paramList.size() > 0) {
				dataDao.query4Update3("saveCategoryMenuList", paramList);
			}
			
			if(subParamList.size() > 0) {
				dataDao.query4Update3("saveMenuOptionList", subParamList);
			}
			
			
//			logger.info("paramList ==== " +paramList);		
			
			jsonRet = GrpcDataUtil.getGrpcResults("0", ErrConstance.NO_ERROR, null);
			
		} catch(Exception ex) {
			ex.printStackTrace();
			errCode = ErrConstance.ERR_9999;
			errMsg  = ex.toString();
			jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, ex.toString(), null);
		} finally {
	       	if (paramMap != null) {
	       		try { paramMap = null; } catch(Exception ex) { }
	       	}
		}
		grpcResp.setResults(jsonRet);
		grpcResp.setErrCode(errMsg);
		grpcResp.setErrMsg(errCode);
		
		return grpcResp;
	}
	
	
	
	@Override
	public GrpcResp updCategoryMenuList(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		GrpcResp grpcResp = new GrpcResp();
		List<Map<String,Object>> paramList = new ArrayList<Map<String,Object>>();
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		Map<String,Object> commInfoMap = new CommService(dataDao).getCommInfoMap(grpcPrms);
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@ paramMap ===== " + paramMap + "  @@@@@@@@@@@@@@@@@@@@@@@ commInfoMap ===== " + commInfoMap);
		
		try {
			Map<String,Object> sqlParam = null;
			String bogOptUid = (String) paramMap.get("bogOptUID");
			String[] bogOptUids = null;
			if(bogOptUid != null) {
				if(bogOptUid.indexOf("|") >= 0) {
					bogOptUids = bogOptUid.split("\\|");
				} else if(bogOptUid.indexOf(",") >= 0) {
					bogOptUids = bogOptUid.split(",");
				} else {
					bogOptUids = new String[] {bogOptUid};
				}
				
				for(int i=0; i<bogOptUids.length; i++) {
					sqlParam = new HashMap<String, Object>();	
					sqlParam.put("bogOptUID", bogOptUids[i]);
					sqlParam.put("menuUID", paramMap.get("menuUID"));
					sqlParam.putAll(commInfoMap);
					sqlParam.put("SQLMODE",  this.SQLMODE);
					paramList.add(sqlParam);
				}
			}
			
			dataDao.query4Update1("updBorgCategoryMenu", paramMap);
			
			if(paramList.size() > 0) {
				dataDao.query4Update1("delBorgCategoryMenuOption", paramMap);
				
				dataDao.query4Update3("saveMenuOptionList", paramList);
			}
			
			jsonRet = GrpcDataUtil.getGrpcResults("0", ErrConstance.NO_ERROR, null);
			
		} catch(Exception ex) {
			ex.printStackTrace();
			errCode = ErrConstance.ERR_9999;
			errMsg  = ex.toString();
			jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, ex.toString(), null);
		} finally {
	       	if (paramMap != null) {
	       		try { paramMap = null; } catch(Exception ex) { }
	       	}
		}
		
		grpcResp.setResults(jsonRet);
		grpcResp.setErrCode(errMsg);
		grpcResp.setErrMsg(errCode);
		
		return grpcResp;
	}
	
	@Override
	public GrpcResp getOrderChannelList(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		GrpcResp grpcResp = new GrpcResp();
		AES256Util aes256 = new AES256Util();
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@ getOrderDetail @@@@@@@@@@@@@@@@@@@@@@@");		
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		Map<String,Object> commInfoMap = new CommService(dataDao).getCommInfoMap(grpcPrms);
		paramMap.putAll(commInfoMap);
		
		try {
    		List<Object> rsList = dataDao.query4List2("getBorgsChannels", paramMap);
    		
    		if (rsList == null || rsList.size() == 0) {
   	        	errMsg   = "null";
   	        	errCode  = ErrConstance.NO_DATA;
   	    		jsonRet = GrpcDataUtil.getGrpcResults("NO_DATA", errMsg, null);
   	        } else {  
   	        	for(int ii=0; ii<rsList.size(); ii++) {
   	        		Map<String, Object> params = (Map<String, Object>) rsList.get(ii);
   	        		Map<String, Object> params2  = new HashMap<String, Object>();
   	        		
   	        		if(params.containsKey("loginPwd")) {
   	        			params2.put("pwd", aes256.decryptII((String) params.get("loginPwd")));
   	        			rsList.add(params2);
   	        		}
   	        	}
   	        	
   	        	
   	        	jsonRet = "{\"results\":"+ new ObjectMapper().writeValueAsString(rsList) + "}";
   	        }
    		
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		errCode = ErrConstance.ERR_9999;
    		errMsg  = ex.toString();
    		jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, ex.toString(), null);
    	} finally {
	       	if (paramMap != null) {
	       		try { paramMap = null; } catch(Exception ex) { }
	       	}
    	}
    	grpcResp.setResults(jsonRet);
    	grpcResp.setErrCode(errMsg);
    	grpcResp.setErrMsg(errCode);
		
		return grpcResp;
	}
	
	@Override
	public GrpcResp saveBorgChannelList(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		GrpcResp grpcResp = new GrpcResp();
		AES256Util aes256 = new AES256Util();
		Map<String, Object> paramMap = GrpcDataUtil.getParams("params", grpcPrms.getData(), true);
		Map<String,Object> commInfoMap = new CommService(dataDao).getCommInfoMap(grpcPrms);
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@ paramMap ===== " + paramMap + "  @@@@@@@@@@@@@@@@@@@@@@@ commInfoMap ===== " + commInfoMap);		
		
		List<String> reqKeys = null;
		String firstKey = null;
		List<Map<String, Object>> params = null;
		List<Map<String, Object>> subParams = null;
		Map<String, Object> finalMap = null;
		reqKeys  = (List<String>)paramMap.get("KEYS");	
		params   = ((List<Map<String, Object>>)paramMap.get("params"));	

		finalMap = params.get(0);
		firstKey = new CommonService().getMapFirstKey4List(finalMap);
		if (firstKey == null) {
			firstKey = this.getFirstKey(reqKeys);
		}
//		logger.info("reqKeys ==== " + reqKeys + " / params ==== " + params  + " / subParams ==== " + subParams   + " / firstKey ==== " + firstKey);		
		
		try {
			List<Map<String,Object>> paramList = new ArrayList<Map<String,Object>>();
			for(int ii=0; ii<params.size(); ii++) {
				Map<String,Object> sqlParam = null;
				Map<String,Object> param = params.get(ii);
				if (param.get(firstKey) instanceof List) {
					List<String> firstLst = (List<String>)param.get(firstKey);
					for (int ll=0; ll<firstLst.size(); ll++) {
						sqlParam = new HashMap<String, Object>();	
						if (firstLst.get(ll) != null && !firstLst.get(ll).isEmpty()) {
							sqlParam.put(firstKey, firstLst.get(ll));
						}
						sqlParam.putAll(commInfoMap);
						sqlParam.put("SQLMODE",  this.SQLMODE);
						
						for (String colID : reqKeys) {
							if (!colID.equals(firstKey)) {
								if (param.get(colID) instanceof List) {
									if("loginPwd".equals(colID)) {
										sqlParam.put(colID, aes256.encryptII(GrpcDataUtil.getVal4MapList(param,colID, ll)));
									} else {
										sqlParam.put(colID, GrpcDataUtil.getVal4MapList(param,colID, ll));
									}
									
								}
							}
						}
						//logger.debug("944 sqlParam -> "+ sqlParam);
						paramList.add(sqlParam);
					}	
				}
			}
			
			if(paramList.size() > 0) {
				dataDao.query4Update3("saveBorgChannelList", paramList);
			}
			
			jsonRet = GrpcDataUtil.getGrpcResults("0", ErrConstance.NO_ERROR, null);
			
			logger.debug("944 paramList -> " + paramList);
		} catch(Exception ex) {
			ex.printStackTrace();
			errCode = ErrConstance.ERR_9999;
			errMsg  = ex.toString();
			jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, ex.toString(), null);
		} finally {
	       	if (paramMap != null) {
	       		try { paramMap = null; } catch(Exception ex) { }
	       	}
		}
		
		
		grpcResp.setResults(jsonRet);
    	grpcResp.setErrCode(errMsg);
    	grpcResp.setErrMsg(errCode);
		
		return grpcResp;
	}

	@Override
	public GrpcResp updBorgChannelList(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		GrpcResp grpcResp = new GrpcResp();
		AES256Util aes256 = new AES256Util();
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		Map<String,Object> commInfoMap = new CommService(dataDao).getCommInfoMap(grpcPrms);
		paramMap.putAll(commInfoMap);
		paramMap.put("SQLMODE",  this.SQLMODE);
		if(paramMap.get("loginPwd") != null) {
			if(!paramMap.get("rawPwd").equals(paramMap.get("loginPwd"))) {
				paramMap.put("loginPwd",  aes256.encryptII("" + paramMap.get("loginPwd")));
			}
		}
		
		
		if(paramMap.size() > 0) {
			dataDao.query4Update1("updBorgChannel", paramMap);
		}
		
		jsonRet = GrpcDataUtil.getGrpcResults("0", ErrConstance.NO_ERROR, null);
		
		grpcResp.setResults(jsonRet);
    	grpcResp.setErrCode(errMsg);
    	grpcResp.setErrMsg(errCode);
		
		return grpcResp;
	}

	
	
	@Override
	public GrpcResp saveOrderChannelList(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		String   chkYN  = "";
		String channel = "";
		GrpcResp grpcResp = new GrpcResp();
		AES256Util aes256 = new AES256Util();
		
		Map<String, Object> subMap = null;
		Map<String, Object> paramMap = GrpcDataUtil.getParams4Map("params", grpcPrms.getData());
		Map<String,Object> commInfoMap = new CommService(dataDao).getCommInfoMap(grpcPrms);
		paramMap.putAll(commInfoMap);
		paramMap.put("SQLMODE",  this.SQLMODE);
		chkYN  = "" + paramMap.get("chkYN");
		channel = ""+ paramMap.get("channel");
		
		if(paramMap.get("loginPwd") != null) {
			if(!paramMap.get("rawPwd").equals(paramMap.get("loginPwd"))) {
				paramMap.put("loginPwd",  aes256.encryptII("" + paramMap.get("loginPwd")));
			}
		}
		
		if(!"Y".equals(chkYN)) {
			subMap = new HashMap<String, Object>();
			subMap.putAll(paramMap);
			if("BAM".equals(channel)) {
				subMap.put("channel", "BM1");
				String channelUID = (String) dataDao.query4Object1("getChannelUID", subMap);
				subMap.put("channelUID", channelUID);
				subMap.put("logInYn", "N");
			} else if("YOG".equals(channel)) {
				subMap.put("channel", "YOS");
				String channelUID = (String) dataDao.query4Object1("getChannelUID", subMap);
				subMap.put("channelUID", channelUID);
			} else {
				subMap = null;
			}
		} else {
			subMap = new HashMap<String, Object>();
			subMap.putAll(paramMap);
			if("BAM".equals(channel)) {
				subMap.put("channel", "BM1");
				String channelUID = (String) dataDao.query4Object1("getChannelUID", subMap);
				subMap.put("channelUID", channelUID);
			} else if("YOG".equals(channel)) {
				subMap.put("channel", "YOS");
				String channelUID = (String) dataDao.query4Object1("getChannelUID", subMap);
				subMap.put("channelUID", channelUID);
			} else {
				subMap = null;
			}
		}
		
		try {
			if("Y".equals(chkYN)) {
				dataDao.query4Update1("updBorgChannel", paramMap);
			} else {
				dataDao.query4Update1("saveBorgChannel", paramMap);
			}
			
			if(subMap != null) {
				if("Y".equals(chkYN)) {
					dataDao.query4Update1("updBorgChannel", subMap);
				} else {
					dataDao.query4Update1("saveBorgChannel", subMap);
				}
			}
			
			jsonRet = GrpcDataUtil.getGrpcResults("0", ErrConstance.NO_ERROR, null);
		} catch(Exception ex) {
			ex.printStackTrace();
			errCode = ErrConstance.ERR_9999;
			errMsg  = ex.toString();
			jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, ex.toString(), null);
		} finally {
	       	if (paramMap != null) {
	       		try { paramMap = null; } catch(Exception ex) { }
	       	}
	       	if (subMap != null) {
	       		try { subMap = null; } catch(Exception ex) { }
	       	}
		}
		grpcResp.setResults(jsonRet);
    	grpcResp.setErrCode(errMsg);
    	grpcResp.setErrMsg(errCode);
		
		return grpcResp;
	}

	private String getFirstKey(List<String> reqKeys) {
		int keyInx = 0;
		String firstKey = ""; // agentID,pMenuID,dtype,PID 제외
		String upperKey = "";				
		
		List<String> excepts = new ArrayList<String>();
		excepts.add("AGENTID");
		excepts.add("PMENUID");
		excepts.add("DTYPE");
		excepts.add("PID");
		excepts.add("USERIP");
		excepts.add("SERVERIP");
		excepts.add("USERUID");
		excepts.add("BORGUID");
		excepts.add("SQLMODE");
		excepts.add("CLANG");
		
		while (true) {
			firstKey = reqKeys.get(keyInx);
			upperKey = firstKey.toUpperCase();
			keyInx++;
			if (!excepts.contains(upperKey)) {
				break;
			}
		}
		return firstKey;
	}
	
	public GrpcResp getDaumMapAddress() throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		GrpcResp grpcResp = new GrpcResp();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<Map<String, Object>> rsParamList = null;
		try {
    		List<Map<String, Object>> rsList = dataDao.query4List1("getDaumMapAddress", paramMap);
    		rsParamList = new ArrayList<Map<String, Object>>();
    		if (rsList == null || rsList.size() == 0) {
   	        	errMsg   = "null";
   	        	errCode  = ErrConstance.NO_DATA;
   	    		jsonRet = GrpcDataUtil.getGrpcResults("NO_DATA", errMsg, null);
   	        } else {  
   	        	for(int ii=0; ii<rsList.size(); ii++) {
   	        		Map<String, Object> param = rsList.get(ii);
   	        		String[] spt = null;
   	        		boolean chkSpt = true;
   	        		String pickupAddress = (String) param.get("pickupAddress");
   	        		String cPickupAddress = convertByAddr(pickupAddress);
   	        		Map<String, Object> daumParam = coordToAddr(pickupAddress);
   	        		daumParam.put("orderUid", param.get("orderUid"));
   	        		
   	        		if(daumParam.get("address") != null) {
   	        			daumParam.put("addrChkYN", "Y");
	   	        		if(daumParam.get("roadNameAddress") != null) {
							if(cPickupAddress.indexOf((String) daumParam.get("roadNameAddress")) >= 0) {
								spt = cPickupAddress.split((String) daumParam.get("roadNameAddress"));
								chkSpt = false;
							}
						}
	   	        		
	   	        		if(chkSpt) {
	   	        			if(cPickupAddress.indexOf((String) daumParam.get("address")) >= 0) {
	   							spt = cPickupAddress.split((String) daumParam.get("address"));
	   							
	   						}
	   	        		}
	   	        		
						if(spt != null) {
							for(int i=0; i<spt.length; i++) {
								if(spt[i] != "") {
									daumParam.put("addressDetail", spt[i].stripLeading());
								}
							}
						}		
   	        		} else {
   	        			daumParam.put("addrChkYN", "E");
   	        		}
   	        		rsParamList.add(daumParam);
   	        	}
   	        	if(rsParamList.size() > 0) {
   	        		dataDao.query4Update3("updDaumMapAddress", rsParamList);
   	        	}
   	        }
    		
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		errCode = ErrConstance.ERR_9999;
    		errMsg  = ex.toString();
    		jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, ex.toString(), null);
    	} finally {
	       	if (paramMap != null) {
	       		try { paramMap = null; } catch(Exception ex) { }
	       	}
    	}
		
    	grpcResp.setResults(jsonRet);
    	grpcResp.setErrCode(errMsg);
    	grpcResp.setErrMsg(errCode);
		
		return grpcResp;
	}
	
	public Map<String, Object> coordToAddr(String addrs) throws UnsupportedEncodingException{
		 String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + URLEncoder.encode(addrs, "utf-8");
//		 String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + addrs;
//		 String url = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?x=127.1086228&y=37.4012191";
		 String query = addrs;
		 Map<String, Object> addr = null;
		 try{
			 String jsonData = getJSONData(url);
//			 System.out.println(jsonData);
			 if(jsonData != null && !"".equals(jsonData)) {
				 addr = getRegionAddress(jsonData);
//				 Map<String,Object> params = new ObjectMapper().readValue(addr, Map.class);
//				 System.out.println("addr===== " + addr);
			 }
		 }catch(Exception e){
			 System.out.println("주소 api 요청 에러");
			 e.printStackTrace();
		 }
		 return addr;
    }

  /**
    * REST API로 통신하여 받은 JSON형태의 데이터를 String으로 받아오는 메소드
    */
	private String getJSONData(String apiUrl) throws Exception {
		HttpURLConnection conn = null;
		StringBuffer response = new StringBuffer();
		 
		//인증키 - KakaoAK하고 한 칸 띄워주셔야해요!
		String auth = "KakaoAK " + this.DAUM_APIKEY;
		
		//URL 설정
		URL url = new URL(apiUrl);
		
		conn = (HttpURLConnection) url.openConnection();
		
		//Request 형식 설정
		conn.setRequestMethod("GET");
		conn.setRequestProperty("X-Requested-With", "curl");
		conn.setRequestProperty("Authorization", auth);
		
		
		//request에 JSON data 준비
		conn.setDoOutput(true);
		 
		//보내고 결과값 받기
		int responseCode = conn.getResponseCode();
		if (responseCode == 400) {
		    System.out.println("400:: 해당 명령을 실행할 수 없음");
		} else if (responseCode == 401) {
		    System.out.println("401:: Authorization가 잘못됨");
		} else if (responseCode == 500) {
		    System.out.println("500:: 서버 에러, 문의 필요");
		} else { // 성공 후 응답 JSON 데이터받기
			System.out.println("responseCode ::: " + responseCode);
			Charset charset = Charset.forName("UTF-8");
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
		     
		    String inputLine;
		    while ((inputLine = br.readLine()) != null) {
		 	   response.append(inputLine); 
		    } 
		}
		 
		return response.toString();
  }
  
   /**
    * JSON형태의 String 데이터에서 주소값(address_name)만 받아오기
    * @throws ParseException 
    */
	private Map<String, Object> getRegionAddress(String jsonString) throws ParseException {
      Map<String, Object> params = new HashMap<String, Object>();
      
      JSONObject jObj = (JSONObject) new JSONParser().parse(jsonString);
      JSONObject meta = (JSONObject) jObj.get("meta");
      long size = (long) meta.get("total_count");
      
      if(size>0){
          JSONArray jArray = (JSONArray) jObj.get("documents");
          JSONObject subJobj = (JSONObject) jArray.get(0);
          JSONObject roadAddress =  (JSONObject) subJobj.get("road_address");
          JSONObject subsubJobj = (JSONObject) subJobj.get("address");
          
          if(roadAddress == null){
              params.put("address", subsubJobj.get("address_name"));
          }else{
              params.put("roadNameAddress", roadAddress.get("address_name"));
              params.put("address", subsubJobj.get("address_name"));
              
          }
          params.put("latitude", subsubJobj.get("y"));
          params.put("longitude", subsubJobj.get("x"));
          
      }
      return params;
  }
	
	public String convertByAddr(String addrs) {
    	String result = "";
    	if(addrs.indexOf("서울특별시") >= 0) {
    		result = addrs.replace("서울특별시", "서울");
    	} else if(addrs.indexOf("인천광역시") >= 0) {
    		result = addrs.replace("인천광역시", "인천");
    	} else if(addrs.indexOf("대전광역시") >= 0) {
    		result = addrs.replace("대전광역시", "대전");
    	} else if(addrs.indexOf("대구광역시") >= 0) {
    		result = addrs.replace("대구광역시", "대구");
    	} else if(addrs.indexOf("광주광역시") >= 0) {
    		result = addrs.replace("광주광역시", "광주");
    	} else if(addrs.indexOf("부산광역시") >= 0) {
    		result = addrs.replace("부산광역시", "부산");
    	} else if(addrs.indexOf("울산광역시") >= 0) {
    		result = addrs.replace("울산광역시", "울산");
    	} else if(addrs.indexOf("경기도") >= 0) {
    		result = addrs.replace("경기도", "경기");
    	} else if(addrs.indexOf("강원도") >= 0) {
    		result = addrs.replace("강원도", "강원");
    	} else if(addrs.indexOf("충청북도") >= 0) {
    		result = addrs.replace("충청북도", "충북");
    	} else if(addrs.indexOf("충청남도") >= 0) {
    		result = addrs.replace("충청남도", "충남");
    	} else if(addrs.indexOf("전라북도") >= 0) {
    		result = addrs.replace("전라북도", "전북");
    	} else if(addrs.indexOf("전라남도") >= 0) {
    		result = addrs.replace("전라남도", "전남");
    	} else if(addrs.indexOf("경상북도") >= 0) {
    		result = addrs.replace("경상북도", "경북");
    	} else if(addrs.indexOf("경상남도") >= 0) {
    		result = addrs.replace("경상남도", "경남");
    	} else {
    		result = addrs;
    	}
    	
    	return result;
    }
	
	public GrpcResp getGrpcChannels(GrpcParams gprms, String sql) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		GrpcResp grpcResp = new GrpcResp();
		List<Map<String,Object>> result = null;
		
		try {
			Channel grpcChannel = new Channel();
			grpcChannel.openChannel(this.THR_URL);
			result = (List<Map<String, Object>>) grpcChannel.callRPC(gprms, this.THR_BUFFER_TYPE, "getData");
			System.out.println("result ::::: " + result);
			
			if(result.size() > 0) {
	        	dataDao.query4Update3(sql, result);
	        }
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		errCode = ErrConstance.ERR_9999;
    		errMsg  = ex.toString();
    		jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, ex.toString(), null);
    	} finally {
	       	if (gprms != null) {
	       		try { gprms = null; } catch(Exception ex) { }
	       	}
    	}
		
    	grpcResp.setResults(jsonRet);
    	grpcResp.setErrCode(errMsg);
    	grpcResp.setErrMsg(errCode);
		
		return grpcResp;
	} 
	
	
	/**
	 * 주문관리 > 주문수정
	 * **/
	@SuppressWarnings("unchecked")
	@Override
	public GrpcResp updOrder(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		String   orderDuid = "";
		
		GrpcResp grpcResp = new GrpcResp();
		AES256Util aes256 = new AES256Util();
		Map<String, Object> paramMap = GrpcDataUtil.getParams("params", grpcPrms.getData(), true);
		Map<String,Object> commInfoMap = new CommService(dataDao).getCommInfoMap(grpcPrms);
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@ paramMap ===== " + paramMap + "  @@@@@@@@@@@@@@@@@@@@@@@ commInfoMap ===== " + commInfoMap);		
		
		List<String> reqKeys = null;
		String firstKey = null;
		String updateMKey = null;
		String updateOKey = null;
		String creMKey = null;
		String creOKey = null;
		String delMKey = null;
		String delOKey = null;
		
		List<String> updMenuKeys = null;
		List<String> updateOKeys = null;
		List<Map<String, Object>> updMenusParams = null;
		List<Map<String, Object>> updOptsParams = null;

		List<String> creMenuKeys = null;
		List<String> creOKeys = null;
		List<Map<String, Object>> creMenusParams = null;
		List<Map<String, Object>> creOptsParams = null;
		
		List<String> delMenuKeys = null;
		List<String> delOKeys = null;
		List<Map<String, Object>> delMenusParams = null;
		List<Map<String, Object>> delOptsParams = null;
		
		Map<String, Object> params = null;
		Map<String, Object> creMenuMap = null;
		Map<String, Object> creOptMap = null;
		Map<String, Object> updMenuMap = null;
		Map<String, Object> updOptMap = null;
		Map<String, Object> delMenuMap = null;
		Map<String, Object> delOptMap = null;
		
		List<Map<String,Object>> updMenusList = null; 
		List<Map<String,Object>> updOptsList = null;
		
		List<Map<String,Object>> creMenusList = null; 
		List<Map<String,Object>> creOptsList = null;
		
		List<Map<String,Object>> delMenusList = null; 
		List<Map<String,Object>> delOptsList = null;
		
		
		reqKeys  = (List<String>)paramMap.get("KEYS");
		logger.info(" =============================== reqKeys  == ",reqKeys);

		params = ((List<Map<String, Object>>)paramMap.get("params")).get(0);
		creMenuMap = (Map<String, Object>) ((List<Map<String, Object>>)paramMap.get("params")).get(0).get("creMenuList");
		creOptMap = (Map<String, Object>) ((List<Map<String, Object>>)paramMap.get("params")).get(0).get("creOptList");
		updMenuMap = (Map<String, Object>) ((List<Map<String, Object>>)paramMap.get("params")).get(0).get("updMenuList");
		updOptMap = (Map<String, Object>) ((List<Map<String, Object>>)paramMap.get("params")).get(0).get("updOptList");
		delMenuMap = (Map<String, Object>) ((List<Map<String, Object>>)paramMap.get("params")).get(0).get("delMenuList");
		delOptMap = (Map<String, Object>) ((List<Map<String, Object>>)paramMap.get("params")).get(0).get("delOptList");
		
		Iterator<String> iterator = null;
		/**
		 *  updOrderlist -- 수정 // 주문 상세
			updOrderDetail -- 수정
			updOdOption   -- 수정
			insOrdDetail  -- 생성
			insOrdOption  -- 생성
			updOrdDetail -- 수정/기존메뉴삭제
			updOrdOption -- 수정/기존메뉴삭제
		 * */
		
		if (firstKey == null) {
			firstKey = this.getFirstKey(reqKeys);
		}
		
		logger.info("firstKey =========== "+firstKey);
		
		try {
			
			params.put("SQLMODE",  this.SQLMODE);
			
			updMenuKeys = new ArrayList();
			updateOKeys = new ArrayList();
			updMenusParams = new ArrayList();
			updOptsParams = new ArrayList();
			
			creMenuKeys = new ArrayList();
			creOKeys = new ArrayList();
			creMenusParams = new ArrayList();
			creOptsParams = new ArrayList();
			
			delMenuKeys = new ArrayList();
			delOKeys = new ArrayList();
			delMenusParams = new ArrayList();
			delOptsParams = new ArrayList();
			
			
			/**1.update orderlist**/
			dataDao.query4Update1("updOrderlist", params);
			
			/** 2.updOrderDetail / 2.updOdOption **/
			if(updMenuMap != null) {
				updateMKey = new CommonService().getMapFirstKey4List(updMenuMap);
				if(updateMKey == null) {
					updateMKey = this.getFirstKey(reqKeys);
				}
				
				iterator = updMenuMap.keySet().iterator();
				while (iterator.hasNext()) {
					String reqKey = iterator.next();	
					if ( (updMenuMap).get(reqKey) instanceof List && updateMKey.length() == 0) {
						updateMKey = reqKey;
					}
					updMenuKeys.add(reqKey);
				}
				updMenusParams.add(updMenuMap);
				updMenusList = getListMapParams(updMenusParams, commInfoMap, updateMKey, updMenuKeys);
				
				if(updMenusList.size() > 0) {
					dataDao.query4Update3("updOrderDetail", updMenusList);
				}
			}
			
			if(updOptMap != null) {
				
				updateOKey = new CommonService().getMapFirstKey4List(updOptMap);
				if(updateOKey == null) {
					updateOKey = this.getFirstKey(reqKeys);
				}
				iterator = updOptMap.keySet().iterator();
				
				while (iterator.hasNext()) {
					String reqKey = iterator.next();	
					if ( (updOptMap).get(reqKey) instanceof List && updateOKey.length() == 0) {
						updateOKey = reqKey;
					}
					updateOKeys.add(reqKey);
				}
				updOptsParams.add(updOptMap);				
				updOptsList = getListMapParams(updOptsParams, commInfoMap, updateOKey, updateOKeys);
				if(updOptsList.size() > 0) {
					dataDao.query4Update3("updOdOption", updOptsList);
				}
			}
			
			/** 3.delOrdDetail / 4.delOrdOption  **/
			if(delMenuMap != null) {
				delMKey = new CommonService().getMapFirstKey4List(delMenuMap);
				if(delMKey == null) {
					delMKey = this.getFirstKey(reqKeys);
				}
				logger.info("   ==== delMKey === "+delMKey);
				iterator = delMenuMap.keySet().iterator();
				while (iterator.hasNext()) {
					String reqKey = iterator.next();	
					if ( (delMenuMap).get(reqKey) instanceof List && delMKey.length() == 0) {
						delMKey = reqKey;
					}
					delMenuKeys.add(reqKey);
				}
				delMenusParams.add(delMenuMap);
				delMenusList = getListMapParams(delMenusParams, commInfoMap, delMKey, delMenuKeys);
				logger.info("delMenusList == "+delMenusList.toString());
				if(delMenusList.size() > 0) {
					dataDao.query4Update3("updOrderDetail", delMenusList);
				}
			}
			
			if(delOptMap != null) {
				delOKey = new CommonService().getMapFirstKey4List(delOptMap);
				if(delOKey == null) {
					delOKey = this.getFirstKey(reqKeys);
				}
				iterator = delOptMap.keySet().iterator();
				while (iterator.hasNext()) {
					String reqKey = iterator.next();	
					if ( (delOptMap).get(reqKey) instanceof List && delOKey.length() == 0) {
						delOKey = reqKey;
					}
					delOKeys.add(reqKey);
				}
				delOptsParams.add(delOptMap);
				delOptsList = getListMapParams(delOptsParams, commInfoMap, delOKey, delOKeys);
				logger.info("delOptsList !@!@!@ "+delOptsList.toString());
				if(delOptsList.size() > 0) {
					dataDao.query4Update3("updOdOption", delOptsList);
				}
			}
			
			/** 4.insOrdDetail / 3.insOrdOption **/
			if(creMenuMap != null) {
				orderDuid = (String) dataDao.query4Object1("getUUID", paramMap);
				params.put("uuid", orderDuid);
				params.putAll(commInfoMap);
				commInfoMap.put("uuid", orderDuid);
				
				creMKey = new CommonService().getMapFirstKey4List(creMenuMap);
				if(creMKey == null) {
					creMKey = this.getFirstKey(reqKeys);
				}
				
				iterator = creMenuMap.keySet().iterator();
				while (iterator.hasNext()) {
					String reqKey = iterator.next();	
					if ( (creMenuMap).get(reqKey) instanceof List && creMKey.length() == 0) {
						creMKey = reqKey;
					}
					creMenuKeys.add(reqKey);
				}
				creMenusParams.add(creMenuMap);
				creMenusList = getListMapParams(creMenusParams, commInfoMap, creMKey, creMenuKeys);
				logger.info(" === creMenusList == "+creMenusList);
				
				if(creMenusList.size() > 0) {
					dataDao.query4Update3("insOrdDetail", creMenusList);
				}
				
			}
			
			if(creOptMap != null) {
				creOKey = new CommonService().getMapFirstKey4List(creOptMap);
				if(creOKey == null) {
					creOKey = this.getFirstKey(reqKeys);
				}
				
				iterator = creOptMap.keySet().iterator();
				while (iterator.hasNext()) {
					String reqKey = iterator.next();	
					if ( (creOptMap).get(reqKey) instanceof List && creOKey.length() == 0) {
						creOKey = reqKey;
					}
					creOKeys.add(reqKey);
				}
				creOptsParams.add(creOptMap);
				creOptsList = getListMapParams(creOptsParams, commInfoMap, creOKey, creOKeys);
				logger.info("creOptsList !@!@!@ "+creOptsList.toString());
				if(creOptsList.size() > 0) {
					dataDao.query4Update3("insOrdOption", creOptsList);
				}
			}
			
			
			
			jsonRet = GrpcDataUtil.getGrpcResults("0", ErrConstance.NO_ERROR, null);
			
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		errCode = ErrConstance.ERR_9999;
    		errMsg  = ex.toString();
    		jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, ex.toString(), null);
    	} finally {
	       	if (paramMap != null) {
	       		try { paramMap = null; } catch(Exception ex) { }
	       	}
    	}
    	grpcResp.setResults(jsonRet);
    	grpcResp.setErrCode(errMsg);
    	grpcResp.setErrMsg(errCode);
    	
    	
		
		return grpcResp;
	}
	
	
	
	
	//--------------------- 영수증 출력용 서비스 시작 --------------------------
	//{"ownerId":"13250580","orderData": {totalSize, contents { order {channel / takeoutNo / printNo / orderNumber / orderDateTime / phoneNo 
	/// pickupAddress / roadNameAddress / storeMemo / riderMemo / memo / items / options 
	/// totalAmount / deliveryTip / payAmount / lastAmount / paymentType / shopNumber} } }
	/**
	jsonReq = "{\"orderUid\":\"125822\"}";//혼밥
	
	final ManagedChannel channel = getManagedChannel(addr, port, MSA_TLS);
	    	
	MsaApiGrpc.MsaApiBlockingStub stub = MsaApiGrpc.newBlockingStub(channel);
	// GRPC 요청을 위한 파라미터 설정
	SciRIO.Data request = SciRIO.Data.newBuilder()
			.setPID("O2O0902")
			.setData(jsonReq)
			.setCsKey(encKey)
			.setUserIP("192.168.219.195")
			.setServerIP("192.168.219.195")
			.setUserUID("15034")
			.setBorgUID(borgUID)
			.setAgentID("14")
			.build();
	
	RetMsg retMsg = stub.callRMsg(request);
	if (!retMsg.getErrCode().equals("0")) {
		System.out.print(retMsg.getErrMsg());
	}
	System.out.println("retMsg.getErrCode() ::: "+ retMsg.getErrCode());
	System.out.println("retMsg.getResults() ::: "+ retMsg.getResults());
	
	channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
	 */
	@Override
	public GrpcResp getPrintOrderInfo(GrpcParams grpcPrms) throws Exception{
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		GrpcResp grpcResp = new GrpcResp();
		JSONObject jsonObj = null;
		StringBuffer sbOrder  = new StringBuffer();

		logger.info("@@@@@@@@@@@@@@@@@@@@@@@ getPrintOrderInfo @@@@@@@@@@@@@@@@@@@@@@@"+ grpcPrms.getData());		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		try {
			jsonObj = (JSONObject) new JSONParser().parse(grpcPrms.getData());
			logger.info("@@@@@@@@@@@@@@@@@@@@@@@ getPrintOrderInfo @@@@@@@@@@@@@@@@@@@@@@@"+jsonObj.get("params"));
			List<Map<String, Object>> list = (List<Map<String, Object>>) jsonObj.get("params");
//			logger.info("@@@@@@@@@@@@@@@@@@@@@@@ getPrintOrderInfo @@@@@@@@@@@@@@@@@@@@@@@"+list.get(0));
//    		paramMap.put("orderUid", jsonObj.get("orderUid").toString());
//    		paramMap.put("borgUID", grpcPrms.getBorgUID());
    		paramMap.put("orderUid", list.get(0).get("orderUid"));
    		paramMap.put("borgUID", list.get(0).get("custRUID"));
    		
    		String[] localGrpcIP = to2oOrderMapper.getLocalGrpcIP(paramMap).split(":");
    		logger.info(localGrpcIP.toString());
    		Map<String,Object> rsMap = to2oOrderMapper.getOrderInfo(paramMap);
    		if (rsMap == null) {
   	        	errMsg   = "";
   	        	errCode  = ErrConstance.NO_DATA;
   	    		jsonRet = GrpcDataUtil.getGrpcResults("NO_DATA", errMsg, null);
   	        } else {  
				sbOrder.append("{\"ownerId\":\""+ rsMap.get("ownerID") +"\"");
				sbOrder.append(",\"orderData\":");
				
				sbOrder.append("{\"totalSize\":1");
				sbOrder.append(",\"totalPayAmount\":"+ rsMap.get("payAmount") +"");
				sbOrder.append(",\"contents\":[{");
				
				sbOrder.append("\"order\":{");

				sbOrder.append("\"channel\":\""+ rsMap.get("channel") +"\"");
				sbOrder.append(",\"orderNumber\":\""+ rsMap.get("orderNumber") +"\"");
				sbOrder.append(",\"takeoutNo\":\""+ (rsMap.get("takeoutNo")==null?"":rsMap.get("takeoutNo")) +"\"");
				sbOrder.append(",\"printNo\":\""+ (rsMap.get("printNo")==null?"":rsMap.get("printNo")) +"\"");
				
				sbOrder.append(",\"orderDateTime\":\""+ (rsMap.get("orderDateTime")==null?"":rsMap.get("orderDateTime")) +"\"");
				sbOrder.append(",\"phoneNo\":\""+ (rsMap.get("phoneNo")==null?"":rsMap.get("phoneNo")) +"\"");
				
				sbOrder.append(",\"pickupAddress\":\""+ (rsMap.get("pickupAddress")==null?"":rsMap.get("pickupAddress")) +"\"");
				sbOrder.append(",\"roadNameAddress\":\""+ (rsMap.get("roadNameAddress")==null?"":rsMap.get("roadNameAddress")) +"\"");
				sbOrder.append(",\"storeMemo\":\""+ (rsMap.get("storeMemo")==null?"":rsMap.get("storeMemo")) +"\"");
				sbOrder.append(",\"riderMemo\":\""+ (rsMap.get("riderMemo")==null?"":rsMap.get("riderMemo")) +"\"");
				sbOrder.append(",\"memo\":\""+ (rsMap.get("memo")==null?"":rsMap.get("memo")) +"\"");
				
				List<Object> rsList = to2oOrderMapper.getOrderItems(paramMap);
				if (rsList != null && rsList.size() > 0) {
					sbOrder.append(",\"items\":[");
					int inx_1 = 0;
					for(Object rsObj : rsList) {
						Map<String,Object> itemMap = (Map<String,Object>)rsObj;
						String orderDUid = ""+ itemMap.get("orderDUid");
						
						if (inx_1 > 0) sbOrder.append(",");
						
						sbOrder.append("{\"seq\":"+ itemMap.get("seq") +"");
						sbOrder.append(",\"name\":\""+ itemMap.get("itemName") +"\"");
						sbOrder.append(",\"totalPrice\":"+ itemMap.get("totalPrice") +"");
						sbOrder.append(",\"quantity\":"+ (itemMap.get("quantity")==null?"0":itemMap.get("quantity")) +"");
						sbOrder.append(",\"discountPrice\":"+ itemMap.get("discountPrice") +"");
						
						
						paramMap.put("orderDUid", orderDUid);
						List<Object> optList = to2oOrderMapper.getOrderOptions(paramMap);
						if (optList != null && optList.size() > 0) {
							int inx_2 = 0;
							sbOrder.append(",\"options\":[");
							for(Object optObj : optList) {
								Map<String,Object> optMap = (Map<String,Object>)optObj;
	
								if (inx_2 > 0) sbOrder.append(",");
								
								sbOrder.append("{\"seq\":"+ optMap.get("seq") +"");
								sbOrder.append(",\"name\":\""+ optMap.get("optName") +"\"");
								sbOrder.append(",\"quantity\":"+ optMap.get("quantity") +"");
								sbOrder.append(",\"price\":"+ optMap.get("optPrice") +"}");
								
								inx_2++;
							}
							sbOrder.append("]");  // options
						}			
						sbOrder.append("}"); // items
						inx_1++;
					}					
					sbOrder.append("]");  // items
				}				
				sbOrder.append(",\"totalAmount\":"+ rsMap.get("totalAmount") +"");
				sbOrder.append(",\"payAmount\":"+ rsMap.get("payAmount") +"");					
				sbOrder.append(",\"lastAmount\":"+ rsMap.get("lastAmount") +"");
				sbOrder.append(",\"deliveryTip\":"+ rsMap.get("deliveryTip") +"");
				sbOrder.append(",\"paymentType\":\""+ rsMap.get("paymentType") +"\"");
				sbOrder.append(",\"shopNumber\":\""+ rsMap.get("shopNumber") +"\"");

				sbOrder.append("}");  // order
				sbOrder.append("}]");  // contents
				sbOrder.append("}");  // totalSize
				sbOrder.append("}");  // ownerId
	        	
   	        	jsonRet = sbOrder.toString();
   	        	logger.error("jsonRet >>>>>>>>>>> " + jsonRet);
   	        	
   	        	ManagedChannel channel = null;
   	        	MsaApiGrpc.MsaApiBlockingStub stub = null;
   	    		SciRIO.Data request = null;   	    		
   	    		RetMsg retMsg = null;
   	        	try
   	            {
   	        		logger.error("localGrpcIP   >>>>>>>>>>> " + localGrpcIP[0]);
   	        		logger.error("localGrpcPort >>>>>>>>>>> " + localGrpcIP[1]);
   	        		
   	        		channel = getManagedChannel(localGrpcIP[0], Integer.parseInt(localGrpcIP[1]), "NONE");
   	        		stub    = MsaApiGrpc.newBlockingStub(channel);
   	        		request = this.getMsaReqData("RECEIPT_PRINT", jsonRet, grpcPrms);   	        		
   	   	    		retMsg  = stub.callRMsg(request);
   	   	    		errCode = retMsg.getErrCode();
   	   	    		errMsg  = retMsg.getErrMsg();   	   	    		
   	   	    		jsonRet = "{\"results\":\""+ errMsg +"\"}";
   	            }
   	            catch (Exception ex)
   	            {
   	            	errCode = "9999";
   	            	errMsg  = ex.toString();
   	            	jsonRet = "{\"results\":\"ERROR\"}";
   	                logger.error("callRMsg.Exception ..." + errMsg);
   	            } finally {
   	            	if (request != null) {
   	            		try { request = null; } catch (Exception ex) { };
   	            	}
   	            	if (stub != null) {
   	            		try { stub = null; } catch (Exception ex) { };
   	            	}
   	            	if (channel != null) {
   	            		try { channel.shutdown().awaitTermination(1, TimeUnit.SECONDS); } catch (Exception ex) { };
   	            	}
   	            }
   	        }    		
    		logger.info("@@@@@@@@@@@@@@@@@@@@@@@ getPrintOrderInfo @@@@@@@@@@@@@@@@@@@@@@@"+ jsonRet);	
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		errCode = ErrConstance.ERR_9999;
    		errMsg  = ex.toString();
    	} finally {
	       	if (paramMap != null) try { paramMap = null; } catch(Exception ex) { }
	        if (jsonObj != null) jsonObj = null; 
    	}
    	grpcResp.setResults(jsonRet);
    	grpcResp.setErrCode(errCode);
    	grpcResp.setErrMsg(errMsg);
		
		return grpcResp;
	} 
	
	public SciRIO.Data getMsaReqData(String pid, String jsonRet, GrpcParams grpcPrms)
    {
		SciRIO.Data reqData = null;
        try
        { //ROLEID=LOGIN
        	reqData = SciRIO.Data.newBuilder()
    				.setPID(pid)
    				.setData(jsonRet)
    				.setCsKey(grpcPrms.getCsKey())
    				.setUserIP(grpcPrms.getUserIP())
    				.setServerIP(grpcPrms.getServerIP())
    				.setUserUID(grpcPrms.getUserUID())
    				.setBorgUID(grpcPrms.getBorgUID())
    				.setAgentID(grpcPrms.getAgentID())
    				.build();
        	
            return reqData;
        }
        catch (Exception ex)
        {
            logger.error(ex.toString());
            return reqData;
        }
        finally
        {
            if (reqData != null) reqData = null;
        }
    }
	
	private ManagedChannel getManagedChannel(String addr, int port, String MSA_TLS) throws SSLException {
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
	private SslContext getBuildSslContext(String caPemFile, String crtPemFile,
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
	//--------------------- 영수증 출력용 서비스  끝  --------------------------

	@Override
	public GrpcResp saveOrderRegistList(GrpcParams grpcPrms) throws Exception {
		String   errCode  = "0";
		String   errMsg   = "";
		String   jsonRet  = "";		
		String   orderNumber = "";
		
		GrpcResp grpcResp = new GrpcResp();
		AES256Util aes256 = new AES256Util();
		Map<String, Object> paramMap = GrpcDataUtil.getParams("params", grpcPrms.getData(), true);
		Map<String,Object> commInfoMap = new CommService(dataDao).getCommInfoMap(grpcPrms);
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@ paramMap ===== " + paramMap + "  @@@@@@@@@@@@@@@@@@@@@@@ commInfoMap ===== " + commInfoMap);		
		
		List<String> reqKeys = null;
		String firstKey = null;
		String subKey = null;
		
		List<String> subMenuKeys = null;
		List<String> subOptKeys = null;
		List<Map<String, Object>> subMenusParams = null;
		List<Map<String, Object>> subOptsParams = null;
		
		List<Map<String,Object>> menusList = null; 
		List<Map<String,Object>> optsList = null;		
		
		Map<String, Object> params = null;
		Map<String, Object> menuMap = null;
		Map<String, Object> optMap = null;
		Map<String, Object> subParamMap = null;
		reqKeys  = (List<String>)paramMap.get("KEYS");	
//		params   = .get("menuList");	
		params = ((List<Map<String, Object>>)paramMap.get("params")).get(0);
		menuMap = (Map<String, Object>) ((List<Map<String, Object>>)paramMap.get("params")).get(0).get("menuList");
		optMap = (Map<String, Object>) ((List<Map<String, Object>>)paramMap.get("params")).get(0).get("optList");
		firstKey = new CommonService().getMapFirstKey4List(menuMap);

		if (firstKey == null) {
			firstKey = this.getFirstKey(reqKeys);
		}
		
		
		try {
			orderNumber = (String) dataDao.query4Object1("getUUID", paramMap);
			params.put("uuid", orderNumber);
			params.putAll(commInfoMap);
			params.put("SQLMODE",  this.SQLMODE);
			commInfoMap.put("uuid", orderNumber);
			
			logger.info("params ===== " + params);	
			
			dataDao.query4Update1("saveOrderList", params);
			
			subMenusParams = new ArrayList<Map<String,Object>>();
			subOptsParams = new ArrayList<Map<String,Object>>();
			subParamMap = new HashMap<String, Object>();	
			subMenuKeys  = new ArrayList<String>();
			subOptKeys  = new ArrayList<String>();

			Iterator<String> iterator = menuMap.keySet().iterator();
			while (iterator.hasNext()) {
				String reqKey = iterator.next();	
				if ( (menuMap).get(reqKey) instanceof List && firstKey.length() == 0) {
					firstKey = reqKey;
				}
				subMenuKeys.add(reqKey);
			}
			subMenusParams.add(menuMap);	
			
			menusList = getListMapParams(subMenusParams, commInfoMap, firstKey, subMenuKeys);
			
			logger.info("menusList ===== " + menusList);	
			
			dataDao.query4Update3("saveOrderMenuList", menusList);
			
//			logger.info("optMap ===== " + optMap);	
			if(optMap != null) {
				subKey = new CommonService().getMapFirstKey4List(optMap);
				
				if (subKey == null) {
					subKey = this.getFirstKey(reqKeys);
				}
				
				Iterator<String> iterator2 = optMap.keySet().iterator();
				while (iterator2.hasNext()) {
					String reqKey = iterator2.next();	
					if ( (optMap).get(reqKey) instanceof List && subKey.length() == 0) {
						subKey = reqKey;
					}
					subOptKeys.add(reqKey);
				}
				subOptsParams.add(optMap);
				optsList = getListMapParams(subOptsParams, commInfoMap, subKey, subOptKeys);
				
				dataDao.query4Update3("saveOrderOptList", optsList);
			}
			logger.info("optsList ===== " + optsList);	
			
			jsonRet = GrpcDataUtil.getGrpcResults("0", ErrConstance.NO_ERROR, null);
			
    	} catch(Exception ex) {
    		ex.printStackTrace();
    		errCode = ErrConstance.ERR_9999;
    		errMsg  = ex.toString();
    		jsonRet = GrpcDataUtil.getGrpcResults(ErrConstance.ERR_9999, ex.toString(), null);
    	} finally {
	       	if (paramMap != null) {
	       		try { paramMap = null; } catch(Exception ex) { }
	       	}
    	}
    	grpcResp.setResults(jsonRet);
    	grpcResp.setErrCode(errMsg);
    	grpcResp.setErrMsg(errCode);
		
		return grpcResp;
	}
	
	private List<Map<String,Object>> getListMapParams(List<Map<String, Object>> params, Map<String,Object> commInfoMap, String firstKey, List<String> reqKeys) {
		List<Map<String,Object>> paramsList = new ArrayList<Map<String,Object>>();
		for(int ii=0; ii<params.size(); ii++) {
			Map<String,Object> sqlParam = null;
			Map<String,Object> param = params.get(ii);
			logger.info("p  param ==== ???? "+param);
			logger.info("param.get(firstKey) ==== ???? "+param.get(firstKey));
			if (param.get(firstKey) instanceof List) {
				List<String> firstLst = (List<String>)param.get(firstKey);
				for (int ll=0; ll<firstLst.size(); ll++) {
					sqlParam = new HashMap<String, Object>();	
					if (firstLst.get(ll) != null && !firstLst.get(ll).isEmpty()) {
						sqlParam.put(firstKey, firstLst.get(ll));
					}
					sqlParam.putAll(commInfoMap);
					sqlParam.put("SQLMODE",  this.SQLMODE);
					
					for (String colID : reqKeys) {
						if (!colID.equals(firstKey)) {
							if (param.get(colID) instanceof List) {
								String value = GrpcDataUtil.getVal4MapList(param,colID, ll);
								if("itemName".equals(colID)) {
									sqlParam.put(colID, value);
									sqlParam.put("cItemName", value.trim());
								} else if("discountPrice".equals(colID)) {
									if(value.equals("")) {
										value = "0";
									}
									
									sqlParam.put(colID, value);
								} else {
									sqlParam.put(colID, value);
								}
								
							}
						}
					}
					//logger.debug("944 sqlParam -> "+ sqlParam);
					paramsList.add(sqlParam);
				}	
			}
		}
		
		return paramsList;
	}
}

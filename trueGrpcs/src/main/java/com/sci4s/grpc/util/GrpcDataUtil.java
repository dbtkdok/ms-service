package com.sci4s.grpc.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sci4s.fbs.Data;
import com.sci4s.grpc.dto.GrpcParams;
import com.sci4s.grpc.util.GrpcDataUtil;
import com.sci4s.utils.DateUtil;

public class GrpcDataUtil {
	/**
	 * 최초 전송되는 GRPC 데이터를 GrpcParams로 리턴하는 메서드
	 * 
	 * @param SciRIO.Data req
	 * @return Map<String, Object>
	 * @throws Exception
	 */
	public static GrpcParams parseGrpcData(Data req) throws Exception {
		Logger logger = LoggerFactory.getLogger(GrpcDataUtil.class);
		
		GrpcParams grpcPrms = new GrpcParams();
		try {
			String startTime = DateUtil.getDateFormat(new Date(), "yyyy-MM-dd HH:mm:ss");
			grpcPrms.setAgentID(req.agentID());
			grpcPrms.setpID(req.pID());
			grpcPrms.setData(req.data());
			grpcPrms.setCsKey(req.csKey());
			grpcPrms.setUserIP(req.userIP());
			grpcPrms.setServerIP(req.serverIP());
			grpcPrms.setUserUID(req.userUID());
			grpcPrms.setBorgUID(req.borgUID());
			grpcPrms.setStartTime(startTime);
			grpcPrms.setClang(req.clang());
			grpcPrms.setErrCode("0");
			grpcPrms.setErrMsg("");
			
			logger.debug("REQUEST DATA ######################################");
			logger.debug("startTime::: " + grpcPrms.getStartTime());
			logger.debug("pID      ::: " + grpcPrms.getpID());
			logger.debug("agentID  ::: " + grpcPrms.getAgentID());
			logger.debug("csKey    ::: " + grpcPrms.getCsKey());
			logger.debug("userIP   ::: " + grpcPrms.getUserIP());
			logger.debug("serverIP ::: " + grpcPrms.getServerIP());
			logger.debug("userUID  ::: " + grpcPrms.getUserUID());
			logger.debug("borgUID  ::: " + grpcPrms.getBorgUID());
//			logger.debug("data     ::: " + grpcPrms.getData());
			logger.debug("REQUEST DATA ######################################");
			
			return grpcPrms;
		} catch (Exception ex) {
			throw ex;
		} finally {
			if (grpcPrms != null) { 
				try { grpcPrms = null; } catch (Exception ex) { }
			}
			if (logger != null) { 
				try { logger = null; } catch (Exception ex) { }
			}
		}
	}
	
	/**
	 * Request로 전송된 Json 파라미터를 파싱하여 리턴함.
	 * 
	 * @author flacom
	 * @param  String jsonReq
	 * @return Map<String, Object> - 파라미터, 파라미터 키값
	 * @since  2008-08-29
	 */
	public static Map<String, Object> getParams4Map(String paramNM, String jsonReq) throws Exception {

		Logger logger = LoggerFactory.getLogger(GrpcDataUtil.class);
		//int rows = 0;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		try {
			//paramMap.put("SQLMODE", Constance.SQL_MODE);
			logger.info("88 getParams4Map =========== Request Parameters parsing start ===========");
			//logger.info(jsonReq);
			JSONParser jsonParser = new JSONParser();
			// JSONObject jsonObj = (JSONObject) jsonParser.parse(new InputStreamReader(new FileInputStream(request.getItem())));

			JSONObject jsonObj = (JSONObject) jsonParser.parse(jsonReq);	
			//logger.debug("getParams4Map.94==>jsonObj class->"+ jsonObj.get(paramNM).getClass().getName());

			if (jsonObj.get(paramNM) instanceof List) { //org.json.simple.JSONArray			
				//logger.debug("getParams4Map.97==>"+ paramNM +" class->"+ jsonObj.getClass().getTypeName());
				
				JSONArray reqArray = (JSONArray) jsonObj.get(paramNM);// "params"
				for (int i = 0; i < reqArray.size(); i++) {
					JSONObject tempObj = (JSONObject) reqArray.get(i);					
					setParamMap(tempObj, paramMap);					
				}
			} else {
				JSONObject tempObj = (JSONObject)jsonObj.get(paramNM);				
				setParamMap(tempObj, paramMap);
			}
			logger.info("108 getParams4Map =========== Request Parameters parsing end ===========");
			return paramMap;
		} catch (Exception e) {
			throw e;
		} finally {
			if (paramMap != null) {
				try { paramMap = null; } catch (Exception ex) { }
			}
			if (logger != null) {
				try { logger = null; } catch (Exception ex) { }
			}
		}
	}
	
	public static void setParamMap(JSONObject jsonObj, Map<String, Object> paramMap) throws Exception {
		//int rows = 0;
		Logger logger = LoggerFactory.getLogger(GrpcDataUtil.class);
		Iterator<String> iterator = jsonObj.keySet().iterator();
		try {
			while (iterator.hasNext()) {
				String reqKey = iterator.next();
				Object reqVal = (jsonObj.get(reqKey) == null ? null : jsonObj.get(reqKey));
				
				//logger.debug("131 setParamMap.reqKey="+ reqKey +" class->"+ reqVal.getClass().getTypeName());
				
				if (reqVal != null) {
					//logger.debug("134 setParamMap.reqVal="+ reqVal +" class->"+ reqVal.getClass().getTypeName());
					if (reqVal.getClass().getTypeName() instanceof String) {
						reqVal = (reqVal == null ? "" : "" + reqVal);
						//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
						String tmpVal = String.valueOf(reqVal);
						if (tmpVal.lastIndexOf("\"}]") >= 0) {
							reqVal = tmpVal; 
						} else if (tmpVal.lastIndexOf("}]") >= 0) {
							reqVal = tmpVal; 
						} else if (tmpVal.lastIndexOf("\"]}") >= 0) {// {"sortORD":["1"],"svcTypeID":["A"],"menuLVL":["3"]}							
							//reqVal = new Gson().fromJson(tmpVal, new TypeToken<Map<String,List<Object>>>() {}.getType());
							reqVal = tmpVal;
						} else if (tmpVal.lastIndexOf("]}") >= 0) {// {"sortORD":[1],"svcTypeID":["A"],"menuLVL":[3]}							
							//reqVal = new Gson().fromJson(tmpVal, new TypeToken<Map<String,List<Object>>>() {}.getType());
							reqVal = tmpVal;
						} else if (tmpVal.lastIndexOf("]") >= 0) {
							if (!"[\"\"]".equals(tmpVal) && !"[]".equals(tmpVal)) {
								reqVal = new Gson().fromJson(String.valueOf(reqVal), new TypeToken<List<String>>(){}.getType());
							} else {
								if ("[\"\"]".equals(tmpVal)) {
									reqVal = null;
								} else if ("[]".equals(tmpVal)) {
									reqVal = null;
								} 
							}
						}
					}
				} else {
					//logger.debug("setParamMap.157:::"+ reqKey + " : " + reqVal);
				}
				if (reqKey.equals("userIP")) {
					paramMap.put("userIP", "" + jsonObj.get(reqKey));
				}
				if (reqKey.equals("serverIP")) {
					paramMap.put("userIP", "" + jsonObj.get(reqKey));
				}
				if (reqVal != null) {
					paramMap.put(reqKey, reqVal);
					//logger.debug("setParamMap.167:::"+ reqKey + " : " + reqVal);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (iterator != null) { try { iterator = null; } catch (Exception ex) { } }
			if (logger != null) {
				try { logger = null; } catch (Exception ex) { }
			}
		}
	}
	
	/**
	 * ArrayList를 포함하고 있는 Map에서 해당 필드명 데이터 리턴하는 함수 
	 */
	public static String getVal4MapList(Map<String, Object> listMap, String pname, int ll) {
		//System.out.println("191 getVal4MapList:::"+ pname);
	    return (((List<String>)listMap.get(pname)).get(ll)==null?"":((List<String>)listMap.get(pname)).get(ll));
	}
	
	/**
	 * 리턴 JSON 문자열을 생성하는 공통 함수
	 */
	public static String getGrpcResults(String ret, String msg, String results) {
		if (results == null) {
			return "{\"errCode\":\""+ ret +"\",\"errMsg\":\""+ msg +"\"}";
		} else {
			return "{\"errCode\":\""+ ret +"\",\"errMsg\":\""+ msg +"\",\"results\":"+ results +"}";
		}
	}
	
	/**
	 * Request로 전송된 Json 파라미터를 파싱하여 리턴함.
	 * 
	 * @author flacom
	 * @param String  jsonReq
	 * @param boolean isKeys
	 * @return HashMap - 파라미터, 파라미터 키값
	 * @since 2008-08-29
	 */
	public static Map<String, Object> getParams(String paramNM, String jsonReq, boolean isKeys) throws Exception {

		Map<String, Object> params = null;
		try {
			params = GrpcDataUtil.getParams(paramNM, jsonReq);
			if (isKeys == true) {
				return params;
			} else {
				return (Map<String, Object>) params.get(paramNM);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (params != null) {
				try { params = null; } catch (Exception ex) { }
			}
		}
	}
	
	/**
	 * Request로 전송된 Json 파라미터를 파싱하여 리턴함.
	 * 
	 * @author flacom
	 * @param  String jsonReq
	 * @return HashMap - 파라미터, 파라미터 키값
	 * @since 2008-08-29
	 */
	public static Map<String, Object> getParams(String paramNM, String jsonReq) throws Exception {

		Logger logger = LoggerFactory.getLogger(GrpcDataUtil.class);

		Map<String, Object> returnMap = new HashMap<String, Object>();

		List<String> reqKeys = new ArrayList<String>();
		List<Object> reqData = new ArrayList<Object>();
		int rows = 0;
		try {
			logger.info("252 getParams =========== Request Parameters parsing start ===========");			
			//logger.info("253 "+ paramNM +".jsonReq ::::: "+ jsonReq);
			JSONParser jsonParser = new JSONParser();
			// JSONObject jsonObj = (JSONObject) jsonParser.parse(new InputStreamReader(new
			// FileInputStream(request.getItem())));

			JSONObject jsonObj = (JSONObject) jsonParser.parse(jsonReq);			
			logger.info("259 jsonObj.get('"+ paramNM +"') instanceof List ::: " + jsonObj);

			if (jsonObj.get(paramNM) instanceof List) {
				JSONArray reqArray = (JSONArray) jsonObj.get(paramNM);// "params"

				for (int i = 0; i < reqArray.size(); i++) {
					Map<String, Object> paramMap = new HashMap<String, Object>();
					//paramMap.put("SQLMODE", Constance.SQL_MODE);
					//----------------
					JSONObject tempObj = (JSONObject) reqArray.get(i);	
					GrpcDataUtil.setParamsMap(tempObj, i, reqKeys, paramMap);					
					reqData.add(paramMap);					
					returnMap.put(paramNM, reqData);
				}
			} else {
				JSONObject tempObj = (JSONObject)jsonObj.get(paramNM);					
				Map<String, Object> paramMap = new HashMap<String, Object>();				
				GrpcDataUtil.setParamsMap(tempObj, 0, reqKeys, paramMap);				
				returnMap.put(paramNM, paramMap);
			}
			logger.debug("279 getParams =========== Request Parameters parsing end ===========");
			returnMap.put("KEYS", reqKeys);

			return returnMap;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (reqData != null)  { try { reqData = null;   } catch (Exception ex) { } }
			if (reqKeys != null)  { try { reqKeys = null;   } catch (Exception ex) { } }
			if (returnMap != null){ try { returnMap = null; } catch (Exception ex) { } }
			if (logger != null)   { try { logger = null;    } catch (Exception ex) { } }
		}
	}
	
	public static void setParamsMap (JSONObject tempObj, int ii, List<String> reqKeys, Map<String, Object> paramMap) {
		
		Logger logger = LoggerFactory.getLogger(GrpcDataUtil.class);
		int rows = 0;
		//JSONObject tempObj = (JSONObject) reqArray.get(i);
		try {		
			Iterator<String> iterator = tempObj.keySet().iterator();
			while (iterator.hasNext()) {
				String reqKey  = iterator.next();
				Object reqVal  = (tempObj.get(reqKey) == null ? null : tempObj.get(reqKey));
				String clsName = null;
				String tmpVal  = null;
				try {
					clsName = reqVal.getClass().getTypeName();
				} catch(NullPointerException ex) {
					clsName = null;
				}				
				if (clsName != null) {
					clsName = reqVal.getClass().getTypeName();					
					logger.info("314 setParamsMap==>reqKey="+ reqKey +", Type.class->" + clsName +"////"+ reqVal);
					// reqKey :::::::: org.json.simple.JSONObject
					// reqKey :::::::: java.lang.String
					if (clsName.indexOf("String") >= 0) {
						reqVal = (reqVal == null ? "" : "" + reqVal);						
						tmpVal = String.valueOf(reqVal);
		
						//logger.debug("320 setParamsMap.reqVal="+ tmpVal);
						int pos = tmpVal.lastIndexOf("}]");
						if (pos >= 0) { // Json Arrary 형식을 경우...Gson 처리
							reqVal = new Gson().fromJson(tmpVal, new TypeToken<List<String>>() {}.getType());
							rows   = ((List<String>) reqVal).size();
							//logger.debug("setParamsMap.320==>gson.fromJson $$$$$$$$$ " + reqVal);
							/*
							 * {"params": [{"agentID":"13", "memberID":"2", "borgID":"14",
							 * "parMenuID":"2"}]}
							 */
							paramMap.put(reqKey, reqVal);
						} else {
							/*
							 * {"params": [{"ACTIMAGE":"[\"-\",\"\/images\/bt182.gif\",\"-\"]"
							 * ,"NOACTIMAGE":"[\"-\",\"-\",\"-\"]","PRIVILEGEID":"[\"46\",\"80\",\"51\"]"
							 * ,"PRIVILEGECD":"[\"RF_SEARCH\",\"AUC_ADM001\",\"BG_ACCGENLIST\"]"
							 * ,"ISREAD":"[\"0\",\"1\",\"1\"]"
							 * ,"DBSTS":"[\"A\",\"A\",\"A\"]"
							 * ,"PRIVILEGENM":"[\"견적관리\",\"계산서출력\",\"계정조회\"]"}]}
							 * 
							 */
							parseString2Map(reqKey, tmpVal, paramMap);
						}
					} else if (clsName.indexOf("JSONObject") >= 0) {
						//logger.info("setParamsMap.345==>reqKey="+ reqKey +", Type.class->" + reqVal.getClass().getTypeName() +"->"+ reqVal);
						paramMap.put(reqKey, GrpcDataUtil.parseMstDltParam0((JSONObject) reqVal));
					} else if (clsName.indexOf("JSONArray") >= 0) {
						tmpVal = String.valueOf(reqVal);
						if (tmpVal.startsWith("[\"")) {// ["0","1"] 형태일 경우
							/**
							 * {"SYS0026_01":[{"menuID":["0"]
							 * ,"topMenuID":["0"]
							 * ,"parMenuID":[""]
							 * ,"menuLVL":["0"]
							 * ,"sortORD":["0"]
							 * ,"dbSTS":["Y"]}]}
							 */
							//logger.debug("357 setParamsMap.reqKey="+ tmpVal +", Type.class->" + reqVal.getClass().getTypeName() +"->"+ reqVal);
							parseString2Map(reqKey, tmpVal, paramMap);
						} else if (tmpVal.startsWith("[")) {// [0,1] 형태일 경우
							/**
							 * {"SYS0026_01":[{
							 * "menuID":[0]
							 * ,"topMenuID":[0]
							 * ,"parMenuID":[""]
							 * ,"menuLVL":[0]
							 * ,"sortORD":[0]
							 * ,"dbSTS":["Y"]}]}
							 */
							//logger.debug("369 setParamsMap.reqKey="+ tmpVal +", Type.class->" + reqVal.getClass().getTypeName() +"->"+ reqVal);
							parseString2Map(reqKey, tmpVal, paramMap);
						} else {
							paramMap.put(reqKey, GrpcDataUtil.getMstDltParam1((JSONArray) reqVal));
						}
					} else {
						paramMap.put(reqKey, reqVal);
					}
					if (reqKey.equals("userIP")) {
						paramMap.put("userIP", "" + tempObj.get(reqKey));
					}
					if (reqKey.equals("serverIP")) {
						paramMap.put("serverIP", "" + tempObj.get(reqKey));
					}
					if (reqKeys != null) reqKeys.add("" + reqKey);
					//logger.debug("384 setParamsMap==>" + (ii + 1) +"-th "+ reqKey +" : "+ paramMap.get(reqKey));
				} else {
					logger.debug("386 setParamsMap==>" + (ii + 1) +"-th "+ reqKey +" : null skip");
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			if (logger != null)   { try { logger = null;    } catch (Exception ex) { } }
		}
		return;
	}
	
	/**
	 * 성능을 위해 파라미터 Json 구조를 아래와 같이 변경해서 전송한 경우, "[]"을 String으로 인식하기 때문에 다시 List로
	 * 변환하여 리턴함. {"params":
	 * 
	 * [{"ACTIMAGE":"[\"-\",\"\/images\/bt182.gif\",\"-\"]","NOACTIMAGE":"[\"-\",\"-\",\"-\"]","PRIVILEGEID":"[\"46\",\"80\",\"51\"]","PRIVILEGECD":"[\"RF_SEARCH\",\"AUC_ADM001\",\"BG_ACCGENLIST\"]","ISREAD":"[\"0\",\"1\",\"1\"]","DBSTS":"[\"A\",\"A\",\"A\"]","PRIVILEGENM":"[\"견적관리\",\"계산서출력\",\"계정조회\"]"}]}
	 * 
	 * @param reqKey
	 * @param reqVal
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	public static void parseString2Map(String reqKey, String reqVal, Map<String, Object> paramMap) throws Exception {
		Logger logger = LoggerFactory.getLogger(GrpcDataUtil.class);
		try {
			int pos1 = reqVal.lastIndexOf("]");
			int pos2 = reqVal.lastIndexOf("\",\"");
			int pos3 = -1;
			if (pos2 < 0) {
				//logger.debug("416 pos3==>"+ pos3);
				pos3 = reqVal.lastIndexOf(",");
				pos2 = pos3; 
			}
			//logger.debug("420 parseString2Map==>(pos1="+ pos1 +", pos2="+ pos2 +", pos3="+ pos3 +")");
			List<String> valLst = new ArrayList<String>();
			if (pos1 >= 0 && pos2 >= 0) { // 문자열 리스트 구조(예: ["46","80"] or [46,80] 
				String[] arrVal = null;		
				if (pos3 >= 0) {
					arrVal = String.valueOf(reqVal).split(",");
				} else {
					arrVal = String.valueOf(reqVal).split("\\Q\",\"\\E");
				}
				for (int ll = 0; ll < arrVal.length; ll++) {
					String tmpVal = arrVal[ll].trim();						
					if (tmpVal.indexOf("[") >= 0) {
						if (pos3 >= 0) {
							tmpVal = tmpVal.replace("[","");
						} else {
							tmpVal = tmpVal.replace("[\"","");
						}
					} else if (tmpVal.indexOf("]") >= 0) {
						if (pos3 >= 0) {
							tmpVal = tmpVal.replace("]","");
						} else {
							tmpVal = tmpVal.replace("\"]","");
						}
					} 
					if (tmpVal.indexOf("\\/") >= 0) {
						tmpVal = tmpVal.replace("\\/", "/");
					} 
					if (tmpVal.indexOf("\\\"") >= 0) {
						tmpVal = tmpVal.replace("\\", "");
					}
					valLst.add(tmpVal);
				}
				paramMap.put(reqKey, valLst);
			} else {
				if (pos1 >= 0) { // 문자열 리스트 구조(예: ["46"] or [46])
					String tmpVal = reqVal.trim();
					if (tmpVal.indexOf("[") >= 0) {
						if(tmpVal.indexOf("[\"") >= 0) {
							tmpVal = tmpVal.replace("[\"","");
						} else {
							tmpVal = tmpVal.replace("[","");
						}
					}
					if (tmpVal.indexOf("]") >= 0) {
						if(tmpVal.indexOf("\"]") >= 0) {
							tmpVal = tmpVal.replace("\"]","");
						} else {
							tmpVal = tmpVal.replace("]","");
						}
					}
					if (tmpVal.indexOf("\\/") >= 0) {
						tmpVal = tmpVal.replace("\\/", "/");
					}
					if (tmpVal.indexOf("\\\"") >= 0) {
						tmpVal = tmpVal.replace("\\", "");
					}
					valLst.add(tmpVal);
					paramMap.put(reqKey, valLst);
				} else {
					paramMap.put(reqKey, String.valueOf(reqVal));
				}
			}
			//logger.debug("464 parseString2Map==>paramMap="+ paramMap);
			return;
		} catch (Exception ex) {
			throw ex;
		} finally {
			if (logger != null)   { try { logger = null;    } catch (Exception ex) { } }
		}
	}
	
	public static List<Map<String, Object>> getMstDltParam1(JSONArray jsonArray) throws Exception {
		List<Map<String, Object>> listObj = new ArrayList<Map<String, Object>>();
		try {
			for (int i = 0; i < jsonArray.size(); i++) {
				Map<String, Object> sub = new HashMap<String, Object>();
				JSONObject jObj = (JSONObject) jsonArray.get(i);
				for (Object esub : jObj.entrySet()) {
					Map.Entry entry = (Map.Entry) esub;
					String reqKey  = String.valueOf(entry.getKey());
					Object reqVal  = entry.getValue();
					String clsName = reqVal.getClass().getTypeName();
					sub.put(reqKey, reqVal);
				}
				listObj.add(sub);
			}
			return listObj;
		} catch (Exception e) {
			throw e;
		} finally {
			if (listObj != null) {
				try { listObj = null; } catch (Exception ex) { }
			}
		}
	}

	public static Map<String, Object> parseMstDltParam0(JSONObject obj) throws Exception {
		// String jsonDataString =
		// "{\"regUserUID\":\"5\",\"agentID\":\"13\",\"msaID\":\"base\",\"returnObj\":\"java.lang.String\",\"methodNM\":\"getQuery4Json\",\"classNM\":\"grpcCommSvc\",\"sqlFileNM\":\"services-sql.xml\",\"procGB\":\"1\",\"userIP\":\"123.54.32.5\",\"serverIP\":\"192.168.1.56\",\"useYN\":\"Y\",\"steps\":[{\"sqlID\":\"getTopMenuList\",\"sqlText\":\"SELECT
		// * FROM EBDUSERTOPMENUS \n WHERE AGENTID = #{agentID} \n <if test='memberID !=
		// null'> \n AND MEMBERID = #{memberID} \n<\\/if> \n<if test='borgID != null'>
		// \n AND BORGID = #{borgID} \n<\\/if> \n<if test='menuID != null'> \n AND
		// MENUID = #{menuID} \n<\\/if> \n<if test='clientID != null'> \n AND CLIENTID =
		// #{clientID} \n<\\/if> \n<if test='loginID != null'> \n AND LOGINID =
		// #{loginID} \n<\\/if> \n<if test='menuCD != null'> \n AND MENUCODE = #{menuCD}
		// \n<\\/if>
		// \n\",\"crudGB\":\"R\",\"paramNM\":\"steps\",\"pID\":\"13\",\"beforeSql\":\"-\",\"seq\":\"1\"}]}";
		// JSONParser parser = new JSONParser();
		// Object obj = parser.parse(args);
		// JSONObject jsonObj = (JSONObject) obj;
		Logger logger = LoggerFactory.getLogger(GrpcDataUtil.class);
		Map<String, Object> subMap = new HashMap<String, Object>();
		try {
			for (Object e : obj.entrySet()) {
				Map.Entry entry = (Map.Entry) e;

				String reqKey  = String.valueOf(entry.getKey());
				Object reqVal  = entry.getValue();
				String clsName = reqVal.getClass().getTypeName();
				//logger.info("523 parseMstDltParam0.reqKey="+ reqKey +", Type.class->" + reqVal.getClass().getTypeName() +"->"+ reqVal);
				//org.json.simple.JSONArray
				if (clsName.indexOf("String") >= 0) {
					subMap.put(reqKey, "" + entry.getValue());
				} else if (clsName.indexOf("JSONArray") >= 0) {
					//logger.info("528 parseMstDltParam0.JSONArray ==> reqKey="+ reqKey +", Type.class->" + reqVal.getClass().getTypeName());
					JSONArray jjArray = (JSONArray)reqVal;
					try {
						JSONObject castObj = (JSONObject)jjArray.get(0);
						subMap.put(reqKey, GrpcDataUtil.getMstDltParam1(jjArray));
					} catch (ClassCastException ce) {
						List<Object> listObj = new ArrayList<Object>();     
						if (jjArray != null) { 
							for (int ii=0; ii<jjArray.size(); ii++){ 
								listObj.add(jjArray.get(ii));
							} 
							subMap.put(reqKey, listObj);
						} else {
							subMap.put(reqKey, reqVal);
						}
					}
				} else {
					subMap.put(reqKey, reqVal);
				}
			}
			return subMap;
		} catch (Exception e) {
			throw e;
		} finally {
			if (subMap != null) { 
				try { subMap = null; } catch (Exception ex) { }
			}
			if (logger != null)   { try { logger = null;    } catch (Exception ex) { } }
		}
	}
	
	/**
	 * List<Map>을 jsonString으로 변환한다.
	 *
	 * @param list List<Map<String, Object>>.
	 * @return String.
	 */
	public static String getJsonStringFromList(List<Map<String, Object>> list) {
		JSONArray jsonArray = null;
		try {
			jsonArray = getJsonArrayFromList(list);			
			return jsonArray.toJSONString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (jsonArray != null) { 
				try { jsonArray = null; } catch (Exception ex) { }
			}
		}
		
	}
	
	/**
	 * List<Map>을 jsonArray로 변환한다.
	 *
	 * @param  List<Map<String, Object>> list 
	 * @return JSONArray.
	 */
	public static JSONArray getJsonArrayFromList(List<Map<String, Object>> list) {
		JSONArray jsonArray = null;
		try {
			jsonArray = new JSONArray();
			for (Map<String, Object> map : list) {
				jsonArray.add(getJsonStringFromMap(map));
			}
			return jsonArray;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (jsonArray != null) { 
				try { jsonArray = null; } catch (Exception ex) { }
			}
		}
	}
	
	/**
	 * Map을 json으로 변환한다.
	 *
	 * @param  Map<String, Object> map
	 * @return JSONObject.
	 */
	public static JSONObject getJsonStringFromMap(Map<String, Object> map) {
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				jsonObject.put(key, ""+ value);
			}
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (jsonObject != null) { 
				try { jsonObject = null; } catch (Exception ex) { }
			}
		}
	}
}
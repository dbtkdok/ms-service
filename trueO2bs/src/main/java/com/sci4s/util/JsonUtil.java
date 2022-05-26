package com.sci4s.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JsonUtil {

	/**
	 * Map을 JsonString으로 변환한다.
	 *
	 * @param map Map<String, Object>.
	 * @return String.
	 */
	public static String getJsonStringFromMapHead(String headName, Map<String, Object> map) throws Exception {
		try {
			return "{\""+ headName +"\": ["+ getJsonStringFromMap(map) +"]}";
		} catch(Exception ex) {
			throw ex;
		}
	}
	
	
	/**
	 * String[]을 JsonString으로 변환한다.
	 *
	 * @param map Map<String, Object>.
	 * @return String.
	 */
	@SuppressWarnings("unchecked")
	public static String getStrArr2JsonArr(String[] valArr) throws Exception {
		StringBuffer sbStr = new StringBuffer();
		try {	
			for (int jj=0; jj<valArr.length; jj++) {
				if (jj == 0) sbStr.append("[\""+ valArr[jj] +"\"");
				else sbStr.append(",\""+ valArr[jj] +"\"");								
				if (jj==valArr.length-1) {
					sbStr.append("]");	
				}
			}			
			return sbStr.toString();
		} catch(Exception ex) {
			throw ex;
		} finally {
			if (sbStr != null) {
				try { sbStr = null; } catch (Exception ex) { }
			}
		}
	}
	/**
	 * Map을 JsonString으로 변환한다.
	 *
	 * @param map Map<String, Object>.
	 * @return String.
	 */
	@SuppressWarnings("unchecked")
	public static String getJsonStringFromMap(Map<String, Object> map) throws Exception {
		Logger logger = LoggerFactory.getLogger(JsonUtil.class);
		try {
			JSONObject jsonObject = new JSONObject();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String key      = entry.getKey();
				Object value    = entry.getValue();
				String valstr   = "";
				String[] valArr = null;				
				StringBuffer sbStr = new StringBuffer();
				
				//logger.info("["+ key +"]"+ value +"->getJsonStringFromMap ######################################"+ value.getClass().getName());
				if (value instanceof String[]) {
					valArr = (String[])value;					
					valstr = JsonUtil.getStrArr2JsonArr(valArr);
//					logger.info("key ######################################"+ key);
//					logger.info(valstr);
//					logger.info("key ######################################"+ key);					
					if (!"[\"\"]".equals(valstr)) {
						jsonObject.put(key, valstr);
					}
				} else if (value instanceof List) { // subPID 파라미터 처리. -> subMainKey=SYS0026_01
					List<Map<String,Object>> subParams = (List<Map<String,Object>>)value;
					sbStr.append("[{");
					int kk = 0;
					for (int ii=0; ii<subParams.size(); ii++) {						
						if (subParams.get(ii) instanceof Map) {
							Map<String,Object> subMap = subParams.get(ii);							
							for( String mapKey : subMap.keySet() ){
								//logger.info("mapKey ######################################"+ mapKey);
								valArr = (String[])subMap.get(mapKey);																
								if (kk == 0) {
									sbStr.append("\""+ mapKey +"\":");
								} else {
									sbStr.append(",\""+ mapKey +"\":");
								}
								sbStr.append(JsonUtil.getStrArr2JsonArr(valArr));																
								kk++;
							}//{"topMenuID":[Ljava.lang.String;@7170f0d7}
						}						
					}
					sbStr.append("}]");
					jsonObject.put(key, sbStr.toString());
				} else {
					jsonObject.put(key, value);
				} 	
			}

			return jsonObject.toJSONString();
		} catch(Exception ex) {
			throw ex;
		} finally {
			if (logger != null) {
				try { logger = null; } catch (Exception ex) { }
			}
		}
	}
	
	/**
	 * Map을 json으로 변환한다.
	 *
	 * @param map Map<String, Object>.
	 * @return JSONObject.
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject getJsonObjectFromMap(Map<String, Object> map) throws Exception {
		try {
			JSONObject jsonObject = new JSONObject();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String key   = entry.getKey();
				Object value = entry.getValue();
				jsonObject.put(key, value);
			}

			return jsonObject;
		} catch(Exception ex) {
			throw ex;
		}
	}

	/**
	 * List<Map>을 jsonArray로 변환한다.
	 *
	 * @param list List<Map<String, Object>>.
	 * @return JSONArray.
	 */
	@SuppressWarnings("unchecked")
	public static JSONArray getJsonArrayFromList(List<Map<String, Object>> list) throws Exception {
		try {
			JSONArray jsonArray = new JSONArray();
			for (Map<String, Object> map : list) {
				jsonArray.add(getJsonObjectFromMap(map));
			}
			return jsonArray;
		} catch(Exception ex) {
			throw ex;
		}
	}

	/**
	 * List<Map>을 jsonString으로 변환한다.
	 *
	 * @param list List<Map<String, Object>>.
	 * @return String.
	 */
	public static String getJsonStringFromList(List<Map<String, Object>> list) throws Exception {
		try {
			JSONArray jsonArray = getJsonArrayFromList(list);
			return jsonArray.toJSONString();
		} catch(Exception ex) {
			throw ex;
		}
	}

	/**
	 * JsonObject를 Map<String, String>으로 변환한다.
	 *
	 * @param jsonObj JSONObject.
	 * @return Map<String, Object>.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getMapFromJsonObject(JSONObject object) throws Exception {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			Iterator<String> keysItr = object.keySet().iterator();
	
			while (keysItr.hasNext()) {
				String key = keysItr.next();
				Object value = object.get(key);
	
				if (value instanceof JSONArray) {
					value = getListMapFromJsonArray((JSONArray) value);
				} else if (value instanceof JSONObject) {
					value = getMapFromJsonObject((JSONObject) value);
				}
				map.put(key, value);
			}
			return map;
		} catch(Exception ex) {
			throw ex;
		}
	}

	/**
	 * JsonArray를 List<Map<String, String>>으로 변환한다.
	 *
	 * @param jsonArray JSONArray.
	 * @return List<Map<String, Object>>.
	 */
	public static List<Map<String, Object>> getListMapFromJsonArray(JSONArray jsonArray) throws Exception {
		try {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			
			if (jsonArray != null) {
				int jsonSize = jsonArray.size();
				for (int i = 0; i < jsonSize; i++) {	
					Map<String, Object> map = getMapFromJsonObject((JSONObject) jsonArray.get(i));
					list.add(map);
				}
			}	
			return list;
		} catch(Exception ex) {
			throw ex;
		}
	}

	/**
     * Map을 json 문자열로 반환한다.
     *
     * @param map Map<String, Object>.
     * @return JSONObject.
     */
    public static String getJsonString4Map( Map<String, Object> map ) {
        JSONObject jsonObject = new JSONObject();
        for( Map.Entry<String, Object> entry : map.entrySet() ) {
            String key = entry.getKey();
            Object value = entry.getValue();
            jsonObject.put(key, value);
        }
        return jsonObject.toJSONString();
    }
    
    /**
     * RPC로 전달받은 결과에 다중의 데이터 json 문자열을 Map으로 변환하여 반환한다.
     *
     * @param  String jsonResults
     * @param  boolean isAddCalls
     * @return Map<String, Object>
     */
    public static Map<String, Object> getJsonToObjMap(String jsonResults, boolean isAddCalls) throws Exception {
    	JSONParser jsonParser = new JSONParser();
		JSONObject jsonObj    = null;
		JSONArray  jsonArr    = null;
		
		Map<String, Object> retMap  = new HashMap<String, Object>();
		Map<String, String> privMap = new HashMap<String, String>();
		Map<String, List<Map<String, Object>>> codesMap = new HashMap<String, List<Map<String, Object>>>();		
		
		try {	
			if (jsonResults.startsWith(",")) { // 권한이 없을 경우를 처리함.
				jsonResults = jsonResults.substring(1);
			}
//			System.out.println("response.getResults() ::: "+ jsonResults);

			jsonObj = (JSONObject) jsonParser.parse(jsonResults);
	        jsonArr = (JSONArray) jsonObj.get("results");
//	        System.out.println(jsonArr.size());
	        for(int i=0;i<jsonArr.size();i++){		        	
	        	JSONObject jsubObj = (JSONObject)jsonArr.get(i);
                Iterator itKeys = jsubObj.keySet().iterator();
                
                while(itKeys.hasNext()) {
                    String key = itKeys.next().toString();	 
//                    System.out.println("Key: "+ key +".......start");
                    if ("privNMs".equals(key)) { // 버튼권한 저장 처리
                    	//System.out.println("privNMs: "+jsubObj.get(key).toString());
                    	retMap.put("privNMs", jsubObj.get(key).toString());
                    } else {	
                    	Object classType  = jsubObj.get(key);
//                    	System.out.println(key +" ::::: "+ jsubObj.get(key).getClass().getTypeName());
                    	
                    	JSONArray  jsubArr = null;
                    	JSONObject dataObj = null;
                    	if (classType instanceof List ) {
                    		jsubArr = (JSONArray)jsubObj.get(key);
//                    		System.out.println(key +" is classType instanceof List");
                    	} else {
                    		dataObj = (JSONObject)jsubObj.get(key);
//                    		System.out.println(key +" is classType instanceof Object");
                    	}  
	                    if (isAddCalls) {
	                    	if (dataObj != null) {
	                    		retMap.put(key, JsonUtil.getMapFromJsonObject(dataObj));
	                    	} else {
	                    		if (jsubArr != null) {	                    		
		                    		List<Map<String, Object>> dataList = new Gson().fromJson(jsubArr.toJSONString(), 
		                    				new TypeToken<List<Map<String, Object>>>(){}.getType());
		                    		retMap.put(key, dataList);
		    		        	}
	                    	}
	                    } else { // 권한 및 공통코드 처리
	                    	if ("privIDs".equals(key)) { // 버튼권한 저장 처리	                    	
		                    	if (jsubArr != null) {	
			                    	for (int ii = 0; ii < jsubArr.size(); ii++) {
			    						JSONObject row = (JSONObject) jsubArr.get(ii);	
			    						System.out.println("row :::::" + row);						
//			    						privMap.put(""+ row.get("privID"), "1");
			    						privMap.put(""+ row.get("privID"), ""+ row.get("activeImg"));
			    					}					
			    					retMap.put("privIDs", privMap);
		                    	}
		                    } else if("searchBox".equals(key)) {
		                    	List<Map<String, Object>> searchList = new Gson().fromJson(jsubArr.toJSONString(), 
	                    				new TypeToken<List<Map<String, Object>>>(){}.getType());
		                    	retMap.put("searchBox", searchList);
		                    } else { // 공통코드 저장 처리
		                    	if (jsubArr != null) {	                    		
		                    		List<Map<String, Object>> codeList = new Gson().fromJson(jsubArr.toJSONString(), 
		                    				new TypeToken<List<Map<String, Object>>>(){}.getType());
		    		        		codesMap.put(key, codeList);
		    		        		retMap.put("codeTypes", codesMap);
		    		        	}
		                    }
	                    }
                    }
                    //System.out.println("Key: "+ key +".......end");
                }
	        }
	        return retMap;
		} catch(Exception e) {
			e.printStackTrace();		
			throw e;		
		} finally {
			if (jsonArr != null) { 
				try { jsonArr = null; } catch(Exception ex) { }
			}
			if (jsonObj != null) { 
				try { jsonObj = null; } catch(Exception ex) { }
			}
			if (jsonParser != null) { 
				try { jsonParser = null; } catch(Exception ex) { }
			}
			if (codesMap != null) { 
				try { codesMap = null; } catch(Exception ex) { }
			}
			if (privMap != null) { 
				try { privMap = null; } catch(Exception ex) { }
			}
			if (retMap != null) { 
				try { retMap = null; } catch(Exception ex) { }
			}
		}	
    }
    
    /**
     * RPC로 전달받은 결과에 다중의 데이터 json 문자열을 List<Map<String, Object>>으로 변환하여 반환한다.
     *
     * @param  String jsonResults
     * @return List<Map<String, Object>>
     */
    public static List<Map<String, Object>> getResultsToListMap(String jsonResults) throws Exception {
    	ObjectMapper objMapper = null;
    	JSONParser jsonParser  = new JSONParser();
		JSONObject jsonObj     = null;
		JSONArray  jsonArr     = null;
		List<Map<String, Object>> retList = null;
		try {	
			//System.out.println("response.getResults() ::: "+ jsonResults);
			jsonObj = (JSONObject) jsonParser.parse(jsonResults);
	        jsonArr = (JSONArray) jsonObj.get("results");
        	if (jsonArr != null && jsonArr.size() > 0) {	
        		objMapper = new ObjectMapper();	        		
        		retList = objMapper.readValue(jsonArr.toJSONString(), objMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        	} else {
        		retList = new ArrayList<Map<String, Object>>();
        	}        		
	        return retList;
		} catch(Exception e) {
			e.printStackTrace();		
			throw e;		
		} finally {
			if (jsonArr != null) { 
				try { jsonArr = null; } catch(Exception ex) { }
			}
			if (jsonObj != null) { 
				try { jsonObj = null; } catch(Exception ex) { }
			}
			if (jsonParser != null) { 
				try { jsonParser = null; } catch(Exception ex) { }
			}
			if (objMapper != null) { 
				try { objMapper = null; } catch(Exception ex) { }
			}
			if (retList != null) { 
				try { retList = null; } catch(Exception ex) { }
			}
		}	
    }
}
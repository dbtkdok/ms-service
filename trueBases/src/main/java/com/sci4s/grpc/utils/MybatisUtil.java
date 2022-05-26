package com.sci4s.grpc.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class MybatisUtil {
	
	public static List<Object> strToList(String obj) {
        return new Gson().fromJson(obj, new TypeToken<List<String>>(){}.getType() );
    }
	
	/**
	 * "1234567,231245,9087656" 형식의 데이터를 List로 변환하여 리턴함.
	 * @param obj
	 * @return
	 */
	public static List<String> strToArray(String obj) {
		
		List<String> lst = new ArrayList<String>();
		
		System.out.println("MybatisUtil.strToList2="+ obj);
		
		if (obj.indexOf(",") >= 0) {
			String[] str = obj.split(",");
			for (int ii=0; ii<str.length; ii++) {
				lst.add(str[ii].trim());
				System.out.println("MybatisUtil.strToList2.str[ii].trim()="+ str[ii].trim());
			}
		}		
        return lst;
    }
}

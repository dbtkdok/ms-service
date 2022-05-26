package com.sci4s.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Config {
	public static List<String> CONFIG_URL = null;
	public static String SVC_KEY = null;
	static {
		CONFIG_URL = new ArrayList<String>();
		CONFIG_URL.add("http://www.sci4s.com:8889/trueSrc-env/dev");
		
		SVC_KEY = UUID.randomUUID().toString();
	}
}

package com.sci4s.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class StringUtil {
	
	public static String getString4UTF8(String val) {
		if (val == null){
			return null;
		} else {
			byte[] b;								
			try {
				b = val.getBytes("8859_1");
				CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();				
				try {
					CharBuffer r = decoder.decode(ByteBuffer.wrap(b));					
					return r.toString();
				} catch (CharacterCodingException e) {
					return new String(b, "EUC-KR");	
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
}

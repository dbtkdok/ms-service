package com.sci4s.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	/**
	 * Date 값을 일정 포맷으로 변환을 담당하는 메서드
	 * 
	 * @param  Date date
	 * @return String frm - yyyy.MM.dd.HH.mm.ss
	 * @throws Exception
	 */
	public static String getDateFormat(Date date, String frm) {
		String ret = null;
		Format formatter = null;
		try {
			formatter = new SimpleDateFormat(frm);
			ret = formatter.format(date);
			return ret;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			if (formatter != null) { formatter = null; }
			if (ret != null) { ret = null; }
		}
	}
	
	/**
	 * 현재 시간을 Julian 시간으로 변환하여 리턴하는 메서드 
	 */
	public static int toJulian(Date d) {
	    final Calendar c = Calendar.getInstance();
	    c.setTime(d);
	    int year  = c.get(Calendar.YEAR);
	    int month = c.get(Calendar.MONTH) + 1;
	    int day   = c.get(Calendar.DAY_OF_MONTH);

	    int a = (14 - month) / 12;
	    int y = year + 4800 - a;
	    int m = month + (12 * a) - 3;
	    int JD = day + (((153 * m) + 2) / 5) + (365 * y) + (y / 4) - (y / 100) + (y / 400) - 32045;

	    return JD;
	}
}

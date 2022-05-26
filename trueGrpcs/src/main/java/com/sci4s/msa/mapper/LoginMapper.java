package com.sci4s.msa.mapper;

import java.util.Map;


import com.sci4s.msa.hr.dto.MemberVO;
import com.sci4s.msa.hr.dto.UserInfo;
public interface LoginMapper {	
	public UserInfo getLoginUserInfoForAll(Map<String,Object> param) throws Exception;
	public int insUserLoginHistory(Map<String,Object> param) throws Exception;
	public MemberVO loginSecurityInfo(Map<String,Object> param) throws Exception;
	
	// 매장 어플리케이션 실행 시, 필요한 인증서 정보를 리턴한다.
	public Map<String,Object> getFirstAuthData() throws Exception;
}

package com.sci4s.grpc.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class DataDao implements IDataDao {
	
	private SqlSession sqlSession;
	
	@Autowired
	public DataDao(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSession = sqlSessionTemplate;
	}	

	/**
	 * 조회 처리
	 * @param  String sqlID
	 * @param  Map<String, Object> params
	 * @return Object
	 */
	public List<Map<String, Object>> query4Object0(String sqlID, Object params) {
		return sqlSession.selectList(sqlID, params);
	}
	public Object query4Object1(String sqlID, Map<String, Object> params) {
		return sqlSession.selectOne(sqlID, params);
	}
	public Object query4Object2(String sqlID, Map<String, String> params) {
		return sqlSession.selectOne(sqlID, params);
	}
	public List<Map<String, Object>> query4List1(String sqlID, Map<String, Object> params) {		
		return sqlSession.selectList(sqlID, params);
	}
	public List<Object> query4List2(String sqlID, Map<String, Object> params) {		
		return sqlSession.selectList(sqlID, params);
	}
	public List<Object> query4List3(String sqlID, Map<String, String> params) {		
		return sqlSession.selectList(sqlID, params);
	}
	public Object query4List4(String sqlID, Map<String, Object> params) {
		return sqlSession.selectList(sqlID, params);
	}
	public Object query4List5(String sqlID, Map<String, String> params) {
		return sqlSession.selectList(sqlID, params);
	}
		
	/**
	 * 인서트, 업데이트 처리
	 * 
	 * @param  String sqlID
	 * @param  Map<String, Object> params
	 * @return Object
	 */
	public Object query4Insert1 (String sqlID, Map<String, Object> params) {
		return sqlSession.insert(sqlID, params);
	}
	public Object query4Insert2 (String sqlID, Map<String, String> params) {
		return sqlSession.insert(sqlID, params);
	}
	public Object query4Insert3 (String sqlID, List<Map<String, Object>> params) {
		return sqlSession.insert(sqlID, params);
	}
	public int query4Update1 (String sqlID, Map<String, Object> params) {
		return sqlSession.update(sqlID, params);
	}
	public int query4Update2 (String sqlID, Map<String, String> params) {
		return sqlSession.update(sqlID, params);
	}
	public int query4Update3 (String sqlID, List<Map<String, Object>> params) {
		return sqlSession.update(sqlID, params);
	}
}

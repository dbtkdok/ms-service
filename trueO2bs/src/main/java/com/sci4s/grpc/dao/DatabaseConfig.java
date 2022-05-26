package com.sci4s.grpc.dao;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;
 
@Configuration
@MapperScan(basePackages="com.sic4s.msa.mapper")
@EnableTransactionManagement
public class DatabaseConfig {

//	@Value("${spring.datasource.url}")
//	String DB_URL;
//	
//	@Value("${spring.datasource.username}")
//	String DB_USER;
//	
//	@Value("${spring.datasource.password}")
//	String DB_PWD;
//	
//	@Bean
//	public DataSource dataSource() {
//		HikariDataSource ds = new HikariDataSource();
////		ds.setDriverClassName("org.mariadb.jdbc.Driver");
////		ds.setJdbcUrl("jdbc:mariadb://192.168.219.195:3377/to2o?characterEncoding=utf8&allowMultiQueries=true&autoReconnect=true&autoReconnectForPools=true");
//		ds.setDriverClassName("org.mariadb.jdbc.Driver");
//		ds.setJdbcUrl(DB_URL);
//		ds.addDataSourceProperty("user", DB_USER);
//		ds.addDataSourceProperty("password", DB_PWD);
//		ds.setMaximumPoolSize(20);
//		ds.setMinimumIdle(20);
//		ds.setIdleTimeout(0);
//		ds.setPoolName("hikariPool");
//		ds.setMaxLifetime(28798000);
//		ds.setConnectionTimeout(10000);
////		ds.setTransactionIsolation(TRANSACTION_READ_COMMITTED);
//		ds.setLeakDetectionThreshold(2000);
//		ds.setConnectionTestQuery("SELECT 1");
//		ds.setAutoCommit(false);
//		return ds;
//	}
	
    @Bean
    public  SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
    	final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources("classpath:com/sci4s/msa/mapper/*.xml"));
        return sessionFactory.getObject();
    }
    
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) throws Exception {
    	final SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
      return sqlSessionTemplate;
    }
}

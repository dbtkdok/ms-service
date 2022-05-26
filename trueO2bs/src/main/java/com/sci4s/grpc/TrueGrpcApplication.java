package com.sci4s.grpc;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@ConfigurationProperties("application")
@EnableEurekaClient
@EnableDiscoveryClient
@EnableScheduling
@ComponentScan({"com.sci4s.grpc","com.sci4s.msa.**.svc"})
@MapperScan(basePackages="com.sci4s.msa.mapper")
public class TrueGrpcApplication {
	
	public static Logger logger = LoggerFactory.getLogger(TrueGrpcApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TrueGrpcApplication.class, args);
	}
}
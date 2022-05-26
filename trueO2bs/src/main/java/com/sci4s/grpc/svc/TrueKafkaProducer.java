package com.sci4s.grpc.svc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sci4s.grpc.dto.KafkaLogin;
import com.sci4s.grpc.dto.KafkaMsg;

/** 카프카로 MSA 로그를 전송함.
 * 
 * E:\Java\kafka_2.12-2.2.1\bin\windows>kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic sci4s-kafka-log
 * E:\Java\kafka_2.12-2.2.1\bin\windows>kafka-console-consumer.bat --bootstrap-server localhost:9093 --topic sci4s-kafka-login
 * 
 * @author flacom
 *
 */
@Service
public class TrueKafkaProducer {
	
	Logger logger = LoggerFactory.getLogger(TrueKafkaProducer.class);
	
	@Value("${kafka.topic}")
	String KAFKA_TOPIC;

	public void send(KafkaMsg kafkaMsg) throws Exception{	
		try {
			send(KAFKA_TOPIC, kafkaMsg);
		} catch(Exception ex) { 
			throw ex;
		}
	}
	
	public void sendLogin(KafkaLogin kafkaLogin) throws Exception{	
		try {
			send(KAFKA_TOPIC, kafkaLogin);
		} catch(Exception ex) { 
			throw ex;
		}
	}
	
	private void send(String kafkaTopic, Object kafkaMsg) throws Exception{	    	    
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = "";
		try {
//			logger.info("KafkaProducer send a PID Log from grpc-sci4j-"+ appProps.getProperty("msa.id") +" : " + kafkaMsg);
			
			jsonInString = mapper.writeValueAsString(kafkaMsg);			
			//kafkaTemplate.send(kafkaTopic, jsonInString);			
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}			    
	}
}
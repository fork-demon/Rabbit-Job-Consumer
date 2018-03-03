package com.sap.datacloud.enrichment.consumer;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class QueueToS3Consumer {

	
	@Autowired
	AmqpAdmin amqpAdmin;
	
	@Autowired
	ConnectionFactory connectionFactory;
	
	@Value("${internal.process.table}")
	String internalTableName;
	
	@Autowired
	private JdbcTemplate template;
	
	@Autowired
	private MessageProcessor receiver;
	
	static final Logger LOGGER = LoggerFactory.getLogger(QueueToS3Consumer.class);
	
	@Scheduled(cron = "${queue.schedule.rate}")
	@Transactional
	public void scheduleBatchJob(){
		
		LOGGER.debug("inside scheduleBatchJob(..) method");
		StringBuilder queryBuilder = new StringBuilder("SELECT * FROM ").append("\""+internalTableName+"\"").append(" WHERE STEP_TYPE='QueueToS3' AND STATUS='STARTED'");
		
		LOGGER.debug("select query to obtain the new jobs ids : {}",queryBuilder);
		List<Map<String,Object>> rows = template.queryForList(queryBuilder.toString());
		
		StringBuilder updateBuilder = new StringBuilder("UPDATE ").append("\""+internalTableName+"\"").append(" SET STATUS='INPROCESS' WHERE STEP_TYPE='QueueToS3' AND STATUS='STARTED'");
		
		LOGGER.debug("bulk update of selected job ids to make it INPROCESS : {}",updateBuilder);
		template.update(updateBuilder.toString());
		
		receiver.processMessage(rows);
		
	}

}

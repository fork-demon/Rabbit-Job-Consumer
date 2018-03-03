package com.sap.datacloud.enrichment.consumer;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.sap.datacloud.enrichment.constant.AppConstants;
import com.sap.datacloud.enrichment.dao.StatusDAO;
import com.sap.datacloud.enrichment.exception.DatacloudEnrichmentException;
import com.sap.datacloud.enrichment.model.StatusDTO;

@Component
public class MessageProcessor {

	@Autowired
	MessageReceiver receiver;

	@Autowired
	protected JdbcTemplate template;
	
	@Autowired
	private StatusDAO statusDAO;
	
	static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

	public void processMessage(List<Map<String, Object>> lst) {
		
		LOGGER.debug("Inside processMessage");
		
		for (Map<String, Object> map : lst) {
			
			String tenantId = (String) map.get(AppConstants.TENANT_ID);
			String jobId = (String) map.get("JOB_ID");
			String queueName = AppConstants.QUEUE_PREFIX + jobId;
			
			LOGGER.debug("Inside processMessage with tenantId : {}, job id : {} and queue name :{}",tenantId,jobId,queueName);

			receiver.setJobId(jobId);
			receiver.setTenantId(tenantId);
			
			boolean isException = false;

			try {
				receiver.handleMessage(queueName);
			} catch ( RuntimeException |DatacloudEnrichmentException e) {
				LOGGER.error("Error occured while retrieving message from queue for job id : {} with error msg : {}",jobId,e.getMessage());
				isException = true;
			}
			
			StatusDTO statusDTO = populateStatus(tenantId, jobId);
			
			if(isException){
				statusDTO.setExternalStatus("ERROR");
				statusDTO.setInternalStatus("ERROR");
			}

			statusDAO.insertStatus(statusDTO);
		}

	}

	public StatusDTO populateStatus(String tennantId, String jobId) {

		StatusDTO statusDTO = new StatusDTO();
		statusDTO.setTenantId(tennantId);
		statusDTO.setJobId(jobId);
		statusDTO.setExternalStatus("UPLOAD_COMPLETED");
		statusDTO.setPreviousExtrnalStatus("UPLOAD_IN_PROCESS");
		statusDTO.setInternalStatus("COMPLETED");
		statusDTO.setPreviousInternalStatus("INPROCESS");
		statusDTO.setInternalStepType("QueueToS3");
		return statusDTO;

	}

}
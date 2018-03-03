package com.sap.datacloud.enrichment.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import com.sap.datacloud.enrichment.dao.StatusDAO;
import com.sap.datacloud.enrichment.model.StatusDTO;

public class StatusDAOImpl implements StatusDAO {
	
	
	@Value("${external.process.table}")
	String externalTableName;

	@Value("${internal.process.table}")
	String internalTableName;
	
	@Autowired
	JdbcTemplate template;

	@Override
	public void insertStatus(StatusDTO statusDTO) {
		
		String externalQuery = "UPDATE "+"\""+internalTableName + "\""+ " SET STATUS= ? WHERE JOB_ID= ? AND STATUS=? AND TENANT_ID=?";
		
		template.update(externalQuery, new Object[]{statusDTO.getExternalStatus(),statusDTO.getJobId(),statusDTO.getPreviousExtrnalStatus(),statusDTO.getTenantId()});
		
		String internalQuery = "UPDATE "+"\""+externalTableName+"\""+ " SET STATUS= ? WHERE JOB_ID= ? AND STATUS=? AND TENANT_ID=? AND STEP_TYPE=?";
		
		template.update(internalQuery, new Object[]{statusDTO.getInternalStatus(),statusDTO.getJobId(),statusDTO.getTenantId(),statusDTO.getPreviousInternalStatus(),statusDTO.getInternalStepType()});
		
		
	}
	
	

}

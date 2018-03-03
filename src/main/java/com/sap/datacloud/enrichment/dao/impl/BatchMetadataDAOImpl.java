package com.sap.datacloud.enrichment.dao.impl;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.sap.datacloud.enrichment.dao.BatchMetadataDAO;
import com.sap.datacloud.enrichment.model.BatchMetadataDTO;

@Component
public class BatchMetadataDAOImpl implements BatchMetadataDAO{

	
	@Autowired
	JdbcTemplate template;
	
	@Value("${enrichment.package.tablename}")
	private String packageTableName;
	
	@Override
	public void insertBatchMetadata( BatchMetadataDTO batchMetadataDTO) {
		
		String batchInsertSQL = "INSERT INTO "+"\""+packageTableName+"\" VALUES(?,?,?,?,?,?)";
		
		template.update(batchInsertSQL, new Object[]{batchMetadataDTO.getTenantId(),batchMetadataDTO.getJobId(),batchMetadataDTO.getBatchNumber(),
				batchMetadataDTO.getCount(),new Timestamp(new Date().getTime()),new Timestamp(new Date().getTime())});
		
		
	}

}

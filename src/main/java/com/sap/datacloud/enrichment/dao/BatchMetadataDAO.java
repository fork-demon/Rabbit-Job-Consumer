package com.sap.datacloud.enrichment.dao;

import com.sap.datacloud.enrichment.model.BatchMetadataDTO;

@FunctionalInterface
public interface BatchMetadataDAO {
	
	void insertBatchMetadata( BatchMetadataDTO batchMetadataDTO);

}

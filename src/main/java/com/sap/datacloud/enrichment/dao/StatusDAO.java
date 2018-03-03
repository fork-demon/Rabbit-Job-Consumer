package com.sap.datacloud.enrichment.dao;

import com.sap.datacloud.enrichment.model.StatusDTO;

@FunctionalInterface
public interface StatusDAO {
	
	public void insertStatus(StatusDTO statusDTO);

}

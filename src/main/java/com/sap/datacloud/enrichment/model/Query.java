
package com.sap.datacloud.enrichment.model;

import java.util.HashMap;
import java.util.Map;

public class Query
{
    private final static long serialVersionUID = -1419502180636110555L;

    private String id;
    private String confidenceLowerLevelThresholdValue;
    private Map<String, Object> BusinessPartner = new HashMap<String, Object>();
    private Map<String, Object> Address = new HashMap<String, Object>();
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getConfidenceLowerLevelThresholdValue() {
		return confidenceLowerLevelThresholdValue;
	}
	public void setConfidenceLowerLevelThresholdValue(String confidenceLowerLevelThresholdValue) {
		this.confidenceLowerLevelThresholdValue = confidenceLowerLevelThresholdValue;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public Map<String, Object> getBusinessPartner() {
		return BusinessPartner;
	}
	public void setBusinessPartner(Map<String, Object> businessPartner) {
		BusinessPartner = businessPartner;
	}
	public Map<String, Object> getAddress() {
		return Address;
	}
	public void setAddress(Map<String, Object> address) {
		Address = address;
	}



}

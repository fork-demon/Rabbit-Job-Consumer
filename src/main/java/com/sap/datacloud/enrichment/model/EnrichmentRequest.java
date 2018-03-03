
package com.sap.datacloud.enrichment.model;

import java.util.List;

public class EnrichmentRequest
{

    private String batchNo;
    private String threshold;
    private String count;
    private List<Query> query = null;
    
	public String getBatchNo() {
		return batchNo;
	}
	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}
	public String getThreshold() {
		return threshold;
	}
	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public List<Query> getQuery() {
		return query;
	}
	public void setQuery(List<Query> query) {
		this.query = query;
	}

   
}

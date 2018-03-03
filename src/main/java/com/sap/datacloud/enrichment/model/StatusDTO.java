package com.sap.datacloud.enrichment.model;

public class StatusDTO {
	
	String internalStatus;
	String externalStatus;
	String tenantId;
	String jobId;
	String previousInternalStatus;
	String previousExtrnalStatus;
	String internalStepType;
	
	public String getInternalStatus() {
		return internalStatus;
	}
	public void setInternalStatus(String internalStatus) {
		this.internalStatus = internalStatus;
	}
	public String getExternalStatus() {
		return externalStatus;
	}
	public void setExternalStatus(String externalStatus) {
		this.externalStatus = externalStatus;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getPreviousInternalStatus() {
		return previousInternalStatus;
	}
	public void setPreviousInternalStatus(String previousInternalStatus) {
		this.previousInternalStatus = previousInternalStatus;
	}
	public String getPreviousExtrnalStatus() {
		return previousExtrnalStatus;
	}
	public void setPreviousExtrnalStatus(String previousExtrnalStatus) {
		this.previousExtrnalStatus = previousExtrnalStatus;
	}
	public String getInternalStepType() {
		return internalStepType;
	}
	public void setInternalStepType(String internalStepType) {
		this.internalStepType = internalStepType;
	}

}

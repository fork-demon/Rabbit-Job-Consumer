package com.sap.datacloud.enrichment.exception;

public class DatacloudEnrichmentException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4709910859875117819L;

	public DatacloudEnrichmentException() {

		super();
	}

	public DatacloudEnrichmentException(String msg) {

		super(msg);
	}

	public DatacloudEnrichmentException(String msg, Throwable t) {

		super(msg,t);
	}

}

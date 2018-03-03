package com.sap.datacloud.enrichment.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EnrichmentContext {
	
	private EnrichmentContext(){}
	
	private Map<String,String> mappingContext = null;

	public static EnrichmentContext  getInstance(){
		
		return EnrichmentContextHolder.INSTANCE;
	}
	
	static class EnrichmentContextHolder {
		private static EnrichmentContext INSTANCE = new EnrichmentContext();
	}
	
	public Map<String,String> getContext(){
		
		return  Collections.unmodifiableMap(mappingContext);
	}
	
	public void loadContext(Map<String,String> inputmap){
		if(null == mappingContext){
			mappingContext = new HashMap<>();
			mappingContext.putAll(inputmap);
		}
		
	}

}

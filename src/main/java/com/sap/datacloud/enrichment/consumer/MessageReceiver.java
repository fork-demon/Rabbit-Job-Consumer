package com.sap.datacloud.enrichment.consumer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.sap.datacloud.enrichment.dao.BatchMetadataDAO;
import com.sap.datacloud.enrichment.exception.DatacloudEnrichmentException;
import com.sap.datacloud.enrichment.model.BatchMetadataDTO;
import com.sap.datacloud.enrichment.model.EnrichmentContext;
import com.sap.datacloud.enrichment.model.EnrichmentRequest;
import com.sap.datacloud.enrichment.model.Query;

@Scope("prototype")
@Component
public class MessageReceiver {

	int messageCount = 0;

	String tenantId;

	String jobId;
	
	@Value("${s3.credential.accesskey}")
	private String accessKey;

	@Value("${s3.credential.secretKey}")
	private String secretKey;

	@Value("${s3.bucket.location}")
	private String bucketLocation;
	
	@Value("${mass.enrichment.domain}")
	private String domain;

	@Value("${mass.enrichment.provider}")
	private String provider;
	
	@Value("${dnb.ranking.field}")
	private String confidenceLable;
	
	@Autowired
	RabbitTemplate rabbitTemplate;
	
	@Autowired
	private BatchMetadataDAO batchMetadataDAO;
	
	static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiver.class);

	public void handleMessage(String queueName) throws DatacloudEnrichmentException   {
		
		LOGGER.debug("Inside handleMessage with queue name : {}",queueName);

		// get the S3 key
		String awsPath = getAWSKey();
		
		LOGGER.debug("Inside handleMessage with s3 path : {}",awsPath);
	
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
		ClientConfiguration clientConfig = new ClientConfiguration();
		clientConfig.setProtocol(Protocol.HTTP);

		AmazonS3Client  s3Client = new AmazonS3Client(credentials, clientConfig);
		
		ObjectMetadata objectMetadata = new ObjectMetadata();
		
		StringBuilder data = new StringBuilder();
		
				boolean isQueueEmpty = false;
				
				int i =0;
				
				while (!isQueueEmpty) {
					
				   Message message =rabbitTemplate.receive(queueName);
				   
				   MessageProperties properties = message.getMessageProperties();
				   
				   String tenenatId = (String) properties.getHeaders().get("tenantId");
				   
				   LOGGER.debug("Inside handleMessage with tenant Id : {}",tenenatId);
				
					if (null == message.getBody()) {
						
						LOGGER.debug("Inside handleMessage with message body as null, exiting the loop");
						isQueueEmpty = true;
					} else {

						String jsonString = null;
						try {
							jsonString = new String(message.getBody(),"UTF-8");
						} catch (UnsupportedEncodingException e) {
							LOGGER.error("Error occured while converting mesage from MQ to String : exception details {}",e.getMessage());
							throw new DatacloudEnrichmentException(e.getMessage(), e);
						}

						// convert message body with mapping
						String result = map(jsonString,tenenatId);
						
						//need to check if appending new line is needed.
						data.append(result).append(System.getProperty("line.separator"));
					}
					i++;
				}
	
				long contentLength = data.toString().getBytes().length;
				objectMetadata.setContentLength(contentLength);
				
		InputStream in = new ByteArrayInputStream(data.toString().getBytes());
		PutObjectRequest request = new PutObjectRequest(bucketLocation, awsPath, in, objectMetadata);
		TransferManager s3transferManager = new TransferManager(s3Client);
		
		Upload upload = s3transferManager.upload(request);

		// wait for upload to S3 server
		try {
			upload.waitForCompletion();
		} catch (AmazonClientException | InterruptedException e) {
			LOGGER.error("Error occured whilewaiting for s3 file completion : exception details {}",e.getMessage());
			throw new DatacloudEnrichmentException(e.getMessage(), e);
		}
		
		LOGGER.debug("upload completed in S3 , total message count : {}",i);
	  
	}

	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
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
	
	public String map(String json, String tenantId){
		
		LOGGER.debug("inside map(..) method tenant : {}",tenantId);
		
		StringBuilder line = new StringBuilder();
		
		Map<String,String> mappingMap = EnrichmentContext.getInstance().getContext();
		
		Gson gson = new Gson();


		JsonReader reader = new JsonReader(new StringReader(json));
		reader.setLenient(true);
		
	
		EnrichmentRequest request = gson.fromJson(reader,  new TypeToken<EnrichmentRequest>() {}.getType());		
		String batchNumber = request.getBatchNo();
		String count = request.getCount();
		String thresholdInHeader = request.getThreshold(); 
		List<Query> records = request.getQuery();
		
		BatchMetadataDTO metadataDTO = new BatchMetadataDTO();
		metadataDTO.setBatchNumber(batchNumber);
		metadataDTO.setCount(count);
		metadataDTO.setJobId(jobId);
		metadataDTO.setTenantId(tenantId);
		//insert record in batch table
		
		LOGGER.debug("before insert batch metatdata");
		batchMetadataDAO.insertBatchMetadata(metadataDTO);
		LOGGER.debug("after insert batch metatdata");
		
		for(Query record: records){
			
			String thresholdValue = record.getConfidenceLowerLevelThresholdValue();
			
			line.append(record.getId()).append(", ");
			
			for(Map.Entry<String, Object>  businessPartnerEntry: record.getBusinessPartner().entrySet()){
				String key = businessPartnerEntry.getKey();
				Object value = businessPartnerEntry.getValue();
				
				line.append(mappingMap.get(key)).append("=").append(value).append("&");
			}
			line.setLength(line.length() -1);
			
			for(Map.Entry<String, Object>  addressEntry: record.getAddress().entrySet()){
				String key = addressEntry.getKey();
				Object value = addressEntry.getValue();
				
				line.append(mappingMap.get(key)).append("=").append(value).append("&");
			}
			
			if(null == thresholdValue){
				thresholdValue = thresholdInHeader;
			}
			
			line.append(mappingMap.get(confidenceLable)).append("=").append(thresholdValue);
		}
		
		LOGGER.debug("returning from map method");
		
		return line.toString();
	}
	
	public String getAWSKey(){
		
		return "/"+domain+"/"+provider+"/"+tenantId+"/"+jobId+"/input.csv";
	}

}

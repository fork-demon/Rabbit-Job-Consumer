package com.sap.datacloud.enrichment.consumer;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.sap.datacloud.enrichment.model.EnrichmentContext;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@EnableScheduling
@PropertySource("classpath:fieldmapping.properties")
public class Application implements CommandLineRunner {

	@Autowired
	private Environment env;
	
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setUri("amqp://eMhtf53hl-g_dfOV:iKPnu3LdGF9oGV9Z@127.0.0.1:60845");
		connectionFactory.setPublisherConfirms(true);
		return connectionFactory;
	}

    @Bean
	RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate();
		RetryTemplate retryTemplate = new RetryTemplate();
		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(500);
		backOffPolicy.setMultiplier(10.0);
		backOffPolicy.setMaxInterval(10000);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		template.setRetryTemplate(retryTemplate);
		template.setMessageConverter(jsonMessageConverter());
		template.setConnectionFactory(connectionFactory);
		
		return template;
	}
    
	@Bean
	public AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(connectionFactory());
	}

	@Bean
	public Jackson2JsonMessageConverter jsonMessageConverter() {
		Jackson2JsonMessageConverter con = new Jackson2JsonMessageConverter();
		return con;
	}

	@Override
	public void run(String... arg0) throws Exception {

		EnrichmentContext context = EnrichmentContext.getInstance();
		
		Map<String,String> map = new HashMap<>();

		String mappingInfo = env.getProperty("dnb.fields.mapping");
		
		if(null != mappingInfo){
			
			String[] mappingArray = mappingInfo.split("\\,");
			
			for(String keyValue : mappingArray){
				
				String[] pair = keyValue.split("\\:");
				map.put(pair[0], pair[1]);
			}
			
			context.loadContext(map);
			
		}
		
	}

	
}

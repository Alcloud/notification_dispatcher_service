package eu.credential.wallet.notification_dispatcher_service.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConsumerGroup {
	
	private static Logger logger = LoggerFactory.getLogger(KafkaConsumerGroup.class);
	
	private final Properties kafkaProperties;
	private final int numberOfConsumers;
	private final String subscribetopic;
	private final String sendtopic;
	private List<KafkaConsumerThread> consumers;

	public KafkaConsumerGroup(Properties kafkaProperties, String subscribetopic, String sendtopic, int numberOfConsumers) {
		
		this.kafkaProperties = kafkaProperties;
		this.subscribetopic = subscribetopic;
		this.sendtopic = sendtopic;
		this.numberOfConsumers = numberOfConsumers;
		consumers = new ArrayList<KafkaConsumerThread>();
		for (int i = 0; i < this.numberOfConsumers; i++) {
			logger.debug("Creating KafkaConsumerThreads: " + numberOfConsumers);
			KafkaConsumerThread consumerThread = new KafkaConsumerThread(kafkaProperties, this.subscribetopic, this.sendtopic);
			consumers.add(consumerThread);
		}
	}

	public void execute() {
		
		logger.debug("Starting Threads ...");
		
		for (KafkaConsumerThread consumerThread : consumers) {
			Thread t = new Thread(consumerThread);
			t.start();
		}
	}

	/**
	 * @return the numberOfConsumers
	 */
	public int getNumberOfConsumers() {
		return numberOfConsumers;
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return kafkaProperties.getProperty("group.id");
	}
}

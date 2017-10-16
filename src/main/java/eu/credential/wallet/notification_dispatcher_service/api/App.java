package eu.credential.wallet.notification_dispatcher_service.api;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {

	private Logger logger = LoggerFactory.getLogger(App.class);
	private PersonalizationProperties properties;

	private Options options = new Options();
	private CommandLineParser parser = new DefaultParser();

	private String confFile = null;

	public static void main(String[] args) {
		App myApp = new App(args);
		myApp.start();
	}

	private App(String[] args) {

		Option optConf = Option.builder("conf").desc("configuration").hasArg().argName("conf").build();
		options.addOption(optConf);

		try {
			CommandLine cmd = parser.parse(options, args, true);

			if (cmd.hasOption("conf")) {
				confFile = cmd.getOptionValue("conf");
			}
		} catch (Exception e) {
			//tbd
		}
	}

	private void start() {

		logger.info("Starting Application");

		if (confFile != null) {
			// load properties from configured file
			properties = PersonalizationProperties.getInstance(confFile);
		} else {
			// load properties from baked in configuration file
			properties = PersonalizationProperties.getInstance();
		}

		if (properties != null) {
		
			// Initialize Kafka Consumer
			Properties kafkaProperties = properties.getPropertyGroup("kafka.consumer");
			String topic = properties.getValue("subscribetopic");
			String sendtopic = properties.getValue("sendtopic");
			logger.info("Subscribe to topic {} and send to topic {}", topic, sendtopic);
			int numberOfConsumer = new Integer(properties.getValue("consumers")).intValue();
			
			KafkaConsumerGroup consumerGroup = new KafkaConsumerGroup(kafkaProperties, topic, sendtopic, numberOfConsumer);
			consumerGroup.execute();

		} else {
			logger.error("Error on loading configuration.");
		}

	}
}
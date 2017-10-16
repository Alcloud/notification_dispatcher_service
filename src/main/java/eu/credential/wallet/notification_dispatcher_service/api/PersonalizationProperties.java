package eu.credential.wallet.notification_dispatcher_service.api;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PersonalizationProperties {

	private static Logger logger = LoggerFactory.getLogger(PersonalizationProperties.class);
	
	private static PersonalizationProperties instance = null;
	private Properties properties;


	protected PersonalizationProperties() throws IOException {
		
		logger.info("Load properties from integrated configuration file /service.properties");
		properties = new Properties();
		properties.load(getClass().getResourceAsStream("/service.properties"));

	}
	
	protected PersonalizationProperties(String confFile) throws IOException {
		
		logger.info("Load properties from provided configuration file " + confFile);
		Reader reader = new FileReader(confFile);
		properties = new Properties();
		properties.load(reader);

	}

	public static PersonalizationProperties getInstance() {
		if(instance == null) {
			try {
				instance = new PersonalizationProperties();
			} catch (IOException e) {
				logger.error("Error while loading configuration: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		return instance;
	}
	
	public static PersonalizationProperties getInstance(String confFile) {
		try {
			instance = new PersonalizationProperties(confFile);
		} catch (IOException e) {
			logger.error("Error while loading configuration: " + e.getLocalizedMessage());
		}
		return instance;
	}

	public String getValue(String key) {
		logger.debug("Load property: " + key +"=" + properties.getProperty(key));
		return properties.getProperty(key);
	}

	public Properties getProperties() {
		return properties;
	}
	
	public Properties getPropertyGroup(String partName) {
		
		Properties propertyGroup = new Properties();
		
		Set<String> set = properties.stringPropertyNames();
		for (String propertyName : set){
		    if (propertyName.startsWith(partName.concat("."))) {
		    	propertyGroup.put(propertyName.replaceFirst(partName.concat("."), ""), properties.getProperty(propertyName, ""));
		    }
		}
		
		return propertyGroup;
	}

}
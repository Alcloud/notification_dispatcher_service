package eu.credential.wallet.notification_dispatcher_service.api;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppProperties {

    private static Logger logger = LoggerFactory.getLogger(AppProperties.class);

    private static AppProperties instance = null;
    private Properties properties;


    protected AppProperties() throws IOException {

        logger.info("Load properties from integrated configuration file /service.properties");
        properties = new Properties();
        properties.load(getClass().getResourceAsStream("/service.properties"));

    }

    protected AppProperties(String confFile) throws IOException {

        logger.info("Load properties from provided configuration file " + confFile);
        Reader reader = new FileReader(confFile);
        properties = new Properties();
        properties.load(reader);

    }

    public static AppProperties getInstance() {
        if(instance == null) {
            try {
                instance = new AppProperties();
            } catch (IOException e) {
                logger.error("Error while loading configuration: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
        return instance;
    }

    public static AppProperties getInstance(String confFile) {
        try {
            instance = new AppProperties(confFile);
        } catch (IOException e) {
            logger.error("Error while loading configuration: " + e.getLocalizedMessage());
        }
        return instance;
    }

    public String getValue(String key) {
        logger.debug("Load property: " + key +"=" + properties.getProperty(key));
        return properties.getProperty(key);
    }

}
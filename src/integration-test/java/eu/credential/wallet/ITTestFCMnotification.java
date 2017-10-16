package eu.credential.wallet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import eu.credential.config.schema.FCMNotification;
import eu.credential.config.schema.KeyValue;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.glassfish.jersey.client.ClientConfig;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.mongodb.MongoClient;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import eu.credential.config.schema.Notification;
import eu.credential.wallet.notification_dispatcher_service.api.PersonalizationProperties;
import eu.credential.wallet.notification_dispatcher_service.api.impl.mongo.NotificationMongoClientFactory;

public class ITTestFCMnotification {

    protected static final Logger logger = LoggerFactory.getLogger(ITTestFCMnotification.class);

    protected static String preferenceId;

    @BeforeClass
    public static void init() throws JsonParseException, JsonMappingException, IOException {

        MongoClient mongoClient = NotificationMongoClientFactory.getNotificationPreferenceMongoClient();
        mongoClient.dropDatabase("notifications");
        MongoDatabase db = mongoClient.getDatabase("notifications");
        MongoCollection<eu.credential.wallet.notification_dispatcher_service.model.Notification> mc =
                db.getCollection("notifications", eu.credential.wallet.notification_dispatcher_service.model.Notification.class);

        // Init the preference database
        ClientConfig cfg = new ClientConfig();
		cfg.register(JacksonJsonProvider.class);
		Client client = ClientBuilder.newClient(cfg);

        String notificationMgmtServiceHost = PersonalizationProperties.getInstance()
                .getValue("notificationmanagementservice.host");
        String notificationMgmtServiceport = PersonalizationProperties.getInstance()
                .getValue("notificationmanagementservice.port");

        WebTarget target = client.target("http://" + notificationMgmtServiceHost + ":" + notificationMgmtServiceport)
				.path("v1").path("notificationManagementService").path("addPreferences");
		Invocation.Builder invocation = target.request(MediaType.APPLICATION_JSON);

        // Dummy preference
        String newPreferenceInput = "{\"preferenceList\":[" +
                "{\"accountId\":{\"value\":\"HansAugust\"},\"preferenceType\":{\"system\":\"https://credential.eu/config/codesystems/preferencetypes\",\"code\":\"newdata\"}," +
                "\"preferenceDetails\":[" +
                "{\"key\":{\"system\":\"https://credential.eu/config/codesystems/preferenceitemkey\",\"code\":\"appid\"},\"value\":\"1234\"}," +
                "{\"key\":{\"system\":\"https://credential.eu/config/codesystems/preferenceitemkey\",\"code\":\"pull\"},\"value\":\"yes\"}]}]}";

        String response = invocation.post(Entity.entity(newPreferenceInput, MediaType.APPLICATION_JSON),
				String.class);
		
        logger.info("Response is " + response);

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
        };

        HashMap<String, Object> o = mapper.readValue(response, typeRef);

        List<Map<String, Object>> preferenceList = (List<Map<String, Object>>) o.get("preferenceList");

        for (Map<String, Object> preference : preferenceList) {
            if (((String) ((Map<String, Object>) preference.get("preferenceType")).get("code")).equals("newdata")) {
                preferenceId = (String) ((Map<String, Object>) preference.get("preferenceId")).get("value");
            }
        }

        logger.info("The preference id is " + preferenceId);

    }

    @AfterClass
    public static void shutdown() {
        // clear all databases:
        // Empty notification preference db
        MongoClient mongoClient = NotificationMongoClientFactory.getNotificationPreferenceMongoClient();
        mongoClient.dropDatabase("notifications");
    }

    /**
     * This test case verifies that the dispatcher service sends out a
     * notification towards kafka after a new notification from
     * processing service was received through kafka.
     */
    @Test(timeout = 10000)
    public void shouldHaveSendAFCMNotificationAfterNotificationWasAdded() {

        KafkaConsumer<String, byte[]> consumer = null;

        Properties kafkaProperties = PersonalizationProperties.getInstance().getPropertyGroup("kafka.consumer");

        consumer = new KafkaConsumer<String, byte[]>(kafkaProperties);
        try {

            // We listen on the send topic to verify that this service emits the correct message
            String topic = PersonalizationProperties.getInstance().getValue("sendtopic");
            consumer.subscribe(Arrays.asList(topic));

            // Send the new data event to the notification service
            Client client = ClientBuilder.newClient();

            String notificationServiceHost = PersonalizationProperties.getInstance()
                    .getValue("notificationservice.host");
            String notificationServiceport = PersonalizationProperties.getInstance()
                    .getValue("notificationservice.port");

            WebTarget target = client.target("http://" + notificationServiceHost + ":" + notificationServiceport)
					.path("v1").path("notificationService").path("notify");
			Invocation.Builder invocation = target.request(MediaType.APPLICATION_JSON);

            final String dataid = "abcd";
            final String initiator = "ErnaMueller";
            String newDocumentInput = "{\"accountId\":{\"type\":{\"system\":\"https://credential.eu/config/codesystems/identifiers\",\"code\":\"systemId\"},\"value\":\"Data Management Service\"},\"event\":{\"eventType\":{\"system\":\"https://credential.eu/config/codesystems/events\",\"code\":\"newdata\"},\"eventCreationTime\":\"2017-06-07T08:53:42.173\",\"eventContent\":[{\"key\":{\"system\":\"https://credential.eu/config/codesystems/eventkey\",\"code\":\"dataid\"},\"value\":\"abcd\"},{\"key\":{\"system\":\"https://credential.eu/config/codesystems/eventkey\",\"code\":\"initiator\"},\"value\":\"ErnaMueller\"},{\"key\":{\"system\":\"https://credential.eu/config/codesystems/eventkey\",\"code\":\"targetparticipant\"},\"value\":\"HansAugust\"}]}}";

            String response = invocation.post(Entity.entity(newDocumentInput, MediaType.APPLICATION_JSON),
					String.class);

            final String appid = "1234";

            while (true) {
                ConsumerRecords<String, byte[]> records = consumer.poll(100);
                boolean found = false;
                for (ConsumerRecord<String, byte[]> record : records) {
                    logger.info("Found a record {}", record);

                    // Do decoding stuff similar to the following example:
                    //
                    SpecificDatumReader<FCMNotification> reader = new SpecificDatumReader<FCMNotification>(
                            FCMNotification.getClassSchema());
                    Decoder decoder = DecoderFactory.get().binaryDecoder(record.value(), null);
                    try {
                        FCMNotification notification = reader.read(null, decoder);
                        logger.info("Found the notification {}. Check if it is the right one.", notification);

                        Assert.assertThat(notification.getAccountId(), Matchers.notNullValue());
                        Assert.assertThat(notification.getAccountId().getValue().toString(), Matchers.is("HansAugust"));
                        Assert.assertThat(notification.getAppId().toString(), Matchers.is(appid));

                        found = true;
                        break;

                    } catch (IOException e) {
                        Assert.fail("Could not decode notification from kafka message bus: " + e.getMessage());
                    }
                }
                if (found) {
                    break;
                }
            }
        } finally {
            consumer.close();
        }
    }
}

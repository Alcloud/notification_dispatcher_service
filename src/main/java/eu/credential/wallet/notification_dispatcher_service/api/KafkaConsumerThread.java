package eu.credential.wallet.notification_dispatcher_service.api;

import java.io.IOException;
import java.util.*;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import eu.credential.config.schema.FCMNotification;
import eu.credential.wallet.notification_dispatcher_service.api.impl.mongo.NotificationMongoClient;
import eu.credential.wallet.notificationservice.NotificationManagementServiceClient;
import eu.credential.wallet.notificationservice.model.GetPreferencesRequest;
import eu.credential.wallet.notificationservice.model.GetPreferencesResponse;
import eu.credential.wallet.notificationservice.model.Preference;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.credential.config.schema.KeyValue;
import eu.credential.config.schema.Notification;
import eu.credential.wallet.notification_dispatcher_service.model.Identifier;

public class KafkaConsumerThread implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(KafkaConsumerThread.class);
    private static String initiator;
    private static String targetParticipant;
    private static String notificationType;
    private static String accessTypeValue;
    private static String dataid;

    private final KafkaConsumer<String, byte[]> consumer;

    private static final String DB_NAME = "notifications";
    private static final String COLLECTION_NAME = "notifications";
    private static final String NOTIFICATION_TYPE_1 = "newdata";
    private static final String NOTIFICATION_TYPE_2 = "documentaccess";
    private static final String NOTIFICATION_TYPE_3 = "documentchanged";
    private static final String NOTIFICATION_TYPE_4 = "grantaccess";
    private static final String NOTIFICATION_TYPE_5 = "requestaccess";
    private static final String NOTIFICATION_DATA_ID_1 = "abcd";
    private static final String NOTIFICATION_DATA_ID_2 = "abc";
    private static final String NOTIFICATION_DATA_ID_3 = "ab";

    KafkaConsumerThread(Properties kafkaProperties, String subscribetopic, String sendtopic) {
        logger.debug("Creating KafkaConsumer");
        this.consumer = new KafkaConsumer<>(kafkaProperties);

        logger.debug("Subscribing to topic: " + subscribetopic);
        this.consumer.subscribe(Arrays.asList(subscribetopic));
    }

    public void run() {

        logger.debug("Running consumer ...");

        while (true) {

            try {
                ConsumerRecords<String, byte[]> records = consumer.poll(100);
                for (ConsumerRecord<String, byte[]> record : records) {

                    // Do decoding stuff similar to the following example:
                    SpecificDatumReader<Notification> reader = new SpecificDatumReader<>(
                            Notification.getClassSchema());
                    Decoder decoder = DecoderFactory.get().binaryDecoder(record.value(), null);
                    try {
                        Notification notification = reader.read(null, decoder);

                        logger.info("Received a new notification {}", notification);

                        eu.credential.config.schema.Identifier accountIdKafka = notification.getAccountId();
                        String preferenceIdValue = notification.getAssociatedPreference().toString();
                        List<KeyValue> notificationDetailsKafka = notification.getNotificationDetails();

                        // get the url and port for the notification management
                        // service
                        String notificationmgmtservicehost = PersonalizationProperties.getInstance()
                                .getValue("notificationmanagementservice.host");
                        String notificationmgmtserviceport = PersonalizationProperties.getInstance()
                                .getValue("notificationmanagementservice.port");

                        // request notification preference from mgmt service
                        NotificationManagementServiceClient mgmtClient = new NotificationManagementServiceClient(
                                notificationmgmtservicehost, notificationmgmtserviceport);
                        logger.info(
                                "Create a request for the notification management service in order to request "
                                        + "the users settings {}:{}",
                                notificationmgmtservicehost, notificationmgmtserviceport);

                        GetPreferencesRequest request = new GetPreferencesRequest();
                        eu.credential.wallet.notificationservice.model.Identifier accountIdFCM = new eu.credential.wallet.notificationservice.model.Identifier();
                        accountIdFCM.setValue(accountIdKafka.getValue().toString());
                        request.setAccountId(accountIdFCM);

                        GetPreferencesResponse preferencesResponse;
                        preferencesResponse = mgmtClient.getPreferences(request);

                        for (KeyValue content : notificationDetailsKafka) {
                            if (content != null && "notificationType".equals(content.getKey().getCode().toString())) {

                                // set notification details
                                if (NOTIFICATION_TYPE_1.equals(content.getValue().toString())) {
                                    notificationType = NOTIFICATION_TYPE_1;
                                }
                                if (NOTIFICATION_TYPE_2.equals(content.getValue().toString())) {
                                    notificationType = NOTIFICATION_TYPE_2;
                                }
                                if (NOTIFICATION_TYPE_3.equals(content.getValue().toString())) {
                                    notificationType = NOTIFICATION_TYPE_3;
                                }
                                if (NOTIFICATION_TYPE_4.equals(content.getValue().toString())
                                        && "accesstype".equals(content.getKey().getCode().toString())) {
                                    notificationType = NOTIFICATION_TYPE_4;
                                    if ("read".equals(content.getValue().toString())) {
                                        accessTypeValue = "read";
                                    } else if ("write".equals(content.getValue().toString())) {
                                        accessTypeValue = "write";
                                    } else if ("readwrite".equals(content.getValue().toString())) {
                                        accessTypeValue = "readwrite";
                                    }
                                }
                                if (NOTIFICATION_TYPE_5.equals(content.getValue().toString())
                                        && "accesstype".equals(content.getKey().getCode().toString())) {
                                    notificationType = NOTIFICATION_TYPE_5;
                                    if ("read".equals(content.getValue().toString())) {
                                        accessTypeValue = "read";
                                    } else if ("write".equals(content.getValue().toString())) {
                                        accessTypeValue = "write";
                                    } else if ("readwrite".equals(content.getValue().toString())) {
                                        accessTypeValue = "readwrite";
                                    }
                                }
                            }

                            if (content != null && "dataid".equals(content.getKey().getCode().toString())) {
                                dataid = getDataFromId(content);
                            }
                            if (content != null && "initiator".equals(content.getKey().getCode().toString())) {
                                initiator = content.getValue().toString();
                            }
                            if (content != null && "targetparticipant".equals(content.getKey().getCode().toString())) {
                                targetParticipant = content.getValue().toString();
                            } else {
                                targetParticipant = accountIdFCM.getValue();
                            }
                        }

                        List<eu.credential.wallet.notification_dispatcher_service.model.KeyValue> notificationDetails = new ArrayList<>();
                        switch (notificationType) {
                            case "newdata":
                                notificationDetails.add(setNotificationDetailsData(createMessage("sent ")));
                                break;
                            case "documentaccess":
                                notificationDetails.add(setNotificationDetailsData(createMessage("gave an access for")));
                                break;
                            case "documentchanged":
                                notificationDetails.add(setNotificationDetailsData(createMessage("has changed ")));
                                break;
                            case "grantaccess":
                                switch (accessTypeValue) {
                                    case "read":
                                        notificationDetails
                                                .add(setNotificationDetailsData(createMessage("has allowed to read ")));
                                        break;
                                    case "write":
                                        notificationDetails
                                                .add(setNotificationDetailsData(createMessage("has allowed to write ")));
                                        break;
                                    case "readwrite":
                                        notificationDetails.add(
                                                setNotificationDetailsData(createMessage("has allowed to read and write ")));
                                        break;
                                    default:
                                        logger.info("There is no right access type");
                                        break;
                                }
                                break;
                            case "requestaccess":
                                switch (accessTypeValue) {
                                    case "read":
                                        notificationDetails.add(
                                                setNotificationDetailsData(createMessage("sent read permission request for")));
                                        break;
                                    case "write":
                                        notificationDetails.add(
                                                setNotificationDetailsData(createMessage("sent write permission request for")));
                                        break;
                                    case "readwrite":
                                        notificationDetails.add(setNotificationDetailsData(
                                                createMessage("sent read and write permission request for")));
                                        break;
                                    default:
                                        logger.info("There is no right access type");
                                        break;
                                }
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid notificationDetails: " + notificationDetails);
                        }

                        // initial Notification DB
                        MongoClient mongoClient = new NotificationMongoClient();
                        MongoDatabase db = mongoClient.getDatabase(DB_NAME);
                        MongoCollection<eu.credential.wallet.notification_dispatcher_service.model.Notification> mc = db
                                .getCollection(COLLECTION_NAME,
                                        eu.credential.wallet.notification_dispatcher_service.model.Notification.class);

                        // 1. Create a notification and save it to
                        // Notification DB
                        // --------------------------------------------------------
                        // create a unique notification ID
                        eu.credential.config.schema.Identifier notificationIdKafka = new eu.credential.config.schema.Identifier();
                        notificationIdKafka.setValue(UUID.randomUUID().toString());
                        notificationIdKafka.setNamespace("eu.credential.wallet");
                        eu.credential.config.schema.Coding type = new eu.credential.config.schema.Coding();
                        type.setCodeSystem("https://credential.eu/config/codesystems/identifiers");
                        type.setCode("notificationId");
                        type.setVersion("1.0");
                        notificationIdKafka.setType(type);
                        logger.info("A new notification ID was created {}", notificationIdKafka.getValue().toString());

                        // set notificationCreationTime
                        Date notificationCreationTime = new Date();
                        notificationCreationTime.getTime();

                        // create a notification
                        eu.credential.wallet.notification_dispatcher_service.model.Notification newNotification = new eu.credential.wallet.notification_dispatcher_service.model.Notification();
                        newNotification.setAccountId(getTargetId(accountIdKafka)); // accountID
                        newNotification.setNotificationId(getTargetId(notificationIdKafka)); // notificationID
                        newNotification.setNotificationCreationTime(notificationCreationTime); // notificationCreationTime
                        newNotification.setNotificationDetails(notificationDetails); // notification
                        // details
                        logger.info("A new notification was added to DB{}", newNotification.toString());
                        mc.insertOne(newNotification);
                        // --------------------------------------------------------------------
                        // 2. Get a appID and send to Kafka FCM message sender
                        // queue
                        // 2.a Looking for an appID in preference DB and send to
                        // Kafka FCM message sender queue
                        logger.info("Looking for an appID in preference DB."
                                + " and build a FCM notification object for kafka and send it.");

                        FCMNotification kafkaNotificationFCM = new FCMNotification();

                        kafkaNotificationFCM.setAccountId(accountIdKafka);
                        kafkaNotificationFCM.setAppId(getAnAppId(preferencesResponse, preferenceIdValue));
                        kafkaNotificationFCM.setNotificationId(notificationIdKafka.getValue());

                        CredentialKafkaProducer producer = new CredentialKafkaProducer();
                        producer.sendMessage(kafkaNotificationFCM);

                    } catch (IOException e) {
                        logger.warn("Could not process received notification event message.", e);
                    }

                }
            } catch (Throwable t) {
                logger.warn("An error occured while processing new data events {}", t);
            }
        }
    }

    /**
     * Returns the target ID from the given notification Content. If no
     * identifier could be found null is returned.
     *
     * @param identifier
     * @return
     */
    static Identifier getTargetId(eu.credential.config.schema.Identifier identifier) {
        Identifier id = new Identifier();
        if (identifier != null) {
            id.setNamespace((identifier.getNamespace() != null ? identifier.getNamespace().toString() : null));
            id.setValue((identifier.getValue() != null ? identifier.getValue().toString() : null));

            eu.credential.wallet.notification_dispatcher_service.model.Coding accountIdType = new eu.credential.wallet.notification_dispatcher_service.model.Coding();
            if (identifier.getType() != null) {
                accountIdType.setSystem((identifier.getType().getCodeSystem() != null
                        ? identifier.getType().getCodeSystem().toString() : null));
                accountIdType.setCode(
                        (identifier.getType().getCode() != null ? identifier.getType().getCode().toString() : null));
                accountIdType.setDisplay((identifier.getType().getDisplay() != null
                        ? identifier.getType().getDisplay().toString() : null));
                accountIdType.setVersion((identifier.getType().getVersion() != null
                        ? identifier.getType().getVersion().toString() : null));
                id.setType(accountIdType);

            } else {
                id.setType(null);
            }
            return id;
        }
        return null;
    }

    /**
     * Returns the APP ID for the given user and Preference ID. If no APP ID can
     * be found null is returned.
     *
     * @param preferencesResponse
     * @return
     */
    static String getAnAppId(GetPreferencesResponse preferencesResponse, String preferenceIdValue) {
        if (preferencesResponse == null || preferencesResponse.getPreferenceList() == null) {
            return null;
        }
        String appId = null;
        Preference preference = getPreferenceById(preferencesResponse, preferenceIdValue);
        for (int i = 0; i < preference.getPreferenceDetails().size(); i++) {
            if ("appid".equals(preference.getPreferenceDetails().get(i).getKey().getCode())) {
                appId = preference.getPreferenceDetails().get(i).getValue();
            }
        }
        return appId;
    }

    /**
     * Returns the preference for the given Preference ID. If no preference can
     * be found null is returned.
     *
     * @param preferencesResponse
     * @return
     */
    private static Preference getPreferenceById(GetPreferencesResponse preferencesResponse, String preferenceIdValue) {
        if (preferencesResponse == null || preferencesResponse.getPreferenceList() == null) {
            return null;
        }
        Preference newPreference = null;
        for (Preference preference : preferencesResponse.getPreferenceList()) {
            if (preference.getPreferenceId().getValue().equals(preferenceIdValue)) {
                newPreference = preference;
            }
        }
        return newPreference;
    }

    /**
     * Returns a specific value for the given Notification data from Kafka. If
     * no preference can be found null is returned.
     *
     * @param content
     * @return
     */
    static String getSpecificValue(KeyValue content, String value) {
        String str;
        if (content != null && value.equals(content.getKey().getCode().toString())) {
            str = content.getValue().toString();
            return str;
        }
        return null;
    }

    /**
     * Returns a text message for the given data ID from preference. If no
     * message can be found null is returned.
     *
     * @param content
     * @return
     */
    static String getDataFromId(KeyValue content) {
        if (NOTIFICATION_DATA_ID_1.equals(content.getValue().toString())) {
            return "some data";
        }
        if (NOTIFICATION_DATA_ID_2.equals(content.getValue().toString())) {
            return "another data";
        }
        if (NOTIFICATION_DATA_ID_3.equals(content.getValue().toString())) {
            return "Bloodsugar data";
        }
        return null;
    }

    /**
     * Returns a KeyValue data for Notification details.
     *
     * @param message
     * @return
     */
    static eu.credential.wallet.notification_dispatcher_service.model.KeyValue setNotificationDetailsData(
            String message) {
        // set notificationDetails
        eu.credential.wallet.notification_dispatcher_service.model.KeyValue keyValue = new eu.credential.wallet.notification_dispatcher_service.model.KeyValue();
        eu.credential.wallet.notification_dispatcher_service.model.Coding detailsType = new eu.credential.wallet.notification_dispatcher_service.model.Coding();

        detailsType.setSystem("https://credential.eu/config/codesystems/notificationdetail");
        detailsType.setCode("message");
        detailsType.setDisplay("new message");
        detailsType.setVersion("1.0");
        keyValue.setKey(detailsType);
        keyValue.setValue(message);
        return keyValue;
    }

    private static String createMessage(String sentence) {
        // create a real message from incoming data
        return initiator + " " + sentence + " " + dataid + " to " + targetParticipant + ".";
    }
}
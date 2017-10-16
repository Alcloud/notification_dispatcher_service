package eu.credential.wallet.notification_dispatcher_service.api;

import eu.credential.wallet.notificationservice.model.GetPreferencesResponse;
import eu.credential.wallet.notificationservice.model.Preference;
import eu.credential.wallet.notificationservice.model.PreferenceList;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import eu.credential.config.schema.Coding;
import eu.credential.config.schema.KeyValue;
import eu.credential.wallet.notification_dispatcher_service.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestKafkaConsumerThreadMethods {

    private static Logger logger = LoggerFactory.getLogger(KafkaConsumerThread.class);

    @Test
    public void shouldReturnTheIdentifierOfTheTarget() {
        eu.credential.config.schema.Identifier identifier = new eu.credential.config.schema.Identifier();

        identifier.setValue((CharSequence) "1234");
        identifier.setNamespace((CharSequence) "eu.credential.wallet");
        Coding type = new Coding();
        type.setCodeSystem((CharSequence) "https://credential.eu/config/codesystems/identifiers");
        type.setCode((CharSequence) "notificationId");
        type.setVersion((CharSequence) "1.0");
        type.setDisplay((CharSequence) "1.0");
        identifier.setType(type);

        Identifier targetIdentifier = KafkaConsumerThread.getTargetId(identifier);

        Assert.assertThat(targetIdentifier, Matchers.notNullValue());
        Assert.assertThat(targetIdentifier.getNamespace(), Matchers.is("eu.credential.wallet"));
        Assert.assertThat(targetIdentifier.getValue(), Matchers.is("1234"));
        Assert.assertThat(targetIdentifier.getType().getSystem(),
                Matchers.is("https://credential.eu/config/codesystems/identifiers"));
        Assert.assertThat(targetIdentifier.getType().getCode(), Matchers.is("notificationId"));
        Assert.assertThat(targetIdentifier.getType().getVersion(), Matchers.is("1.0"));
    }

    @Test
    public void shouldReturnTheSpecificValueOfTheNotification() {

        KeyValue identifier = new KeyValue();
        Coding type = new Coding();
        type.setCode("initiator");
        identifier.setKey(type);
        identifier.setValue("123");
        String initiator;

        initiator = KafkaConsumerThread.getSpecificValue(identifier, "initiator");

        Assert.assertThat(initiator, Matchers.is("123"));
    }

    @Test
    public void shouldReturnTheSpecificMessage() {

        KeyValue identifier = new KeyValue();
        Coding type = new Coding();
        type.setCode("dataid");
        type.setCodeSystem("https://credential.eu/config/codesystems/eventkey");
        identifier.setKey(type);
        identifier.setValue("abcd");
        String message;

        message = KafkaConsumerThread.getDataFromId(identifier);

        Assert.assertThat(message, Matchers.is("some data"));
    }

    @Test
    public void shouldReturnTheSpecificAppId() {
        GetPreferencesResponse getPreferencesResponse = new GetPreferencesResponse();
        PreferenceList preferenceList = new PreferenceList();
        Preference preference = new Preference();
        eu.credential.wallet.notificationservice.model.Identifier accountIdIdentifier = new eu.credential.wallet.notificationservice.model.Identifier();

        accountIdIdentifier.setNamespace("https://credential.eu/config/codesystems/identifiers");
        accountIdIdentifier.setValue("HansAugust");
        eu.credential.wallet.notificationservice.model.Coding accountType =
                new eu.credential.wallet.notificationservice.model.Coding();
        accountType.setCode("accountId");
        accountType.setDisplay("Account ID of a CREDENTIAL user.");
        accountType.setSystem("https://credential.eu/config/codesystems/identifiers");
        accountType.setVersion("1.0");
        accountIdIdentifier.setType(accountType);
        preference.setAccountId(accountIdIdentifier);

        eu.credential.wallet.notificationservice.model.Identifier preferenceIdIdentifier = new eu.credential.wallet.notificationservice.model.Identifier();

        preferenceIdIdentifier.setNamespace("https://credential.eu/config/codesystems/identifiers");
        preferenceIdIdentifier.setValue("abcd");

        eu.credential.wallet.notificationservice.model.Coding preferenceIdType = new eu.credential.wallet.notificationservice.model.Coding();
        preferenceIdType.setCode("preferenceId");
        preferenceIdType.setDisplay("ID of a notification.");
        preferenceIdType.setSystem("https://credential.eu/config/codesystems/identifiers");
        preferenceIdType.setVersion("1.0");
        preferenceIdIdentifier.setType(preferenceIdType);
        preference.setPreferenceId(preferenceIdIdentifier);
        preference.setPreferenceCreationTime(new Date());

        eu.credential.wallet.notificationservice.model.Coding preferenceType = new eu.credential.wallet.notificationservice.model.Coding();
        preferenceType.setCode("newdata");
        preferenceType.setDisplay("");
        preferenceType.setSystem("https://credential.eu/config/codesystems/preferencetypes");
        preferenceType.setVersion("1.0");
        preference.setPreferenceType(preferenceType);

        eu.credential.wallet.notificationservice.model.Coding keyAppId = new eu.credential.wallet.notificationservice.model.Coding();
        keyAppId.setCode("appid");
        keyAppId.setDisplay("");
        keyAppId.setSystem("https://credential.eu/config/codesystems/preferenceitemkey");
        keyAppId.setVersion("1.0");
        eu.credential.wallet.notificationservice.model.KeyValue appId = new eu.credential.wallet.notificationservice.model.KeyValue();
        appId.setKey(keyAppId);
        appId.setValue("1234");

        List<eu.credential.wallet.notificationservice.model.KeyValue> preferenceDetails = new ArrayList<eu.credential.wallet.notificationservice.model.KeyValue>();
        preferenceDetails.add(appId);

        preference.setPreferenceDetails(preferenceDetails);
        preferenceList.add(preference);
        getPreferencesResponse.setPreferenceList(preferenceList);

        String appID;
        appID = KafkaConsumerThread.getAnAppId(getPreferencesResponse, "abcd");

        Assert.assertThat(appID, Matchers.is("1234"));
    }

    @Test
    public void shouldReturnTheSpecificNotificationDetailsForNewData() {
        List<KeyValue> notificationDetailsKafka = new ArrayList<KeyValue>();

        notificationDetailsKafka.add(keyValueCreator("abcd", "dataid"));
        notificationDetailsKafka.add(keyValueCreator("ErnaMueller", "initiator"));
        notificationDetailsKafka.add(keyValueCreator("newdata", "notificationType"));
        List<eu.credential.wallet.notification_dispatcher_service.model.KeyValue> notificationDetails = new ArrayList<eu.credential.wallet.notification_dispatcher_service.model.KeyValue>();

        String initiator = "";
        String dataid = "";
        String message;

        for (KeyValue content : notificationDetailsKafka) {
            if ("initiator".equals(content.getKey().getCode().toString())) {
                initiator = KafkaConsumerThread.getSpecificValue(content, "initiator");
            }
            if ("dataid".equals(content.getKey().getCode().toString())) {
                dataid = KafkaConsumerThread.getDataFromId(content);
            }
            if ("newdata".equals(content.getValue().toString())) {

                message = initiator
                        + " " + "sent" + " " + dataid + " to " + "HansAugust.";
                notificationDetails.add(KafkaConsumerThread.setNotificationDetailsData
                        (message));
            }
        }
        logger.info(notificationDetails.get(0).toString());
        Assert.assertThat(notificationDetails.get(0).getValue(), Matchers.is("ErnaMueller sent some data to HansAugust."));
    }

    public static KeyValue keyValueCreator(String value, String code) {
        KeyValue content = new KeyValue();
        content.setValue(value);
        Coding coding = new Coding();
        coding.setCode(code);
        coding.setCodeSystem("https://credential.eu/config/codesystems/notificationdetails");
        content.setKey(coding);
        return content;
    }
}

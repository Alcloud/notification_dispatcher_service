package eu.credential.wallet.notification_dispatcher_service.api.impl.mongo;

import eu.credential.wallet.notification_dispatcher_service.api.PersonalizationProperties;

public class NotificationMongoClientFactory {
	
	
	/**
	 * Returns the notification mongo client configured to access the notification db
	 * @return
	 */
	public static NotificationMongoClient getNotificationMongoClient() {
		String bindIp = PersonalizationProperties.getInstance().getValue("notificationdb.host");
		String port = PersonalizationProperties.getInstance().getValue("notificationdb.port");
		return new NotificationMongoClient(bindIp, port);
	}
	/**
	 * Returns the notification mongo client configured to access the notification preference db
	 * @return
	 */
	public static NotificationMongoClient getNotificationPreferenceMongoClient() {
		String bindIp = PersonalizationProperties.getInstance().getValue("notificationpreferencedb.host");
		String port = PersonalizationProperties.getInstance().getValue("notificationpreferencedb.port");
		return new NotificationMongoClient(bindIp, port);
	}
}

package eu.credential.wallet.notification_dispatcher_service.api.impl.mongo;

import eu.credential.wallet.notification_dispatcher_service.model.Coding;
import eu.credential.wallet.notification_dispatcher_service.model.Identifier;
import eu.credential.wallet.notification_dispatcher_service.model.KeyValue;
import eu.credential.wallet.notification_dispatcher_service.model.Notification;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.Date;
import java.util.List;

public class NotificationCodec implements Codec<Notification> {

	protected final CodecRegistry codecRegistry;

	public NotificationCodec(final CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	public void encode(BsonWriter writer, Notification value, EncoderContext encoderContext) {

		writer.writeStartDocument();

		// Write account id if available
		if (value.getAccountId() != null) {
			Identifier identifier = value.getAccountId();

			Codec<Identifier> identifierCodec = codecRegistry.get(Identifier.class);
			writer.writeName("accountId");
			encoderContext.encodeWithChildContext(identifierCodec, writer, identifier);

		}

		if (value.getNotificationId() != null) {
			Identifier identifier = value.getNotificationId();
			Codec<Identifier> identifierCodec = codecRegistry.get(Identifier.class);
			writer.writeName("notificationId");
			encoderContext.encodeWithChildContext(identifierCodec, writer, identifier);

		}

		if (value.getNotificationCreationTime() != null) {
			writer.writeDateTime("notificationCreationTime", value.getNotificationCreationTime().getTime());
		}

		if (value.getNotificationDetails() != null) {
			List<KeyValue> details = value.getNotificationDetails();

			Codec<List> listCodec = codecRegistry.get(List.class);
			writer.writeName("notificationDetails");
			encoderContext.encodeWithChildContext(listCodec, writer, details);

		}


		writer.writeEndDocument();

	}

	@Override
	public Class<Notification> getEncoderClass() {
		return Notification.class;
	}

	@Override
	public Notification decode(BsonReader reader, DecoderContext decoderContext) {
		reader.readStartDocument();
		Codec<Identifier> identifierCodec = codecRegistry.get(Identifier.class);
		Codec<List> listCodec = codecRegistry.get(List.class);

		Notification notification = new Notification();
		while(reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			String fieldName = reader.readName();
			if("_id".equals(fieldName)){
				reader.readObjectId();
			}
			if("accountId".equals(fieldName)){
				Identifier accountId = identifierCodec.decode(reader, decoderContext);
				notification.setAccountId(accountId);
			}
			if("notificationId".equals(fieldName)){
				Identifier notificationId = identifierCodec.decode(reader, decoderContext);
				notification.setNotificationId(notificationId);
			}
			if("notificationCreationTime".equals(fieldName)){
				Long time = reader.readDateTime();
				notification.setNotificationCreationTime(new Date(time));
			}
			if("notificationDetails".equals(fieldName)){
				List notificationDetails = listCodec.decode(reader, decoderContext);
				notification.setNotificationDetails(notificationDetails);
			}
		}

		reader.readEndDocument();

		return notification;
	}


}

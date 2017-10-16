package eu.credential.wallet.notification_dispatcher_service.api.impl.mongo;

import eu.credential.wallet.notification_dispatcher_service.model.Coding;
import eu.credential.wallet.notification_dispatcher_service.model.Identifier;
import eu.credential.wallet.notification_dispatcher_service.model.Notification;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class NotificationCodecProvider implements CodecProvider {

	@Override
	public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
		if(clazz == Notification.class) {
			return (Codec<T>) new NotificationCodec(registry);
		}
		if(clazz == Identifier.class) {
			return (Codec<T>) new IdentifierCodec(registry);
		}
		if(clazz == Coding.class) {
			return (Codec<T>) new CodingCodec();
		}
		return null;
	}

}

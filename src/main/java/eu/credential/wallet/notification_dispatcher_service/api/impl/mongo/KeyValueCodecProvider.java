package eu.credential.wallet.notification_dispatcher_service.api.impl.mongo;

import eu.credential.wallet.notification_dispatcher_service.model.Coding;
import eu.credential.wallet.notification_dispatcher_service.model.KeyValue;

import java.util.List;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class KeyValueCodecProvider implements CodecProvider {

    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz == KeyValue.class) {
            return (Codec<T>) new KeyValueCodec(registry);
        }
        if (clazz == Coding.class) {
            return (Codec<T>) new CodingCodec();
        }
        return null;
    }
}

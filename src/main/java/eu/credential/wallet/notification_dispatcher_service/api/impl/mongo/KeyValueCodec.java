package eu.credential.wallet.notification_dispatcher_service.api.impl.mongo;

import eu.credential.wallet.notification_dispatcher_service.model.Coding;
import eu.credential.wallet.notification_dispatcher_service.model.KeyValue;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

public class KeyValueCodec implements Codec<KeyValue> {
    protected final CodecRegistry codecRegistry;

    public KeyValueCodec(final CodecRegistry codecRegistry) {
        this.codecRegistry = codecRegistry;
    }

    @Override
    public void encode(BsonWriter writer, KeyValue keyValue, EncoderContext encoderContext) {

        Coding key = keyValue.getKey();
        String value = keyValue.getValue();

        writer.writeStartDocument();
        if (key != null) {
            Codec<Coding> codingCodec = codecRegistry.get(Coding.class);
            writer.writeName("key");
            encoderContext.encodeWithChildContext(codingCodec, writer, key);
        }
        if (value != null) {
            writer.writeName("value");
            writer.writeString(new String(value));
        }
        writer.writeEndDocument();
    }

    @Override
    public Class<KeyValue> getEncoderClass() {
        return KeyValue.class;
    }

    @Override
    public KeyValue decode(BsonReader reader, DecoderContext decoderContext) {
        KeyValue keyValue = new KeyValue();
        reader.readStartDocument();

        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String fieldName = reader.readName();
            if ("_id".equals(fieldName)) {
                reader.readObjectId();
            } else if ("key".equals(fieldName)) {
                Codec<Coding> codingCodec = codecRegistry.get(Coding.class);
                Coding coding = codingCodec.decode(reader, decoderContext);
                keyValue.setKey(coding);
            } else if ("value".equals(fieldName)) {
                String value = reader.readString();
                keyValue.setValue(value);
            } else {
                reader.readUndefined();
            }
        }

        reader.readEndDocument();

        return keyValue;

    }

}

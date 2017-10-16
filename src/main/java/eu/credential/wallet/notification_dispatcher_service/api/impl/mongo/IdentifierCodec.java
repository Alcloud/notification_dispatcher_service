package eu.credential.wallet.notification_dispatcher_service.api.impl.mongo;

import eu.credential.wallet.notification_dispatcher_service.model.Coding;
import eu.credential.wallet.notification_dispatcher_service.model.Identifier;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

public class IdentifierCodec implements Codec<Identifier> {

	protected final CodecRegistry codecRegistry;

	public IdentifierCodec(final CodecRegistry codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	public void encode(BsonWriter writer, Identifier value, EncoderContext encoderContext) {

		String namespace = value.getNamespace();
		Coding coding = value.getType();
		String identifierValue = value.getValue();

		writer.writeStartDocument();
		writer.writeName("namespace");
		writer.writeString(namespace);
		if (coding != null) {
			Codec<Coding> codingCodec = codecRegistry.get(Coding.class);
			writer.writeName("type");
			encoderContext.encodeWithChildContext(codingCodec, writer, coding);
		}
		writer.writeName("value");
		writer.writeString(identifierValue);
		writer.writeEndDocument();

	}

	@Override
	public Class<Identifier> getEncoderClass() {
		return Identifier.class;
	}

	@Override
	public Identifier decode(BsonReader reader, DecoderContext decoderContext) {
		Identifier identifier = new Identifier();
		reader.readStartDocument();

		String fieldName = reader.readName();
		if (fieldName.equals("_id")) {
			reader.readObjectId();
		}
		identifier.setNamespace(reader.readString());
		
		reader.readName("type");
		Codec<Coding> codingCodec = codecRegistry.get(Coding.class);
		Coding coding = codingCodec.decode(reader, decoderContext);
		identifier.setType(coding);
		
		identifier.setValue(reader.readString("value"));

		reader.readEndDocument();

		return identifier;
	}

}

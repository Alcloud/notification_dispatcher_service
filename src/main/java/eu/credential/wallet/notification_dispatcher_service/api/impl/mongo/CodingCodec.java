package eu.credential.wallet.notification_dispatcher_service.api.impl.mongo;

import eu.credential.wallet.notification_dispatcher_service.model.Coding;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class CodingCodec implements Codec<Coding> {

	protected Codec<Document> documentCodec;

	public CodingCodec() {
		documentCodec = new DocumentCodec();
	}

	@Override
	public void encode(BsonWriter writer, Coding value, EncoderContext encoderContext) {

		Document doc = new Document();

		doc.put("system", value.getSystem());
		doc.put("version", value.getVersion());
		doc.put("code", value.getCode());
		doc.put("display", value.getDisplay());

		documentCodec.encode(writer, doc, encoderContext);
	}

	@Override
	public Class<Coding> getEncoderClass() {
		return Coding.class;
	}

	@Override
	public Coding decode(BsonReader reader, DecoderContext decoderContext) {
		Document document = documentCodec.decode(reader, decoderContext);

		Coding coding = new Coding();
		coding.setSystem(document.getString("system"));
		coding.setVersion(document.getString("version"));
		coding.setCode(document.getString("code"));
		coding.setDisplay(document.getString("display"));

		return coding;
	}
}

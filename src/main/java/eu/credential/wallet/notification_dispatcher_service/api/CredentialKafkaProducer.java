package eu.credential.wallet.notification_dispatcher_service.api;

import java.io.ByteArrayOutputStream;

import eu.credential.config.schema.FCMNotification;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.credential.config.schema.Notification;

public class CredentialKafkaProducer {

	private static Logger logger = LoggerFactory.getLogger(CredentialKafkaProducer.class);

	private static CredentialKafkaProducer instance = null;
	private KafkaProducer<String, byte[]> producer;
	private String topic = "";

	public static CredentialKafkaProducer getInstance() {
		try {
			instance = new CredentialKafkaProducer();
		} catch (Exception e) {
			logger.error("Error on loading configuration: " + e.getLocalizedMessage());
		}
		return instance;
	}

	protected CredentialKafkaProducer() {

		try {
			producer = new KafkaProducer<String, byte[]>(
					PersonalizationProperties.getInstance().getPropertyGroup("kafka.producer"));
			topic = PersonalizationProperties.getInstance().getValue("sendtopic");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//
	// This is a placeholder method. Rewrite the encodeMessage and this
	// signature for your needs.
	//
	public void sendMessage(Notification notification) {

		try {
			logger.info("Send a notification towards kafka for further processing: {}", notification);
			producer.send(new ProducerRecord<String, byte[]>(topic, encodeMessage(notification)));
		} catch (Throwable throwable) {
			System.out.printf("%s", throwable.getStackTrace());
		} finally {
			producer.close();
		}

	}

	public void sendMessage(FCMNotification notification) {

		try {
			logger.info("Send a notification towards kafka for further processing: {}", notification);
			producer.send(new ProducerRecord<String, byte[]>(topic, encodeMessage(notification)));
		} catch (Throwable throwable) {
			System.out.printf("%s", throwable.getStackTrace());
		} finally {
			producer.close();
		}

	}

	// This is a placeholder method. Rewrite for your needs.
	private byte[] encodeMessage(Notification notification) {

		try {

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
			DatumWriter<Notification> writer = new SpecificDatumWriter<Notification>(Notification.getClassSchema());
			writer.write(notification, encoder);
			encoder.flush();
			out.close();
			byte[] serialized = out.toByteArray();
			return serialized;
		} catch (Exception e) {
			logger.warn("Could not write notification to kafka", e);
		}

		return null;
	}
	// This is a placeholder method. Rewrite for your needs.
	private byte[] encodeMessage(FCMNotification notification) {

		try {

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
			DatumWriter<FCMNotification> writer = new SpecificDatumWriter<FCMNotification>(FCMNotification.getClassSchema());
			writer.write(notification, encoder);
			encoder.flush();
			out.close();
			byte[] serialized = out.toByteArray();
			return serialized;
		} catch (Exception e) {
			logger.warn("Could not write notification to kafka", e);
		}

		return null;
	}

}

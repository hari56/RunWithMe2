package com.example.harald.runwithme2;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.harald.runwithme2.mymqttmessages.ProtobufMessages;
import com.google.protobuf.InvalidProtocolBufferException;


public class ProtobufMqttProgram {
	private final static Logger LOG = LoggerFactory.getLogger(ProtobufMqttProgram.class);

	private final int qos = 0;
	
	private boolean connected;
	private String sendTopic;
	private MqttAsyncClient sampleClient;
	private String broker;
	private String clientId;
	private Collection<String> topics;

	//gives access to members of the model
	private IProtobufBridge modelBridge;

	public ProtobufMqttProgram(IProtobufBridge modelBridge) {
		this(modelBridge, "tcp://iot.soft.uni-linz.ac.at:1883", MqttAsyncClient.generateClientId(),
				Collections.singleton("protobuf"), "RunWithMe2");
	}

	public ProtobufMqttProgram(IProtobufBridge modelBridge, String broker, String clientId, Collection<String> topics, String sendTopic) {
		if (topics != null && topics.size() == 1 && sendTopic == null) {
			sendTopic = topics.iterator().next();
		}
		if (clientId == null || sendTopic == null || broker == null || topics == null || broker.isEmpty()
				|| clientId.isEmpty() || sendTopic.isEmpty()) {
			throw new RuntimeException(String.format("invalid configuration broker: %s  clientId: %s  topics: %s ",
					broker, clientId, topics));
		}
		this.sendTopic = sendTopic;
		this.broker = broker;
		this.clientId = clientId;
		this.topics = topics;
		this.modelBridge = modelBridge;
		setup();
	}

	private void setup() {
		MemoryPersistence persistence = new MemoryPersistence();
		try {
			sampleClient = new MqttAsyncClient(this.broker, this.clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			LOG.info("Connecting to broker: " + broker);

			sampleClient.setCallback(new MqttCallback() {

				public void messageArrived(String topic, MqttMessage message) throws Exception {
					LOG.info("message arrived Topic: {}", topic);
					processProtobufMessage(message.getPayload());
				}

				public void deliveryComplete(IMqttDeliveryToken token) {
					LOG.trace("deliveryComplete with token {}", token);
				}

				public void connectionLost(Throwable cause) {
					LOG.error("connection lost", cause);
				}
			});

			sampleClient.connect(connOpts, null, new IMqttActionListener() {

				public void onSuccess(IMqttToken asyncActionToken) {
					LOG.info("connected to {}", ProtobufMqttProgram.this.broker);
					for (String topic : ProtobufMqttProgram.this.topics) {
						try {
							sampleClient.subscribe(topic, qos);
						} catch (MqttException e) {
							LOG.error("error subscribing to topic", e);
						}
					}
					connected = true;
				}

				public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
					LOG.error("failed to connect", exception);
					connected = false;
				}
			});
		} catch (MqttException me) {
			processEx(me);
		}
	}

	private void processProtobufMessage(byte[] data) {

		ProtobufMessages.PathItemMessage pbm;
		try {
			pbm = ProtobufMessages.PathItemMessage.parseFrom(data);
			
			switch (pbm.getMsgtypeCase()) {
			case GPS:
				ProtobufMessages.GPSMessage gpsMessage = pbm.getGps();
			    LOG.info("got gps latitude = {} longitude = {}", gpsMessage.getLatitude(), gpsMessage.getLongitude());
				break;
			case META:
				ProtobufMessages.MetaValues metaValues = pbm.getMeta();
				LOG.info("got meta values time {} distance {} speed {}", metaValues.getTime(), metaValues.getDistance(), metaValues.getSpeed(), metaValues.getSteps());
				break; 	
				
			default:
				LOG.error("invalid msgtype {}  found", pbm.getMsgtypeCase().name());
				break;
			}
		} catch (InvalidProtocolBufferException e) {
			LOG.error("message content is invalid - not a valid protobuf message?", e);
		}

		
	}

	private static void processEx(MqttException me) {
		LOG.error("exception raised (1): reason: {}; msg; {}; ", me.getReasonCode(), me.getMessage());
		LOG.error("exception raised (2): error", me);
	}


	public void sendPathItem(double latitude, double longitude, long time, double distance, double speed, int steps) throws MqttPersistenceException, MqttException {
		if (connected) {
			LOG.debug("Publishing pathItem: {}, {}, {}, {}, {}, {}", latitude, longitude, time, distance, speed, steps);
			MqttMessage message = this.genericPathItemMessage(latitude, longitude, time, distance, speed, steps);
			message.setQos(qos);
			sampleClient.publish(sendTopic, message);
		} else {
			LOG.error("connect first before sending");
		}
	}

	private MqttMessage genericPathItemMessage(double latitude, double longitude, long time, double distance, double speed, int steps) {
		// General Message
		ProtobufMessages.PathItemMessage.Builder builder = ProtobufMessages.PathItemMessage.newBuilder();
		builder.setSource(clientId.hashCode());

		// Concrete Message
		ProtobufMessages.GPSMessage.Builder gps = ProtobufMessages.GPSMessage.newBuilder();
		gps.setLatitude(latitude);
		gps.setLongitude(longitude);
		builder.setGps(gps);

		// Concrete Message
		ProtobufMessages.MetaValues.Builder meta = ProtobufMessages.MetaValues.newBuilder();

		meta.setTime(time);
		meta.setDistance(distance);
		meta.setSpeed(speed);
		meta.setSteps(steps);

		builder.setMeta(meta);

		MqttMessage message = new MqttMessage(builder.build().toByteArray());
		return message;
	}


	public void close() {
		if (connected) {
			try {
				sampleClient.disconnect();
			} catch (MqttException e) {
				processEx(e);
			}
		} else {
			LOG.error("not connected - cannot disconnect");
		}
	}

	public static void main(String[] args) {

		//ProtobufMqttProgram connector = new ProtobufMqttProgram();

			

	}



}

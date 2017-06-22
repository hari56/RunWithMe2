package at.jku.pervasive.eps.ss2017;

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
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextMqttProgram {
	private final static Logger LOG = LoggerFactory.getLogger(TextMqttProgram.class);

	private final int qos = 0;
	
	private boolean connected;
	private String sendTopic;
	private MqttAsyncClient sampleClient;
	private String broker;
	private String clientId;
	private Collection<String> topics;

	public TextMqttProgram() {
		this("tcp://iot.soft.uni-linz.ac.at:1883", MqttAsyncClient.generateClientId(),
				Collections.singleton("myiottopic"));
	}

	public TextMqttProgram(String broker, String clientId, Collection<String> topics) {
		if (topics != null && topics.size() == 1 && sendTopic == null) {
			sendTopic = topics.iterator().next();
		}
		if (clientId == null || sendTopic == null || broker == null || topics == null || broker.isEmpty()
				|| clientId.isEmpty() || sendTopic.isEmpty()) {
			throw new RuntimeException(String.format("invalid configuration broker: %s  clientId: %s  topics: %s ",
					broker, clientId, topics));
		}
		this.broker = broker;
		this.clientId = clientId;
		this.topics = topics;
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
					LOG.info("message arrived Topic: {} with Content: {}", topic, message);
					System.out.println("Length: " + message.getPayload().length);
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
					LOG.info("connected to {}", TextMqttProgram.this.broker);
					for (String topic : TextMqttProgram.this.topics) {
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

	private static void processEx(MqttException me) {
		LOG.error("exception raised (1): reason: {}; msg; {}; ", me.getReasonCode(), me.getMessage());
		LOG.error("exception raised (2): error", me);
	}

	public void sendMessage(String content) throws MqttException {
		if (connected) {
			LOG.debug("Publishing message: {}", content);
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(qos);
			sampleClient.publish(sendTopic, message);
		} else {
			LOG.error("connect first before sending");
		}
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
		TextMqttProgram connector = new TextMqttProgram();
		

		try 
		{	
			Thread.sleep(5000);
			LOG.info("Send messages via string value of");
			LOG.info("send: longMin");
			connector.sendMessage(String.valueOf(Long.MIN_VALUE));
			
			Thread.sleep(1000);
			LOG.info("send: intMin");
			connector.sendMessage(String.valueOf(Integer.MIN_VALUE));
			
			Thread.sleep(1000);
			LOG.info("send: 0");
			connector.sendMessage(String.valueOf(0));
			
			Thread.sleep(1000);
			LOG.info("send: intMax");
			connector.sendMessage(String.valueOf(Integer.MAX_VALUE));
			
			Thread.sleep(1000);
			LOG.info("send: longMax");
			connector.sendMessage(String.valueOf(Long.MAX_VALUE));
			
			
		} catch (MqttException e) {
			processEx(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

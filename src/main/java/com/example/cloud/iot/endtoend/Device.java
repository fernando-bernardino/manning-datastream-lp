package com.example.cloud.iot.endtoend;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.text.*;
import java.util.Date;
import java.util.Random;

/** Represents the state of a single device. */
public class Device implements MqttCallback {
  private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

  private boolean isConnected = false;
  private Date timestamp;
  private int tsunamiEventValidity;
  private int tsunamiCauseCode;
  private float earthquakeMagnitude;
  private float latitude;
  private float longitude;
  private float maximumWaterHeight;
  private Random random = new Random();

  public Device() {
  }

  public void updateSensorData() {
    timestamp = new Date();
    tsunamiEventValidity = random.nextInt(5);
    tsunamiCauseCode = random.nextInt(12);
    earthquakeMagnitude = random.nextFloat() * 9;
    latitude = random.nextFloat() * 180;
    longitude = random.nextFloat() * 90;
    maximumWaterHeight = random.nextFloat() * 3;
  }

  public String getState() {
    JSONObject payload = new JSONObject();
    payload.put("timestamp", dateFormat.format(timestamp));
    payload.put("tsunami_event_validity", tsunamiEventValidity);
    payload.put("tsunami_cause_code", tsunamiCauseCode);
    payload.put("earthquake_magnitude", earthquakeMagnitude);
    payload.put("latitude", latitude);
    payload.put("longitude", longitude);
    payload.put("maximum_water_height", maximumWaterHeight);
    return payload.toString();
  }

  /** Wait for the device to become connected. */
  public void waitForConnection(int timeOut) throws InterruptedException {
    // Wait for the device to become connected.
    int totalTime = 0;
    while (!this.isConnected && totalTime < timeOut) {
      Thread.sleep(1000);
      totalTime += 1;
    }

    if (!this.isConnected) {
      throw new RuntimeException("Could not connect to MQTT bridge.");
    }
  }

  /** Callback when the device receives a PUBACK from the MQTT bridge. */
  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    System.out.println("Published message acked.");
  }

  /** Callback when the device receives a message on a subscription. */
  @Override
  public void messageArrived(String topic, MqttMessage message) {
    String payload = new String(message.getPayload());
    System.out.println(
      String.format(
        "Received message %s on topic %s with Qos %d", payload, topic, message.getQos()));
  }

  /** Callback for when a device disconnects. */
  @Override
  public void connectionLost(Throwable cause) {
    System.out.println("Disconnected: " + cause.getMessage());
    this.isConnected = false;
  }

  public void setConnected(boolean connected) {
    this.isConnected = connected;
  }
}
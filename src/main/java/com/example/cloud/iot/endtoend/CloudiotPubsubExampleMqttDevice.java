/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.cloud.iot.endtoend;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Sample device that consumes configuration from Google Cloud IoT. This example represents a simple
 * device with a temperature sensor and a fan (simulated with software). When the device's fan is
 * turned on, its temperature decreases by one degree per second, and when the device's fan is
 * turned off, its temperature increases by one degree per second.
 *
 * Every second, the device publishes its temperature reading to Google Cloud IoT Core. The
 * server meanwhile receives these temperature readings, and decides whether to re-configure the
 * device to turn its fan on or off. The server will instruct the device to turn the fan on when the
 * device's temperature exceeds 10 degrees, and to turn it off when the device's temperature is less
 * than 0 degrees. In a real system, one could use the cloud to compute the optimal thresholds for
 * turning on and off the fan, but for illustrative purposes we use a simple threshold model.
 *
 * To connect the device you must have downloaded Google's CA root certificates, and a copy of
 * your private key file. See cloud.google.com/iot for instructions on how to do this. Run this
 * script with the corresponding algorithm flag.
 *
 * <prev> <code>
 * $ mvn clean compile assembly:single
 *
 * $ mvn exec:java \
 *       -Dexec.mainClass="com.example.cloud.iot.endtoend.CloudiotPubsubExampleMqttDevice" \
 *       -Dexec.args="-project_id=your-iot-project \
 *                 -registry_id=your-registry-id \
 *                 -device_id=device-id \
 *                 -private_key_file=path-to-keyfile \
 *                 -algorithm=RS256|ES256"
 * </code> </prev>
 *
 * With a single server, you can run multiple instances of the device with different device ids,
 * and the server will distinguish them. Try creating a few devices and running them all at the same
 * time.
 */
public class CloudiotPubsubExampleMqttDevice {

  /** Create a RSA-based JWT for the given project id, signed with the given private key. */
  private static String createJwtRsa(String projectId, String privateKeyFile) throws Exception {
    DateTime now = new DateTime();
    // Create a JWT to authenticate this device. The device will be disconnected after the token
    // expires, and will have to reconnect with a new token. The audience field should always be set
    // to the GCP project id.
    JwtBuilder jwtBuilder =
        Jwts.builder()
            .setIssuedAt(now.toDate())
            .setExpiration(now.plusMinutes(20).toDate())
            .setAudience(projectId);

    byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyFile));
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("RSA");

    return jwtBuilder.signWith(SignatureAlgorithm.RS256, kf.generatePrivate(spec)).compact();
  }

  /** Create an ES-based JWT for the given project id, signed with the given private key. */
  private static String createJwtEs(String projectId, String privateKeyFile) throws Exception {
    DateTime now = new DateTime();
    // Create a JWT to authenticate this device. The device will be disconnected after the token
    // expires, and will have to reconnect with a new token. The audience field should always be set
    // to the GCP project id.
    JwtBuilder jwtBuilder =
        Jwts.builder()
            .setIssuedAt(now.toDate())
            .setExpiration(now.plusMinutes(20).toDate())
            .setAudience(projectId);

    byte[] keyBytes = Files.readAllBytes(Paths.get(privateKeyFile));
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory kf = KeyFactory.getInstance("EC");

    return jwtBuilder.signWith(SignatureAlgorithm.ES256, kf.generatePrivate(spec)).compact();
  }

  /** Entry point for CLI. */
  public static void main(String[] args) throws Exception {
    CloudiotPubsubExampleMqttDeviceOptions options =
        CloudiotPubsubExampleMqttDeviceOptions.fromFlags(args);
    if (options == null) {
      System.exit(1);
    }
    final Device device = new Device();
    final String mqttTelemetryTopic = String.format("/devices/%s/events", options.deviceId);
    // This is the topic that the device will receive configuration updates on.
    final String mqttConfigTopic = String.format("/devices/%s/config", options.deviceId);
    final String mqttServerAddress = String.format("ssl://%s:%s", options.mqttBridgeHostname, options.mqttBridgePort);
    final String mqttClientId = String.format("projects/%s/locations/%s/registries/%s/devices/%s",
            options.projectId, options.cloudRegion, options.registryId, options.deviceId);

    MqttConnectOptions connectOptions = new MqttConnectOptions();
    connectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
    Properties sslProps = new Properties();
    sslProps.setProperty("com.ibm.ssl.protocol", "TLSv1.2");
    connectOptions.setSSLProperties(sslProps);
    connectOptions.setUserName("unused");
    if (options.algorithm.equals("RS256")) {
      System.out.println(options.privateKeyFile);

      connectOptions.setPassword(
          createJwtRsa(options.projectId, options.privateKeyFile).toCharArray());
      System.out.println(
          String.format(
              "Creating JWT using RS256 from private key file %s", options.privateKeyFile));
    } else if (options.algorithm.equals("ES256")) {
      connectOptions.setPassword(
          createJwtEs(options.projectId, options.privateKeyFile).toCharArray());
    } else {
      throw new IllegalArgumentException(
          "Invalid algorithm " + options.algorithm + ". Should be one of 'RS256' or 'ES256'.");
    }

    device.setConnected(true);

    MqttClient client = new MqttClient(mqttServerAddress, mqttClientId, new MemoryPersistence());

    try {
      client.setCallback(device);
      client.connect(connectOptions);
    } catch (MqttException e) {
      e.printStackTrace();
    }

    // wait for it to connect
    device.waitForConnection(5);

    client.subscribe(mqttConfigTopic, 1);

    for (int i = 0; i < options.numMessages; i++) {
      device.updateSensorData();

      String payload = device.getState();
      System.out.println("Publishing payload " + payload);
      MqttMessage message = new MqttMessage(payload.getBytes());
      message.setQos(1);
      client.publish(mqttTelemetryTopic, message);
      Thread.sleep(1000);
    }
    client.disconnect();

    System.out.println("Finished looping successfully : " + options.mqttBridgeHostname);
  }
}

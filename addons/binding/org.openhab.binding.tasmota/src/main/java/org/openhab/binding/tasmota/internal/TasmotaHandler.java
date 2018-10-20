/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tasmota.internal;

import static org.openhab.binding.tasmota.internal.TasmotaBindingConstants.CHANNEL_1;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TasmotaHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author wallaroo - Initial contribution
 */
@NonNullByDefault
public class TasmotaHandler extends BaseThingHandler {
    private static int cnt = 0;
    private final Logger logger = LoggerFactory.getLogger(TasmotaHandler.class);
    @Nullable
    protected MqttClient sampleClient;
    @Nullable
    private TasmotaConfiguration config;

    public TasmotaHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_1:
                if (command instanceof OnOffType) {
                    // binding specific logic goes here
                    OnOffType deviceSwitchState = (OnOffType) command;
                    updateDeviceState(deviceSwitchState);
                }
                if (command instanceof RefreshType) {
                    readState();
                }
                break;
            // ...
        }

        updateStatus(ThingStatus.ONLINE);
    }

    private void readState() {
        MqttMessage message = new MqttMessage();
        String pwrLine = config.powerLine == null ? "" : config.powerLine.toString();
        try {
            sampleClient.publish("cmnd/" + config.mqttTopic + "/POWER" + pwrLine, message);
        } catch (MqttException e) {
            logger.error(e.getMessage(), e);
        }
    }

    void updateDeviceState(OnOffType onOff) {
        MqttMessage message = new MqttMessage(onOff.name().getBytes());
        try {
            String pwrLine = config.powerLine == null ? "" : config.powerLine.toString();
            sampleClient.publish("cmnd/" + config.mqttTopic + "/POWER" + pwrLine, message);
        } catch (MqttException e) {
            updateStatus(ThingStatus.OFFLINE);
            logger.error("error:", e);
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(TasmotaConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        try {
            sampleClient = new MqttClient("tcp://" + config.mqttBroker, "tasmota" + cnt++);
            String pwrLine = config.powerLine == null ? "" : config.powerLine.toString();
            sampleClient.connect();
            sampleClient.subscribe("stat/" + config.mqttTopic + "/POWER" + pwrLine);
            sampleClient.setCallback(new MqttCallback() {

                @Override
                public void messageArrived(@Nullable String topic, @Nullable MqttMessage msg) throws Exception {
                    logger.info(topic + "" + msg.toString());
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_1), OnOffType.valueOf(msg.toString()));
                }

                @Override
                public void deliveryComplete(@Nullable IMqttDeliveryToken arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void connectionLost(@Nullable Throwable arg0) {
                    updateStatus(ThingStatus.OFFLINE);

                }
            });

            logger.info("mqtt connection");
            updateStatus(ThingStatus.ONLINE);
        } catch (MqttException e) {
            updateStatus(ThingStatus.OFFLINE);
            logger.error("error:", e);
        } // TODO: da parametrizzare clientId

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}

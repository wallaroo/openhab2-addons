/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tasmota.internal;

/**
 * The {@link TasmotaConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author wallaroo - Initial contribution
 */
public class TasmotaConfiguration {

    /**
     * Sample configuration parameter. Replace with your own.
     */
    public String mqttBroker;
    public String mqttTopic;
    public String sonoffModel;
    public Integer powerLine;
}

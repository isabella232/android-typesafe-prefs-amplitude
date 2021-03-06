/*
 * Copyright (C) 2018 Peel Technologies Inc.
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
package com.peel.prefs.amplitude;

import java.lang.reflect.Type;

import org.json.JSONObject;

import com.amplitude.api.Amplitude;
import com.amplitude.api.AmplitudeClient;
import com.google.gson.Gson;
import com.peel.prefs.Prefs;
import com.peel.prefs.TypedKey;

/**
 * Bind this listener to {@code Prefs#addListener(Prefs.EventListener)} to automatically sync all
 * properties of type {@code PrefsKeyAmplitudeSynced} as Amplitude user properties
 *
 * @author Inderjeet Singh
 */
public class PrefsAmplitudeSyncListener implements Prefs.EventListener {
    private final AmplitudeClient amplitudeClient;
    private final String amplitudeTag;
    private Gson gson;

    public PrefsAmplitudeSyncListener(Gson gson) {
        this(Amplitude.getInstance(), gson, "amplitude");
    }

    public PrefsAmplitudeSyncListener(AmplitudeClient client, Gson gson, String amplitudeTag) {
        this.amplitudeClient = client;
        this.gson = gson;
        this.amplitudeTag = amplitudeTag;
    }

    @Override
    public <T> void onPut(TypedKey<T> key, T value) {
        if (key.containsTag(amplitudeTag)) {
            try {
                JSONObject props = new JSONObject();
                String keyName = key.getName();
                if (isPrimitive(key.getTypeOfValue()) || value instanceof String) {
                    props.put(keyName, value);
                } else {
                    props.put(keyName, gson.toJson(value));
                }
                amplitudeClient.setUserProperties(props);
            } catch (Exception ignored) {
            }
        }
    }

    private <T> boolean isPrimitive(Type type) {
        return Primitives.isPrimitive(type) || Primitives.isWrapperType(type);
    }

    @Override
    public <T> void onRemove(TypedKey<T> key) {
        try {
            if (key.containsTag(amplitudeTag) && key.getTypeOfValue() == Boolean.class) {
                // Only for boolean keys, set them to false in Amplitude
                JSONObject props = new JSONObject();
                props.put(key.getName(), false);
                amplitudeClient.setUserProperties(props);
            }
        } catch (Exception ignored) {
        }
    }
}

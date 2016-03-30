/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageService {

    protected List<MessageListener> listenerList = new ArrayList<>();

    public MessageService() {
    }

    public void addListener(MessageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "MessageService", "addListener", "missingListener"));
        }

        synchronized (this) {
            this.listenerList.add(listener);
        }
    }

    public void removeListener(MessageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "MessageService", "removeListener", "missingListener"));
        }

        synchronized (this) {
            this.listenerList.remove(listener);
        }
    }

    public void postMessage(String name, Object sender, Map<Object, Object> userProperties) {
        if (name == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "MessageService", "postMessage", "missingName"));
        }

        synchronized (this) {
            for (MessageListener listener : this.listenerList) {
                listener.onMessage(name, sender, userProperties);
            }
        }
    }
}

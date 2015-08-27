package org.n52.client.sos.event.data.handler;

import org.n52.client.sos.event.data.FinishedAddFlagEvent;

import com.google.gwt.event.shared.EventHandler;

public interface FinishedAddFlagEventHandler extends EventHandler {

    void onFinishedAddFlag(FinishedAddFlagEvent evt);
}
package org.n52.client.sos.event.data.handler;

import org.n52.client.sos.event.data.UpdateTimeTextFieldEvent;

import com.google.gwt.event.shared.EventHandler;

public interface UpdateTimeTextFieldEventHandler extends EventHandler {

    void onUpdateTextFields(UpdateTimeTextFieldEvent evt);

}
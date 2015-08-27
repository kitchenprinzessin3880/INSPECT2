package org.n52.client.sos.event.data.handler;

import org.n52.client.sos.event.data.GetQualityCodeEvent;

import com.google.gwt.event.shared.EventHandler;

public interface GetQualityCodeEventHandler extends EventHandler {

    void onGetQualityCodeEvent(GetQualityCodeEvent evt);

}

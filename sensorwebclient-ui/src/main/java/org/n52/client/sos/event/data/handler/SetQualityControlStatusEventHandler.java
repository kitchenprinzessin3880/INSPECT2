package org.n52.client.sos.event.data.handler;



import org.n52.client.sos.event.data.SetQualityControlStatusEvent;

import com.google.gwt.event.shared.EventHandler;


public interface SetQualityControlStatusEventHandler extends EventHandler {
	void onSetQualityControlStatus(SetQualityControlStatusEvent evt);
}

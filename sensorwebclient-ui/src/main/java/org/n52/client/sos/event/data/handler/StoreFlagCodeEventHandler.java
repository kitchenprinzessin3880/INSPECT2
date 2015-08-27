package org.n52.client.sos.event.data.handler;


import org.n52.client.sos.event.data.StoreFlagCodeEvent;
import com.google.gwt.event.shared.EventHandler;

public interface StoreFlagCodeEventHandler extends EventHandler{

	void onStore(StoreFlagCodeEvent evt);
}

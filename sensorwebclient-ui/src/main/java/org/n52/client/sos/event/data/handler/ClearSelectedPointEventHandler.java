package org.n52.client.sos.event.data.handler;
import org.n52.client.sos.event.data.ClearSelectedPointEvent;

import com.google.gwt.event.shared.EventHandler;

public interface ClearSelectedPointEventHandler extends EventHandler {
	void onClearSelectedPoint(ClearSelectedPointEvent evt);
}

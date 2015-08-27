package org.n52.client.sos.event.data.handler;

import org.n52.client.sos.event.data.SetQualityTimeMapEvent;

import com.google.gwt.event.shared.EventHandler;

public interface SetQualityTimeMapEventHandler extends EventHandler {
	void onSetQualityTimeMap(SetQualityTimeMapEvent evt);
}

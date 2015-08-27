package org.n52.client.sos.event.data.handler;

import org.n52.client.sos.event.data.CustomRenderingEvent;

import com.google.gwt.event.shared.EventHandler;

public interface CustomRenderingEventHandler extends EventHandler {

	void onCustomRendering(CustomRenderingEvent customRenderingEvent) ;

}

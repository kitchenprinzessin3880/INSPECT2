package org.n52.client.sos.event.data.handler;

import org.n52.client.sos.event.data.SetQCTimeRangeEvent;

import com.google.gwt.event.shared.EventHandler;

public interface SetQCTimeRangeEventHandler extends EventHandler  {
	void onSetQCTimeRange(SetQCTimeRangeEvent evt);
}

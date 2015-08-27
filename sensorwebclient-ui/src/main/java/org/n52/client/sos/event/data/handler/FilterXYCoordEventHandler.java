package org.n52.client.sos.event.data.handler;


import org.n52.client.sos.event.data.FilterXYCoordEvent;

import com.google.gwt.event.shared.EventHandler;

public interface FilterXYCoordEventHandler extends EventHandler  {

	void onFilterXYCoord(FilterXYCoordEvent evt);
}

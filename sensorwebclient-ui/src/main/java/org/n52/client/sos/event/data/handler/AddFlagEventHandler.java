package org.n52.client.sos.event.data.handler;


import org.n52.client.sos.event.data.AddFlagEvent;

import com.google.gwt.event.shared.EventHandler;

public interface AddFlagEventHandler extends EventHandler {

    void onAddFlag(AddFlagEvent evt);
}

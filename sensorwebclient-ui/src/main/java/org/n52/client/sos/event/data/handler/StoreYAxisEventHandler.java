package org.n52.client.sos.event.data.handler;

import org.n52.client.sos.event.data.StoreYAxisEvent;

import com.google.gwt.event.shared.EventHandler;

public interface StoreYAxisEventHandler extends EventHandler {

    /**
     * On change.
     * 
     * @param evt
     *            the evt
     */
    void onStoreYAxis(StoreYAxisEvent evt);

}
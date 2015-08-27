package org.n52.client.sos.event.data.handler;

import org.n52.client.sos.event.data.DeleteAllTimeSeriesEvent;

import com.google.gwt.event.shared.EventHandler;
//ASD
public interface DeleteAllTimeSeriesEventHandler extends EventHandler {

    void onDeleteAllTimeSeries(DeleteAllTimeSeriesEvent evt);
}

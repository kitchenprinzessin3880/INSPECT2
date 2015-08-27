package org.n52.client.sos.event.handler;

import org.n52.client.sos.event.UpdateMaintenanceEvent;

import com.google.gwt.event.shared.EventHandler;

public interface UpdateMaintenanceEventHandler extends EventHandler {

    void onUpdateMaintenance(UpdateMaintenanceEvent evt);

}
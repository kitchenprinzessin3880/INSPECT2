//ASD
package org.n52.client.sos.event;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.handler.TimeSeriesChangedEventHandler;
import org.n52.client.sos.event.handler.UpdateMaintenanceEventHandler;
import org.n52.client.ui.legend.LegendElement;

public class UpdateMaintenanceEvent extends FilteredDispatchGwtEvent<UpdateMaintenanceEventHandler> {

    /** The TYPE. */
    public static Type<UpdateMaintenanceEventHandler> TYPE = new Type<UpdateMaintenanceEventHandler>();

	public UpdateMaintenanceEvent() {
    }
	
    @Override
    protected void onDispatch(UpdateMaintenanceEventHandler handler) {
        handler.onUpdateMaintenance(this);
    }

    @Override
    public Type<UpdateMaintenanceEventHandler> getAssociatedType() {
        return TYPE;
    }

}


package org.n52.client.sos.event.data;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.DeleteAllTimeSeriesEventHandler;

//ASD
public class DeleteAllTimeSeriesEvent extends
        FilteredDispatchGwtEvent<DeleteAllTimeSeriesEventHandler> {

    /** The TYPE. */
    public static com.google.gwt.event.shared.GwtEvent.Type<DeleteAllTimeSeriesEventHandler> TYPE =
            new Type<DeleteAllTimeSeriesEventHandler>();

    /** The ID. */
    //private String ID;

    /**
     * Instantiates a new delete time series event.
     * 
     * @param ID
     *            the iD
     * @param blockedHandlers
     *            the blocked handlers
     */
    public DeleteAllTimeSeriesEvent(DeleteAllTimeSeriesEventHandler... blockedHandlers) {
        super(blockedHandlers);
        //this.ID = ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent#onDispatch(com
     * .google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void onDispatch(DeleteAllTimeSeriesEventHandler handler) {
        handler.onDeleteAllTimeSeries(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<DeleteAllTimeSeriesEventHandler> getAssociatedType() {
        return TYPE;
    }


}

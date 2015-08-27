package org.n52.client.sos.event.data;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.ClearSelectedPointEventHandler;

public class ClearSelectedPointEvent extends FilteredDispatchGwtEvent<ClearSelectedPointEventHandler> {

    public static Type<ClearSelectedPointEventHandler> TYPE = new Type<ClearSelectedPointEventHandler>();
    
    @Override
    protected void onDispatch(ClearSelectedPointEventHandler handler) {
        handler.onClearSelectedPoint(this);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ClearSelectedPointEventHandler> getAssociatedType() {
        return TYPE;
    }

}
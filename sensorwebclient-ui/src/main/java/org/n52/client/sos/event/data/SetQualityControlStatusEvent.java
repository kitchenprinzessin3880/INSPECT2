package org.n52.client.sos.event.data;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.SetQualityControlStatusEventHandler;



public class SetQualityControlStatusEvent extends FilteredDispatchGwtEvent<SetQualityControlStatusEventHandler> {

    public static Type<SetQualityControlStatusEventHandler> TYPE = new Type<SetQualityControlStatusEventHandler>();

    private boolean status;
    public SetQualityControlStatusEvent(boolean b) {
        this.status = b;

    }
    
    @Override
    protected void onDispatch(SetQualityControlStatusEventHandler handler) {
        handler.onSetQualityControlStatus(this);
    }

    public boolean getQCStatus() {
        return this.status;
    }
    
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<SetQualityControlStatusEventHandler> getAssociatedType() {
        return TYPE;
    }

}
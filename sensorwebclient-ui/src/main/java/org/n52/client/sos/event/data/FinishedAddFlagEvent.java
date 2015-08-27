package org.n52.client.sos.event.data;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.FinishedAddFlagEventHandler;

public class FinishedAddFlagEvent extends FilteredDispatchGwtEvent<FinishedAddFlagEventHandler>{

    
    public static Type<FinishedAddFlagEventHandler> TYPE = new Type<FinishedAddFlagEventHandler>();
    /* (non-Javadoc)
     * @see org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent#onDispatch(com.google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void onDispatch(FinishedAddFlagEventHandler handler) {
        handler.onFinishedAddFlag(this);
    }

    /* (non-Javadoc)
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<FinishedAddFlagEventHandler> getAssociatedType() {
        return TYPE;
    }

}


package org.n52.client.sos.event.data;

import java.util.ArrayList;
import java.util.Map;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.StoreYAxisEventHandler;

public class StoreYAxisEvent extends
        FilteredDispatchGwtEvent<StoreYAxisEventHandler> {

    /** The TYPE. */
    public static Type<StoreYAxisEventHandler> TYPE =
            new Type<StoreYAxisEventHandler>();

    private Map<String,String> props;

    public StoreYAxisEvent(Map<String,String> props,
    		StoreYAxisEventHandler... blockedHandlers) {
        super(blockedHandlers);
        this.props = props;

    }

    public Map<String,String> getProps() {
        return this.props;
    }

    @Override
    protected void onDispatch(StoreYAxisEventHandler handler) {
        handler.onStoreYAxis(this);
    }


    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<StoreYAxisEventHandler> getAssociatedType() {
        return TYPE;
    }

}

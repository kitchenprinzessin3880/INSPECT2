package org.n52.client.sos.event.data;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.CustomRenderingEventHandler;

import com.smartgwt.client.widgets.grid.ListGridRecord;

public class CustomRenderingEvent extends FilteredDispatchGwtEvent<CustomRenderingEventHandler>{

    public static Type<CustomRenderingEventHandler> TYPE =new Type<CustomRenderingEventHandler>();

    private ListGridRecord[] seriesAndSymbols;

    public CustomRenderingEvent(ListGridRecord[] seriessym, CustomRenderingEventHandler... blockedHandlers) {
        super(blockedHandlers);
        this.seriesAndSymbols= seriessym;
    }
    
    public ListGridRecord[] getSeriesSymbolsPairs() {
        return this.seriesAndSymbols;
    }
    
    public void setSeriesSymbolsPairs(ListGridRecord[] seriessym) {
        this.seriesAndSymbols=seriessym;
    }

    @Override
    protected void onDispatch(CustomRenderingEventHandler handler) {
        handler.onCustomRendering(this);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<CustomRenderingEventHandler> getAssociatedType() {
        return TYPE;
    }

	
}

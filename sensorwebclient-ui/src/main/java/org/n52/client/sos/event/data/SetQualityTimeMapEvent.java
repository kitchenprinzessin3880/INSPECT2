package org.n52.client.sos.event.data;

import java.util.List;
import java.util.Map;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.SetQualityTimeMapEventHandler;

public class SetQualityTimeMapEvent extends FilteredDispatchGwtEvent<SetQualityTimeMapEventHandler> {
	  public static Type<SetQualityTimeMapEventHandler> TYPE = new Type<SetQualityTimeMapEventHandler>();

	    private Map<String, Map<Double, List<String>>> qualTimeMap;

	    public SetQualityTimeMapEvent(Map<String, Map<Double, List<String>>> map) {
	        this.qualTimeMap = map;
	    }
	    
	    public Map<String, Map<Double, List<String>>> getQualityTimeMap() {
	        return this.qualTimeMap;
	    }

	    protected void onDispatch(SetQualityTimeMapEventHandler handler) {
	        handler.onSetQualityTimeMap(this);
	    }

	    public com.google.gwt.event.shared.GwtEvent.Type<SetQualityTimeMapEventHandler> getAssociatedType() {
	        return TYPE;
	    }
}

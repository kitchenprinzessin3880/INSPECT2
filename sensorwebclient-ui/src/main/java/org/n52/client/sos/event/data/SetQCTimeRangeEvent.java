package org.n52.client.sos.event.data;

import java.util.HashMap;
import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.SetQCTimeRangeEventHandler;

public class SetQCTimeRangeEvent extends FilteredDispatchGwtEvent<SetQCTimeRangeEventHandler> {
	  public static Type<SetQCTimeRangeEventHandler> TYPE = new Type<SetQCTimeRangeEventHandler>();
	    
	    private HashMap<String,String> timeRange;
	    private FlaggingPointType tpoint;

	    public enum FlaggingPointType {
	        START,END, TIMERANGE,RULE,MULTIPOINT,VIEW, RENDER, WINDOW, APPROVE;
	    }

	    public SetQCTimeRangeEvent(FlaggingPointType type) {
	        this.tpoint = type;
	    }

	    public SetQCTimeRangeEvent(HashMap<String,String> timerangeMap) {
	        this.timeRange = timerangeMap;
	    }
	    
	    protected void onDispatch(SetQCTimeRangeEventHandler handler) {
	        handler.onSetQCTimeRange(this);
	    }

	    public com.google.gwt.event.shared.GwtEvent.Type<SetQCTimeRangeEventHandler> getAssociatedType() {
	        return TYPE;
	    }
	    
	    public HashMap<String,String> getTimeRangeValues() {
	        return timeRange;
	    }
	    
	    public FlaggingPointType getTimePointType() {
	        return this.tpoint;
	    }

}

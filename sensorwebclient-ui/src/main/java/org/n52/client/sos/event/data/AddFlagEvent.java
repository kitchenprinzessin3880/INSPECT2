package org.n52.client.sos.event.data;
import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.AddFlagEventHandler;
//ASD
public class AddFlagEvent extends FilteredDispatchGwtEvent<AddFlagEventHandler> {

    /** The TYPE. */
    public static Type<AddFlagEventHandler> TYPE =new Type<AddFlagEventHandler>();

    private boolean isOverwrite=false;
    private boolean isOffline =false;
    private boolean isByRule =false;

	public AddFlagEvent(Boolean isOvr,Boolean isOff, Boolean isRuleActive, AddFlagEventHandler... blockedHandlers) {
        super(blockedHandlers);
        this.isOverwrite = isOvr;
        this.isOffline = isOff;
        this.isByRule= isRuleActive;
    }

   
    @Override
    protected void onDispatch(AddFlagEventHandler handler) {
        handler.onAddFlag(this);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<AddFlagEventHandler> getAssociatedType() {
        return TYPE;
    }


    public boolean isOverwrite() {
 		return isOverwrite;
 	}

 	public void setOverwrite(boolean isOverwrite) {
 		this.isOverwrite = isOverwrite;
 	}
	
    
    public boolean isOffline() {
		return isOffline;
	}

	public void setOffline(boolean isOffline) {
		this.isOffline = isOffline;
	}
	

	public boolean isByRule() {
		return isByRule;
	}


	public void setByRule(boolean isByRule) {
		this.isByRule = isByRule;
	}

}

package org.n52.client.sos.event.data;

import java.util.Map;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.UpdateTimeTextFieldEventHandler;


public class UpdateTimeTextFieldEvent extends FilteredDispatchGwtEvent<UpdateTimeTextFieldEventHandler> {
	
    public static Type<UpdateTimeTextFieldEventHandler> TYPE =new Type<UpdateTimeTextFieldEventHandler>();
    private Map<String, String> responseFields;
  
    public UpdateTimeTextFieldEvent(Map<String, String> res,UpdateTimeTextFieldEventHandler... blockedHandlers) {
        super(blockedHandlers);;
        this.responseFields = res;
    }

    public Map<String, String> getResponseFields()
    {
    	return this.responseFields;
    }
   
    @Override
    protected void onDispatch(UpdateTimeTextFieldEventHandler handler) {
        handler.onUpdateTextFields(this);
    }

    @Override
    public Type<UpdateTimeTextFieldEventHandler> getAssociatedType() {
        return TYPE;
    }

  
}

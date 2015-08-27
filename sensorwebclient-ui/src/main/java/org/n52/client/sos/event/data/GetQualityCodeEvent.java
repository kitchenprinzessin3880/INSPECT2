package org.n52.client.sos.event.data;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.GetQualityCodeEventHandler;


public class GetQualityCodeEvent extends FilteredDispatchGwtEvent<GetQualityCodeEventHandler> {

public static Type<GetQualityCodeEventHandler> TYPE = new Type<GetQualityCodeEventHandler>();
	
	public GetQualityCodeEvent() {
	}

	@Override
	protected void onDispatch(GetQualityCodeEventHandler handler) {
		handler.onGetQualityCodeEvent(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<GetQualityCodeEventHandler> getAssociatedType() {
		return TYPE;
	}

}

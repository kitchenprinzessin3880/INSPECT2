package org.n52.client.sos.event.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.StoreFlagCodeEventHandler;
import org.n52.shared.responses.QCDataResponse;

public class StoreFlagCodeEvent extends FilteredDispatchGwtEvent<StoreFlagCodeEventHandler> {

	public static Type<StoreFlagCodeEventHandler> TYPE = new Type<StoreFlagCodeEventHandler>();

	private List<String> shapesList;

	private Map<String, ArrayList<String>> flagCodeList;

	private Map<String, String> flagShapePairs;
	
	private String username ="";
	
	private Long sourceId;
	
	private String email="";
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	private Map<String, ArrayList<String>> allowedSitesList;
	
	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long sId) {
		this.sourceId = sId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public StoreFlagCodeEvent(QCDataResponse result) {
		this.flagCodeList = result.getCompleteFlagsList();
		this.shapesList = result.getAllSeriesShapes();
		this.flagShapePairs = result.getDefaultFlagShapesPair();
		this.username = result.getUsername();
		this.sourceId = result.getSourceId();
		this.allowedSitesList = result.getAllowedSitePropertiesList();
		this.email = result.getEmail();
	}

	@Override
	protected void onDispatch(StoreFlagCodeEventHandler handler) {
		handler.onStore(this);
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<StoreFlagCodeEventHandler> getAssociatedType() {
		return TYPE;
	}

	public Map<String, ArrayList<String>> getCompleteFlagCodes() {
		return this.flagCodeList;
	}

	public List<String> getShapesList() {
		return shapesList;
	}

	public Map<String, String> getFlagShapePairs() {
		return flagShapePairs;
	}

	public Map<String, ArrayList<String>> getAllowedSitesList() {
		return allowedSitesList;
	}

	public void setAllowedSitesList(Map<String, ArrayList<String>> allowedSitesList) {
		this.allowedSitesList = allowedSitesList;
	}

}

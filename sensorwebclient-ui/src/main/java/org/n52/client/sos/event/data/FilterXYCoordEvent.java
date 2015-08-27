package org.n52.client.sos.event.data;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.FilterXYCoordEventHandler;

public class FilterXYCoordEvent extends FilteredDispatchGwtEvent<FilterXYCoordEventHandler> {

    public static Type<FilterXYCoordEventHandler> TYPE = new Type<FilterXYCoordEventHandler>();
    private int x;
    private int y;
	private String genericFlg="";
    private String specificFlg="";
    
    public FilterXYCoordEvent(int xCoord, int yCoord) {
    	this.x=xCoord;
    	this.y=yCoord;
    }
    
    public FilterXYCoordEvent(int xCoord, int yCoord, String gen, String spec) {
    	this.x=xCoord;
    	this.y=yCoord;
    	this.genericFlg = gen;
    	this.specificFlg= spec;
    	
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public String getGenericFlg() {
		return genericFlg;
	}

	public void setGenericFlg(String genericFlg) {
		this.genericFlg = genericFlg;
	}

	public String getSpecificFlg() {
		return specificFlg;
	}

	public void setSpecificFlg(String specificFlg) {
		this.specificFlg = specificFlg;
	}


    @Override
    protected void onDispatch(FilterXYCoordEventHandler handler) {
        handler.onFilterXYCoord(this);
    }

    @Override
    public Type<FilterXYCoordEventHandler> getAssociatedType() {
        return TYPE;
    }

  
}


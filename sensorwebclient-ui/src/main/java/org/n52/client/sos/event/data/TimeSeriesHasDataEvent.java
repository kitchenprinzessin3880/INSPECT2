/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.client.sos.event.data;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.TimeSeriesHasDataEventHandler;

public class TimeSeriesHasDataEvent extends FilteredDispatchGwtEvent<TimeSeriesHasDataEventHandler> {

    public static Type<TimeSeriesHasDataEventHandler> TYPE = new Type<TimeSeriesHasDataEventHandler>();
    
    private boolean hasData = false;
    
    private String tsID;
    
    private String color;


	//ASD
    public TimeSeriesHasDataEvent(String tsID, boolean hasData, String seriesColor) {
        this.tsID = tsID;
        this.hasData = hasData;
        this.color = seriesColor;
    }
    
    /*
    public TimeSeriesHasDataEvent(String tsID, boolean hasData) {
        this.tsID = tsID;
        this.hasData = hasData;
    }*/
    
    public String getTSID() {
        return this.tsID;
    }
    
    public boolean hasData() {
        return this.hasData;
    }

    @Override
    protected void onDispatch(TimeSeriesHasDataEventHandler handler) {
        handler.onHasData(this);
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<TimeSeriesHasDataEventHandler> getAssociatedType() {
        return TYPE;
    }


    public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
}
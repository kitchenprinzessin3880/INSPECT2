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

import java.util.List;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.data.handler.NewTimeSeriesEventHandler;
import org.n52.client.ui.View;
import org.n52.shared.serializable.pojos.sos.FeatureOfInterest;
import org.n52.shared.serializable.pojos.sos.Offering;
import org.n52.shared.serializable.pojos.sos.Phenomenon;
import org.n52.shared.serializable.pojos.sos.Procedure;
import org.n52.shared.serializable.pojos.sos.Station;

public class NewTimeSeriesEvent extends FilteredDispatchGwtEvent<NewTimeSeriesEventHandler> {

    public static Type<NewTimeSeriesEventHandler> TYPE = new Type<NewTimeSeriesEventHandler>();

    private String sos;
    
    private Station station;
    
    private Offering offering;

    private FeatureOfInterest feature;

    private Procedure procedure;

    private Phenomenon phenomenon;

    private int width;

    private int height;

    private boolean requestSensorData;

    //ASD
    List<Builder> builderList = null;


	public static class Builder {
		private String serviceURL; // required
		private Station station; // required
		private Offering offering; // required
		private FeatureOfInterest feature; // required
		private Procedure procedure; // required
		private Phenomenon phenomenon; // required
		private int width = View.getInstance().getDataPanelWidth();
		private int height = View.getInstance().getDataPanelHeight();
		private boolean requestSensorData = true;
		private NewTimeSeriesEventHandler[] blockedHandlers = new NewTimeSeriesEventHandler[0];
    	
    	public Builder(String sosURL) {
    		assert(sosURL != null);
    		this.serviceURL = sosURL;
    	}

    	public Builder addStation(final Station station) {
    		assert(station != null);
    		this.station = station;
    		return this;
    	}
    	public Builder addOffering(final Offering offering) {
    		assert(offering != null);
    		this.offering = offering;
    		return this;
    	}
    	public Builder addFOI(final FeatureOfInterest foi) {
    		assert(foi != null);
    		this.feature = foi;
    		return this;
    	}
    	public Builder addProcedure(final Procedure procedure) {
    		assert(procedure != null);
    		this.procedure = procedure;
    		return this;
    	}
    	public Builder addPhenomenon(final Phenomenon phenomenon) {
    		assert(phenomenon != null);
    		this.phenomenon = phenomenon;
    		return this;
    	}
    	public Builder addWidth(final int width) {
    		this.width = width;
    		return this;
    	}
    	public Builder addHeight(final int height) {
    		this.height = height;
    		return this;
    	}
    	/**
    	 * Default is <code>true</code>.
    	 */
    	public Builder isRequestSensordata(final boolean requestSensordata) {
    		this.requestSensorData = requestSensordata;
    		return this;
    	}
    	public Builder addBlockedHandlers(NewTimeSeriesEventHandler... handlers) {
    		this.blockedHandlers = handlers;
    		return this;
    	}
    	public NewTimeSeriesEvent build() {
    		return new NewTimeSeriesEvent(this);
    	}
    	public Builder buildCustom() {
    		return this;
    	}
    	public String getServiceURL() {
    		return this.serviceURL;
    	}
    	public Station getStation() {
    		return this.station;
    	}
    	public Offering getOffering() {
    		return this.offering;
    	}
    	public FeatureOfInterest getFeatureOfInterest() {
    		return this.feature;
    	}
    	public Procedure getProcedure() {
    		return this.procedure;
    	}
    	public Phenomenon getPhenomenon() {
    		return this.phenomenon;
    	}
    	public int getWidth() {
    		return this.width;
    	}
    	public int getHeight() {
    		return this.height;
    	}
    	public boolean isRequestSensordata() {
    		return requestSensorData;
    	}
    	NewTimeSeriesEventHandler[] getBlockedHandlers() {
    		return blockedHandlers;
    	}
    }
    
    private NewTimeSeriesEvent(Builder builder) {
    	this.sos = builder.getServiceURL();
    	this.station = builder.getStation();
    	this.offering = builder.getOffering();
    	this.feature = builder.getFeatureOfInterest();
    	this.procedure = builder.getProcedure();
    	this.phenomenon = builder.getPhenomenon();
    	this.width = builder.getWidth();
    	this.height = builder.getHeight();
    	this.requestSensorData = builder.isRequestSensordata();
    }
    
    //ASD
    public NewTimeSeriesEvent(List<Builder> buildList) {
    	this.builderList = buildList;
    }
    
    public boolean requestSensordata() {
        return this.requestSensorData;
    }

    public String getSos() {
        return this.sos;
    }
    
    public Station getStation() {
    	return this.station;
    }

    public Offering getOffering() {
        return this.offering;
    }

    public FeatureOfInterest getFeature() {
        return this.feature;
    }

    public Procedure getProcedure() {
        return this.procedure;
    }

    public Phenomenon getPhenomenon() {
        return this.phenomenon;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    protected void onDispatch(NewTimeSeriesEventHandler handler) {
        handler.onNewTimeSeries(this);
    }

    public Type<NewTimeSeriesEventHandler> getAssociatedType() {
        return TYPE;
    }

    
    public List<Builder> getBuilderList() {
		return builderList;
	}
}

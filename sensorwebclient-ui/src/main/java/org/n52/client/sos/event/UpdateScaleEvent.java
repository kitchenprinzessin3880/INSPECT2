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
package org.n52.client.sos.event;

import org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent;
import org.n52.client.sos.event.handler.UpdateScaleEventHandler;

/**
 * @author <a href="mailto:f.bache@52north.de">Felix Bache</a>
 *
 */
public class UpdateScaleEvent extends FilteredDispatchGwtEvent<UpdateScaleEventHandler> {
    
    public static Type<UpdateScaleEventHandler> TYPE = new Type<UpdateScaleEventHandler>();
    
    private String phenomenonID;

    private String timeSeriesId;

	private boolean scaleToNull;

    private boolean autoScale;
    
    private boolean customScale;

	private double minY;
    private double maxY;

    //ASD 17.09.2014
    public UpdateScaleEvent(String tsId, boolean scaleToNull, boolean autoScale, boolean custom, double min, double max) {
        this.timeSeriesId = tsId;
        this.scaleToNull = scaleToNull;
        this.autoScale = autoScale;
        this.customScale = custom;
        this.minY=min;
        this.maxY=max;
    }
    
    /* ASD 17.09.2014
    public UpdateScaleEvent(String phenomenID, boolean scaleToNull, boolean autoScale) {
        this.phenomenonID = phenomenID;
        this.scaleToNull = scaleToNull;
        this.autoScale = autoScale;
    } */

    /* (non-Javadoc)
     * @see org.eesgmbh.gimv.client.event.FilteredDispatchGwtEvent#onDispatch(com.google.gwt.event.shared.EventHandler)
     */
    @Override
    protected void onDispatch(UpdateScaleEventHandler handler) {
        handler.onUpdateScale(this);
    }

    /* (non-Javadoc)
     * @see com.google.gwt.event.shared.GwtEvent#getAssociatedType()
     */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<UpdateScaleEventHandler> getAssociatedType() {
        return TYPE;
    }

    /**
     * @return the phenomenonID
     */
    public String getPhenomenonID() {
        return this.phenomenonID;
    }

    /**
     * @return the scaleToNull
     */
    public boolean isScaleToNull() {
        return this.scaleToNull;
    }

    /**
     * @return the autoScale
     */
    public boolean isAutoScale() {
        return this.autoScale;
    }

    public boolean isCustomScale() {
        return this.customScale;
    }
    
    
    public double getMinY() {
		return minY;
	}

	public void setMinY(int minY) {
		this.minY = minY;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}

    
    public String getTimeSeriesId() {
		return timeSeriesId;
	}

	public void setTimeSeriesId(String timeSeriesId) {
		this.timeSeriesId = timeSeriesId;
	}
}

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
package org.n52.shared.responses;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.shared.serializable.pojos.TimeSeriesProperties;

public class SensorMetadataResponse implements Serializable {

	private static final long serialVersionUID = -572577336556117796L;

	private TimeSeriesProperties props;

	private List<TimeSeriesProperties> propList;
	
	//ASD
	private Map<String,String> maintenanceMap =null;

	public void setPropList(List<TimeSeriesProperties> propList) {
		this.propList = propList;
	}

	private SensorMetadataResponse() {
		// serializable for GWT needs empty default constructor
	}
	
	//ASD 11.08.2014
	public SensorMetadataResponse(Map<String,String> map) {
		this.maintenanceMap = map;
	}

	/*
	public SensorMetadataResponse(TimeSeriesProperties props) {
		this.props = props;
	}

	// ASD
	public SensorMetadataResponse(List<TimeSeriesProperties> props) {
		this.propList = props;
	}*/

	public SensorMetadataResponse(TimeSeriesProperties props, Map<String,String> map ) {
		this.props = props;
		this.maintenanceMap = map;
	}

	public SensorMetadataResponse(List<TimeSeriesProperties> props, Map<String,String> map) {
		this.propList = props;
		this.maintenanceMap = map;
	}
	
	public TimeSeriesProperties getProps() {
		return this.props;
	}

	public List<TimeSeriesProperties> getPropList() {
		return propList;
	}

	public String toDebugString() {
		StringBuilder sb = new StringBuilder("Sensormetadata: \n");
		sb.append("\tOffering: ").append(props.getOffering().getId()).append("\n");
		sb.append("\tProcedure: ").append(props.getProcedure().getId()).append("\n");
		sb.append("\tFeatureOfInterest: ").append(props.getFoi().getId()).append("\n");
		sb.append("\tPhenomenon: ").append(props.getPhenomenon().getId()).append("\n");
		sb.append("\tlat: ").append(props.getLat()).append("  lng: ").append(props.getLon());
		return sb.toString();
	}

	//ASD
	public String toFullDebugString() {
		StringBuilder sb = new StringBuilder("Sensormetadata: \n");
		for (int i = 0; i < propList.size(); i++) {
			TimeSeriesProperties  props = propList.get(i);
			sb.append("\tOffering: ").append(props.getOffering().getId()).append(" | ");
			sb.append("\tProcedure: ").append(props.getProcedure().getId()).append(" | ");
			sb.append("\tFeatureOfInterest: ").append(props.getFoi().getId()).append(" | ");
			sb.append("\tPhenomenon: ").append(props.getPhenomenon().getId()).append("\n");
		}
		return sb.toString();
	}
	
	public Map<String, String> getMaintenanceMap() {
		return maintenanceMap;
	}

	public void setMaintenanceMap(Map<String, String> maintenanceMap) {
		this.maintenanceMap = maintenanceMap;
	}
}

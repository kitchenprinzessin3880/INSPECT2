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

package org.n52.client.sos.ctrl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.client.bus.EventBus;
import org.n52.client.ctrl.DataManager;
import org.n52.client.ctrl.ExceptionHandler;
import org.n52.client.ctrl.RequestFailedException;
import org.n52.client.ctrl.TimeManager;
import org.n52.client.sos.DataparsingException;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.event.AddMarkerEvent;
import org.n52.client.sos.event.DatesChangedEvent;
import org.n52.client.sos.event.data.DeleteAllTimeSeriesEvent;
import org.n52.client.sos.event.data.GetFeatureEvent;
import org.n52.client.sos.event.data.GetOfferingEvent;
import org.n52.client.sos.event.data.GetProcedureEvent;
import org.n52.client.sos.event.data.NewPhenomenonsEvent;
import org.n52.client.sos.event.data.NewStationPositionsEvent;
import org.n52.client.sos.event.data.NewTimeSeriesEvent;
import org.n52.client.sos.event.data.StoreFlagCodeEvent;
import org.n52.client.sos.event.data.StorePhenomenaEvent;
import org.n52.client.sos.event.data.StoreSOSMetadataEvent;
import org.n52.client.sos.event.data.StoreStationsEvent;
import org.n52.client.sos.event.data.NewTimeSeriesEvent.Builder;
import org.n52.client.sos.event.data.handler.StoreFlagCodeEventHandler;
import org.n52.client.sos.event.data.handler.StorePhenomenaEventHandler;
import org.n52.client.sos.event.data.handler.StoreSOSMetadataEventHandler;
import org.n52.client.sos.event.data.handler.StoreStationsEventHandler;
import org.n52.client.sos.legend.TimeSeries;
import org.n52.client.sos.ui.OfferingTab;
import org.n52.client.ui.View;
import org.n52.client.ui.legend.LegendEntryTimeSeries;
import org.n52.shared.serializable.pojos.sos.FeatureOfInterest;
import org.n52.shared.serializable.pojos.sos.Offering;
import org.n52.shared.serializable.pojos.sos.Phenomenon;
import org.n52.shared.serializable.pojos.sos.Procedure;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;
import org.n52.shared.serializable.pojos.sos.Station;

import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Window;

public class DataManagerSosImpl implements DataManager<SOSMetadata> {

	private static DataManagerSosImpl instance;
	private List<String> allDefaultShapes;
	private Map<String, SOSMetadata> metadatas = new HashMap<String, SOSMetadata>();
	// ASD
	private Map<String, ArrayList<String>> qualityCodeList;
	private Map<String, String> defaultFlagsAndShapes;
	private Map<String, String> activeFlagsAndShapes;
	private String userName = null;

	private boolean isUserLoggedIn = false;
	private Map<String, String> sosRequestStatus = new HashMap<String, String>();

	private DataManagerSosImpl() {
		new SOSEventBroker();
	}

	public static DataManagerSosImpl getInst() {
		if (instance == null) {
			instance = new DataManagerSosImpl();
		}
		return instance;
	}

	public boolean contains(String serviceURL) {
		return metadatas.containsKey(serviceURL);
	}

	private class SOSEventBroker implements StoreSOSMetadataEventHandler, StorePhenomenaEventHandler, StoreStationsEventHandler, StoreFlagCodeEventHandler {

		public SOSEventBroker() {
			EventBus.getMainEventBus().addHandler(StoreSOSMetadataEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(StorePhenomenaEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(StoreStationsEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(StoreFlagCodeEvent.TYPE, this);
		}

		public void onStore(StoreSOSMetadataEvent evt) {
			storeData(evt.getMetadata().getId(), evt.getMetadata());
		}

		public void onStore(StorePhenomenaEvent evt) {
			SOSMetadata meta = getServiceMetadata(evt.getSosURL());
			if (meta == null) {
				String reason = "Request failed for datamapping reasons.";
				RequestFailedException e = new RequestFailedException(reason);
				ExceptionHandler.handleUnexpectedException(e);
				return;
			}

			Set<String> phenomenonIds = evt.getPhenomena().keySet();
			for (String phenomenonId : phenomenonIds) {
				meta.addPhenomenon(new Phenomenon(phenomenonId));
			}
			EventBus.getMainEventBus().fireEvent(new NewPhenomenonsEvent(meta.getId(), phenomenonIds));
		}

		public void onStore(StoreStationsEvent evt) {
			SOSMetadata metadata = getServiceMetadata(evt.getSosURL());
			boolean isDropDownSearch = evt.isThemeSearch();
			if (metadata == null) {
				String reason = "An unknown SERVICES instance was requested.";
				RequestFailedException e = new RequestFailedException(reason);
				ExceptionHandler.handleUnexpectedException(e);
				return;
			}

			try {
				ArrayList<Station> stations = new ArrayList<Station>();
				metadata.setSrs(evt.getSrs());
				for (Station station : evt.getStations()) {
					if (station == null) {
						GWT.log("StoreProcedurePositionsEvent contained a 'null' station.");
						continue; // cannot throw IllegalStateException at
									// client side
					}
					Station local = metadata.getStation(station.getId());
					if (local == null) {
						// means we don not have data on that station in the
						// client metadata
						stations.add(station);
						metadata.addStation(station);
					}
					stations.add(station);
				}
				if (!isDropDownSearch) {
					EventBus.getMainEventBus().fireEvent(new NewStationPositionsEvent());
					EventBus.getMainEventBus().fireEvent(new AddMarkerEvent(stations));
				}
				// update local data map
				OfferingTab.getController().updateAllStationsFromMetaData(evt.getSosURL());
			} catch (Exception e) {
				ExceptionHandler.handleUnexpectedException(new DataparsingException("Failed to load positions", e));
			}
		}

		@Override
		// ASD
		public void onStore(StoreFlagCodeEvent evt) {
			qualityCodeList = evt.getCompleteFlagCodes();
			allDefaultShapes = evt.getShapesList();
			defaultFlagsAndShapes = evt.getFlagShapePairs();
			activeFlagsAndShapes = evt.getFlagShapePairs();
			userName = evt.getUsername();
			Long sourceId = evt.getSourceId();
			if (sourceId != null)
			// update header
			{
				View.getInstance().getHeaderInstance().updateUserName(userName);
				isUserLoggedIn = true;
			} else {
				View.getInstance().getHeaderInstance().updateUserName("Unknown");
				isUserLoggedIn = false;
			}
		}

	}

	public void storeData(String id, SOSMetadata data) {
		metadatas.put(id, data);
	}

	public SOSMetadata getServiceMetadata(String id) {
		return metadatas.get(id);
	}

	public Collection<SOSMetadata> getServiceMetadatas() {
		return metadatas.values();
	}

	public Map<String, SOSMetadata> getServiceMetadataSet() {
		return this.metadatas;
	}

	public Map<String, ArrayList<String>> getQualityCodeList() {
		return this.qualityCodeList;
	}

	// ASD
	public int getSOSMetadataSize() {
		return this.metadatas.size();
	}

	public List<String> getAllDefaultShapes() {
		return allDefaultShapes;
	}

	public void setAllDefaultShapes(List<String> allDefaultShapes) {
		this.allDefaultShapes = allDefaultShapes;
	}

	public Map<String, String> getDefaultFlagsAndShapes() {
		return defaultFlagsAndShapes;
	}

	public void setDefaultFlagsAndShapes(Map<String, String> defaultFlagsAndShapes) {
		this.defaultFlagsAndShapes = defaultFlagsAndShapes;
	}

	public Map<String, String> getActiveFlagsAndShapes() {
		return activeFlagsAndShapes;
	}

	public void setActiveFlagsAndShapes(Map<String, String> activeFlagsAndShapes) {
		this.activeFlagsAndShapes = activeFlagsAndShapes;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isUserHasLoggedIn() {
		return isUserLoggedIn;
	}

	public void setUserLoginStatus(boolean st) {
		isUserLoggedIn = st;
	}

	public Boolean getSosRequestStatus(String url) {

		if (sosRequestStatus.get(url).equals("completed")) {
			return true;
		} else {
			return false;
		}

	}

	public void setSosRequestStatus(Map<String, String> sosRequestStatus) {
		this.sosRequestStatus = sosRequestStatus;
	}

}

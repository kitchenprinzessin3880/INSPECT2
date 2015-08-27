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

import static org.n52.client.sos.ctrl.SOSController.SOS_PARAM_FIRST;
import static org.n52.client.sos.ctrl.SOSController.SOS_PARAM_LAST;
import static org.n52.client.sos.i18n.SosStringsAccessor.i18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.n52.client.sos.event.data.StoreProcedureDetailsUrlEvent;
import org.eesgmbh.gimv.client.event.SetDataAreaPixelBoundsEvent;
import org.eesgmbh.gimv.client.event.SetDomainBoundsEvent;
import org.eesgmbh.gimv.client.event.SetDomainBoundsEventHandler;
import org.eesgmbh.gimv.client.event.SetImageEntitiesEvent;
import org.eesgmbh.gimv.client.event.SetImageUrlEvent;
import org.eesgmbh.gimv.client.event.SetMaxDomainBoundsEvent;
import org.eesgmbh.gimv.client.event.SetOverviewDomainBoundsEvent;
import org.eesgmbh.gimv.client.event.SetViewportPixelBoundsEvent;
import org.eesgmbh.gimv.shared.util.Bounds;
import org.n52.client.Application;
import org.n52.client.bus.EventBus;
import org.n52.client.ctrl.ExceptionHandler;
import org.n52.client.ctrl.PropertiesManager;
import org.n52.client.ctrl.RequestFailedException;
import org.n52.client.ctrl.RequestManager;
import org.n52.client.ctrl.TimeManager;
import org.n52.client.ctrl.callbacks.EESDataCallback;
import org.n52.client.ctrl.callbacks.FileCallback;
import org.n52.client.ctrl.callbacks.GetFeatureCallback;
import org.n52.client.ctrl.callbacks.GetOfferingCallback;
import org.n52.client.ctrl.callbacks.GetPhenomenaCallback;
import org.n52.client.ctrl.callbacks.GetPositionsCallback;
import org.n52.client.ctrl.callbacks.GetProcedureCallback;
import org.n52.client.ctrl.callbacks.GetProcedureDetailsUrlCallback;
import org.n52.client.ctrl.callbacks.GetQualityCodeCallback;
import org.n52.client.ctrl.callbacks.GetStationCallback;
import org.n52.client.ctrl.callbacks.SensorMetadataCallback;
import org.n52.client.ctrl.callbacks.TimeSeriesDataCallback;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.event.DeleteMarkersEvent;
import org.n52.client.sos.event.LegendElementSelectedEvent;
import org.n52.client.sos.event.UpdateMaintenanceEvent;
import org.n52.client.sos.event.data.FinishedLoadingTimeSeriesEvent;
import org.n52.client.sos.event.data.FirstValueOfTimeSeriesEvent;
import org.n52.client.sos.event.data.GetProcedurePositionsFinishedEvent;
import org.n52.client.sos.event.data.NewTimeSeriesEvent;
import org.n52.client.sos.event.data.RequestDataEvent;
import org.n52.client.sos.event.data.SetQualityTimeMapEvent;
import org.n52.client.sos.event.data.StoreAxisDataEvent;
import org.n52.client.sos.event.data.StoreFeatureEvent;
import org.n52.client.sos.event.data.StoreFlagCodeEvent;
import org.n52.client.sos.event.data.StoreOfferingEvent;
import org.n52.client.sos.event.data.StorePhenomenaEvent;
import org.n52.client.sos.event.data.StoreProcedureEvent;
import org.n52.client.sos.event.data.StoreStationEvent;
import org.n52.client.sos.event.data.StoreStationsEvent;
import org.n52.client.sos.event.data.StoreTimeSeriesDataEvent;
import org.n52.client.sos.event.data.StoreTimeSeriesEvent;
import org.n52.client.sos.event.data.StoreTimeSeriesLastValueEvent;
import org.n52.client.sos.event.data.StoreTimeSeriesPropsEvent;
import org.n52.client.sos.event.data.StoreYAxisEvent;
import org.n52.client.sos.event.data.TimeSeriesHasDataEvent;
import org.n52.client.sos.event.data.NewTimeSeriesEvent.Builder;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent.FlaggingPointType;
import org.n52.client.sos.event.data.UpdateTimeTextFieldEvent;
import org.n52.client.sos.legend.TimeSeries;
import org.n52.client.sos.ui.EESTab;
import org.n52.client.sos.ui.OfferingTab;
import org.n52.client.ui.QualityFlagger;
import org.n52.client.ui.QualityFlaggerByView;
import org.n52.client.ui.Toaster;
import org.n52.client.ui.View;
import org.n52.client.ui.legend.LegendElement;
import org.n52.shared.exceptions.CompatibilityException;
import org.n52.shared.exceptions.ServerException;
import org.n52.shared.exceptions.TimeoutException;
import org.n52.shared.requests.EESDataRequest;
import org.n52.shared.requests.QCDataRequest;
import org.n52.shared.requests.TimeSeriesDataRequest;
import org.n52.shared.responses.EESDataResponse;
import org.n52.shared.responses.GetFeatureResponse;
import org.n52.shared.responses.GetOfferingResponse;
import org.n52.shared.responses.GetPhenomenonResponse;
import org.n52.shared.responses.GetProcedureDetailsUrlResponse;
import org.n52.shared.responses.GetProcedureResponse;
import org.n52.shared.responses.GetStationResponse;
import org.n52.shared.responses.QCDataResponse;
import org.n52.shared.responses.SensorMetadataResponse;
import org.n52.shared.responses.StationPositionsResponse;
import org.n52.shared.responses.TimeSeriesDataResponse;
import org.n52.shared.serializable.pojos.BoundingBox;
import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.QCOptions;
import org.n52.shared.serializable.pojos.ResponseOptions;
import org.n52.shared.serializable.pojos.TimeSeriesProperties;
import org.n52.shared.serializable.pojos.sos.FeatureOfInterest;
import org.n52.shared.serializable.pojos.sos.Offering;
import org.n52.shared.serializable.pojos.sos.Phenomenon;
import org.n52.shared.serializable.pojos.sos.Procedure;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;
import org.n52.shared.serializable.pojos.sos.Station;
import org.n52.shared.service.rpc.RpcEESDataService;
import org.n52.shared.service.rpc.RpcEESDataServiceAsync;
import org.n52.shared.service.rpc.RpcFileDataService;
import org.n52.shared.service.rpc.RpcFileDataServiceAsync;
import org.n52.shared.service.rpc.RpcQualityControlService;
import org.n52.shared.service.rpc.RpcQualityControlServiceAsync;
import org.n52.shared.service.rpc.RpcSensorMetadataService;
import org.n52.shared.service.rpc.RpcSensorMetadataServiceAsync;
import org.n52.shared.service.rpc.RpcServiceMetadataService;
import org.n52.shared.service.rpc.RpcServiceMetadataServiceAsync;
import org.n52.shared.service.rpc.RpcStationPositionsService;
import org.n52.shared.service.rpc.RpcStationPositionsServiceAsync;
import org.n52.shared.service.rpc.RpcTimeSeriesDataService;
import org.n52.shared.service.rpc.RpcTimeSeriesDataServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class SOSRequestManager extends RequestManager {

	private static SOSRequestManager instance;

	private RpcTimeSeriesDataServiceAsync timeSeriesDataService;

	private RpcSensorMetadataServiceAsync sensorMetadataService;

	private RpcStationPositionsServiceAsync stationPositionService;

	private RpcServiceMetadataServiceAsync serviceMetadataService;

	private RpcEESDataServiceAsync eesDataService;

	private RpcFileDataServiceAsync fileDataService;

	private RpcQualityControlServiceAsync qualityControlService;

	private boolean isRequestDiagramRunning = false;

	private static ArrayList<String> colorMappings = null;

	private static int COLOR_MAPPING_SIZE = 0;

	private static boolean isResetColors = false;

	// private Request mainRequestCall = null;
	// private Request overviewRequestCall = null;

	private Request multipleRequestCall = null;

	// ASD
	private String mainImageURL = "";
	private boolean isChangeStyleActive = false;

	public static SOSRequestManager getInstance() {
		if (instance == null) {
			instance = new SOSRequestManager();
		}
		return instance;
	}

	private SOSRequestManager() {
		// ASD
		PropertiesManager propertiesMng = PropertiesManager.getInstance();
		colorMappings = propertiesMng.getParameters("color");
		COLOR_MAPPING_SIZE = colorMappings.size();
		createRpcServices();
	}

	private void createRpcServices() {
		this.serviceMetadataService = GWT.create(RpcServiceMetadataService.class);
		this.stationPositionService = GWT.create(RpcStationPositionsService.class);
		this.sensorMetadataService = GWT.create(RpcSensorMetadataService.class);
		this.timeSeriesDataService = GWT.create(RpcTimeSeriesDataService.class);
		this.fileDataService = GWT.create(RpcFileDataService.class);
		this.eesDataService = GWT.create(RpcEESDataService.class);
		this.qualityControlService = GWT.create(RpcQualityControlService.class);
	}

	private void getSensorData(final TimeSeries timeSeries, TimeSeriesProperties properties, final boolean requestSensorData) throws Exception {
		SensorMetadataCallback callback = new SensorMetadataCallback(this, "Could not get sensor data.") {
			@Override
			public void onSuccess(SensorMetadataResponse result) {
				removeRequest();
				try {
					DataStoreTimeSeriesImpl.getInst().setMaintenanceData(result.getMaintenanceMap());
					TimeSeriesProperties tsProperties = result.getProps();
					String tsID = tsProperties.getTsID();
					DataStoreTimeSeriesImpl dataManager = DataStoreTimeSeriesImpl.getInst();
					LegendElement legendElement = dataManager.getDataItem(tsID).getLegendElement();
					
					//ASD 17.09.2014
					Map<String,String> yaxes = new HashMap<String,String>();
					String property = tsProperties.getPhenomenon().getId();
					String axisName = "";
					if (property.contains("_")) {
						axisName = property.substring(0, property.indexOf("_"));
					} else {
						axisName = property;
					}
					yaxes.put(tsID,axisName);
					
					// ASD
					if (tsProperties.getFirstValue() == null) {
						requestFirstValueOf(timeSeries);
					} else {
						long timestamp = tsProperties.getFirstValue();
						EventBus.getMainEventBus().fireEvent(new FirstValueOfTimeSeriesEvent(timestamp, null, tsID));
					}

					if (tsProperties.getLastValue() == null) {
						requestLastValueOf(timeSeries);
					} else {
						long timestamp = tsProperties.getLastValue();
						EventBus.getMainEventBus().fireEvent(new StoreTimeSeriesLastValueEvent(timestamp, null, tsID));
					}
					
					EventBus.getMainEventBus().fireEvent(new StoreTimeSeriesPropsEvent(tsID, tsProperties));
					EventBus.getMainEventBus().fireEvent(new LegendElementSelectedEvent(legendElement, true));
					EventBus.getMainEventBus().fireEvent(new UpdateMaintenanceEvent());
					if (requestSensorData) {
						EventBus.getMainEventBus().fireEvent(new RequestDataEvent(tsID));
					}
					EventBus.getMainEventBus().fireEvent(new FinishedLoadingTimeSeriesEvent());
					EventBus.getMainEventBus().fireEvent(new StoreYAxisEvent(yaxes));

				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				}
			}
		};
		addRequest();
		long s = TimeManager.getInst().getBegin();
		long e = TimeManager.getInst().getEnd();
		this.sensorMetadataService.getSensorMetadata(properties, s, e, callback);
	}

	public void getMaintenanceInformation(long begin, long end) throws Exception {
		// ASD
		List<String> sensorList = new ArrayList<String>();
		HashMap<String, TimeSeries> data = DataStoreTimeSeriesImpl.getInst().getDataItems();
		for (TimeSeries dw : data.values()) {
			sensorList.add(dw.getProcedureId() + "#" + dw.getPhenomenonId());
		}
		SensorMetadataCallback callback = new SensorMetadataCallback(this, "Could not get maintenance data.") {
			@Override
			public void onSuccess(SensorMetadataResponse result) {
				removeRequest();
				DataStoreTimeSeriesImpl.getInst().setMaintenanceData(result.getMaintenanceMap());
				try {
					EventBus.getMainEventBus().fireEvent(new UpdateMaintenanceEvent());
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				}
			}
		};
		addRequest();
		this.sensorMetadataService.getMaintenanceDetails(sensorList, begin, end, callback);
	}

	private void getMultipleSensorData(final Map<TimeSeries, TimeSeriesProperties> timeSeriesMap, final boolean requestSensorData) throws Exception {
		SensorMetadataCallback callback = new SensorMetadataCallback(this, "Could not get sensor data.") {
			@Override
			public void onSuccess(SensorMetadataResponse result) {
				removeRequest();
				try {
					List<TimeSeriesProperties> tsPropertiesList = result.getPropList();
					DataStoreTimeSeriesImpl.getInst().setMaintenanceData(result.getMaintenanceMap());
					
					//ASD 17.09.2014
					Map<String,String> yaxes = new HashMap<String,String>();
					
					for (int i = 0; i < tsPropertiesList.size(); i++) {
						TimeSeriesProperties tsProperties = tsPropertiesList.get(i);
						String tsID = tsProperties.getTsID();
						LegendElement legendElement = DataStoreTimeSeriesImpl.getInst().getDataItem(tsID).getLegendElement();
						TimeSeries tSeries = getKeyByValue(timeSeriesMap, tsID);
						
						//ASD 17.09.2014
						String property = tsProperties.getPhenomenon().getId();
						String axisName = "";
						if (property.contains("_")) {
							axisName = property.substring(0, property.indexOf("_"));
						} else {
							axisName = property;
						}
						yaxes.put(tsID,axisName);
						
						// ASD
						if (tsProperties.getFirstValue() == null) {
							requestFirstValueOf(tSeries);
						} else {
							long timestamp = tsProperties.getFirstValue();
							EventBus.getMainEventBus().fireEvent(new FirstValueOfTimeSeriesEvent(timestamp, null, tsID));
						}

						if (tsProperties.getLastValue() == null) {
							requestLastValueOf(tSeries);
						} else {
							long timestamp = tsProperties.getLastValue();
							EventBus.getMainEventBus().fireEvent(new StoreTimeSeriesLastValueEvent(timestamp, null, tsID));
						}

						EventBus.getMainEventBus().fireEvent(new StoreTimeSeriesPropsEvent(tsID, tsProperties));
						EventBus.getMainEventBus().fireEvent(new LegendElementSelectedEvent(legendElement, true));
					}
					EventBus.getMainEventBus().fireEvent(new UpdateMaintenanceEvent());
					EventBus.getMainEventBus().fireEvent(new RequestDataEvent());
					// ASD
					EventBus.getMainEventBus().fireEvent(new FinishedLoadingTimeSeriesEvent());
					EventBus.getMainEventBus().fireEvent(new StoreYAxisEvent(yaxes));
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				}
			}
		};
		addRequest();
		List<TimeSeriesProperties> list = new ArrayList<TimeSeriesProperties>(timeSeriesMap.values());
		long s = TimeManager.getInst().getBegin();
		long e = TimeManager.getInst().getEnd();
		this.sensorMetadataService.getMultipleSensorMetadata(list, s, e, callback);
	}

	public static TimeSeries getKeyByValue(Map<TimeSeries, TimeSeriesProperties> map, String tsID) {
		for (Entry<TimeSeries, TimeSeriesProperties> entry : map.entrySet()) {
			if (tsID.equals(entry.getKey().getId())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public void requestSensorMetadata(NewTimeSeriesEvent evt) throws Exception {
		int width = evt.getWidth();
		int height = evt.getHeight();
		String url = evt.getSos();

		Station station = evt.getStation();
		Offering offering = evt.getOffering();
		FeatureOfInterest foi = evt.getFeature();
		Procedure procedure = evt.getProcedure();
		Phenomenon phenomenon = evt.getPhenomenon();
		int currentSeriesCount = DataStoreTimeSeriesImpl.getInst().getDataItems().size();

		TimeSeries timeSeries = null;
		TimeSeriesProperties props = null;
		if (currentSeriesCount >= COLOR_MAPPING_SIZE) {
			props = new TimeSeriesProperties(url, station, offering, foi, procedure, phenomenon, width, height);
			timeSeries = new TimeSeries("TS_" + System.currentTimeMillis(), props);
		} else {
			props = new TimeSeriesProperties(url, station, offering, foi, procedure, phenomenon, width, height, colorMappings.get(currentSeriesCount));
			timeSeries = new TimeSeries("TS_" + System.currentTimeMillis(), props);
		}

		try {
			EventBus.getMainEventBus().fireEvent(new StoreTimeSeriesEvent(timeSeries));
		} catch (Exception e) {
			ExceptionHandler.handleUnexpectedException(e);
		} finally {
			try {
				getSensorData(timeSeries, props, evt.requestSensordata());
			} catch (Exception e) {
				ExceptionHandler.handleException(new RequestFailedException("Server did not respond!", e));
			}
		}

	}

	// ASD
	public void requestMultipleSensorMetadata(NewTimeSeriesEvent evt) throws Exception {
		List<Builder> builderList = evt.getBuilderList();

		Map<TimeSeries, TimeSeriesProperties> timeSeriesList = new HashMap<TimeSeries, TimeSeriesProperties>();
		int currentSeriesCount = DataStoreTimeSeriesImpl.getInst().getDataItems().size();

		for (int i = 0; i < builderList.size(); i++) {
			int width = builderList.get(i).getWidth();
			int height = builderList.get(i).getHeight();
			String url = builderList.get(i).getServiceURL();

			Station station = builderList.get(i).getStation();
			Offering offering = builderList.get(i).getOffering();
			FeatureOfInterest foi = builderList.get(i).getFeatureOfInterest();
			Procedure procedure = builderList.get(i).getProcedure();
			Phenomenon phenomenon = builderList.get(i).getPhenomenon();

			TimeSeries timeSeries = null;
			TimeSeriesProperties props = null;
			if (currentSeriesCount >= COLOR_MAPPING_SIZE) {
				props = new TimeSeriesProperties(url, station, offering, foi, procedure, phenomenon, width, height);
				timeSeries = new TimeSeries("TS_" + System.currentTimeMillis(), props);
			} else {
				props = new TimeSeriesProperties(url, station, offering, foi, procedure, phenomenon, width, height, colorMappings.get(currentSeriesCount));
				timeSeries = new TimeSeries("TS_" + System.currentTimeMillis(), props);
				currentSeriesCount++;
			}

			timeSeriesList.put(timeSeries, props);
			try {
				EventBus.getMainEventBus().fireEvent(new StoreTimeSeriesEvent(timeSeries));
			} catch (Exception e) {
				ExceptionHandler.handleUnexpectedException(e);
			}
		}
		try {
			getMultipleSensorData(timeSeriesList, evt.requestSensordata());
		} catch (Exception e) {
			ExceptionHandler.handleException(new RequestFailedException("Server did not respond!", e));
		}
	}

	public void requestFirstValueOf(TimeSeries timeSeries) {
		try {
			ArrayList<TimeSeriesProperties> series = new ArrayList<TimeSeriesProperties>();
			series.add(timeSeries.getProperties());

			boolean grid = DataStoreTimeSeriesImpl.getInst().isGridEnabled();
			long begin = TimeManager.getInst().getBegin();
			long end = TimeManager.getInst().getEnd();
			DesignOptions options = new DesignOptions(series, begin, end, SOS_PARAM_FIRST, grid);
			requestFirstValueFromTimeSeries(new TimeSeriesDataRequest(options), timeSeries);
		} catch (TimeoutException ex) {
			ExceptionHandler.handleException(ex);
		} catch (Exception e) {
			ExceptionHandler.handleException(new RequestFailedException("Request failed", e));
		}
	}

	public void requestLastValueOf(TimeSeries timeSeries) {
		try {
			ArrayList<TimeSeriesProperties> series = new ArrayList<TimeSeriesProperties>();
			series.add(timeSeries.getProperties());
			boolean grid = DataStoreTimeSeriesImpl.getInst().isGridEnabled();
			long begin = TimeManager.getInst().getBegin();
			long end = TimeManager.getInst().getEnd();
			DesignOptions options = new DesignOptions(series, begin, end, SOS_PARAM_LAST, grid);
			requestLastTimeSeriesData(new TimeSeriesDataRequest(options), timeSeries);
		} catch (TimeoutException ex) {
			ExceptionHandler.handleException(ex);
		} catch (Exception e) {
			ExceptionHandler.handleException(new RequestFailedException("Request failed", e));
		}
	}

	public void requestSensorData(TimeSeries[] timeSeries, String id) {
		try {
			ArrayList<TimeSeriesProperties> series = new ArrayList<TimeSeriesProperties>();
			for (TimeSeries timeSerie : timeSeries) {
				if (timeSerie.getId().equals(id)) {
					timeSerie.getProperties().setHeight(View.getInstance().getDataPanelHeight());
					timeSerie.getProperties().setWidth(View.getInstance().getDataPanelWidth());
					series.add(timeSerie.getProperties());
					break;
				}
			}
			boolean grid = DataStoreTimeSeriesImpl.getInst().isGridEnabled();
			long begin = TimeManager.getInst().getBegin();
			long end = TimeManager.getInst().getEnd();
			DesignOptions options = new DesignOptions(series, begin, end, grid);
			getTimeSeriesData(new TimeSeriesDataRequest(options));
		} catch (TimeoutException ex) {
			ExceptionHandler.handleException(ex);
		} catch (Exception e) {
			ExceptionHandler.handleException(new RequestFailedException("Request failed", e));
		}
	}

	private void requestFirstValueFromTimeSeries(TimeSeriesDataRequest request, final TimeSeries timeSeries) throws Exception {
		final long startTimeOfRequest = System.currentTimeMillis();
		addRequest();

		AsyncCallback<TimeSeriesDataResponse> callback = new AsyncCallback<TimeSeriesDataResponse>() {

			public void onFailure(Throwable caught) {
				removeRequest();
				Application.setHasStarted(true);
				ExceptionHandler.handleException(new CompatibilityException("Could not get first time series value", caught));
			}

			public void onSuccess(TimeSeriesDataResponse response) {
				removeRequest(System.currentTimeMillis() - startTimeOfRequest);
				HashMap<String, HashMap<Long, String>> payloadData = response.getPayloadData();
				try {
					if (payloadData.isEmpty()) {
						return;
					}
					String id = timeSeries.getId();
					HashMap<Long, String> timeSeriesData = payloadData.get(id);
					if (timeSeriesData.keySet().iterator().hasNext()) {
						long timestamp = timeSeriesData.keySet().iterator().next().longValue();
						String firstValue = timeSeriesData.get(timestamp).toString();
						FirstValueOfTimeSeriesEvent event = new FirstValueOfTimeSeriesEvent(timestamp, firstValue, id);
						EventBus.getMainEventBus().fireEvent(event);
					}
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				} finally {
					Application.setHasStarted(true);
				}
			}
		};
		this.timeSeriesDataService.getTimeSeriesData(request, callback);
	}

	private void requestLastTimeSeriesData(TimeSeriesDataRequest request, final TimeSeries timeSeries) throws Exception {
		final long startRequest = System.currentTimeMillis();
		addRequest();

		AsyncCallback<TimeSeriesDataResponse> callback = new AsyncCallback<TimeSeriesDataResponse>() {
			public void onFailure(Throwable caught) {
				removeRequest();
				Application.setHasStarted(true);
				ExceptionHandler.handleException(new CompatibilityException("Could not get last time series value.", caught));
			}

			public void onSuccess(TimeSeriesDataResponse response) {
				removeRequest(System.currentTimeMillis() - startRequest);
				HashMap<String, HashMap<Long, String>> payloadData = response.getPayloadData();

				try {
					if (payloadData.isEmpty()) {
						return; // nothing returned from server
					}

					String id = timeSeries.getId();
					HashMap<Long, String> timeSeriesData = payloadData.get(id);
					if (timeSeriesData.keySet().iterator().hasNext()) {
						long date = timeSeriesData.keySet().iterator().next().longValue();
						String lastValue = timeSeriesData.get(date).toString();
						EventBus.getMainEventBus().fireEvent(new StoreTimeSeriesLastValueEvent(date, lastValue, id));
					}
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				} finally {
					Application.setHasStarted(true);
				}
			}
		};

		this.timeSeriesDataService.getTimeSeriesData(request, callback);
	}

	private void getTimeSeriesData(TimeSeriesDataRequest req) throws Exception {
		// prepare callback
		final long start = System.currentTimeMillis();
		TimeSeriesDataCallback callback = new TimeSeriesDataCallback(this, "Could not get timeseries data.") {
			@Override
			public void onSuccess(TimeSeriesDataResponse result) {
				requestMgr.removeRequest(System.currentTimeMillis() - start);
				try {
					EventBus.getMainEventBus().fireEvent(new StoreTimeSeriesDataEvent(result.getPayloadData()));
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				} finally {
					Application.setHasStarted(true);
				}
			}
		};
		addRequest();
		this.timeSeriesDataService.getTimeSeriesData(req, callback);
	}

	public void requestSensorData(TimeSeries[] timeseriesArray) {
		if (timeseriesArray.length > 0) {
			ArrayList<TimeSeriesProperties> series = new ArrayList<TimeSeriesProperties>();
			for (int i = 0; i < timeseriesArray.length; i++) {
				TimeSeries timeseries = timeseriesArray[i];
				series.add(timeseries.getProperties());
			}
			try {
				boolean gridEnabled = DataStoreTimeSeriesImpl.getInst().isGridEnabled();
				long begin = TimeManager.getInst().getBegin();
				long end = TimeManager.getInst().getEnd();
				DesignOptions options = new DesignOptions(series, begin, end, gridEnabled);
				getTimeSeriesData(new TimeSeriesDataRequest(options));
			} catch (Exception e) {
				ExceptionHandler.handleException(new RequestFailedException("Request failed", e));
			}
		}
	}

	/*
	 * public void requestDiagram() { TimeSeries[] timeSeries =
	 * DataStoreTimeSeriesImpl.getInst().getTimeSeriesSorted(); if
	 * (timeSeries.length == 0) { // reset diagram to blank image
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetImageUrlEvent("img/blank.gif"));
	 * EventBus.getOverviewChartEventBus().fireEvent(new
	 * SetImageUrlEvent("img/blank.gif"));
	 * EventBus.getOverviewChartEventBus().fireEvent(new
	 * SetDomainBoundsEvent(new Bounds(0d, null, 0d, null))); return; }
	 * ArrayList<TimeSeriesProperties> properties = new
	 * ArrayList<TimeSeriesProperties>(); for (TimeSeries timeSerie :
	 * timeSeries) {
	 * timeSerie.getProperties().setHeight(EESTab.getPanelHeight());
	 * timeSerie.getProperties().setWidth(EESTab.getPanelWidth());
	 * properties.add(timeSerie.getProperties()); }
	 * 
	 * long begin = TimeManager.getInst().getBegin(); long end =
	 * TimeManager.getInst().getEnd(); boolean grid =
	 * DataStoreTimeSeriesImpl.getInst().isGridEnabled();
	 * 
	 * // ASD boolean isOverviewTimeInitialized =
	 * TimeManager.getInst().getIsOverviewTimeFixed(); try { long interval; long
	 * middle; long ovBegin; long ovEnd; long timeRangeOverview =
	 * TimeManager.getInst().getOverviewInterval();
	 * 
	 * if (!isOverviewTimeInitialized) { interval = end - begin; middle = (long)
	 * (end - (interval * 0.5)); ovBegin = middle - timeRangeOverview / 2; ovEnd
	 * = middle + timeRangeOverview / 2;
	 * TimeManager.getInst().setIsOverviewTimeFixed(true);
	 * TimeManager.getInst().setIntervalBeginFixed(ovBegin);
	 * TimeManager.getInst().setIntervalEndFixed(ovEnd); } ovBegin =
	 * TimeManager.getInst().getIntervalBeginFixed(); ovEnd =
	 * TimeManager.getInst().getIntervalEndFixed();
	 * 
	 * ArrayList<TimeSeriesProperties> copySeries = new
	 * ArrayList<TimeSeriesProperties>(); for (TimeSeriesProperties pc :
	 * properties) { TimeSeriesProperties copy = pc.copy();
	 * setDefaultValues(copy); copy.setLanguage(PropertiesManager.language);
	 * copy.setShowYAxis(false); copy.setScaledToZero(true);
	 * copy.setHeight(100); copySeries.add(copy); }
	 * 
	 * DesignOptions o2 = new DesignOptions(copySeries, ovBegin, ovEnd, grid);
	 * getDiagramOverview(new EESDataRequest(o2));
	 * 
	 * DesignOptions o1 = new DesignOptions(properties, begin, end, grid);
	 * getDiagram(new EESDataRequest(o1));
	 * 
	 * } catch (TimeoutException ex) { ExceptionHandler.handleException(ex); }
	 * catch (Exception e) { ExceptionHandler.handleException(new
	 * RequestFailedException("Could not get diagram", e)); } }
	 */

	// ASD Combine both main and overview requests
	public void requestDiagrams(boolean isFlaggingActive) {
		this.isRequestDiagramRunning = true;
		TimeSeries[] timeSeries = null;
		// if (isFlaggingActive) {
		// ASD sort timeseries; first series must be the current series
		// selected by the user;
		// annotations directly added to the plot will be drawn on the
		// topmost layer.
		// timeSeries =
		// DataStoreTimeSeriesImpl.getInst().getTimeSeriesCustomSorted();
		// } else {
		timeSeries = DataStoreTimeSeriesImpl.getInst().getTimeSeriesSorted();
		// }

		if (timeSeries.length == 0) {
			// reset diagram to blank image
			EventBus.getMainEventBus().fireEvent(new SetImageUrlEvent("img/blank.gif"));
			EventBus.getOverviewChartEventBus().fireEvent(new SetImageUrlEvent("img/blank.gif"));
			EventBus.getOverviewChartEventBus().fireEvent(new SetDomainBoundsEvent(new Bounds(0d, null, 0d, null)));
			// ASD
			isRequestDiagramRunning = false;
			EESTab.getOverviewPresenter().setHorizontallyLocked(false);
			return;
		}
		ArrayList<TimeSeriesProperties> properties = new ArrayList<TimeSeriesProperties>();
		for (TimeSeries timeSerie : timeSeries) {
			timeSerie.getProperties().setHeight(EESTab.getPanelHeight());
			timeSerie.getProperties().setWidth(EESTab.getPanelWidth());
			properties.add(timeSerie.getProperties());
		}

		long begin = TimeManager.getInst().getBegin();
		long end = TimeManager.getInst().getEnd();
		boolean grid = DataStoreTimeSeriesImpl.getInst().isGridEnabled();

		// ASD
		boolean isOverviewTimeInitialized = TimeManager.getInst().getIsOverviewTimeFixed();
		try {
			long interval;
			long middle;
			long ovBegin;
			long ovEnd;
			long timeRangeOverview = TimeManager.getInst().getOverviewInterval();

			if (!isOverviewTimeInitialized) {
				interval = end - begin;
				middle = (long) (end - (interval * 0.5));
				ovBegin = middle - timeRangeOverview / 2;
				ovEnd = middle + timeRangeOverview / 2;
				TimeManager.getInst().setIsOverviewTimeFixed(true);
				TimeManager.getInst().setIntervalBeginFixed(ovBegin);
				TimeManager.getInst().setIntervalEndFixed(ovEnd);
			}
			ovBegin = TimeManager.getInst().getIntervalBeginFixed();
			ovEnd = TimeManager.getInst().getIntervalEndFixed();

			/*
			 * ArrayList<TimeSeriesProperties> copySeries = new
			 * ArrayList<TimeSeriesProperties>(); for (TimeSeriesProperties pc :
			 * properties) { TimeSeriesProperties copy = pc.copy();
			 * setDefaultValues(copy);
			 * copy.setLanguage(PropertiesManager.language);
			 * copy.setShowYAxis(false); copy.setScaledToZero(true);
			 * copy.setHeight(100); copySeries.add(copy); }
			 */

			ArrayList<TimeSeriesProperties> copySeries = setDefaultStyleValues(properties);

			DesignOptions o1 = null;
			DesignOptions o2 = new DesignOptions(copySeries, ovBegin, ovEnd, grid);

			if (isFlaggingActive) {
				o1 = new DesignOptions(properties, DataStoreTimeSeriesImpl.getInst().getSelectedCurrentTimeSeries().getId(), begin, end, grid, QualityFlaggingController.getInstance().getRenderingPoints(), isFlaggingActive);
			} else {
				o1 = new DesignOptions(properties, begin, end, grid);
			}

			// combine both timeseries info
			HashMap<String, DesignOptions> options = new HashMap<String, DesignOptions>();
			options.put("main", o1);
			options.put("overview", o2);
			getDiagrams(new EESDataRequest(options), isFlaggingActive);

		} catch (TimeoutException ex) {
			ExceptionHandler.handleException(ex);
		} catch (Exception e) {
			ExceptionHandler.handleException(new RequestFailedException("Could not get diagram", e));
		}
	}

	private void setDefaultValues(TimeSeriesProperties copy) {
		PropertiesManager properties = PropertiesManager.getInstance();
		ArrayList<String> mappings = properties.getParameters("phenomenon");
		for (String mapping : mappings) {
			String[] values = mapping.split(",");
			if (copy.getPhenomenon().getLabel().equals(values[0])) {
				try {
					copy.setLineStyle(values[1]);
					copy.setSeriesType(values[2]);
					if (RegExp.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$").test(values[3])) {
						copy.setHexColor(values[3]);
					} else {
						throw new Exception("Pattern for hex color do not match");
					}
				} catch (Exception e) {
					Toaster.getInstance().addErrorMessage(i18n.errorPhenomenonProperties());
				}
			}
		}
	}

	// ASD
	private ArrayList<TimeSeriesProperties> setDefaultStyleValues(ArrayList<TimeSeriesProperties> properties) {
		ArrayList<TimeSeriesProperties> copySeries = new ArrayList<TimeSeriesProperties>();
		int currentSeriesCount = 0;
		for (TimeSeriesProperties pc : properties) {
			TimeSeriesProperties copy = pc.copy();
			if (!isChangeStyleActive) {
				if (currentSeriesCount < COLOR_MAPPING_SIZE) {
					try {
						if (RegExp.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$").test(colorMappings.get(currentSeriesCount))) {
							// GWT.log(copy.getOffFoiProcPhenCombination() + " "
							// + colorMappings.get(currentSeriesCount));
							copy.setHexColor(colorMappings.get(currentSeriesCount));
							pc.setHexColor(colorMappings.get(currentSeriesCount));
							currentSeriesCount++;
							// update TR,TV
						} else {
							throw new Exception("Pattern for hex color do not match");
						}
					} catch (Exception e) {
						Toaster.getInstance().addErrorMessage(i18n.errorPhenomenonProperties());
					}
				}
			}
			copy.setLanguage(PropertiesManager.language);
			copy.setShowYAxis(false);
			copy.setScaledToZero(true);
			copy.setHeight(100);
			copySeries.add(copy);
		}
		return copySeries;
	}

	/*
	 * private void getDiagramOverview(final EESDataRequest req) throws
	 * Exception {
	 * 
	 * // prepare callback EESDataCallback callback = new EESDataCallback(this,
	 * "Could not get overview diagram.") {
	 * 
	 * @Override public void onSuccess(EESDataResponse result) {
	 * removeRequest(); try { // FIXME wait for fix //
	 * EESController.getOverviewEventBus().fireEvent(new //
	 * SetViewportBoundsEvent(new Bounds( // 0d, new
	 * Double(result.getPlotArea().getWidth()), // 0d, 100d)));
	 * GWT.log("Got OverviewDiagram: " + result.getWidth() + "w x " +
	 * result.getHeight() + "h"); GWT.log("For Viewportsize: " +
	 * EESTab.getPanelWidth() + "w x " + EESTab.getPanelHeight() + "h"); //
	 * EESController.getOverviewEventBus().fireEvent( // new
	 * SetMaxBoundsEvent(new Bounds((double) //
	 * result.getDateRangeOnXAxis().getStart().getTime(), // (double)
	 * result.getDateRangeOnXAxis().getEnd().getTime(), // null, null)));
	 * 
	 * // inner pixel bounds = plotarea EventBus bus =
	 * EventBus.getOverviewChartEventBus(); bus.fireEvent(new
	 * SetDataAreaPixelBoundsEvent(result.getPlotArea())); bus.fireEvent(new
	 * SetImageUrlEvent(result.getImageUrl()));
	 * 
	 * // Bounds maxDomainBounds = new Bounds(0d, (double) new //
	 * Date().getTime(), null, null); // bus.fireEvent(new //
	 * SetMaxDomainBoundsEvent(maxDomainBounds));
	 * 
	 * Double begin = new Double(result.getBegin() + 0.0); Double end = new
	 * Double(result.getEnd() + 0.0); Double top =
	 * result.getPlotArea().getTop(); Double bottom =
	 * result.getPlotArea().getBottom(); Bounds domainBounds = new Bounds(begin,
	 * end, top, bottom); bus.fireEvent(new SetDomainBoundsEvent(domainBounds));
	 * 
	 * Double vpLeft = new Double(0.0); Double vpRight = new
	 * Double(result.getWidth()); Double vpTop = new Double(0.0); Double
	 * pixelBottom = new Double(result.getHeight()); Bounds viewportPixelBounds
	 * = new Bounds(vpLeft, vpRight, vpTop, pixelBottom); bus.fireEvent(new
	 * SetViewportPixelBoundsEvent(viewportPixelBounds)); } catch (Exception e)
	 * { ExceptionHandler.handleUnexpectedException(e); } } };
	 * 
	 * // request addRequest(); overviewRequestCall =
	 * this.eesDataService.getEESOverview(req, callback); }
	 */

	/*
	 * rendering the timeseries diagram at client side private void
	 * getDiagram(final EESDataRequest request) throws Exception {
	 * 
	 * // prepare callback EESDataCallback callback = new EESDataCallback(this,
	 * "Could not get diagram.") {
	 * 
	 * @Override public void onSuccess(EESDataResponse result) {
	 * removeRequest(); try { // block overview if (!GWT.isProdMode()) {
	 * GWT.log("Got Diagram: " + result.getWidth() + "w x " + result.getHeight()
	 * + "h"); GWT.log("For Viewportsize: " + EESTab.getPanelWidth() + "w x " +
	 * EESTab.getPanelHeight() + "h"); } for (String key :
	 * result.getAxis().keySet()) { EventBus.getMainEventBus().fireEvent(new
	 * StoreAxisDataEvent(key, result.getAxis().get(key))); }
	 * 
	 * for (TimeSeriesProperties prop : result.getPropertiesList()) {
	 * TimeSeriesHasDataEvent hasDataEvent = new
	 * TimeSeriesHasDataEvent(prop.getTsID(), prop.hasData());
	 * EventBus.getMainEventBus().fireEvent(hasDataEvent); }
	 * 
	 * // ASD mainImageURL = result.getImageUrl();
	 * 
	 * // ASD SetQualityTimeMapEvent setQualityTimeMap = new
	 * SetQualityTimeMapEvent(result.getQualityTimeMap());
	 * EventBus.getMainEventBus().fireEvent(setQualityTimeMap);
	 * 
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetImageUrlEvent(result.getImageUrl())); // inner pixel bounds = plotarea
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetDataAreaPixelBoundsEvent(result.getPlotArea()));
	 * 
	 * Double left = new Double(0); Double right = new Double(new
	 * Date().getTime()); Bounds maxDomainBounds = new Bounds(left, right, null,
	 * null); EventBus.getMainEventBus().fireEvent(new
	 * SetMaxDomainBoundsEvent(maxDomainBounds));
	 * 
	 * SetDomainBoundsEventHandler[] blocked = {
	 * DataStoreTimeSeriesImpl.getInst().getEventBroker() }; Double mainLeft =
	 * new Double(result.getBegin() + 0.0); Double mainRight = new
	 * Double(result.getEnd() + 0.0); Double mainTop =
	 * result.getPlotArea().getTop(); Double mainBottom =
	 * result.getPlotArea().getBottom(); Bounds diagramBounds = new
	 * Bounds(mainLeft, mainRight, mainTop, mainBottom);
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetDomainBoundsEvent(diagramBounds, blocked));
	 * 
	 * Double overviewRight = new Double(result.getWidth()); Double
	 * overviewBottom = new Double(result.getHeight()); Bounds viewportBounds =
	 * new Bounds(left, overviewRight, left, overviewBottom);
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetViewportPixelBoundsEvent(viewportBounds));
	 * 
	 * // tooltips List<ImageEntity> ie = new ArrayList<ImageEntity>(); for
	 * (ImageEntity imageEntity : result.getImageEntities()) {
	 * ie.add(imageEntity); } EventBus.getMainEventBus().fireEvent(new
	 * SetImageEntitiesEvent(ie));
	 * EventBus.getOverviewChartEventBus().fireEvent(new
	 * SetOverviewDomainBoundsEvent(diagramBounds)); } catch (Exception e) {
	 * ExceptionHandler.handleUnexpectedException(e); } finally {
	 * Application.setHasStarted(true); result.destroy(); result = null; } } };
	 * // request addRequest(); mainRequestCall =
	 * this.eesDataService.getEESDiagram(request, callback); }
	 */

	// rendering the timeseries diagram at client side
	private void getDiagrams(final EESDataRequest request, final boolean isFlaggingActive) throws Exception {
		// prepare callback
		EESDataCallback callback = new EESDataCallback(this, "Could not get diagram.") {
			@Override
			public void onSuccess(EESDataResponse result) {
				removeRequest();
				try {
					isRequestDiagramRunning = false; // reset to default

					HashMap<String, ResponseOptions> optionsMap = result.getResponseOptionsMap();
					// overview diagram and associated events
					ResponseOptions overviewResult = optionsMap.get("overview");
					if (!GWT.isProdMode()) {
						GWT.log("Got OverviewDiagram: " + overviewResult.getWidth() + "w x " + overviewResult.getHeight() + "h");
						GWT.log("For Viewportsize: " + EESTab.getPanelWidth() + "w x " + EESTab.getPanelHeight() + "h");
					}
					EventBus bus = EventBus.getOverviewChartEventBus();
					bus.fireEvent(new SetDataAreaPixelBoundsEvent(overviewResult.getPlotArea()));
					bus.fireEvent(new SetImageUrlEvent(overviewResult.getImageUrl()));
					Double begin = new Double(overviewResult.getBegin() + 0.0);
					Double end = new Double(overviewResult.getEnd() + 0.0);
					Double top = overviewResult.getPlotArea().getTop();
					Double bottom = overviewResult.getPlotArea().getBottom();
					Bounds domainBounds = new Bounds(begin, end, top, bottom);
					bus.fireEvent(new SetDomainBoundsEvent(domainBounds));
					Double vpLeft = new Double(0.0);
					Double vpRight = new Double(overviewResult.getWidth());
					Double vpTop = new Double(0.0);
					Double pixelBottom = new Double(overviewResult.getHeight());
					Bounds viewportPixelBounds = new Bounds(vpLeft, vpRight, vpTop, pixelBottom);
					bus.fireEvent(new SetViewportPixelBoundsEvent(viewportPixelBounds));

					// main diagram and associate events
					ResponseOptions mainResult = optionsMap.get("main");
					generateMainTS(mainResult, isFlaggingActive);
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				} finally {
					Application.setHasStarted(true);
					result.destroy();
					result = null;
				}
			}
		};
		// request
		addRequest();
		multipleRequestCall = this.eesDataService.getDiagrams(request, callback);
	}

	private void generateMainTS(ResponseOptions mainResult, boolean isFlaggingActive) {
		SetQualityTimeMapEvent setQualityTimeMap = new SetQualityTimeMapEvent(mainResult.getQualityTimeMap());
		EventBus.getMainEventBus().fireEvent(setQualityTimeMap);

		if (!GWT.isProdMode()) {
			GWT.log("Got Diagram: " + mainResult.getWidth() + "w x " + mainResult.getHeight() + "h");
			GWT.log("For Viewportsize: " + EESTab.getPanelWidth() + "w x " + EESTab.getPanelHeight() + "h");
		}
		for (String key : mainResult.getAxis().keySet()) {
			EventBus.getMainEventBus().fireEvent(new StoreAxisDataEvent(key, mainResult.getAxis().get(key)));
		}

		for (TimeSeriesProperties prop : mainResult.getPropertiesList()) {
			TimeSeriesHasDataEvent hasDataEvent = new TimeSeriesHasDataEvent(prop.getTsID(), prop.hasData(), prop.getHexColor());
			EventBus.getMainEventBus().fireEvent(hasDataEvent);
		}

		if (isResetColors) // update view-based and range-based windows
		{
			QualityFlagger.getInst().updateListGridRecords();
			QualityFlaggerByView.getInst().updateListGridRecords();
		}
		isResetColors = false; // reset to default

		if (isFlaggingActive && !QualityFlaggingController.getInstance().getActiveTimeField().equals(FlaggingPointType.RENDER.toString())) {
			EventBus.getMainEventBus().fireEvent(new UpdateTimeTextFieldEvent(mainResult.getResponseCoord()));
		}

		EventBus.getMainEventBus().fireEvent(new SetImageUrlEvent(mainResult.getImageUrl()));
		EventBus.getMainEventBus().fireEvent(new SetDataAreaPixelBoundsEvent(mainResult.getPlotArea()));

		Double left = new Double(0);
		Double right = new Double(new Date().getTime());
		Bounds maxDomainBounds = new Bounds(left, right, null, null);
		EventBus.getMainEventBus().fireEvent(new SetMaxDomainBoundsEvent(maxDomainBounds));

		SetDomainBoundsEventHandler[] blocked = { DataStoreTimeSeriesImpl.getInst().getEventBroker() };
		Double mainLeft = new Double(mainResult.getBegin() + 0.0);
		Double mainRight = new Double(mainResult.getEnd() + 0.0);
		Double mainTop = mainResult.getPlotArea().getTop();
		Double mainBottom = mainResult.getPlotArea().getBottom();
		Bounds diagramBounds = new Bounds(mainLeft, mainRight, mainTop, mainBottom);
		EventBus.getMainEventBus().fireEvent(new SetDomainBoundsEvent(diagramBounds, blocked));

		Double overviewRight = new Double(mainResult.getWidth());
		Double overviewBottom = new Double(mainResult.getHeight());
		Bounds viewportBounds = new Bounds(left, overviewRight, left, overviewBottom);
		EventBus.getMainEventBus().fireEvent(new SetViewportPixelBoundsEvent(viewportBounds));

		/*
		 * List<ImageEntity> ie = new ArrayList<ImageEntity>(); for (ImageEntity
		 * imageEntity : mainResult.getImageEntities()) { ie.add(imageEntity); }
		 */
		EventBus.getMainEventBus().fireEvent(new SetImageEntitiesEvent(mainResult.getImageEntities()));
		EventBus.getOverviewChartEventBus().fireEvent(new SetOverviewDomainBoundsEvent(diagramBounds));

		// enable browse buttons
		if (EESTab.getRightInteractionMenuButton().isDisabled()) {
			EESTab.getRightInteractionMenuButton().enable();
		}
		if (EESTab.getLeftInteractionMenuButton().isDisabled()) {
			EESTab.getLeftInteractionMenuButton().enable();
		}
		if (EESTab.getCenterInteractionMenuButton().isDisabled()) {
			EESTab.getCenterInteractionMenuButton().enable();
		}

	}

	/*
	 * private void getQCDiagram(final EESDataRequest request) throws Exception
	 * {
	 * 
	 * // prepare callback QCDataCallback callback = new QCDataCallback(this,
	 * "Could not get diagram with quality flags.") {
	 * 
	 * @Override public void onSuccess(EESDataResponse result) {
	 * removeRequest(); try { // block overview if (!GWT.isProdMode()) {
	 * GWT.log("Got Diagram: " + result.getWidth() + "w x " + result.getHeight()
	 * + "h "); GWT.log("For Viewportsize: " + EESTab.getPanelWidth() + "w x " +
	 * EESTab.getPanelHeight() + "h"); GWT.log("Image url: " +
	 * result.getImageUrl()); } for (String key : result.getAxis().keySet()) {
	 * EventBus.getMainEventBus().fireEvent(new StoreAxisDataEvent(key,
	 * result.getAxis().get(key))); }
	 * 
	 * mainImageURL = result.getImageUrl();
	 * 
	 * for (TimeSeriesProperties prop : result.getPropertiesList()) {
	 * TimeSeriesHasDataEvent hasDataEvent = new
	 * TimeSeriesHasDataEvent(prop.getTsID(), prop.hasData());
	 * EventBus.getMainEventBus().fireEvent(hasDataEvent); }
	 * 
	 * // ASD EventBus.getMainEventBus().fireEvent(new
	 * UpdateTimeTextFieldEvent(result.getResponseCoord()));
	 * 
	 * // ASD SetQualityTimeMapEvent setQualityTimeMap = new
	 * SetQualityTimeMapEvent(result.getQualityTimeMap());
	 * EventBus.getMainEventBus().fireEvent(setQualityTimeMap);
	 * 
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetImageUrlEvent(result.getImageUrl())); // inner pixel bounds = plotarea
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetDataAreaPixelBoundsEvent(result.getPlotArea()));
	 * 
	 * Double left = new Double(0); Double right = new Double(new
	 * Date().getTime()); Bounds maxDomainBounds = new Bounds(left, right, null,
	 * null); EventBus.getMainEventBus().fireEvent(new
	 * SetMaxDomainBoundsEvent(maxDomainBounds));
	 * 
	 * SetDomainBoundsEventHandler[] blocked = {
	 * DataStoreTimeSeriesImpl.getInst().getEventBroker() }; Double mainLeft =
	 * new Double(result.getBegin() + 0.0); Double mainRight = new
	 * Double(result.getEnd() + 0.0); Double mainTop =
	 * result.getPlotArea().getTop(); Double mainBottom =
	 * result.getPlotArea().getBottom(); Bounds diagramBounds = new
	 * Bounds(mainLeft, mainRight, mainTop, mainBottom);
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetDomainBoundsEvent(diagramBounds, blocked));
	 * 
	 * Double overviewRight = new Double(result.getWidth()); Double
	 * overviewBottom = new Double(result.getHeight()); Bounds viewportBounds =
	 * new Bounds(left, overviewRight, left, overviewBottom);
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetViewportPixelBoundsEvent(viewportBounds));
	 * 
	 * // tooltips List<ImageEntity> ie = new ArrayList<ImageEntity>(); for
	 * (ImageEntity imageEntity : result.getImageEntities()) {
	 * ie.add(imageEntity); } EventBus.getMainEventBus().fireEvent(new
	 * SetImageEntitiesEvent(ie));
	 * EventBus.getOverviewChartEventBus().fireEvent(new
	 * SetOverviewDomainBoundsEvent(diagramBounds)); } catch (Exception e) {
	 * ExceptionHandler.handleUnexpectedException(e); } finally {
	 * Application.setHasStarted(true); result.destroy(); result = null; } } };
	 * 
	 * // request addRequest(); mainRequestCall =
	 * this.qualityControlService.getEESQCDiagram(request, callback); }
	 */

	public void abruptEnding() {
		// mainRequestCall.cancel();
		// removeRequest();
		// overviewRequestCall.cancel();
		multipleRequestCall.cancel();
		removeRequest();

		GWT.log("SOSRequestManager - Killing all process.....................");
		TimeManager.getInst().setIsOverviewTimeFixed(false);

		EventBus.getMainEventBus().fireEvent(new FinishedLoadingTimeSeriesEvent());
		// to-Do EESTab.hideLoadingSpinner();

	}

	public void requestProcedurePositions(String sosURL, BoundingBox boundingBox, boolean isSearchByTheme) {
		try {
			// ASD
			getProcedurePositions(sosURL, boundingBox, isSearchByTheme);
		} catch (Exception e) {
			ExceptionHandler.handleException(new ServerException("could not get procedures", e));
		}
	}

	private void getProcedurePositions(String sosURL, BoundingBox boundingBox, boolean isSearchByTheme) throws Exception {
		SOSMetadata meta = DataManagerSosImpl.getInst().getServiceMetadata(sosURL);
		// ASD
		if (!isSearchByTheme) {
			EventBus.getMainEventBus().fireEvent(new DeleteMarkersEvent());
		}

		int chunkSize = meta.getRequestChunk() > 0 ? meta.getRequestChunk() : 25;
		if (meta != null) {
			// no position data available, request it
			getPositions(sosURL, 0, chunkSize, boundingBox, isSearchByTheme);
		}
	}

	void getPositions(final String sosURL, int startIdx, final int interval, final BoundingBox boundingBox, boolean isSearchByTheme) throws Exception {
		final long begin = System.currentTimeMillis();
		final boolean isDropDownSearch = isSearchByTheme;

		GetPositionsCallback callback = new GetPositionsCallback(this, "Could not get positions.") {
			@Override
			public void onSuccess(final StationPositionsResponse result) {
				try {
					removeRequest();
					if (result.isFinished()) {
						//ASD 10.11.2014
						if (isDropDownSearch) {
							//stop spinner of offeringTab - ASD 10.11.2014
							OfferingTab.getInst().setStationFinishedLoading(true);
							OfferingTab.getInst().showStationLoadingSpinner(false);
						}
						else {
							requestMgr.removeRequest(System.currentTimeMillis() - begin);
							EventBus.getMainEventBus().fireEvent(new GetProcedurePositionsFinishedEvent());
						}
						// update sensor box for a single sos and a single offering
						//OfferingTab.getInst().checkAndUpdateSensorBoxBySOSOffering(sosURL);
					} else {
						getNextChunk(sosURL, interval, boundingBox, result, isDropDownSearch);
					}

					String srs = result.getSrs();
					String url = result.getSosURL();
					List<Station> stations = result.getStations();
					// ASD
					StoreStationsEvent event = null;
					if (isDropDownSearch) {
						event = new StoreStationsEvent(url, stations, srs, true);
					} else {
						event = new StoreStationsEvent(url, stations, srs, false);
					}
					EventBus.getMainEventBus().fireEvent(event);
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
					removeRequest();
				}
			}

			private void getNextChunk(final String sosURL, final int interval, final BoundingBox boundingBox, final StationPositionsResponse result, boolean isDropDownSearch) {
				final int start = result.getEndIdx();
				try {
					// ASD
					getPositions(sosURL, start, interval, boundingBox, isDropDownSearch);
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
					removeRequest();
				}
			}
		};
		addRequest();
		this.stationPositionService.getStationPositions(sosURL, startIdx, interval, boundingBox, callback);
	}
	
	/**
	 * Request export pdf.
	 * 
	 * @param timeseries
	 *            the timeseries
	 */
	public void requestExportPDF(Collection<TimeSeries> timeseries) {
		try {
			getPDF(timeseries);
		} catch (TimeoutException e) {
			ExceptionHandler.handleException(e);
		}
	}

	/**
	 * Gets the pDF.
	 * 
	 * @param timeseries
	 *            the timeseries
	 * @throws TimeoutException
	 *             the timeout exception
	 */
	private void getPDF(Collection<TimeSeries> timeseries) throws TimeoutException {
		addRequest();
		TimeSeriesDataRequest req = createTimeSeriesDataRequest(timeseries);
		this.fileDataService.getPDF(req, new FileCallback(SOSRequestManager.this));
	}

	private TimeSeriesDataRequest createTimeSeriesDataRequest(Collection<TimeSeries> tsCollection) {
		ArrayList<TimeSeriesProperties> series = new ArrayList<TimeSeriesProperties>();
		for (TimeSeries timeSeries : tsCollection) {
			timeSeries.getProperties().setLanguage(PropertiesManager.language);
			series.add(timeSeries.getProperties());
		}
		boolean grid = DataStoreTimeSeriesImpl.getInst().isGridEnabled();
		long begin = TimeManager.getInst().getBegin();
		long end = TimeManager.getInst().getEnd();
		DesignOptions options = new DesignOptions(series, begin, end, grid);
		TimeSeriesDataRequest req = new TimeSeriesDataRequest(options);
		return req;
	}

	public void requestExportXLS(Collection<TimeSeries> timeseries) {
		try {
			getXLS(timeseries);
		} catch (TimeoutException e) {
			ExceptionHandler.handleException(e);
		}
	}

	private void getXLS(Collection<TimeSeries> timeseries) throws TimeoutException {
		addRequest();
		TimeSeriesDataRequest req = createTimeSeriesDataRequest(timeseries);

		this.fileDataService.getXLS(req, new FileCallback(SOSRequestManager.this));

	}

	public void requestExportCSV(Collection<TimeSeries> timeseries) {
		try {
			getCSV(timeseries);
		} catch (TimeoutException e) {
			ExceptionHandler.handleException(e);
		}
	}

	private void getCSV(Collection<TimeSeries> timeseries) throws TimeoutException {
		addRequest();
		TimeSeriesDataRequest req = createTimeSeriesDataRequest(timeseries);
		this.fileDataService.getCSV(req, new FileCallback(SOSRequestManager.this));
	}

	public void requestExportPDFzip(Collection<TimeSeries> timeseries) {
		try {
			getPDFzip(timeseries);
		} catch (TimeoutException e) {
			ExceptionHandler.handleException(e);
		}
	}

	private void getPDFzip(Collection<TimeSeries> timeseries) throws TimeoutException {
		addRequest();
		TimeSeriesDataRequest req = createTimeSeriesDataRequest(timeseries);

		this.fileDataService.getPDFzip(req, new FileCallback(SOSRequestManager.this));
	}

	public void requestExportXLSzip(Collection<TimeSeries> timeseries) {
		addRequest();
		TimeSeriesDataRequest req = createTimeSeriesDataRequest(timeseries);
		this.fileDataService.getXLSzip(req, new FileCallback(SOSRequestManager.this));
	}

	public void requestExportCSVzip(Collection<TimeSeries> timeseries) {
		addRequest();
		TimeSeriesDataRequest req = createTimeSeriesDataRequest(timeseries);
		this.fileDataService.getCSVzip(req, new FileCallback(SOSRequestManager.this));
	}

	public void requestExportPDFallInOne(Collection<TimeSeries> timeseries) {
		try {
			getPDF(timeseries);
		} catch (TimeoutException e) {
			ExceptionHandler.handleException(e);
		}
	}

	public void requestPhenomenons(String sosURL) {

		// prepare callback
		GetPhenomenaCallback callback = new GetPhenomenaCallback(this, "Could not request phenomena.") {
			@Override
			public void onSuccess(GetPhenomenonResponse result) {
				removeRequest();
				try {
					String url = result.getSosURL();

					HashMap<String, String> phenomenons = result.getPhenomenons();
					StorePhenomenaEvent event = new StorePhenomenaEvent(url, null, phenomenons);
					EventBus.getMainEventBus().fireEvent(event);
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				}
			}
		};

		// request
		addRequest();
		this.serviceMetadataService.getPhen4SOS(sosURL, callback);
	}

	public void requestProcedureDetailsUrl(String serviceURL, String procedure) {

		// prepare callback
		GetProcedureDetailsUrlCallback callback = new GetProcedureDetailsUrlCallback(this, "Could not get procedure details url") {

			@Override
			public void onSuccess(GetProcedureDetailsUrlResponse result) {
				removeRequest();
				try {
					String url = result.getUrl();
					StoreProcedureDetailsUrlEvent event = new StoreProcedureDetailsUrlEvent(url);
					EventBus.getMainEventBus().fireEvent(event);
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				}
			}
		};

		// request
		addRequest();
		this.sensorMetadataService.getProcedureDetailsUrl(serviceURL, procedure, callback);
	}

	public void requestProcedure(String serviceURL, String procedureID) {
		GetProcedureCallback callback = new GetProcedureCallback(this, "Could not get the procedure with ID: " + procedureID) {

			@Override
			public void onSuccess(GetProcedureResponse result) {
				removeRequest();
				try {
					StoreProcedureEvent event = new StoreProcedureEvent(result.getServiceURL(), result.getProcedure());
					EventBus.getMainEventBus().fireEvent(event);
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				}
			}
		};

		addRequest();
		this.serviceMetadataService.getProcedure(serviceURL, procedureID, callback);
	}

	// ASD
	public void requestAllOffering(String serviceURL, String offeringID) {
		GetOfferingCallback callback = new GetOfferingCallback(this, "[RequestAllOffering] Could not get the offering with ID: " + offeringID) {
			@Override
			public void onSuccess(GetOfferingResponse result) {
				removeRequest();
				try {
					// update offering list @ OfferingTabController
					OfferingTab.getInst().getController().getAllOfferingListBySOS().put(result.getServiceURL(), result.getOfferingList());
					OfferingTab.getInst().checkAndUpdateOfferingBox(result.getServiceURL(), result.getOfferingList());
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				}
			}
		};

		addRequest();
		this.serviceMetadataService.getAllOffering(serviceURL, callback);
	}

	public void requestOffering(String serviceURL, String offeringID) {
		GetOfferingCallback callback = new GetOfferingCallback(this, "Could not get the offering with ID: " + offeringID) {
			@Override
			public void onSuccess(GetOfferingResponse result) {
				removeRequest();
				try {
					StoreOfferingEvent event = new StoreOfferingEvent(result.getServiceURL(), result.getOffering());
					EventBus.getMainEventBus().fireEvent(event);
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				}
			}
		};
		addRequest();
		this.serviceMetadataService.getOffering(serviceURL, offeringID, callback);
	}

	public void requestFeature(String serviceURL, String featureID) {
		GetFeatureCallback callback = new GetFeatureCallback(this, "Could not get the feature with ID: " + featureID) {

			@Override
			public void onSuccess(GetFeatureResponse result) {
				removeRequest();
				try {
					StoreFeatureEvent event = new StoreFeatureEvent(result.getServiceURL(), result.getFeature());
					EventBus.getMainEventBus().fireEvent(event);
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				}
			}
		};

		addRequest();
		this.serviceMetadataService.getFeature(serviceURL, featureID, callback);
	}

	public void requestStation(String serviceURL, String offeringID, String procedureID, String phenomenonID, String featureID) {

		GetStationCallback callback = new GetStationCallback(this, "Could not get the station") {

			@Override
			public void onSuccess(GetStationResponse result) {
				removeRequest();
				try {
					StoreStationEvent event = new StoreStationEvent(result.getServiceURL(), result.getStation());
					EventBus.getMainEventBus().fireEvent(event);
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				}

			}
		};

		addRequest();
		this.serviceMetadataService.getStation(serviceURL, offeringID, procedureID, phenomenonID, featureID, callback);
	}

	// ASD : Get a list of qualifiers
	public void requestQualityCode()  {
		GetQualityCodeCallback callback = new GetQualityCodeCallback(this, "Could not get the default quality flags and shapes.") {
			@Override
			public void onSuccess(QCDataResponse result) {
				removeRequest();
				try {
					if (result.getSourceId() != null) {
						Toaster.getInstance().addMessage("Source Info :" + result.getSourceCode() + " " + result.getEmail());
					} else {
						Toaster.getInstance().addMessage("Source Info : Unknown User. Data flagging is disabled!");
					}
					StoreFlagCodeEvent event = new StoreFlagCodeEvent(result);
					EventBus.getMainEventBus().fireEvent(event);
				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				GWT.log("SOSRequestManager - RequestQualityCode: " + caught.getMessage());
			}
		};
		addRequest();
		
		//ASD 17.11.2014
		PropertiesManager prop = PropertiesManager.getInstance();
		String url = prop.getParameterAsString("wpsUrl");
		String user = prop.getParameterAsString("wpsUsername");
		String pwd = prop.getParameterAsString("wpsPwd");
		String authenticate = prop.getParameterAsString("authenticateUrl");
		QCOptions options = new QCOptions(url,user,pwd,authenticate);
		this.qualityControlService.getQualityCode(new QCDataRequest(options),callback);

	}

	public boolean isRequestDiagramRunning() {
		return isRequestDiagramRunning;
	}

	public void setRequestDiagramRunning(boolean isRequestDiagramRunning) {
		isRequestDiagramRunning = isRequestDiagramRunning;
	}

	public static ArrayList<String> getColorMappings() {
		return colorMappings;
	}

	public static void setColorMappings(ArrayList<String> colorMappings) {
		SOSRequestManager.colorMappings = colorMappings;
	}

	public boolean isChangeStyleActive() {
		return isChangeStyleActive;
	}

	public void setChangeStyleActive(boolean isChangeStyleActive) {
		this.isChangeStyleActive = isChangeStyleActive;
	}

	public String getMainImageURL() {
		return mainImageURL;
	}

	public void setMainImageURL(String mainImageURL) {
		this.mainImageURL = mainImageURL;
	}

	public static int getCOLOR_MAPPING_SIZE() {
		return COLOR_MAPPING_SIZE;
	}

	public static void setCOLOR_MAPPING_SIZE(int cOLOR_MAPPING_SIZE) {
		COLOR_MAPPING_SIZE = cOLOR_MAPPING_SIZE;
	}

	public static boolean isResetColors() {
		return isResetColors;
	}

	public static void setResetColors(boolean isResetColors) {
		SOSRequestManager.isResetColors = isResetColors;
	}

}

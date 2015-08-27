package org.n52.client.sos.ctrl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eesgmbh.gimv.client.event.LoadImageDataEvent;
import org.n52.client.bus.EventBus;
import org.n52.client.ctrl.ExceptionHandler;
import org.n52.client.ctrl.RequestManager;
import org.n52.client.ctrl.callbacks.AddQualityFlagCallback;
import org.n52.client.ctrl.callbacks.ModifyFlagShapesCallback;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.event.LegendElementSelectedEvent;
import org.n52.client.sos.event.data.AddFlagEvent;
import org.n52.client.sos.event.data.ClearSelectedPointEvent;
import org.n52.client.sos.event.data.CustomRenderingEvent;
import org.n52.client.sos.event.data.DeleteAllTimeSeriesEvent;
import org.n52.client.sos.event.data.DeleteTimeSeriesEvent;
import org.n52.client.sos.event.data.FilterXYCoordEvent;
import org.n52.client.sos.event.data.FinishedAddFlagEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent;
import org.n52.client.sos.event.data.SetQualityControlStatusEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent.FlaggingPointType;
import org.n52.client.sos.event.data.SetQualityTimeMapEvent;
import org.n52.client.sos.event.data.StoreFlagCodeEvent;
import org.n52.client.sos.event.data.UpdateTimeTextFieldEvent;
import org.n52.client.sos.event.data.handler.AddFlagEventHandler;
import org.n52.client.sos.event.data.handler.ClearSelectedPointEventHandler;
import org.n52.client.sos.event.data.handler.CustomRenderingEventHandler;
import org.n52.client.sos.event.data.handler.DeleteAllTimeSeriesEventHandler;
import org.n52.client.sos.event.data.handler.DeleteTimeSeriesEventHandler;
import org.n52.client.sos.event.data.handler.FilterXYCoordEventHandler;
import org.n52.client.sos.event.data.handler.SetQCTimeRangeEventHandler;
import org.n52.client.sos.event.data.handler.SetQualityControlStatusEventHandler;
import org.n52.client.sos.event.data.handler.SetQualityTimeMapEventHandler;
import org.n52.client.sos.event.data.handler.StoreFlagCodeEventHandler;
import org.n52.client.sos.event.data.handler.UpdateTimeTextFieldEventHandler;
import org.n52.client.sos.event.handler.LegendElementSelectedEventHandler;
import org.n52.client.sos.legend.TimeSeries;
import org.n52.client.sos.ui.EESTab;
import org.n52.client.sos.ui.FilteringTab;
import org.n52.client.ui.MultiPointFlagger;
import org.n52.client.ui.QualityFlagger;
import org.n52.client.ui.QualityFlaggerByView;
import org.n52.shared.requests.QCDataRequest;
import org.n52.shared.responses.QCDataResponse;
import org.n52.shared.serializable.pojos.QCOptions;
import org.n52.shared.service.rpc.RpcQualityControlService;
import org.n52.shared.service.rpc.RpcQualityControlServiceAsync;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class QualityFlaggingController extends RequestManager {
	private static QualityFlaggingController instance;
	private boolean isAddFlagRunning = false;
	private Map<String, Map<Double, List<String>>> timeQualityMap;
	private RpcQualityControlServiceAsync qualityControlService;
	private String selectedTimeSeriesId;
	private Map<String, ArrayList<String>> allowedSitePropertiesList = null;;
	private String activeTimeField = "";
	private static LinkedHashMap<String, ArrayList<String>> renderTimePoint = new LinkedHashMap<String, ArrayList<String>>();
	private boolean isQualityControlSelected = false;

	private Canvas responseTextSelectedTab = null;
	private Long sourceId;
	private String email;
	// private Collection<String> notAllowedSeries = null;
	private static Collection<TimeSeries> allowedSeries = null;

	public static QualityFlaggingController getInstance() {
		if (instance == null) {
			instance = new QualityFlaggingController();
		}
		return instance;
	}

	private QualityFlaggingController() {
		this.qualityControlService = GWT.create(RpcQualityControlService.class);
		new QualityFlaggingControllerEventBroker();
	}

	private String getTimeSeriesIdBySensorProp(String sensor, String prop) {
		TimeSeries[] timeSeries = DataStoreTimeSeriesImpl.getInst().getTimeSeriesSorted();
		String tId = null;
		for (int i = 0; i < timeSeries.length; i++) {
			TimeSeries ts = timeSeries[i];
			if (ts.getPhenomenonId().equals(prop) && ts.getProcedureId().equals(sensor)) {
				tId = ts.getId();
				break;
			}
		}
		return tId;
	}

	private TimeSeries getTimeSeriesBySensorProp(String sensor, String prop) {
		TimeSeries[] timeSeries = DataStoreTimeSeriesImpl.getInst().getTimeSeriesSorted();
		TimeSeries tId = null;
		for (int i = 0; i < timeSeries.length; i++) {
			TimeSeries ts = timeSeries[i];
			if (ts.getPhenomenonId().equals(prop) && ts.getProcedureId().equals(sensor)) {
				tId = ts;
				break;
			}
		}
		return tId;
	}

	private class QualityFlaggingControllerEventBroker implements DeleteAllTimeSeriesEventHandler, SetQualityTimeMapEventHandler, DeleteTimeSeriesEventHandler, StoreFlagCodeEventHandler, ClearSelectedPointEventHandler, SetQCTimeRangeEventHandler, FilterXYCoordEventHandler, LegendElementSelectedEventHandler, AddFlagEventHandler,
			UpdateTimeTextFieldEventHandler, SetQualityControlStatusEventHandler, CustomRenderingEventHandler {

		QualityFlaggingControllerEventBroker() {
			EventBus.getMainEventBus().addHandler(SetQualityTimeMapEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(SetQCTimeRangeEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(FilterXYCoordEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(LegendElementSelectedEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(AddFlagEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(UpdateTimeTextFieldEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(SetQualityControlStatusEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(CustomRenderingEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(ClearSelectedPointEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(StoreFlagCodeEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(DeleteTimeSeriesEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(DeleteAllTimeSeriesEvent.TYPE, this);
		}

		@Override
		public void onSetQualityTimeMap(SetQualityTimeMapEvent evt) {
			// TODO Auto-generated method stub
			timeQualityMap = evt.getQualityTimeMap();
		}

		@Override
		public void onSetQCTimeRange(SetQCTimeRangeEvent evt) {
			String type = evt.getTimePointType().toString();
			if (type.equals(FlaggingPointType.MULTIPOINT.toString())) {
				activeTimeField = type;
			}
			if (type.equals(FlaggingPointType.TIMERANGE.toString())) {
				// can be start or end
				activeTimeField = QualityFlagger.getInst().getRadioGroupItem().getValueAsString();
			}
			if (type.equals(FlaggingPointType.VIEW.toString())) {
				activeTimeField = type;
			}
			if (type.equals(FlaggingPointType.RENDER.toString())) {
				activeTimeField = type;
			}
		}

		@Override
		public void onFilterXYCoord(FilterXYCoordEvent evt) {
			// TODO Auto-generated method stub
			int xScreen = evt.getX();
			int yScreen = evt.getY();

			boolean isAllowed = false;
			ArrayList<TimeSeries> ts = new ArrayList<TimeSeries>(allowedSeries);
			for (int i = 0; i < ts.size(); i++) {
				String seriesId = ts.get(i).getId();
				if (seriesId.equalsIgnoreCase(getSelectedTimeSeriesId())) {
					isAllowed = true;
					break;
				}
			}

			if (isQualityControlSelected) {
				if (activeTimeField.equals(FlaggingPointType.MULTIPOINT.toString()) || activeTimeField.equals(FlaggingPointType.START.toString()) || activeTimeField.equals(FlaggingPointType.END.toString())) {
					if (!isAllowed) {
						TimeSeries t = getTimeSeriesProperties(getSelectedTimeSeriesId());
						if (activeTimeField.equals(FlaggingPointType.MULTIPOINT.toString())) {
							responseTextSelectedTab = MultiPointFlagger.getInst().getResponseTextItem();
							if (MultiPointFlagger.getInst().getExportLoadingSpinner().isVisible()) {
								MultiPointFlagger.getInst().getExportLoadingSpinner().hide();
							}
						}
						if (activeTimeField.equals(FlaggingPointType.START.toString()) || activeTimeField.equals(FlaggingPointType.END.toString())) {
							responseTextSelectedTab = QualityFlagger.getInst().getResponseTextItem();
						}
						updateFlaggingResponseMessage(QCResponseCode.FLAGGINGNOTALLOWED, t.getPhenomenonId() + " @ " + t.getProcedureId());
					} else {
						compareCoordinates(xScreen, yScreen, evt);
					}
				}
			} else {
				renderTimePoint.clear();
			}
		}

		@Override
		public void onSelected(LegendElementSelectedEvent evt) {
			// TODO Auto-generated method stub
			if (evt.getElement().getDataWrapper() instanceof TimeSeries) {
				TimeSeries ts = (TimeSeries) evt.getElement().getDataWrapper();
				setSelectedTimeSeries(ts.getId());
			}

		}

		@Override
		public void onAddFlag(AddFlagEvent evt) {
			if (!allowedSitePropertiesList.isEmpty()) {
				QCOptions options = null;
				boolean isAddingByOverwrite = evt.isOverwrite();
				boolean isOffline = evt.isOffline();
				boolean isByRule = evt.isByRule();
				// boolean isRestOK = false;
				if (activeTimeField.equals(FlaggingPointType.VIEW.toString())) {
					long st = QualityFlaggerByView.getInst().getBegin();
					String startView = QualityFlaggingController.this.convertTime(st);
					long ed = QualityFlaggerByView.getInst().getEnd();
					String endView = QualityFlaggingController.this.convertTime(ed);

					responseTextSelectedTab = QualityFlaggerByView.getInst().getResponseTextItem();
					String genericFlag = QualityFlaggerByView.getInst().getQualityFlag().getValue().toString();
					String specificFlag = QualityFlaggerByView.getInst().getFlagSpecificSelect().getValue().toString();

					HashMap<String, String> dataMap = null;
					if (!isByRule) {
						ListGridRecord[] records = QualityFlaggerByView.getInst().getMainGrid().getSelectedRecords();
						dataMap = getPermittedSitesAndProperties(records, activeTimeField);
						options = new QCOptions(FlaggingPointType.VIEW.toString(), sourceId, email, startView, endView, dataMap, genericFlag, specificFlag);
						// GWT.log("QualityFlaggingController - Adding flags by view mode :"
						// + options.printViewRequest());
					} else {
						String[] tseries = (QualityFlaggerByView.getInst().getRulePropertyBox().getValueAsString()).split(":");
						String operator = QualityFlaggerByView.getInst().getRuleOperatorBox().getValueAsString().trim();
						double value = Double.parseDouble(QualityFlaggerByView.getInst().getRuleBasedValue().getValueAsString());
						String s = tseries[0].trim();
						String p = tseries[1].trim();
						TimeSeries series = getTimeSeriesBySensorProp(s, p);

						options = new QCOptions(FlaggingPointType.RULE.toString(), sourceId, email, series.getId(), s, p, startView, endView, genericFlag, specificFlag, value, operator);
						// GWT.log("QualityFlaggingController - Adding flags by view mode :"
						// + options.printRuleRequest());
					}
					QualityFlaggerByView.getInst().getExportLoadingSpinner().show();

				} else if (activeTimeField.equals(FlaggingPointType.START.toString()) || activeTimeField.equals(FlaggingPointType.END.toString())) {
					responseTextSelectedTab = QualityFlagger.getInst().getResponseTextItem();
					String startRange = QualityFlagger.getInst().getStartTextITem().getValue().toString();
					String endRange = QualityFlagger.getInst().getEndTextITem().getValue().toString();
					String genericFlag = QualityFlagger.getInst().getQualityFlag().getValue().toString();
					String specificFlag = QualityFlagger.getInst().getFlagSpecificSelect().getValue().toString();
					ListGridRecord[] records = QualityFlagger.getInst().getMainGrid().getSelectedRecords();
					HashMap<String, String> dataMap = getPermittedSitesAndProperties(records, activeTimeField);
					QualityFlagger.getInst().getExportLoadingSpinner().show();
					options = new QCOptions(FlaggingPointType.TIMERANGE.toString(), sourceId, email, startRange, endRange, dataMap, genericFlag, specificFlag);
					// GWT.log("QualityFlaggingController - Adding flags by time range :"
					// + options.printRangeRequest());

				} else if (activeTimeField.equals(FlaggingPointType.MULTIPOINT.toString())) {
					responseTextSelectedTab = MultiPointFlagger.getInst().getResponseTextItem();
					ListGridRecord[] clickedRecords = MultiPointFlagger.getInst().getDataGrid().getRecords();
					HashMap<String, String> dataMapGrid = getPermittedSitesAndProperties(clickedRecords, activeTimeField);
					MultiPointFlagger.getInst().getExportLoadingSpinner().show();
					options = new QCOptions(FlaggingPointType.MULTIPOINT.toString(), sourceId, email, dataMapGrid);
					// GWT.log("QualityFlaggingController - Adding flags by multipoints :"
					// + options.prinMultiPointsRequest());
				}

				if (isAddingByOverwrite) {
					try {
						if (isOffline) {
							modifyQualityOffline(new QCDataRequest(options));
						} else {
							modifyQuality(new QCDataRequest(options));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					try {
						if (isOffline) {
							addQualityOffline(new QCDataRequest(options));
						} else {
							addQuality(new QCDataRequest(options));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				updateFlaggingResponseMessage(QCResponseCode.FLAGGINGNOTAUTHORIZED, "");
			}
		}

		@Override
		public void onUpdateTextFields(UpdateTimeTextFieldEvent evt) {
			boolean isMultiplePoints = false;
			Map<String, String> responseFields = evt.getResponseFields();
			Iterator it = responseFields.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				String type = (String) pairs.getKey();
				String date = (String) pairs.getValue();

				if (type.contains(FlaggingPointType.START.toString())) {
					QualityFlagger.getInst().getStartTextITem().setValue(date);
				} else if (type.contains(FlaggingPointType.END.toString())) {
					QualityFlagger.getInst().getEndTextITem().setValue(date);
				} else if (type.contains("#")) {
					isMultiplePoints = true;
					break;
				}
			}
			if (isMultiplePoints) {
				MultiPointFlagger.getInst().updateDataTable(responseFields);
			}
		}

		@Override
		public void onSetQualityControlStatus(SetQualityControlStatusEvent evt) {
			// TODO Auto-generated method stub
			QualityFlaggingController.this.isQualityControlSelected = evt.getQCStatus();
		}

		@Override
		public void onCustomRendering(CustomRenderingEvent customRenderingEvent) {
			// TODO Auto-generated method stub
			HashMap<String, String> renderMap = new HashMap<String, String>();
			ListGridRecord[] records = customRenderingEvent.getSeriesSymbolsPairs();
			for (int i = 0; i < records.length; i++) {
				Record record = records[i];
				String flag = record.getAttribute("flag");
				String shape = record.getAttribute("symbol");
				renderMap.put(flag, shape);
			}

			QCOptions options = new QCOptions(renderMap);
			try {
				// update tab
				DataManagerSosImpl.getInst().setActiveFlagsAndShapes(renderMap);
				modifyFlagShapes(new QCDataRequest(options));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public void onClearSelectedPoint(ClearSelectedPointEvent evt) {
			// TODO Auto-generated method stub
			clearTimeMap();
		}

		@Override
		public void onStore(StoreFlagCodeEvent evt) {
			sourceId = evt.getSourceId();
			email = evt.getEmail();
			allowedSitePropertiesList = evt.getAllowedSitesList();
		}

		@Override
		public void onDeleteTimeSeries(DeleteTimeSeriesEvent evt) {
			// TODO Auto-generated method stub
			String id = evt.getId();
			Iterator<Map.Entry<String, ArrayList<String>>> iter = renderTimePoint.entrySet().iterator();
			if (activeTimeField.equals(FlaggingPointType.MULTIPOINT.toString())) {
				while (iter.hasNext()) {
					Map.Entry<String, ArrayList<String>> entry = iter.next();
					String[] keys = entry.getKey().split("#");
					if (keys[0].equals(id)) {
						iter.remove();
					}
				}
			} else {
				while (iter.hasNext()) {
					Map.Entry<String, ArrayList<String>> entry = iter.next();
					if (entry.getKey().contains(id)) {
						iter.remove();
						// update texboxes
						if (entry.getKey().contains(FlaggingPointType.START.toString())) {
							QualityFlagger.getInst().getStartTextITem().setValue("");
						}
						if (entry.getKey().contains(FlaggingPointType.END.toString())) {
							QualityFlagger.getInst().getEndTextITem().setValue("");
						}
					}
				}
			}
		}

		@Override
		public void onDeleteAllTimeSeries(DeleteAllTimeSeriesEvent evt) {
			renderTimePoint.clear();
		}

	}

	public void approveDataSeries(boolean isOffline, String st, String ed, ListGridRecord[] records) {
		if (!allowedSitePropertiesList.isEmpty()) {
			if (activeTimeField.equals(FlaggingPointType.START.toString()) || activeTimeField.equals(FlaggingPointType.END.toString())) {
				responseTextSelectedTab = QualityFlagger.getInst().getResponseTextItem();
			}
			if (activeTimeField.equals(FlaggingPointType.VIEW.toString())) {
				responseTextSelectedTab = QualityFlaggerByView.getInst().getResponseTextItem();
			}
			HashMap<String, String> dataMap = null;
			dataMap = getPermittedSitesAndProperties(records, activeTimeField);
			QCOptions options = new QCOptions(FlaggingPointType.APPROVE.toString(), sourceId, email, st, ed, dataMap);
			try {
				if (isOffline) {
					approveOffline(new QCDataRequest(options));
				} else {
					approveOnline(new QCDataRequest(options));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			updateFlaggingResponseMessage(QCResponseCode.FLAGGINGNOTAUTHORIZED, "");
		}

	}

	public Collection<TimeSeries> filterPermittedSitesAndProperties() {
		TimeSeries[] currentSeries = DataStoreTimeSeriesImpl.getInst().getTimeSeriesSorted();
		Collection<TimeSeries> tempListExists = new ArrayList<TimeSeries>();
		for (int i = 0; i < currentSeries.length; i++) {
			TimeSeries ts = currentSeries[i];
			String sensor = ts.getProcedureId().toString();
			String property = ts.getPhenomenonId().toString();
			if (this.allowedSitePropertiesList.get(sensor) != null) {
				ArrayList<String> tempProp = this.allowedSitePropertiesList.get(sensor);
				for (int a = 0; a < tempProp.size(); a++) {
					if (tempProp.get(a).equals(property)) {
						TimeSeries id = getTimeSeriesBySensorProp(sensor, property);
						tempListExists.add(id);
					}
				}
			}
		}

		// HashSet hs = new HashSet();
		// hs.addAll(tempListExists);
		// tempListExists.clear();
		// tempListExists.addAll(hs);
		allowedSeries = tempListExists;
		return tempListExists;
	}

	private HashMap<String, String> getPermittedSitesAndProperties(ListGridRecord[] records, String flagMode) {
		HashMap<String, String> dataMap = new HashMap<String, String>();
		for (int i = 0; i < records.length; i++) {
			ListGridRecord record = records[i];
			String series = record.getAttributeAsString("ts");
			String sensor = record.getAttributeAsString("sensor");
			String property = record.getAttributeAsString("property");
			String date = "";
			String flags = "";
			String[] flgArray = null;
			String val = "";
			if (flagMode.equals(FlaggingPointType.MULTIPOINT.toString())) {
				date = record.getAttributeAsString("date");
				series = record.getAttributeAsString("ts") + "#" + date;
				flags = record.getAttributeAsString("flag");
				flgArray = flags.split(",");
				val = sensor + "#" + property + "#" + flgArray[0].trim() + "#" + flgArray[1].trim();
			} else {
				val = sensor + "#" + property;
			}
			dataMap.put(series, val);
		}
		return dataMap;
	}

	private void clearTimeMap() {
		renderTimePoint.clear();
	}

	public void setSelectedTimeSeries(String timeSeriesId) {
		this.selectedTimeSeriesId = timeSeriesId;
	}

	public String getSelectedTimeSeriesId() {
		return selectedTimeSeriesId;
	}

	private void compareCoordinates(int xCoord, int yCoord, FilterXYCoordEvent evt) {
		Map<Double, List<String>> xySubCoordMap = null;
		xySubCoordMap = timeQualityMap.get(this.getSelectedTimeSeriesId());
		if (xySubCoordMap != null) {
			if (activeTimeField.equals(FlaggingPointType.MULTIPOINT.toString())) {
				if (!MultiPointFlagger.getInst().getExportLoadingSpinner().isVisible()) {
					MultiPointFlagger.getInst().getExportLoadingSpinner().show();
				}
			}
			List<Double> xCoordList = new ArrayList<Double>(xySubCoordMap.keySet());
			// Number you want to find in array
			double searchFor = xCoord;
			// Nearest lower you searching for
			double nearestLower = Integer.MIN_VALUE;
			// Nearest upper you searching for
			double nearestUpper = Integer.MAX_VALUE;
			double searchForExist = -1;
			for (int i = 0; i < xCoordList.size(); i++) {
				double j = xCoordList.get(i);
				if (j < searchFor) {
					if (j > nearestLower) {
						nearestLower = j;
					}
				} else if (j > searchFor) {
					if (j < nearestUpper) {
						nearestUpper = j;
					}
				} else {
					nearestLower = -1;
					nearestUpper = -1;
					searchForExist = j;
					break;
				}
			}

			double finalNumber = 0;
			if ((searchForExist == -1.0))
			// find the most nearest value
			{
				Map<Double, Double> tempDistanceMap = new HashMap<Double, Double>();
				int indexNearestLower = xCoordList.indexOf(nearestLower);
				int indexNearestUpper = xCoordList.indexOf(nearestUpper);
				//10.11.2014 ASD
				int mn =2;
				int mx =2;
				if(indexNearestLower < 2) {
					mn = indexNearestLower;
				}
				if(indexNearestUpper > (xCoordList.size() -3)) {
					mx = xCoordList.size()-indexNearestUpper;
				}
				
				// get coordinates of x -2
				for (int i = indexNearestLower; i > indexNearestLower - mn; i -= 1) {
					double xNeighbor = xCoordList.get(i);
					if(xCoordList.get(i)!=null)
					{
						ArrayList<String> yList = (ArrayList<String>) xySubCoordMap.get(xNeighbor);
						double yNeighbor = Double.parseDouble(yList.get(2));
						double distance = calculateDistance(xCoord, yCoord, xNeighbor, yNeighbor);			
						tempDistanceMap.put(xNeighbor, distance);
					}
				}

				// get coordinates of x +2
				for (int i = indexNearestUpper; i < indexNearestUpper + mx; i += 1) {
					if(i != -1)
					{
						double xNeighbor = xCoordList.get(i);
						if(xCoordList.get(i)!=null)
						{
							ArrayList<String> yList = (ArrayList<String>) xySubCoordMap.get(xNeighbor);
							double yNeighbor = Double.parseDouble(yList.get(2));
							double distance = calculateDistance(xCoord, yCoord, xNeighbor, yNeighbor);
							tempDistanceMap.put(xNeighbor, distance);
						}
					}
				}
				// get the min value of the distance map
				Entry<Double, Double> min = null;
				for (Entry<Double, Double> entry : tempDistanceMap.entrySet()) {
					if (min == null || min.getValue() > entry.getValue()) {
						min = entry;
					}
				}
				finalNumber = min.getKey();
			} else {
				finalNumber = searchForExist;
			}
			Object newValue = xySubCoordMap.get(finalNumber);

			if (newValue != null) {
				ArrayList<String> yNewValue = (ArrayList<String>) newValue;
				String t = yNewValue.get(0);
				String v = yNewValue.get(1);
				ArrayList<String> selectedCoordPair = new ArrayList<String>();
				selectedCoordPair.add(t);
				selectedCoordPair.add(v);
				if (this.activeTimeField.equals(FlaggingPointType.MULTIPOINT.toString())) {
					selectedCoordPair.add(evt.getGenericFlg());
					selectedCoordPair.add(evt.getSpecificFlg());
					renderTimePoint.put(this.getSelectedTimeSeriesId() + "#" + t, selectedCoordPair);
				} else {
					renderTimePoint.put(this.activeTimeField + "$" + this.getSelectedTimeSeriesId(), selectedCoordPair);
				}
				EESTab.showLoadingSpinner();
				EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
			} else {
				if (activeTimeField.equals(FlaggingPointType.MULTIPOINT.toString())) {
					responseTextSelectedTab = MultiPointFlagger.getInst().getResponseTextItem();
					if (MultiPointFlagger.getInst().getExportLoadingSpinner().isVisible()) {
						MultiPointFlagger.getInst().getExportLoadingSpinner().hide();
					}
				}
				if (activeTimeField.equals(FlaggingPointType.START.toString()) || activeTimeField.equals(FlaggingPointType.END.toString())) {
					responseTextSelectedTab = QualityFlagger.getInst().getResponseTextItem();
				}
				updateFlaggingResponseMessage(QCResponseCode.COORDINATENOTDETECTED, "");
				// GWT.log("QualityFlaggingController - compareCoordinates - coordinate not found:");
			}
		}
	}

	public double calculateDistance(double x1, double y1, double x2, double y2) {
		double dx = Math.abs(x2 - x1);
		double dy = Math.abs(y2 - y1);
		double distance = Math.sqrt((dx * dx) + (dy * dy));
		return distance;
	}

	protected Map<String, Map<Double, List<String>>> getQualityTimeMap() {
		return this.timeQualityMap;
	}

	protected LinkedHashMap<String, ArrayList<String>> getRenderingPoints() {
		return renderTimePoint;
	}

	public TimeSeries getTimeSeriesProperties() {
		DataStoreTimeSeriesImpl dataManager = DataStoreTimeSeriesImpl.getInst();
		TimeSeries ts = dataManager.getDataItem(this.getSelectedTimeSeriesId());
		return ts;
	}

	public TimeSeries getTimeSeriesProperties(String id) {
		DataStoreTimeSeriesImpl dataManager = DataStoreTimeSeriesImpl.getInst();
		TimeSeries ts = dataManager.getDataItem(id);
		return ts;
	}

	private void modifyFlagShapes(QCDataRequest qcDataRequest) throws Exception {
		// prepare callback

		ModifyFlagShapesCallback callback = new ModifyFlagShapesCallback(this, "Could not modify the shapes of flags.") {
			@Override
			public void onSuccess(QCDataResponse result) {
				removeRequest();
				// GWT.log("QualityFlaggingController - Shapes are successfully updated!:");
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				try {
					// update status message
					FilteringTab.getInst().updateReponseTextItem(QCResponseCode.SHAPESUPDATED);
					Timer timer = new Timer() {
						@Override
						public void run() {
							FilteringTab.getInst().clearReponseTextItem();
						}

					};
					timer.schedule(4000);
					EESTab.showLoadingSpinner();
					EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());

				} catch (Exception e) {
					ExceptionHandler.handleUnexpectedException(e);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				// GWT.log("QualityFlaggingController - Modifying flags :" +
				// caught.getMessage());
				FilteringTab.getInst().updateReponseTextItem(QCResponseCode.SHAPESNOTUPDATED);
				Timer timer = new Timer() {
					@Override
					public void run() {
						FilteringTab.getInst().clearReponseTextItem();
					}
				};
				timer.schedule(4000);
			}
		};
		addRequest();
		this.qualityControlService.modifyFlagShapes(qcDataRequest, callback);
	}

	private void updateFlaggingResponseMessage(QCResponseCode code, String additionalTxt) {
		responseTextSelectedTab.setVisible(true);
		responseTextSelectedTab.setContents("<span style='color:" + code.getColor() + ";'>" + code.getResponseMessage() + "<br> " + additionalTxt + "</span>");
		Timer timer = new Timer() {
			@Override
			public void run() {
				responseTextSelectedTab.setVisible(false);
				responseTextSelectedTab.setContents("");
			}
		};
		timer.schedule(4000);
	}

	private String addQuality(final QCDataRequest req) throws Exception {
		// prepare callback
		String status = "";
		this.isAddFlagRunning = true;
		AddQualityFlagCallback callback = new AddQualityFlagCallback(this, "Could not add flagging information.") {
			@Override
			public void onSuccess(QCDataResponse result) {
				// clear clicked point
				isAddFlagRunning = false;
				EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
				EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				updateFlaggingResponseMessage(QCResponseCode.FLAGADDED, "");
				String flaggingMode = req.getOptions().getFlaggingType();
				if (flaggingMode.equals(FlaggingPointType.MULTIPOINT.toString())) {
					MultiPointFlagger.getInst().clearLocalTabValues();
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				isAddFlagRunning = false;
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				updateFlaggingResponseMessage(QCResponseCode.FLAGGINGFAILED, "");
			}
		};
		addRequest();
		this.qualityControlService.addQualityFlag(req, callback);
		return status;

	}

	private String addQualityOffline(QCDataRequest req) throws Exception {
		// prepare callback
		String status = "";
		this.isAddFlagRunning = true;
		AddQualityFlagCallback callback = new AddQualityFlagCallback(this, "Could not add flagging information.") {
			@Override
			public void onSuccess(QCDataResponse result) {
				isAddFlagRunning = false;
				EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				updateFlaggingResponseMessage(QCResponseCode.FLAGADDEDOFFLINE, "");
			}

			@Override
			public void onFailure(Throwable caught) {
				isAddFlagRunning = false;
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				updateFlaggingResponseMessage(QCResponseCode.FLAGADDEDOFFLINE, "");
			}
		};
		addRequest();
		this.qualityControlService.addQualityFlagWPS(req, callback);
		return status;

	}

	private String modifyQuality(final QCDataRequest req) throws Exception {
		// prepare callback
		String status = "";
		this.isAddFlagRunning = true;
		AddQualityFlagCallback callback = new AddQualityFlagCallback(this, "Could not add flagging information.") {
			@Override
			public void onSuccess(QCDataResponse result) {
				isAddFlagRunning = false;
				EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
				EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				updateFlaggingResponseMessage(QCResponseCode.FLAGADDED, "");
				String flaggingMode = req.getOptions().getFlaggingType();
				if (flaggingMode.equals(FlaggingPointType.MULTIPOINT.toString())) {
					MultiPointFlagger.getInst().clearLocalTabValues();
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				isAddFlagRunning = false;
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				updateFlaggingResponseMessage(QCResponseCode.FLAGGINGFAILED, "");
			}
		};
		addRequest();
		this.qualityControlService.modifyQualityFlag(req, callback);
		return status;

	}

	private String modifyQualityOffline(QCDataRequest req) throws Exception {
		String status = "";
		this.isAddFlagRunning = true;
		AddQualityFlagCallback callback = new AddQualityFlagCallback(this, "Could not add flagging information.") {
			@Override
			public void onSuccess(QCDataResponse result) {
				isAddFlagRunning = false;
				EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				updateFlaggingResponseMessage(QCResponseCode.FLAGADDEDOFFLINE, "");
			}

			@Override
			public void onFailure(Throwable caught) {
				isAddFlagRunning = false;
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				updateFlaggingResponseMessage(QCResponseCode.FLAGGINGFAILEDOFFLINE, "");
			}
		};
		addRequest();
		this.qualityControlService.modifyQualityFlagWPS(req, callback);
		return status;

	}

	private String approveOnline(final QCDataRequest req) throws Exception {
		String status = "";
		this.isAddFlagRunning = true;
		AddQualityFlagCallback callback = new AddQualityFlagCallback(this, "Could not approve data release.") {
			@Override
			public void onSuccess(QCDataResponse result) {
				isAddFlagRunning = false;
				EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
				EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				updateFlaggingResponseMessage(QCResponseCode.LEVELUPDATED, "");
			}

			@Override
			public void onFailure(Throwable caught) {
				isAddFlagRunning = false;
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				updateFlaggingResponseMessage(QCResponseCode.LEVELFAILED, "");
			}
		};
		addRequest();
		this.qualityControlService.approveData(req, callback);
		return status;
	}

	private String approveOffline(QCDataRequest req) throws Exception {
		String status = "";
		this.isAddFlagRunning = true;
		AddQualityFlagCallback callback = new AddQualityFlagCallback(this, "Could not approve data release.") {
			@Override
			public void onSuccess(QCDataResponse result) {
				isAddFlagRunning = false;
				EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
				EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				updateFlaggingResponseMessage(QCResponseCode.LEVELUPDATEDOFFLINE, "");
			}

			@Override
			public void onFailure(Throwable caught) {
				isAddFlagRunning = false;
				EventBus.getMainEventBus().fireEvent(new FinishedAddFlagEvent());
				updateFlaggingResponseMessage(QCResponseCode.LEVELFAILEDOFFLINE, "");
			}
		};
		addRequest();
		this.qualityControlService.approveDataWPS(req, callback);
		return status;
	}

	public String convertTime(long time) {
		Date date = new Date(time);
		DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ssZ");
		return format.format(date).toString();
	}

	public int closest(int of, List<Integer> in) {
		int min = Integer.MAX_VALUE;
		int closest = of;

		for (int v : in) {
			final int diff = Math.abs(v - of);

			if (diff < min) {
				min = diff;
				closest = v;
			}
		}

		return closest;
	}

	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}

	public String getActiveTimeField() {
		return activeTimeField;
	}

	public void setActiveTimeField(String activeTimeField) {
		this.activeTimeField = activeTimeField;
	}

	public LinkedHashMap<String, ArrayList<String>> getRenderTimePoint() {
		return renderTimePoint;
	}

	public void setRenderTimePoint(LinkedHashMap<String, ArrayList<String>> renderTimePoint) {
		QualityFlaggingController.renderTimePoint = renderTimePoint;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isAddFlagRunning() {
		return isAddFlagRunning;
	}

	public void setAddFlagRunning(boolean isAddFlagRunning) {
		this.isAddFlagRunning = isAddFlagRunning;
	}

	// 10.11.2014 ASD
	public boolean isQualityControlSelected() {
		return isQualityControlSelected;
	}

	public void setQualityControlSelected(boolean isQualityControlSelected) {
		this.isQualityControlSelected = isQualityControlSelected;
	}
}

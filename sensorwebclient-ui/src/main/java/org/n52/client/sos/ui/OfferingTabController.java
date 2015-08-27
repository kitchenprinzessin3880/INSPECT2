package org.n52.client.sos.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.n52.client.bus.EventBus;
import org.n52.client.sos.ctrl.DataManagerSosImpl;
import org.n52.client.sos.event.data.NewTimeSeriesEvent;
import org.n52.client.sos.event.data.NewTimeSeriesEvent.Builder;
import org.n52.shared.serializable.pojos.sos.FeatureOfInterest;
import org.n52.shared.serializable.pojos.sos.Offering;
import org.n52.shared.serializable.pojos.sos.Phenomenon;
import org.n52.shared.serializable.pojos.sos.Procedure;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;
import org.n52.shared.serializable.pojos.sos.SOSMetadataBuilder;
import org.n52.shared.serializable.pojos.sos.Station;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.Record;

public class OfferingTabController {
	// private Map<String, ArrayList<String>> sensorStationMap;
	private static OfferingTabController instance;
	private ArrayList<String> servicesList = null;
	private static QueryTemplate queryTempInstance = null;
	private Map<String, ArrayList<String>> offeringList = new HashMap<String, ArrayList<String>>();
	private static Map<String, Map<String, ArrayList<String>>> completeStationListBySOS = null;

	public static OfferingTabController getInstance(QueryTemplate queryInstance) {
		if (instance == null) {
			instance = new OfferingTabController();
			completeStationListBySOS = new HashMap<String, Map<String, ArrayList<String>>>();
		}
		queryTempInstance = queryInstance;
		return instance;
	}

	public OfferingTabController() {

	}

	public ArrayList<String> getOfferingBySOS(String url) {
		return offeringList.get(url);
	}

	public void updateAllStationsFromMetaData(String selectedServiceURL) {
		// for (int i = 0; i < servicesList.size(); i++) {
		// String selectedServiceURL = servicesList.get(i);
		SOSMetadata metadata = DataManagerSosImpl.getInst().getServiceMetadata(selectedServiceURL);
		Collection<Station> stationList = null;
		if (metadata != null) {
			stationList = metadata.getStations();
			// if (completeStationListBySOS.get(selectedServiceURL) == null) {
			Map<String, ArrayList<String>> sensorStationMap = new HashMap<String, ArrayList<String>>();
			for (Station station : stationList) {
				ArrayList<String> dataList = new ArrayList<String>();
				dataList.add(station.getOffering());
				dataList.add(station.getProcedure());
				dataList.add(station.getPhenomenon());
				sensorStationMap.put(station.getId(), dataList);
			}
			Map<String, ArrayList<String>> sosMapExists = completeStationListBySOS.get(selectedServiceURL);

			if (sosMapExists != null) {
				// append values
				//Map<String, ArrayList<String>> newStationMap = new HashMap<String, ArrayList<String>>();
				//newStationMap.putAll(sosMapExists);
				sosMapExists.putAll(sensorStationMap);
				completeStationListBySOS.put(selectedServiceURL, sosMapExists);
			} else {
				completeStationListBySOS.put(selectedServiceURL, sensorStationMap);
			}
			OfferingTab.getInst().checkAndUpdateSensorBoxBySOSOffering(selectedServiceURL);
		}
		// }
	}

	/*
	 * public void loadAllStationsFromMetaData() { for (int i = 0; i <
	 * servicesList.size(); i++) { String selectedServiceURL =
	 * servicesList.get(i); SOSMetadata metadata =
	 * DataManagerSosImpl.getInst().getServiceMetadata(selectedServiceURL);
	 * Collection<Station> stationList = null; if (metadata != null) {
	 * stationList = metadata.getStations(); GWT.log(
	 * "OfferignTabController-loadAllStationsFromMetaDataeeeeeeeeeeeeeeeeeeeee :"
	 * + stationList.size()); //if
	 * (completeStationListBySOS.get(selectedServiceURL) == null) { Map<String,
	 * ArrayList<String>> sensorStationMap = new HashMap<String,
	 * ArrayList<String>>(); for (Station station : stationList) {
	 * ArrayList<String> dataList = new ArrayList<String>();
	 * dataList.add(station.getOffering());
	 * dataList.add(station.getProcedure());
	 * dataList.add(station.getPhenomenon());
	 * sensorStationMap.put(station.getId(), dataList); }
	 * completeStationListBySOS.put(selectedServiceURL, sensorStationMap);
	 * GWT.log("OfferignTabController-loadAllStationssensorStationMap.... :" +
	 * sensorStationMap.size()); //} } } }
	 */

	public void loadAllStationsFromMetaDataOld() {
		for (int i = 0; i < servicesList.size(); i++) {
			String selectedServiceURL = servicesList.get(i);
			SOSMetadata metadata = DataManagerSosImpl.getInst().getServiceMetadata(selectedServiceURL);
			Collection<Station> stationList = null;
			if (metadata != null) {
				stationList = metadata.getStations();
				if (completeStationListBySOS.get(selectedServiceURL) == null) {
					Map<String, ArrayList<String>> sensorStationMap = new HashMap<String, ArrayList<String>>();
					for (Station station : stationList) {
						// station name, property name
						ArrayList<String> dataList = new ArrayList<String>();
						dataList.add(station.getOffering());
						dataList.add(station.getProcedure());
						dataList.add(station.getPhenomenon());
						sensorStationMap.put(station.getId(), dataList);
					}
					completeStationListBySOS.put(selectedServiceURL, sensorStationMap);
				}
			}
		}
	}

	void parseAndStoreSOSMetadata(String serviceURL, Record record) {
		SOSMetadataBuilder builder = new SOSMetadataBuilder();
		DataManagerSosImpl dataManager = DataManagerSosImpl.getInst();
		if (!dataManager.contains(serviceURL)) {
			parseAndSetServiceConfiguration(builder, record);
			dataManager.storeData(serviceURL, builder.build());
		}
	}

	private void parseAndSetServiceConfiguration(SOSMetadataBuilder builder, Record record) {
		try {
			builder.addServiceURL(getValueFor(record, "url")).addServiceName(getValueFor(record, "itemName")).addServiceVersion(getValueFor(record, "version")).setWaterML(Boolean.parseBoolean(getValueFor(record, "waterML"))).setForceXYAxisOrder(Boolean.parseBoolean(getValueFor(record, "forceXYAxisOrder")))
					.setAutoZoom(Boolean.parseBoolean(getValueFor(record, "autoZoom"))).setRequestChunk(Integer.parseInt(getValueFor(record, "requestChunk")))
					// .addDefaultZoom(Integer.parseInt(getValueFor(record,
					// "defaultZoom")))
					.addLowerLeftEasting(Double.parseDouble(getValueFor(record, "llEasting"))).addLowerLeftNorthing(Double.parseDouble(getValueFor(record, "llNorthing"))).addUpperRightEasting(Double.parseDouble(getValueFor(record, "urEasting"))).addUpperRightNorthing(Double.parseDouble(getValueFor(record, "urNorthing")));
		} catch (Exception e) {
			GWT.log("Could not parse SERVICES configuration for: " + builder.getServiceURL(), e);
		}
	}

	private String getValueFor(Record record, String parameter) {
		String value = record.getAttribute(parameter);
		return value == null || value.isEmpty() ? null : value;
	}

	public ArrayList<String> getSensorsByOffering(String sos, String offeringId) {
		Map<String, ArrayList<String>> sensorStationMap = new HashMap<String, ArrayList<String>>();
		sensorStationMap = completeStationListBySOS.get(sos);
		ArrayList<String> statList = new ArrayList<String>();

		if (sensorStationMap != null) {
			// ASD
			Iterator it = sensorStationMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				//String sensorId = pairs.getKey().toString();
				ArrayList<String> tempDataList = (ArrayList<String>) pairs.getValue();
				String offering = tempDataList.get(0).toString();
				if (offering.equals(offeringId)) {
					String stationName = tempDataList.get(1);
					statList.add(stationName);
				}
			}
		}
		return statList;
	}

	public HashSet<String> getPropertiesBySensors(String sensorId, String offeringId, String sos) {
		Map<String, ArrayList<String>> sensorStationMap = completeStationListBySOS.get(sos);
		ArrayList<String> propList = new ArrayList<String>();
		Iterator it = sensorStationMap.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			ArrayList<String> tempDataList = (ArrayList<String>) pairs.getValue();
			String offering = tempDataList.get(0).toString();
			String sensorName = tempDataList.get(1).toString();
			if (sensorName.equals(sensorId) && offering.equals(offeringId)) {
				String property = tempDataList.get(2).toString();
				propList.add(property);
			}
		}
		// unique properties
		HashSet<String> set = new HashSet<String>(propList);
		return set;
	}

	// Add time series
	public void addTimeSeries(String sosURL, String off, String proc, String[] propArray) {
		SOSMetadata metadata = DataManagerSosImpl.getInst().getServiceMetadata(sosURL);
		String propList = "";
		List<Builder> buildList = new ArrayList<Builder>();
		for (int i = 0; i < propArray.length; i++) {
			propList = propArray[i];
			String id = getSensorId(sosURL, off.trim(), proc.trim(), propList.trim());

			Station station = metadata.getStation(id);
			final String offeringId = station.getOffering();
			final String featureId = station.getFeature();
			final String procedureId = station.getProcedure();
			final String phenomenonId = station.getPhenomenon();

			Offering offering = metadata.getOffering((String) offeringId);
			FeatureOfInterest foi = metadata.getFeature((featureId));
			Procedure procedure = metadata.getProcedure((String) procedureId);
			Phenomenon phenomenon = metadata.getPhenomenon(phenomenonId);

			// NewTimeSeriesEvent event = new
			// NewTimeSeriesEvent.Builder(sosURL).addStation(station).addOffering(offering).addFOI(foi).addProcedure(procedure).addPhenomenon(phenomenon).build();
			Builder buildTemp = new NewTimeSeriesEvent.Builder(sosURL).addStation(station).addOffering(offering).addFOI(foi).addProcedure(procedure).addPhenomenon(phenomenon).buildCustom();
			buildList.add(buildTemp);
		}
		NewTimeSeriesEvent event = new NewTimeSeriesEvent(buildList);
		EventBus.getMainEventBus().fireEvent(event);
	}

	public void addTimeSeriesCustom(String sosURL, String off, String proc, String[] propArray) {
			SOSMetadata metadata = DataManagerSosImpl.getInst().getServiceMetadata(sosURL);
			String propList = "";
			List<Builder> buildList = new ArrayList<Builder>();
			for (int i = 0; i < propArray.length; i++) {
				propList = propArray[i];
				String id = getSensorId(sosURL, off.trim(), proc.trim(), propList.trim());
				Station station = metadata.getStation(id);
				final String offeringId = station.getOffering();
				final String featureId = station.getFeature();
				final String procedureId = station.getProcedure();
				final String phenomenonId = station.getPhenomenon();

				Offering offering = metadata.getOffering((String) offeringId);
				FeatureOfInterest foi = metadata.getFeature((featureId));
				Procedure procedure = metadata.getProcedure((String) procedureId);
				Phenomenon phenomenon = metadata.getPhenomenon(phenomenonId);
				if (station != null && offering != null && foi != null && procedure != null && phenomenon != null) {
					Builder buildTemp = new NewTimeSeriesEvent.Builder(sosURL).addStation(station).addOffering(offering).addFOI(foi).addProcedure(procedure).addPhenomenon(phenomenon).buildCustom();
					buildList.add(buildTemp);
				}
			}
			NewTimeSeriesEvent event = new NewTimeSeriesEvent(buildList);
			EventBus.getMainEventBus().fireEvent(event);
	}

	public void addQueryBasedNewTimeSeries(String sosURL, String off, String proc, String[] propArray) {
		SOSMetadata metadata = DataManagerSosImpl.getInst().getServiceMetadata(sosURL);
		//Collection<Station> stations = metadata.getStations();
		List<Builder> buildList = new ArrayList<Builder>();
		String propList = "";
		for (int i = 0; i < propArray.length; i++) {
			propList = propArray[i];
			String id = getSensorId(sosURL, off.trim(), proc.trim(), propList.trim());
			Station station = metadata.getStation(id);

			final String offeringId = station.getOffering();
			final String featureId = station.getFeature();
			final String procedureId = station.getProcedure();
			final String phenomenonId = station.getPhenomenon();

			Offering offering = null;
			FeatureOfInterest foi = null;

			offering = metadata.getOffering((String) offeringId);
			foi = metadata.getFeature((featureId));
			Procedure procedure = metadata.getProcedure((String) procedureId);
			Phenomenon phenomenon = metadata.getPhenomenon(phenomenonId);

			if (offering != null && foi != null && procedure != null) {
				// NewTimeSeriesEvent event = new
				// NewTimeSeriesEvent.Builder(sosURL).addStation(station).addOffering(offering).addFOI(foi).addProcedure(procedure).addPhenomenon(phenomenon).build();
				// EventBus.getMainEventBus().fireEvent(event);
				Builder buildTemp = new NewTimeSeriesEvent.Builder(sosURL).addStation(station).addOffering(offering).addFOI(foi).addProcedure(procedure).addPhenomenon(phenomenon).buildCustom();
				buildList.add(buildTemp);
			}
		}
		NewTimeSeriesEvent event = new NewTimeSeriesEvent(buildList);
		EventBus.getMainEventBus().fireEvent(event);
	}

	// get sensor id based on sensor name, offering and property
	public String getSensorId(String sosURL, String off, String proc, String phen) {
		String sensorId = "";
		Map<String, ArrayList<String>> sensorStationMap = completeStationListBySOS.get(sosURL);
		for (String key : sensorStationMap.keySet()) {
			ArrayList<String> value = sensorStationMap.get(key);
			String offering = value.get(0);
			String procedure = value.get(1);
			String phenomenon = value.get(2);

			if (offering.equalsIgnoreCase(off) && procedure.equalsIgnoreCase(proc) && phenomenon.equals(phen)) {
				sensorId = key.toString();
				break;
			}
		}

		return sensorId;
	}

	public ArrayList<String> getServicesList() {
		return servicesList;
	}

	public void setServicesList(ArrayList<String> sList) {
		this.servicesList = sList;
	}

	public Map<String, ArrayList<String>> getAllOfferingListBySOS() {
		return offeringList;
	}

	public void setAllOfferingListBySOS(Map<String, ArrayList<String>> offeringList) {
		this.offeringList = offeringList;
	}

	/*
	public class CustomNewTimeSeriesController {

		private String url;
		private String offeringId;
		private String procedureId;
		private String foiId;
		private boolean procedureReady = false;
		private boolean featureReady = false;
		private String[] properties;

		public CustomNewTimeSeriesController(String sosURL, String off, String proc, String[] propArray) {
			new CustomNewTimeSeriesControllerBroker();
			this.url = sosURL;
			this.offeringId = off;
			this.procedureId = proc;
			this.properties = propArray;
			this.foiId = proc;
		}

		private class CustomNewTimeSeriesControllerBroker implements StoreProcedureEventHandler, StoreFeatureEventHandler {

			public CustomNewTimeSeriesControllerBroker() {
				EventBus.getMainEventBus().addHandler(StoreProcedureEvent.TYPE, this);
				EventBus.getMainEventBus().addHandler(StoreFeatureEvent.TYPE, this);
			}

			@Override
			public void onStore(StoreFeatureEvent evt) {
				featureReady = true;
				GWT.log("FOI OK" + evt.getFeature().getId());
				check();
			}

			@Override
			public void onStore(StoreProcedureEvent evt) {
				procedureReady = true;
				GWT.log("PROC OK" + evt.getProcedure().getId());
				check();
			}

		}

		public void check() {
			if (featureReady && procedureReady) {
				GWT.log("Check............................initiate");
				SOSMetadata metadata = DataManagerSosImpl.getInst().getServiceMetadata(url);
				String propList = "";
				List<Builder> buildList = new ArrayList<Builder>();

				for (int i = 0; i < properties.length; i++) {
					propList = properties[i];
					String id = getSensorId(url, offeringId, procedureId, propList.trim());
					GWT.log("CheckId............................:" + id);
					Station station = metadata.getStation(id);
					final String offId = station.getOffering();
					final String fId = station.getFeature();
					final String procId = station.getProcedure();
					final String phenomenonId = station.getPhenomenon();

					Offering offering = metadata.getOffering(offId);
					FeatureOfInterest foi = metadata.getFeature(fId);
					Procedure procedure = metadata.getProcedure(procId);
					Phenomenon phenomenon = metadata.getPhenomenon(phenomenonId);

					if (station != null && offering != null && foi != null && procedure != null && phenomenon != null) {
						Builder buildTemp = new NewTimeSeriesEvent.Builder(url).addStation(station).addOffering(offering).addFOI(foi).addProcedure(procedure).addPhenomenon(phenomenon).buildCustom();
						buildList.add(buildTemp);
						GWT.log("CustomNewTimeSeriesControllerBroker-CheckProp...........................:" + propList + offId);
					}
				}
				NewTimeSeriesEvent event = new NewTimeSeriesEvent(buildList);
				EventBus.getMainEventBus().fireEvent(event);
			}
		}

		// get sensor id based on sensor name, offering and property
		private String getSensorId(String sosURL, String off, String proc, String phen) {
			String sensorId = "";
			Map<String, ArrayList<String>> sensorStationMap = completeStationListBySOS.get(sosURL);
			for (String key : sensorStationMap.keySet()) {
				ArrayList<String> value = sensorStationMap.get(key);
				String offering = value.get(0);
				String procedure = value.get(1);
				String phenomenon = value.get(2);

				if (offering.equalsIgnoreCase(off) && procedure.equalsIgnoreCase(proc) && phenomenon.equals(phen)) {
					sensorId = key.toString();
					break;
				}
			}

			return sensorId;
		}

	} */

}

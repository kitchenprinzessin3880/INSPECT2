package org.n52.client.ui.legend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.n52.client.bus.EventBus;
import org.n52.client.ctrl.TimeManager;
import org.n52.client.sos.ctrl.DataManagerSosImpl;
import org.n52.client.sos.ctrl.SOSRequestManager;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.event.DatesChangedEvent;
import org.n52.client.sos.event.data.DeleteAllTimeSeriesEvent;
import org.n52.client.sos.event.data.GetFeatureEvent;
import org.n52.client.sos.event.data.GetOfferingEvent;
import org.n52.client.sos.event.data.GetProcedureEvent;
import org.n52.client.sos.event.data.NewTimeSeriesEvent;
import org.n52.client.sos.event.data.NewTimeSeriesEvent.Builder;
import org.n52.client.sos.legend.TimeSeries;
import org.n52.client.sos.ui.OfferingTab;
import org.n52.client.sos.ui.OfferingTabController;
import org.n52.client.sos.ui.QueryTemplate;
import org.n52.shared.serializable.pojos.sos.FeatureOfInterest;
import org.n52.shared.serializable.pojos.sos.Offering;
import org.n52.shared.serializable.pojos.sos.Phenomenon;
import org.n52.shared.serializable.pojos.sos.Procedure;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;
import org.n52.shared.serializable.pojos.sos.Station;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Window;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.HTMLFlow;

public class HistoryManager {

	private static HistoryManager instance;
	private Storage queryStore;

	public static HistoryManager getInst() {
		// save the search query as a query template
		if (instance == null) {
			instance = new HistoryManager();
		}
		return instance;
	}

	private HistoryManager() {

	}

	public void saveHistory(final String hist) {
		SC.confirm("This will overwrite existing entry. Save current data series view?", new BooleanCallback() {
			public void execute(Boolean value) {
				if (value != null && value) {
					Storage queryStore = QueryTemplate.getInst().getQueryStore();
					queryStore.setItem("history", hist);
				}
			}
		});
	}

	public void openViewHistory() {
		Storage queryStore = QueryTemplate.getInst().getQueryStore();
		String parameters = queryStore.getItem("history");

		HashSet<String> sosList = OfferingTab.getInst().getDefaultServices();
		boolean isTimeUpdated = false;
		if (queryStore.getItem("history") != null) {
			DataStoreTimeSeriesImpl dataManager = DataStoreTimeSeriesImpl.getInst();
			if (!dataManager.getDataItems().isEmpty()) {
				TimeSeries[] ts = dataManager.getTimeSeriesSorted();
				for (int i = 0; i < ts.length; i++) {
					// String id = ts[i].getId();
					TimeSeries series = dataManager.getDataItem(ts[i].getId());
					LegendEntryTimeSeries le = (LegendEntryTimeSeries) series.getLegendElement();
					le.getEventBroker().unregister();
				}
				SOSRequestManager.getInstance().setChangeStyleActive(false);
				EventBus.getMainEventBus().fireEvent(new DeleteAllTimeSeriesEvent());
			}

			String[] params = parameters.split("#");
			List<Builder> buildList = new ArrayList<Builder>();
			for (String ts : params) {
				// sos + "," + offering + "," + sensor + "," + property + "#"
				String[] tokens = ts.split(",");
				String url = tokens[0].trim();
				String off = tokens[1].trim();
				String proc = tokens[2].trim();
				String prop = tokens[3].trim();
				long start = Long.parseLong(tokens[4].trim());
				long end = Long.parseLong(tokens[5].trim());
				if (!isTimeUpdated) {
					TimeManager.getInst().setIsOverviewTimeFixed(false);
					EventBus.getMainEventBus().fireEvent(new DatesChangedEvent(start, end, true));
					isTimeUpdated = true;
				}
				if (sosList.size() > 0 && sosList.contains(url)) {
					SOSMetadata metadata = DataManagerSosImpl.getInst().getServiceMetadata(url);
					String id = OfferingTab.getController().getSensorId(url, off, proc, prop);
					Station station = metadata.getStation(id);
					Offering offering = null;
					FeatureOfInterest foi = null;
					Procedure procedure = null;
					Phenomenon phenomenon = null;
					offering = metadata.getOffering(off);
					foi = metadata.getFeature(proc);
					procedure = metadata.getProcedure(proc);
					phenomenon = metadata.getPhenomenon(prop);
					if (offering != null && foi != null && procedure != null) {
						Builder buildTemp = new NewTimeSeriesEvent.Builder(url).addStation(station).addOffering(offering).addFOI(foi).addProcedure(procedure).addPhenomenon(phenomenon).buildCustom();
						buildList.add(buildTemp);
					}
				}
			}
			NewTimeSeriesEvent event = new NewTimeSeriesEvent(buildList);
			EventBus.getMainEventBus().fireEvent(event);
		} else {
			Window.alert("Saved view does not exist!");
		}

	}

	public void checkHistory() {
		// ASD 06.10.2014 fire pre-events related to saved history
		Storage queryStore = QueryTemplate.getInst().getQueryStore();
		String parameters = queryStore.getItem("history");
		HashSet<String> sosList = OfferingTab.getInst().getDefaultServices();

		if (parameters != null && parameters.length()>0) {
			String[] params = parameters.split("#");
			for (String ts : params) {
				String[] tokens = ts.split(",");
				String url = tokens[0].trim();
				String off = tokens[1].trim();
				String proc = tokens[2].trim();
				SOSMetadata metadata = DataManagerSosImpl.getInst().getServiceMetadata(url);
				if (sosList.size() > 0 && sosList.contains(url)) {
					if (metadata.getOffering(off) == null) {
						GetOfferingEvent getOffEvent = new GetOfferingEvent(url, off);
						EventBus.getMainEventBus().fireEvent(getOffEvent);
					}
					if (metadata.getProcedure(proc) == null) {
						GetProcedureEvent getProcEvent = new GetProcedureEvent(url, proc);
						EventBus.getMainEventBus().fireEvent(getProcEvent);
					}
					if (metadata.getFeature(proc) == null) {
						GetFeatureEvent getFoiEvent = new GetFeatureEvent(url, proc);
						EventBus.getMainEventBus().fireEvent(getFoiEvent);
					}
				}
			}
		}
	}

}

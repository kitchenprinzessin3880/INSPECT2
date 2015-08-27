package org.n52.client.sos.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.n52.client.bus.EventBus;
import org.n52.client.sos.ctrl.DataManagerSosImpl;
import org.n52.client.sos.ctrl.QCResponseCode;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.event.data.DeleteAllTimeSeriesEvent;
import org.n52.client.sos.event.data.GetFeatureEvent;
import org.n52.client.sos.event.data.GetOfferingEvent;
import org.n52.client.sos.event.data.GetProcedureEvent;
import org.n52.client.sos.legend.TimeSeries;
import org.n52.client.ui.Toaster;
import org.n52.client.ui.legend.LegendEntryTimeSeries;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;
import org.n52.shared.serializable.pojos.sos.Station;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.storage.client.StorageMap;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.Label;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.google.gwt.i18n.client.DateTimeFormat;

public class QueryTemplate extends Tab {

	protected VLayout layout;
	private Button clearAllButton;
	/** The form. */
	private static FlexTable storageFlexTable;
	private static QueryTemplate instance;
	private HTMLFlow responseTextItem;
	private static Storage queryStore;

	private String selectedkey = "";

	VLayout wrapper;

	public static QueryTemplate getInst() {
		// save the search query as a query template
		if (instance == null) {
			instance = new QueryTemplate();
		}
		return instance;
	}

	private QueryTemplate() {
		setTitle("Saved Search Templates");
		queryStore = Storage.getLocalStorageIfSupported();
		createContent();
	}

	private void createContent() {
		wrapper = new VLayout();
		wrapper.setMargin(5);
		responseTextItem = new HTMLFlow();
		responseTextItem.setVisible(false);

		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setHeight("400px");
		storageFlexTable = new FlexTable();
		storageFlexTable.setWidth("100%");
		generateTable();
		scrollPanel.add(storageFlexTable);

		clearAllButton = new Button("Clear All");
		clearAllButton.setVisible(false);
		clearAllButton.setTitle("This will remove all saved query templates permenantly");

		// Listen for mouse events on the Clear all button.
		clearAllButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Widget sender = (Widget) event.getSource();
				if (sender == clearAllButton) {
					if (queryStore != null) {
						SC.confirm("This will delete all saved queries permenantly. Proceed with 'Clear All' operation?", new BooleanCallback() {
							public void execute(Boolean value) {
								if (value != null && value) {
									storageFlexTable.removeAllRows();
									generateTable();
									if (queryStore != null) {
										for (int i = 0; i < queryStore.getLength(); i++) {
											String k = queryStore.key(i);
											// remove all entries except history (saved view)
											if (!k.equalsIgnoreCase("history")) {
												queryStore.removeItem(k);
											}
										}
									}
									clearAllButton.setVisible(false);
								} else {

								}
							}
						});
					}
				}
			} // if sender is the clear all button
		});
		Label space = new Label();
		// wrapper.setHeight100();
		wrapper.addMember(scrollPanel);
		wrapper.addMember(responseTextItem);
		wrapper.addMember(space);
		wrapper.addMember(clearAllButton);
		this.setPane(wrapper);

	}

	private void generateTable() {
		storageFlexTable.getRowFormatter().addStyleName(0, "tereno_queryListHeader");
		storageFlexTable.setStyleName("tereno_queryList");
		storageFlexTable.getFlexCellFormatter().setAlignment(0, 4, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
		storageFlexTable.getFlexCellFormatter().setAlignment(0, 5, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
		storageFlexTable.getFlexCellFormatter().setAlignment(0, 6, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
		storageFlexTable.setText(0, 0, "Created");
		storageFlexTable.setText(0, 1, "Service");
		storageFlexTable.setText(0, 2, "Station");
		storageFlexTable.setText(0, 3, "Property");
		storageFlexTable.setText(0, 4, "Modify Station");
		storageFlexTable.setText(0, 5, "Add Series (Existing)");
		storageFlexTable.setText(0, 6, "Add Series (Overwrite)");
		storageFlexTable.setText(0, 7, "Delete Record");
	}

	public void reloadStorageTable() {
		if (queryStore != null && queryStore.getLength() > 0) {
			clearAllButton.setVisible(true);
			String[] sosListTemp = OfferingTab.getInst().getSosBox().getValues();
			List<String> sosList = Arrays.asList(sosListTemp);

			Map<String, String> map = new StorageMap(queryStore);
			String history = map.get("history");
			if (map.containsKey("history")) {
				map.remove("history");
			}

			SortedSet<String> keys = new TreeSet<String>(map.keySet());
			int i = 0;
			for (String key : keys) {
				i++;
				String value = map.get(key);
				String[] tokens = value.split("#");
				String dtCreated = tokens[0].trim();
				String sos = tokens[1].trim();
				String offering = tokens[2].trim();
				String sensor = tokens[3].trim();
				String fullPhenom = tokens[4].trim();

				// remove unrelated sos
				if (sosList.size() > 0 && sosList.contains(sos)) {
					storageFlexTable.setText(i, 0, dtCreated);
					storageFlexTable.setText(i, 1, sos);
					storageFlexTable.setText(i, 2, sensor);
					storageFlexTable.setText(i, 3, fullPhenom);
					storageFlexTable.getFlexCellFormatter().setWidth(i, 2, "80px");
					storageFlexTable.getFlexCellFormatter().setWidth(i, 3, "260px");

					final Button removeButton = createRemoveButton();
					final Button queryButton = createQueryButton();
					final Button modifySensorButton = createModifySensorButton();
					final Button queryOverwriteButton = createOverWriteQueryButton();
					storageFlexTable.setWidget(i, 4, modifySensorButton);
					storageFlexTable.setWidget(i, 5, queryButton);
					storageFlexTable.setWidget(i, 6, queryOverwriteButton);
					storageFlexTable.setWidget(i, 7, removeButton);
					storageFlexTable.getFlexCellFormatter().setAlignment(i, 4, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
					storageFlexTable.getFlexCellFormatter().setAlignment(i, 5, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
					storageFlexTable.getFlexCellFormatter().setAlignment(i, 6, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
					storageFlexTable.getFlexCellFormatter().setAlignment(i, 7, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);

					// execute pre-events
					SOSMetadata metadata = DataManagerSosImpl.getInst().getServiceMetadata(sos);
					if (metadata.getProcedure(sensor) == null) {
						GetProcedureEvent getProcEvent = new GetProcedureEvent(sos, sensor);
						EventBus.getMainEventBus().fireEvent(getProcEvent);
					}
					if (metadata.getFeature(sensor) == null) {
						GetFeatureEvent getFoiEvent = new GetFeatureEvent(sos, sensor);
						EventBus.getMainEventBus().fireEvent(getFoiEvent);
					}
					if (metadata.getOffering(offering) == null) {
						GetOfferingEvent getOffEvent = new GetOfferingEvent(sos, offering);
						EventBus.getMainEventBus().fireEvent(getOffEvent);
					}
				}
			}
			queryStore.setItem("history", history);

			/*
			 * for (int i = 0; i < queryStore.getLength(); i++) {
			 * 
			 * String key = queryStore.key(i); String value =
			 * queryStore.getItem(key);
			 * 
			 * String[] tokens = value.split("#"); String dtCreated =
			 * tokens[0].trim(); String sos = tokens[1].trim(); String offering
			 * = tokens[2].trim(); String sensor = tokens[3].trim(); String
			 * fullPhenom = tokens[4].trim();
			 * 
			 * storageFlexTable.setText(i+1, 0, dtCreated);
			 * storageFlexTable.setText(i+1, 1, sensor);
			 * storageFlexTable.setText(i+1, 2, fullPhenom);
			 * storageFlexTable.getFlexCellFormatter().setWidth(i+1, 2,
			 * "290px");
			 * 
			 * final Button removeButton = createRemoveButton(); final Button
			 * queryButton = createQueryButton();
			 * 
			 * storageFlexTable.setWidget(i+1, 3, removeButton);
			 * storageFlexTable.setWidget(i+1, 4, queryButton);
			 * storageFlexTable.getFlexCellFormatter().setAlignment(i+1, 3,
			 * HasHorizontalAlignment.ALIGN_CENTER,
			 * HasVerticalAlignment.ALIGN_TOP);
			 * storageFlexTable.getFlexCellFormatter().setAlignment(i+1, 4,
			 * HasHorizontalAlignment.ALIGN_CENTER,
			 * HasVerticalAlignment.ALIGN_TOP); }
			 */

		} else {
			clearAllButton.setVisible(false);
		}

	}

	public void clearReponseTextItem() {
		this.responseTextItem.setVisible(false);
		this.responseTextItem.setContents("");
	}

	public void updateReponseTextItem(QCResponseCode msg) {
		this.responseTextItem.setVisible(true);
		this.responseTextItem.setContents("<span style='color:" + msg.getColor() + ";'>" + msg.getResponseMessage() + "</span>");
	}

	public void addQuery(String sos, String off, String sense, String[] propList) {
		int numRows = storageFlexTable.getRowCount();
		Date date = new Date();
		DateTimeFormat dtf = DateTimeFormat.getFormat("dd-MM-yyyy-HH:mm:ss");
		String dateCreated = dtf.format(date).toString();

		String properties = "";
		for (int i = 0; i < propList.length; i++) {
			properties += propList[i] + ", ";
		}

		String formattedProperties = "";
		// remove the last comma from properties list
		if (properties.trim().endsWith(",")) {
			formattedProperties = properties.substring(0, properties.length() - 1);
		}

		storageFlexTable.setText(numRows, 0, dateCreated);
		storageFlexTable.setText(numRows, 1, sos);
		storageFlexTable.setText(numRows, 2, sense);
		storageFlexTable.setText(numRows, 3, formattedProperties);
		storageFlexTable.getFlexCellFormatter().setWidth(numRows, 3, "260px");

		// Add a button to remove this stock from the table.
		final Button removeButton = createRemoveButton();
		final Button queryButton = createQueryButton();
		final Button modifySensorButton = createModifySensorButton();
		final Button queryOverwriteButton = createOverWriteQueryButton();

		storageFlexTable.setWidget(numRows, 4, modifySensorButton);
		storageFlexTable.setWidget(numRows, 5, queryButton);
		storageFlexTable.setWidget(numRows, 6, queryOverwriteButton);
		storageFlexTable.setWidget(numRows, 7, removeButton);
		storageFlexTable.getFlexCellFormatter().setAlignment(numRows, 4, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
		storageFlexTable.getFlexCellFormatter().setAlignment(numRows, 5, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
		storageFlexTable.getFlexCellFormatter().setAlignment(numRows, 6, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
		storageFlexTable.getFlexCellFormatter().setAlignment(numRows, 7, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_TOP);
		String value = "";
		String created = dateCreated + "#";
		String service = sos + "#";
		String offering = off + "#";
		String sensor = sense + "#";
		String phenomenon = formattedProperties;
		value = created + service + offering + sensor + phenomenon;

		// we use a StorageMap to see if a certain value is already in the
		// storage,
		// and if it is not yet stored, we write the data to storage.
		Map<String, String> map = new StorageMap(queryStore);
		if (map.containsKey(dateCreated) != true) {
			queryStore.setItem(dateCreated, value);
		}

	}

	private Button createRemoveButton() {
		// Add a button to remove this stock from the table.
		final Button removeButton = new Button("X");
		removeButton.setWidth("20");
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				TableRowElement tableRow = findNearestParentRow(removeButton.getElement());
				int row = tableRow.getRowIndex();
				String key = storageFlexTable.getText(row, 0);

				storageFlexTable.removeRow(row);
				queryStore.removeItem(key);

				if (storageFlexTable.getRowCount() <= 1) {
					clearAllButton.setVisible(false);
				}
			}
		});

		return removeButton;
	}

	private Button createOverWriteQueryButton() {
		final Button queryButton = new Button("O");
		queryButton.setWidth("20");
		queryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				TableRowElement tableRow = findNearestParentRow(queryButton.getElement());
				int row = tableRow.getRowIndex();
				String key = storageFlexTable.getText(row, 0);
				setSelectedkey(key);
				String value = queryStore.getItem(key);
				String[] tokens = value.split("#");
				DataStoreTimeSeriesImpl dataManager = DataStoreTimeSeriesImpl.getInst();

				// order : service + offering + sensor + phenomenon +datetofrom;
				// String dtCreated = tokens[0].trim();
				String sos = tokens[1].trim();
				String offering = tokens[2].trim();
				String sensor = tokens[3].trim();
				String fullPhenom = tokens[4].trim();
				String[] propArray = fullPhenom.split(",");

				TimeSeries[] timeSeriesExisting = DataStoreTimeSeriesImpl.getInst().getTimeSeriesSorted();
				ArrayList<String> phenomenaList = new ArrayList<String>();
				for (int a = 0; a < propArray.length; a++) {
					String newProperty = propArray[a].trim();
					phenomenaList.add(newProperty);
				}

				if (timeSeriesExisting.length > 0) {
					// delete existing series
					for (int i = 0; i < timeSeriesExisting.length; i++) {
						// String id = timeSeriesExisting[i].getId();
						TimeSeries series = dataManager.getDataItem(timeSeriesExisting[i].getId());
						LegendEntryTimeSeries le = (LegendEntryTimeSeries) series.getLegendElement();
						le.getEventBroker().unregister();
					}
					EventBus.getMainEventBus().fireEvent(new DeleteAllTimeSeriesEvent());
				}

				OfferingTab.getInst().getController().addTimeSeriesCustom(sos, offering, sensor, propArray);

			}
		});
		return queryButton;
	}

	private Button createQueryButton() {
		final Button queryButton = new Button("A");
		queryButton.setWidth("20");
		queryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				TableRowElement tableRow = findNearestParentRow(queryButton.getElement());
				int row = tableRow.getRowIndex();
				String key = storageFlexTable.getText(row, 0);
				setSelectedkey(key);
				String value = queryStore.getItem(key);
				String[] tokens = value.split("#");

				// order : service + offering + sensor + phenomenon +datetofrom;
				// String dtCreated = tokens[0].trim();
				String sos = tokens[1].trim();
				String offering = tokens[2].trim();
				String sensor = tokens[3].trim();
				String fullPhenom = tokens[4].trim();
				String[] propArray = fullPhenom.split(",");

				TimeSeries[] timeSeriesExisting = DataStoreTimeSeriesImpl.getInst().getTimeSeriesSorted();
				ArrayList<String> phenomenaList = new ArrayList<String>();
				for (int a = 0; a < propArray.length; a++) {
					String newProperty = propArray[a].trim();
					phenomenaList.add(newProperty);
				}

				if (timeSeriesExisting.length > 0) {
					for (int i = 0; i < propArray.length; i++) {
						String newProperty = propArray[i].trim();
						for (TimeSeries timeSerie : timeSeriesExisting) {
							String procedureOld = timeSerie.getProcedureId();
							String sosUrlOld = timeSerie.getSosUrl();
							String offOld = timeSerie.getOfferingId();
							String phenOld = timeSerie.getPhenomenonId();
							if (sosUrlOld.equalsIgnoreCase(sos) && offOld.equalsIgnoreCase(offering) && procedureOld.equalsIgnoreCase(sensor) && phenOld.equalsIgnoreCase(newProperty)) {
								phenomenaList.remove(newProperty);
								Toaster.getInstance().addMessage("Adding duplicate time series is not allowed:" + sensor + " - " + newProperty);
								break;
							}

						}
					}
				}
				String[] filteredPhenArray = new String[phenomenaList.size()];
				filteredPhenArray = phenomenaList.toArray(filteredPhenArray);
				if (filteredPhenArray.length > 0) {
					OfferingTab.getInst().getController().addTimeSeriesCustom(sos, offering, sensor, filteredPhenArray);
				}

			}
		});
		return queryButton;
	}

	private Button createModifySensorButton() {
		final Button modifySensorButton = new Button("M");
		modifySensorButton.setWidth("20");
		modifySensorButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				TableRowElement tableRow = findNearestParentRow(modifySensorButton.getElement());
				final int row = tableRow.getRowIndex();
				String key = storageFlexTable.getText(row, 0);
				setSelectedkey(key);
				String value = queryStore.getItem(key);
				String[] tokens = value.split("#");

				// order : service + offering + sensor + phenomenon +datetofrom;
				final String dtCreated = tokens[0].trim();
				final String sos = tokens[1].trim();
				final String offering = tokens[2].trim();
				final String sensor = tokens[3].trim();
				String fullPhenom = tokens[4].trim();

				final String c = dtCreated + "#";
				final String s = sos + "#";
				final String o = offering + "#";
				final String phenomenon = fullPhenom;
				String[] propArray = fullPhenom.split(",");

				// get all sensor
				String[] stringArray = getAllStationsBySOS(sos, Arrays.asList(propArray));

				final ListBox sensorListBox = new ListBox();
				for (int i = 0; i < stringArray.length; i++) {
					sensorListBox.addItem(stringArray[i]);
				}

				sensorListBox.setSelectedIndex(Arrays.asList(stringArray).indexOf(sensor));

				if (stringArray.length == 1) {
					storageFlexTable.setText(row, 2, stringArray[0]);
					responseTextItem.setVisible(true);
					updateReponseTextItem(QCResponseCode.ONESTATIONAVAILABLE);
					Timer timer = new Timer() {
						@Override
						public void run() {
							responseTextItem.setVisible(false);
							responseTextItem.setContents("");
						}
					};
					timer.schedule(4000);
				} else {
					storageFlexTable.setWidget(row, 2, sensorListBox);
					sensorListBox.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							int selectedIndex = sensorListBox.getSelectedIndex();
							String selectedStation = sensorListBox.getValue(selectedIndex);
							SOSMetadata metadata = DataManagerSosImpl.getInst().getServiceMetadata(sos);
							if (metadata.getProcedure(selectedStation) == null) {
								GetProcedureEvent getProcEvent = new GetProcedureEvent(sos, selectedStation);
								EventBus.getMainEventBus().fireEvent(getProcEvent);
							}
							if (metadata.getFeature(selectedStation) == null) {
								GetFeatureEvent getFoiEvent = new GetFeatureEvent(sos, selectedStation);
								EventBus.getMainEventBus().fireEvent(getFoiEvent);
							}
							storageFlexTable.setText(row, 2, selectedStation);
							String v = c + s + o + selectedStation + "#" + phenomenon;
							queryStore.setItem(dtCreated, v);

						}

					});
				}

			}
		});
		return modifySensorButton;
	}

	public static TableRowElement findNearestParentRow(Node node) {
		Node element = findNearestParentNodeByType(node, "tr");
		if (element != null) {
			return element.cast();
		}
		return null;
	}

	public static Node findNearestParentNodeByType(Node node, String nodeType) {
		while ((node != null)) {
			if (Element.is(node)) {
				Element elem = Element.as(node);

				String tagName = elem.getTagName();

				if (nodeType.equalsIgnoreCase(tagName)) {
					return elem.cast();
				}

			}
			node = node.getParentNode();
		}
		return null;
	}

	public String getSelectedkey() {
		return selectedkey;
	}

	public void setSelectedkey(String selectedkey) {
		this.selectedkey = selectedkey;
	}

	public String[] getAllStationsBySOS(String sos, List<String> proplist) {
		SOSMetadata metadata = DataManagerSosImpl.getInst().getServiceMetadata(sos);
		String[] stringArray = null;
		if (metadata != null) {
			ArrayList<String> senseList = new ArrayList<String>();
			ArrayList<String> senseListFinal = new ArrayList<String>();
			int occurrence = proplist.size();

			for (String p : proplist) {
				for (Station station : metadata.getStations()) {
					String procedure = station.getProcedure();
					String phenomenon = station.getPhenomenon();
					if (phenomenon.trim().equalsIgnoreCase(p.trim())) {
						senseList.add(procedure);
					}
				}
			}

			Set<String> unique = new HashSet<String>(senseList);
			for (String key : unique) {
				if (Collections.frequency(senseList, key) == occurrence) {
					senseListFinal.add(key);
				}
			}

			Set<String> treeSet = new TreeSet<String>(new SortListAlphabeticalOrder());
			treeSet.addAll(senseListFinal);
			stringArray = new String[treeSet.size()];
			treeSet.toArray(stringArray);
		}
		return stringArray;
	}

	public class SortListAlphabeticalOrder implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			String u1 = (String) o1;
			String u2 = (String) o2;
			return u1.compareTo(u2);
		}
	}

	public static Storage getQueryStore() {
		return queryStore;
	}

	public static void setQueryStore(Storage queryStore) {
		QueryTemplate.queryStore = queryStore;
	}

}

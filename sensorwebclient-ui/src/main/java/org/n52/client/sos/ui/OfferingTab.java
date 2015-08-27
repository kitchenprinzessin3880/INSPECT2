package org.n52.client.sos.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.n52.client.bus.EventBus;
import org.n52.client.ctrl.TimeManager;
import org.n52.client.sos.ctrl.DataManagerSosImpl;
import org.n52.client.sos.ctrl.QCResponseCode;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.data.SOSDataSource;
import org.n52.client.sos.event.DatesChangedEvent;
import org.n52.client.sos.event.data.DeleteAllTimeSeriesEvent;
import org.n52.client.sos.event.data.GetFeatureEvent;
import org.n52.client.sos.event.data.GetOfferingEvent;
import org.n52.client.sos.event.data.GetPhenomenonsEvent;
import org.n52.client.sos.event.data.GetProcedureEvent;
import org.n52.client.sos.event.data.GetStationsEvent;
import org.n52.client.sos.event.handler.DatesChangedEventHandler;
import org.n52.client.sos.legend.TimeSeries;
import org.n52.client.ui.Toaster;
import org.n52.client.ui.legend.HistoryManager;
import org.n52.client.ui.legend.LegendEntryTimeSeries;
import org.n52.shared.serializable.pojos.BoundingBox;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;

import com.google.gwt.core.shared.GWT;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DateDisplayFormat;
import com.smartgwt.client.types.FormErrorOrientation;
import com.smartgwt.client.types.MultipleAppearance;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.RelativeDateItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;

public class OfferingTab extends Tab {
	private CheckboxItem checkboxItem;
	protected VLayout layout;
	private static final int COMBOBOX_WIDTH = 450;
	private RelativeDateItem startDate;
	private RelativeDateItem endDate;
	boolean isTimeSeleted = false;

	private SelectItem sosBox;

	private SelectItem offBox;

	private HashSet<String> defaultServices = new HashSet<String>();

	private SelectItem phenBox;

	private SelectItem sensorBox;

	/** The form. */
	private DynamicForm formTop;
	private DynamicForm formTemporal;
	private Canvas responseTextItem;
	private static OfferingTab instance;
	private static OfferingTabController controller;
	private IButton saveTemplateBtn;
	private IButton addTSBtn;
	private IButton overwriteTSBtn;
	private boolean isDateOrderValid = true;
	private static QueryTemplate queryTempInstance;
	private boolean isSOSMultiple = false;
	private boolean isOfferingMultiple = false;
	private ArrayList<String> sosURLMap = new ArrayList<String>();

	// ASD 10.11.2014
	private Img stationLoadingSpinner;
	VLayout wrapper;
	private boolean isStationFinishedLoading = false;


	public static OfferingTab getInst() {
		if (instance == null) {
			queryTempInstance = QueryTemplate.getInst();
			controller = OfferingTabController.getInstance(queryTempInstance);
			instance = new OfferingTab();
		}
		return instance;
	}

	private OfferingTab() {
		setTitle("Theme Search");
		new OfferingTabEventBroker();
		createFormContent();
	}

	private void createFormContent() {
		wrapper = new VLayout();
		wrapper.setMargin(30);
		wrapper.setAlign(Alignment.LEFT);
		wrapper.setWidth100();

		this.formTop = new DynamicForm();
		this.formTop.setNumCols(2);
		this.formTop.setColWidths("25%", "*");
		this.formTop.setCellPadding(15);
		this.formTop.setErrorOrientation(FormErrorOrientation.RIGHT);

		this.formTemporal = new DynamicForm();
		this.formTemporal.setTitle("Temporal");
		this.formTemporal.setNumCols(6);
		this.formTemporal.setErrorOrientation(FormErrorOrientation.RIGHT);

		this.sosBox = new SelectItem();
		this.sosBox.setTitle("Select Service");
		this.sosBox.setWidth(COMBOBOX_WIDTH);
		this.sosBox.setName("sos");
		this.sosBox.setRequired(true);

		this.offBox = new SelectItem();
		this.offBox.setDisabled(true);
		this.offBox.setName("offering");
		this.offBox.setTitle("Select Offering");
		this.offBox.setWidth(COMBOBOX_WIDTH);
		this.offBox.setRequired(true);

		this.sensorBox = new SelectItem();
		this.sensorBox.setDisabled(true);
		this.sensorBox.setName("procedure");
		this.sensorBox.setTitle("Select Station");
		this.sensorBox.setWidth(COMBOBOX_WIDTH);
		this.sensorBox.setRequired(true);

		this.phenBox = new SelectItem();
		this.phenBox.setDisabled(true);
		this.phenBox.setName("property");
		this.phenBox.setTitle("Select Property");
		this.phenBox.setMultiple(true);
		this.phenBox.setWidth(COMBOBOX_WIDTH);
		this.phenBox.setMultipleAppearance(MultipleAppearance.PICKLIST);
		this.phenBox.setRequired(true);

		sosBox = generateSOSBox();
		offBox = generateOfferingBox();
		sensorBox = generateSensorBox();
		phenBox = generatePropertiesBox();

		responseTextItem = new Canvas();
		responseTextItem.setVisible(false);

		HLayout bottomLayout = new HLayout();
		bottomLayout.setAlign(Alignment.CENTER);
		bottomLayout.setMargin(20);
		// createButton
		addTSBtn = new IButton("Add to Existing Series");
		addTSBtn.setWidth("130");
		addTSBtn.setTooltip("This button add selected time series to existing time series");
		addTSBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				boolean validMainForm = formTop.validate();
				boolean isTimeSelected = checkboxItem.getValueAsBoolean();

				if (isTimeSelected) {
					if (isDateOrderValid) {
						long start = ((Date) startDate.getValue()).getTime();
						long end = ((Date) endDate.getValue()).getTime();
						TimeManager.getInst().setIsOverviewTimeFixed(false);
						EventBus.getMainEventBus().fireEvent(
								new DatesChangedEvent(start, end, true));
					} else {
						validMainForm = false;
					}
				}

				if (validMainForm) {
					String[] propList = phenBox.getValues();
					ArrayList<String> phenomenaList = new ArrayList<String>(
							Arrays.asList(propList));
					TimeSeries[] timeSeriesExisting = DataStoreTimeSeriesImpl
							.getInst().getTimeSeriesSorted();

					if (timeSeriesExisting.length > 0) {
						for (int i = 0; i < propList.length; i++) {
							String newProperty = propList[i].trim();
							for (TimeSeries timeSerie : timeSeriesExisting) {
								String procedureOld = timeSerie
										.getProcedureId();
								String sosUrlOld = timeSerie.getSosUrl();
								String offOld = timeSerie.getOfferingId();
								String phenOld = timeSerie.getPhenomenonId();

								if (sosUrlOld.equalsIgnoreCase(sosBox
										.getValueAsString())
										&& offOld.equalsIgnoreCase(offBox
												.getValueAsString())
										&& procedureOld
												.equalsIgnoreCase(sensorBox
														.getValueAsString())
										&& phenOld
												.equalsIgnoreCase(newProperty)) {
									phenomenaList.remove(newProperty);
									Toaster.getInstance().addMessage(
											"Adding duplicate time series is not allowed:"
													+ sensorBox
															.getValueAsString()
													+ " - " + newProperty);
									break;
								}

							}
						}
					}

					String[] filteredPhenArray = new String[phenomenaList
							.size()];
					filteredPhenArray = phenomenaList
							.toArray(filteredPhenArray);
					if (filteredPhenArray.length > 0) {
						controller.addTimeSeries(sosBox.getValueAsString(),
								offBox.getValueAsString(),
								sensorBox.getValueAsString(), filteredPhenArray);
						StationSelector.getInst().hide();
					}
				}
			}
		});

		overwriteTSBtn = new IButton("Clear and Add New Series");
		overwriteTSBtn
				.setTooltip("This button clears all existing time series before adding new time series");
		overwriteTSBtn.setWidth("150");
		overwriteTSBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				boolean validMainForm = formTop.validate();
				boolean isTimeSelected = checkboxItem.getValueAsBoolean();

				if (isTimeSelected) {
					if (isDateOrderValid) {
						long start = ((Date) startDate.getValue()).getTime();
						long end = ((Date) endDate.getValue()).getTime();
						TimeManager.getInst().setIsOverviewTimeFixed(false);
						EventBus.getMainEventBus().fireEvent(
								new DatesChangedEvent(start, end, true));
					} else {
						validMainForm = false;
					}
				}

				DataStoreTimeSeriesImpl dataManager = DataStoreTimeSeriesImpl
						.getInst();
				TimeSeries[] timeSeriesExisting = dataManager
						.getTimeSeriesSorted();
				if (validMainForm) {
					if (timeSeriesExisting.length > 0) {
						// delete existing series
						for (int i = 0; i < timeSeriesExisting.length; i++) {
							// String id = timeSeriesExisting[i].getId();
							TimeSeries series = dataManager
									.getDataItem(timeSeriesExisting[i].getId());
							LegendEntryTimeSeries le = (LegendEntryTimeSeries) series
									.getLegendElement();
							le.getEventBroker().unregister();
						}
						EventBus.getMainEventBus().fireEvent(
								new DeleteAllTimeSeriesEvent());
					}

					String[] propList = phenBox.getValues();
					controller.addTimeSeries(sosBox.getValueAsString(),
							offBox.getValueAsString(),
							sensorBox.getValueAsString(), propList);
					StationSelector.getInst().hide();
				}
			}
		});

		IButton clearBtn = new IButton("Clear Form");
		clearBtn.setTooltip("This button clears all fields");
		clearBtn.setWidth("100");
		clearBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				clearThemeValues();
			}
		});

		HLayout timeLayout = new HLayout();
		timeLayout.setAlign(Alignment.RIGHT);
		timeLayout.setMargin(10);

		checkboxItem = new CheckboxItem();
		checkboxItem.setName("TimeCheck");
		checkboxItem.setTitle("Enable Time Range");
		checkboxItem.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				boolean checked = (Boolean) event.getValue();
				if (checked) {
					startDate.setDisabled(false);
					endDate.setDisabled(false);
					long start = TimeManager.getInst().getBegin();
					long end = TimeManager.getInst().getEnd();
					Date st = new Date(start);
					Date ed = new Date(end);
					startDate.setValue(st);
					endDate.setValue(ed);
				} else {
					startDate.setDisabled(true);
					endDate.setDisabled(true);
				}
			}
		});

		startDate = new RelativeDateItem("FromDateTime");
		startDate.setTitle("Start");
		startDate.setValidateOnExit(true);
		startDate.setDisabled(true);
		startDate.setDateFormatter(DateDisplayFormat.TOEUROPEANSHORTDATE);
		startDate.addChangedHandler(new ChangedHandler() {

			@Override
			public void onChanged(ChangedEvent event) {
				// TODO Auto-generated method stub
				String errorMsg = "";
				Date startDt = (Date) event.getValue();
				Date endDt = (Date) endDate.getValue();

				if (startDt.after(new Date())) {
					errorMsg = "Date must be today's date or prior.\n";
				}

				if (startDt.after(endDt) || startDt.equals(endDt)) {
					errorMsg += "The dates cannot be crossed or same.";
				}
				if (startDt.before(endDt) && startDt.before(new Date())
						&& !startDt.equals(endDt)) {
					isDateOrderValid = true;
					startDate.getForm().clearFieldErrors("FromDateTime", true);
					endDate.getForm().clearFieldErrors("ToDateTime", true);
				} else {
					isDateOrderValid = false;
					startDate.getForm().setFieldErrors("FromDateTime",
							errorMsg, true);
				}

			}
		});

		endDate = new RelativeDateItem("ToDateTime");
		endDate.setTitle("End");
		endDate.setDateFormatter(DateDisplayFormat.TOEUROPEANSHORTDATE);
		endDate.setValidateOnExit(true);
		endDate.setDisabled(true);
		endDate.addChangedHandler(new ChangedHandler() {
			@Override
			public void onChanged(ChangedEvent event) {
				// TODO Auto-generated method stub
				String errorMsg = "";
				Date endDt = (Date) event.getValue();
				Date startDt = (Date) startDate.getValue();
				if (endDt.after(new Date())) {
					errorMsg = "Date must be today's date or prior.\n";
				}

				if (endDt.before(startDt) || endDt.equals(startDt)) {
					errorMsg += "The dates cannot be crossed or same.";
				}
				if (endDt.after(startDt) && endDt.before(new Date())
						&& !endDt.equals(startDt)) {
					isDateOrderValid = true;
					startDate.getForm().clearFieldErrors("FromDateTime", true);
					endDate.getForm().clearFieldErrors("ToDateTime", true);
				} else {
					isDateOrderValid = false;
					endDate.getForm().setFieldErrors("ToDateTime", errorMsg,
							true);
				}
			}
		});

		Label space = new Label();
		space.setWidth(5);
		formTemporal.setAlign(Alignment.CENTER);
		formTemporal.setFields(checkboxItem, startDate, endDate);

		// createButton - save template
		saveTemplateBtn = new IButton("Save Current Search");
		saveTemplateBtn.setWidth(130);
		saveTemplateBtn
				.setTooltip("Click this button to save the search query");
		saveTemplateBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				boolean validMainForm = formTop.validate();
				// boolean isTimeSelected = checkboxItem.getValueAsBoolean();

				if (validMainForm) {
					String[] propList = phenBox.getValues();
					queryTempInstance.addQuery(sosBox.getValueAsString(),
							offBox.getValueAsString(),
							sensorBox.getValueAsString(), propList);

					// String user = DataManagerSosImpl.getInst().getUserName();
					// if(isTimeSelected)
					// {
					// if (isDateOrderValid)
					// {
					// String start = startDate.getValue().toString();
					// String end = endDate.getValue().toString();
					// queryTempInstance.addQuery(sosBox.getValueAsString(),
					// offBox.getValueAsString(), sensorBox.getValueAsString(),
					// propList,true,start,end);
					// }
					// }
					// else
					// {
					// queryTempInstance.addQuery(sosBox.getValueAsString(),
					// offBox.getValueAsString(), sensorBox.getValueAsString(),
					// propList,false, null,null);
					// }
				}
			}
		});

		bottomLayout.addMember(addTSBtn);
		bottomLayout.addMember(space);
		bottomLayout.addMember(overwriteTSBtn);
		bottomLayout.addMember(space);
		bottomLayout.addMember(clearBtn);
		bottomLayout.addMember(space);
		bottomLayout.addMember(saveTemplateBtn);
		bottomLayout.addMember(space);
		bottomLayout.addMember(space);

		/*
		 * final ListBox listBox = new ListBox(); listBox.addItem(" ");
		 * listBox.addItem("A"); listBox.addItem("B"); listBox.addItem("C");
		 * listBox.addChangeHandler(new
		 * com.google.gwt.event.dom.client.ChangeHandler() {
		 * 
		 * @Override public void
		 * onChange(com.google.gwt.event.dom.client.ChangeEvent changeEvent) {
		 * SelectElement selectElement = listBox.getElement().cast();
		 * selectElement.getOptions().getItem(0).setDisabled(true); int
		 * selectedIndex = listBox.getSelectedIndex();
		 * GWT.log(listBox.getValue(selectedIndex));
		 * 
		 * } });
		 */
		formTop.setFields(sosBox, offBox, sensorBox, phenBox);
		wrapper.addMember(formTop);
		// ASD 10.11.2014
		wrapper.addChild(createStationLoadingSpinner());

		wrapper.addMember(formTemporal);
		wrapper.addMember(bottomLayout);
		wrapper.addMember(responseTextItem);
		this.setPane(wrapper);

	}

	public void clearThemeValues() {
		if (isSOSMultiple) {
			sosBox.clearValue();
			offBox.clearValue();
			offBox.setDisabled(true);
			sensorBox.clearValue();
			sensorBox.setDisabled(true);
		}
		formTemporal.clearValues();
		phenBox.clearValue();
		checkboxItem.setValue(false);
		startDate.setDisabled(true);
		endDate.setDisabled(true);
	}

	public SelectItem getSosBox() {
		return sosBox;
	}

	public void setSosBox(SelectItem sosBox) {
		this.sosBox = sosBox;
	}

	protected SelectItem generateSOSBox() {
		SOSDataSource.getInstance().get("sosDataSource")
				.fetchData(null, new DSCallback() {
					public void execute(DSResponse response, Object rawData,
							DSRequest request) {
						Record[] records = response.getData();
						if (records.length > 1) {
							isSOSMultiple = true;
						} else {
							isSOSMultiple = false;
						}
						for (int a = 0; a < records.length; a++) {
							String sosURL = records[a].getAttribute("url")
									.toString();
							GetOfferingEvent getOffering = new GetOfferingEvent(sosURL, true);
							EventBus.getMainEventBus().fireEvent(getOffering);
							sosURLMap.add(sosURL);
							defaultServices.add(sosURL);
							SOSMetadata sosMetadata = DataManagerSosImpl.getInst().getServiceMetadata(sosURL);
							if (sosMetadata == null) {
								controller.parseAndStoreSOSMetadata(sosURL,records[a]);
								DataManagerSosImpl dataManager = DataManagerSosImpl.getInst();
								SOSMetadata meta = dataManager.getServiceMetadata(sosURL);
								BoundingBox bbox = meta.getConfiguredExtent();

								GetStationsEvent getStations = new GetStationsEvent(sosURL, bbox, true);
								EventBus.getMainEventBus().fireEvent(getStations);
								GetPhenomenonsEvent getPhenomenons = new GetPhenomenonsEvent.Builder(sosURL).build();
								EventBus.getMainEventBus().fireEvent(getPhenomenons);
							}
						}
						controller.setServicesList(sosURLMap);

						// ASD 6.10.2014 pre-events related to history
						HistoryManager.getInst().checkHistory();

						String[] stringSOSArray = new String[sosURLMap.size()];
						Collections.sort(sosURLMap,
								new SortListAlphabeticalOrder());
						sosURLMap.toArray(stringSOSArray);

						if (isSOSMultiple) {
							sosBox.setValueMap(stringSOSArray);
							sosBox.addChangeHandler(new ChangeHandler() {
								@Override
								public void onChange(ChangeEvent event) {
									String serviceURL = (String) event
											.getValue();

									// clear the rest boxes
									formTop.clearValue("offering");
									formTop.clearValue("procedure");
									formTop.clearValue("property");

									ArrayList<String> offeringList = controller
											.getOfferingBySOS(serviceURL);

									// sort hashset
									String[] stringOffArray = new String[offeringList
											.size()];
									Collections.sort(offeringList,
											new SortListAlphabeticalOrder());
									offeringList.toArray(stringOffArray);

									if (stringOffArray.length == 1) {
										isOfferingMultiple = false;
										formTop.getField("offering").setValue(
												stringOffArray[0]);
										GetOfferingEvent getOffEvent = new GetOfferingEvent(
												serviceURL, stringOffArray[0]);
										EventBus.getMainEventBus().fireEvent(
												getOffEvent);
										checkAndUpdateSensorBoxBySOSOffering(serviceURL);

									} else {
										isOfferingMultiple = true;
										formTop.getField("offering")
												.setValueMap(stringOffArray);
										formTop.getField("procedure")
												.setDisabled(true);
									}
									formTop.getField("offering").setDisabled(
											false);
								}
							});
						} else {
							formTop.getField("sos").setValue(stringSOSArray[0]);
							// ASD: TODO - one sos multiple offering
							// get all offering for a given sos
						}
					}
				});
		return this.sosBox;
	}

	public void checkAndUpdateOfferingBox(String url,
			ArrayList<String> offeringList) {
		String currentURL = sosBox.getValueAsString();
		if (!isSOSMultiple) {
			if (currentURL.equalsIgnoreCase(url)) {
				// clear the rest boxes
				formTop.clearValue("offering");
				formTop.clearValue("procedure");
				formTop.clearValue("property");

				// sort hashset
				String[] stringOffArray = new String[offeringList.size()];
				Collections.sort(offeringList, new SortListAlphabeticalOrder());
				offeringList.toArray(stringOffArray);

				if (stringOffArray.length == 1) {
					isOfferingMultiple = false;
					GetOfferingEvent getOffEvent = new GetOfferingEvent(url,
							stringOffArray[0]);
					EventBus.getMainEventBus().fireEvent(getOffEvent);
					formTop.getField("offering").setValue(stringOffArray[0]);
					formTop.getField("procedure").setDisabled(false);
					checkAndUpdateSensorBoxBySOSOffering(url);
				} else {
					isOfferingMultiple = true;
					formTop.getField("offering").setValueMap(stringOffArray);
					formTop.getField("procedure").setDisabled(true);
				}
				formTop.getField("offering").setDisabled(false);
				formTop.getField("offering").redraw();
			}
		}
	}

	public void checkAndUpdateSensorBoxBySOSOffering(String url) {
		if (!isOfferingMultiple) {
			formTop.clearValue("procedure");
			formTop.clearValue("property");

			String off = offBox.getValueAsString();
			ArrayList<String> senseList = controller.getSensorsByOffering(url,off);

			Set<String> treeSet = new TreeSet<String>(
					new SortListAlphabeticalOrder());
			treeSet.addAll(senseList);

			String[] stringArray = new String[treeSet.size()];
			treeSet.toArray(stringArray);

			formTop.getField("procedure").setValueMap(stringArray);
			formTop.getField("procedure").setDisabled(false);
			formTop.getField("procedure").redraw();
		}
	}

	protected SelectItem generateOfferingBox() {

		this.offBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String offeringId = (String) event.getValue();
				GetOfferingEvent getOffEvent = new GetOfferingEvent(sosBox
						.getValueAsString(), offeringId);
				EventBus.getMainEventBus().fireEvent(getOffEvent);

				formTop.clearValue("procedure");
				formTop.clearValue("property");

				ArrayList<String> senseList = controller.getSensorsByOffering(
						sosBox.getValueAsString(), offeringId);

				Set<String> treeSet = new TreeSet<String>(
						new SortListAlphabeticalOrder());
				treeSet.addAll(senseList);
				String[] stringArray = new String[treeSet.size()];
				treeSet.toArray(stringArray);

				sosBox.setValueMap(stringArray);
				formTop.getField("procedure").setValueMap(stringArray);
				formTop.getField("procedure").setDisabled(false);

			}

		});
		return this.offBox;
	}

	protected SelectItem generateSensorBox() {
		this.sensorBox.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				formTop.getField("property").clearValue();
				// form.clearValue("itemName");
				String sensorId = (String) event.getValue();

				SOSMetadata metadata = DataManagerSosImpl.getInst()
						.getServiceMetadata(sosBox.getValueAsString());
				if (metadata.getProcedure(sensorId) == null) {
					GetProcedureEvent getProcEvent = new GetProcedureEvent(
							sosBox.getValueAsString(), sensorId);
					EventBus.getMainEventBus().fireEvent(getProcEvent);
				}
				if (metadata.getFeature(sensorId) == null) {
					GetFeatureEvent getFoiEvent = new GetFeatureEvent(sosBox
							.getValueAsString(), sensorId);
					EventBus.getMainEventBus().fireEvent(getFoiEvent);
				}

				/*
				 * GetFeatureEvent getFoiEvent = new
				 * GetFeatureEvent(sosBox.getValueAsString(), sensorId);
				 * EventBus.getMainEventBus().fireEvent(getFoiEvent);
				 * GetProcedureEvent getProcEvent = new
				 * GetProcedureEvent(sosBox.getValueAsString(), sensorId);
				 * EventBus.getMainEventBus().fireEvent(getProcEvent);
				 */

				HashSet<String> propList = controller.getPropertiesBySensors(
						sensorId, offBox.getValueAsString(),
						sosBox.getValueAsString());

				Set<String> treeSet = new TreeSet<String>(
						new SortListAlphabeticalOrder());
				treeSet.addAll(propList);
				String[] stringArray = new String[treeSet.size()];
				treeSet.toArray(stringArray);

				formTop.getField("property").setValueMap(stringArray);
				formTop.getField("property").setDisabled(false);
			}
		});
		return sensorBox;
	}

	protected SelectItem generatePropertiesBox() {

		this.phenBox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				// String id = (String) event.getValue();
			}

		});

		return phenBox;
	}

	public class SortListAlphabeticalOrder implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			String u1 = (String) o1;
			String u2 = (String) o2;
			return u1.compareTo(u2);
		}
	}

	public void updateReponseTextItem(QCResponseCode msg) {
		this.responseTextItem.setVisible(true);
		this.responseTextItem
				.setContents("<span style='color:" + msg.getColor() + ";'>"
						+ msg.getResponseMessage() + "</span>");
	}

	public static OfferingTabController getController() {
		return controller;
	}

	public static void setController(OfferingTabController controller) {
		OfferingTab.controller = controller;
	}

	private class OfferingTabEventBroker implements DatesChangedEventHandler {

		public OfferingTabEventBroker() {
			EventBus.getMainEventBus().addHandler(DatesChangedEvent.TYPE, this);
		}

		@Override
		public void onDatesChanged(DatesChangedEvent evt) {
			// TODO Auto-generated method stub
			startDate.setValue(new Date(evt.getStart()));
			endDate.setValue(new Date(evt.getEnd()));
		}

	}

	public class ConfigurableSelectItem extends SelectItem {

		private void fireChangedEvent() {
		};

		@Override
		public void setValue(String value) {
			super.setValue(value);
			fireChangedEvent();
		}

	}

	public HashSet<String> getDefaultServices() {
		return defaultServices;
	}

	public void setDefaultServices(HashSet<String> defaultServices) {
		this.defaultServices = defaultServices;
	}

	// ASD 10.11.2014
	private Canvas createStationLoadingSpinner() {
		stationLoadingSpinner = new Img("../img/loader_wide.gif", 60, 11);
		setStationLoadingSpinnerPosition();
		return stationLoadingSpinner;	
	}

	// ASD 10.11.2014
	private void setStationLoadingSpinnerPosition() {
		stationLoadingSpinner.setTop((650 - stationLoadingSpinner.getHeight()) / 2);
		stationLoadingSpinner.setLeft((800 - stationLoadingSpinner.getWidth()) / 2);
	}

	public void showStationLoadingSpinner(boolean show) {
		if (show) {
			// ASD 10.11.2014
			if (!stationLoadingSpinner.isVisible()) {
				stationLoadingSpinner.show();
			}
		} else {
			if (stationLoadingSpinner.isVisible()) {
				stationLoadingSpinner.hide();
			}
		}
	} 


	public boolean isStationFinishedLoading() {
		return isStationFinishedLoading;
	}

	public void setStationFinishedLoading(boolean isStationFinishedLoading) {
		this.isStationFinishedLoading = isStationFinishedLoading;
	}
}

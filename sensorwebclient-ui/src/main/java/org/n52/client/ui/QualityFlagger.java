package org.n52.client.ui;

import static org.n52.client.sos.i18n.SosStringsAccessor.i18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import org.eesgmbh.gimv.client.event.LoadImageDataEvent;
import org.n52.client.bus.EventBus;
import org.n52.client.ctrl.PropertiesManager;
import org.n52.client.sos.ctrl.DataManagerSosImpl;
import org.n52.client.sos.ctrl.QCResponseCode;
import org.n52.client.sos.ctrl.QualityFlaggingController;
import org.n52.client.sos.ctrl.SOSRequestManager;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.event.ChangeTimeSeriesStyleEvent;
import org.n52.client.sos.event.LegendElementSelectedEvent;
import org.n52.client.sos.event.data.AddFlagEvent;
import org.n52.client.sos.event.data.ClearSelectedPointEvent;
import org.n52.client.sos.event.data.DeleteAllTimeSeriesEvent;
import org.n52.client.sos.event.data.DeleteTimeSeriesEvent;
import org.n52.client.sos.event.data.FinishedAddFlagEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent;
import org.n52.client.sos.event.data.StoreTimeSeriesEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent.FlaggingPointType;
import org.n52.client.sos.event.data.handler.DeleteAllTimeSeriesEventHandler;
import org.n52.client.sos.event.data.handler.DeleteTimeSeriesEventHandler;
import org.n52.client.sos.event.data.handler.FinishedAddFlagEventHandler;
import org.n52.client.sos.event.handler.ChangeTimeSeriesStyleEventHandler;
import org.n52.client.sos.event.handler.LegendElementSelectedEventHandler;
import org.n52.client.sos.legend.TimeSeries;
import org.n52.shared.serializable.pojos.TimeSeriesProperties;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.regexp.shared.RegExp;
import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.FormErrorOrientation;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;

//ASD-2013
public class QualityFlagger extends Tab {

	private static final String COMPONENT_ID = "qualityControlFlagger";
	private static ListGrid mainGrid;
	private static QualityFlagger instance;
	private String selectedTimeSeriesId;
	private DynamicForm formRange;
	private DynamicForm formOffline;
	private CheckboxItem offLineCheckboxItem;
	private String activeTextField = "";
	private TextItem startTxt;
	private TextItem endTxt;
	private SelectItem flagSelect;
	private SelectItem flagSpecificSelect;
	private HLayout exportLoadingSpinner;
	private ListGridRecord selectedTabRecord;
	private static LinkedHashMap<String, String> lMap = new LinkedHashMap<String, String>();
	private RadioGroupItem radioGroupItem;
	private Map<String, ArrayList<String>> codeList = null;
	IButton approveButton;

	public static QualityFlagger getInst() {
		if (instance == null) {
			instance = new QualityFlagger();
		}
		return instance;
	}

	private QualityFlagger() {
		// setShowModalMask(true);
		new QualityFlaggerEventBroker();
		setID(COMPONENT_ID);
		/*
		 * setIsModal(false); setCanDragResize(true);
		 * setShowMaximizeButton(true); setShowMinimizeButton(false);
		 * setMargin(0);
		 */
		setTitle(i18n.qualityFlaggingRange());
		initializeQualityRangeWindow();

		/*
		 * addCloseClickHandler(new CloseClickHandler() { public void
		 * onCloseClick(CloseClickEvent event) { closeQualityFlagger(); } });
		 */

	}

	private void initializeQualityRangeWindow() {

		VLayout messageLayout = new VLayout();
		// messageLayout.setBorder("1px solid #6a6a6a");
		messageLayout.setLayoutMargin(15);
		messageLayout.setMembersMargin(3);

		if (formRange == null) {
			formRange = new DynamicForm();
			formRange.setWidth100();
			formRange.setPadding(10);
			formRange.setLayoutAlign(VerticalAlignment.CENTER);
			formRange.setNumCols(2);
			formRange.setErrorOrientation(FormErrorOrientation.RIGHT);

			// -------- OFFLINE ---------------
			formOffline = new DynamicForm();
			offLineCheckboxItem = new CheckboxItem();
			offLineCheckboxItem.setTitle("Offline");
			offLineCheckboxItem.setHeight(25);
			offLineCheckboxItem.setTitle("Enable Offline Flagging (flagging status will be notified via email)");
			offLineCheckboxItem.setStartRow(true);
			offLineCheckboxItem.setEndRow(true);
			formOffline.setFields(offLineCheckboxItem);

			Label label = new Label();
			label.setHeight(20);
			label.setPadding(10);
			label.setAlign(Alignment.CENTER);
			label.setWrap(false);
			label.setContents("Note: The following table only includes permitted time series. ");

			startTxt = new TextItem();
			startTxt.setShowTitle(false);
			startTxt.setName("Start");
			startTxt.setCanEdit(true);
			startTxt.setStartRow(false);
			startTxt.setEndRow(false);
			startTxt.setRequired(true);
			startTxt.setValue("");

			endTxt = new TextItem();
			endTxt.setShowTitle(false);
			endTxt.setName("End");
			endTxt.setStartRow(false);
			endTxt.setEndRow(true);
			endTxt.setCanEdit(true);
			endTxt.setRequired(true);
			endTxt.setValue("");

			lMap.put("START", "Start Date");
			lMap.put("END", "End Date");

			radioGroupItem = new RadioGroupItem();
			radioGroupItem.setShowTitle(false);
			radioGroupItem.setValueMap(lMap);
			radioGroupItem.setStartRow(true);
			radioGroupItem.setEndRow(true);
			radioGroupItem.setColSpan(2);
			radioGroupItem.setVertical(false);
			radioGroupItem.setDefaultValue("START");

			createExportLoadingSpinner();

			// fire default selection
			// if (QualityControlTab.getInst().getTabSelected() == 1)
			// {
			// only if active tab = time range tab
			SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.START);
			EventBus.getMainEventBus().fireEvent(setTimeRange);
			// }

			radioGroupItem.addChangedHandler(new ChangedHandler() {
				@Override
				public void onChanged(ChangedEvent event) {
					// TODO Auto-generated method stub
					String value = event.getValue().toString();
					if (value.equalsIgnoreCase("Start Date")) {
						// fire event to get coordinate
						// startTxt.setSelectOnFocus(true);
					} else {
						endTxt.setSelectOnFocus(true);
					}
					SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.TIMERANGE);
					EventBus.getMainEventBus().fireEvent(setTimeRange);
				}

			});

			codeList = new HashMap<String, ArrayList<String>>();
			codeList = DataManagerSosImpl.getInst().getQualityCodeList();

			Map<String, ArrayList<String>> codeListModified = new HashMap<String, ArrayList<String>>();
			codeListModified.putAll(codeList);
			codeListModified.remove("unevaluated");

			String[] genericFlags = codeListModified.keySet().toArray(new String[codeListModified.size()]);

			// get flag code
			flagSpecificSelect = new SelectItem("SpecificCode", "Specific Flag");
			flagSpecificSelect.setAlign(Alignment.LEFT);
			flagSpecificSelect.setAddUnknownValues(false);
			flagSpecificSelect.setRequired(true); 
			
			flagSelect = new SelectItem("FlagCode", "Generic Flag");
			flagSelect.setAlign(Alignment.LEFT);
			flagSelect.setRequired(true);
			flagSelect.setValueMap(genericFlags);
			flagSelect.addChangeHandler(new ChangeHandler() {
				public void onChange(ChangeEvent event) {
					String selectedItem = (String) event.getValue();
					String[] specificArray = codeList.get(selectedItem).toArray(new String[codeList.get(selectedItem).size()]);
					formRange.getField("SpecificCode").setValueMap(specificArray);
					if (specificArray.length == 1 && specificArray[0].equalsIgnoreCase(selectedItem)) {
						formRange.getField("SpecificCode").setValue(selectedItem);
						flagSpecificSelect.setDisabled(true);
					} else {
						flagSpecificSelect.setDisabled(false);
					}

				}
			});

			final IButton addFlagButton = new IButton("Add Flag");
			addFlagButton.setTooltip("This button only updates data values with unevaluated flags.");
			addFlagButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					boolean dateComparison = false;
					boolean valid = formRange.validate();
					boolean isFirstWellFormatted = false;
					boolean isSecondWellFormatted = false;
					String errorMsg = "";
					if (valid) {
						Date first = null;
						Date end = null;
						// GWT doesnot support CET
						String startDate = startTxt.getValue().toString().replaceAll("CET", "").trim();
						String endDate = endTxt.getValue().toString().replaceAll("CET", "").trim();

						if (!isTimeZoneValid(startDate)) {
							errorMsg += "(Start Date) " + QCResponseCode.TIMEZONEERROR.getResponseMessage() + "<br>";
						}
						if (!isTimeZoneValid(endDate)) {
							errorMsg += "(End Date) " + QCResponseCode.TIMEZONEERROR.getResponseMessage() + "<br>";
						}

						if (isTimeZoneValid(startDate) && isTimeZoneValid(endDate)) {
							try {
								first = toDate(startDate);
								isFirstWellFormatted = true;
							} catch (IllegalArgumentException iae) {
								GWT.log("QualityFlagger - Start Date IllegalArgumentException.");
								errorMsg += QCResponseCode.INVALIDSTARTDATEFORMAT.getResponseMessage();
							}
							try {
								end = toDate(endDate);
								isSecondWellFormatted = true;
							} catch (IllegalArgumentException iae) {
								GWT.log("QualityFlagger - End Date IllegalArgumentException.");
								errorMsg += QCResponseCode.INVALIDENDDATEFORMAT.getResponseMessage() + "<br>";
							}
						}

						if (isFirstWellFormatted && isSecondWellFormatted) {
							dateComparison = first.before(end);
							if (!dateComparison) {
								errorMsg += QCResponseCode.TIMERANGEINVALID.getResponseMessage() + "<br>";
							}
						}

						if (isFirstWellFormatted && isSecondWellFormatted && dateComparison) {
							// check if offline
							boolean checked = offLineCheckboxItem.getValueAsBoolean();
							if (checked) {
								EventBus.getMainEventBus().fireEvent(new AddFlagEvent(false, true, false));
							} else {
								EventBus.getMainEventBus().fireEvent(new AddFlagEvent(false, false, false));
							}
						} else {
							responseTextItem.setVisible(true);
							responseTextItem.setContents("<p style='color:" + "red" + ";'>" + errorMsg + "</p>");
						}

					}

				}
			});

			final IButton modifyFlagButton = new IButton("Overwrite Flag");
			modifyFlagButton.setTooltip("This button overwrites the flags of all selected data values.");
			// modifyFlagButton.setEndRow(false);
			modifyFlagButton.addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent event) {
					boolean dateComparison = false;
					boolean valid = formRange.validate();
					boolean isFirstWellFormatted = false;
					boolean isSecondWellFormatted = false;
					String errorMsg = "";
					if (valid) {
						Date first = null;
						Date end = null;
						// GWT doesnot support CET
						String startDate = startTxt.getValue().toString().replaceAll("CET", "").trim();
						String endDate = endTxt.getValue().toString().replaceAll("CET", "").trim();

						if (!isTimeZoneValid(startDate)) {
							errorMsg += "(Start Date) " + QCResponseCode.TIMEZONEERROR.getResponseMessage() + "<br>";
						}
						if (!isTimeZoneValid(endDate)) {
							errorMsg += "(End Date) " + QCResponseCode.TIMEZONEERROR.getResponseMessage() + "<br>";
						}

						if (isTimeZoneValid(startDate) && isTimeZoneValid(endDate)) {
							try {
								first = toDate(startDate);
								isFirstWellFormatted = true;
							} catch (IllegalArgumentException iae) {
								GWT.log("QualityFlagger - Start Date IllegalArgumentException.");
								errorMsg += QCResponseCode.INVALIDSTARTDATEFORMAT.getResponseMessage();
							}
							try {
								end = toDate(endDate);
								isSecondWellFormatted = true;
							} catch (IllegalArgumentException iae) {
								GWT.log("QualityFlagger - End Date IllegalArgumentException.");
								errorMsg += QCResponseCode.INVALIDENDDATEFORMAT.getResponseMessage() + "<br>";
							}
						}

						if (isFirstWellFormatted && isSecondWellFormatted) {
							dateComparison = first.before(end);
							if (!dateComparison) {
								errorMsg += QCResponseCode.TIMERANGEINVALID.getResponseMessage() + "<br>";
							}
						}

						if (isFirstWellFormatted && isSecondWellFormatted && dateComparison) {
							boolean checked = offLineCheckboxItem.getValueAsBoolean();
							if (checked) {
								EventBus.getMainEventBus().fireEvent(new AddFlagEvent(true, true, false));
							} else {
								EventBus.getMainEventBus().fireEvent(new AddFlagEvent(true, false, false));
							}
						} else {
							responseTextItem.setVisible(true);
							responseTextItem.setContents("<p style='color:" + "red" + ";'>" + errorMsg + "</p>");
						}

					}

				}
			});

			approveButton = new IButton("Approve Data Series");
			approveButton.setTooltip("This button only updates the data level of non-unevaluated data. Their existing flags remain the same.");
			approveButton.setWidth(130);
			approveButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					boolean dateComparison = false;
					boolean isFirstWellFormatted = false;
					boolean isSecondWellFormatted = false;
					String errorMsg = "";
					Date first = null;
					Date end = null;
					// GWT doesnot support CET
					String startDate = startTxt.getValue().toString().replaceAll("CET", "").trim();
					String endDate = endTxt.getValue().toString().replaceAll("CET", "").trim();

					if (!isTimeZoneValid(startDate)) {
						errorMsg += "(Start Date) " + QCResponseCode.TIMEZONEERROR.getResponseMessage() + "<br>";
					}
					if (!isTimeZoneValid(endDate)) {
						errorMsg += "(End Date) " + QCResponseCode.TIMEZONEERROR.getResponseMessage() + "<br>";
					}

					if (isTimeZoneValid(startDate) && isTimeZoneValid(endDate)) {
						try {
							first = toDate(startDate);
							isFirstWellFormatted = true;
						} catch (IllegalArgumentException iae) {
							GWT.log("QualityFlagger - Start Date IllegalArgumentException.");
							errorMsg += QCResponseCode.INVALIDSTARTDATEFORMAT.getResponseMessage();
						}
						try {
							end = toDate(endDate);
							isSecondWellFormatted = true;
						} catch (IllegalArgumentException iae) {
							GWT.log("QualityFlagger - End Date IllegalArgumentException.");
							errorMsg += QCResponseCode.INVALIDENDDATEFORMAT.getResponseMessage() + "<br>";
						}
					}

					if (isFirstWellFormatted && isSecondWellFormatted) {
						dateComparison = first.before(end);
						if (!dateComparison) {
							errorMsg += QCResponseCode.TIMERANGEINVALID.getResponseMessage() + "<br>";
						}
					}
					
					if (isFirstWellFormatted && isSecondWellFormatted && dateComparison) {
						boolean checked = offLineCheckboxItem.getValueAsBoolean();
						QualityFlaggingController.getInstance().approveDataSeries(checked, startDate,endDate,getMainGrid().getSelectedRecords());
					} else {
						responseTextItem.setVisible(true);
						responseTextItem.setContents("<p style='color:" + "red" + ";'>" + errorMsg + "</p>");
					}
				}
			});
			
			mainGrid = new ListGrid();
			mainGrid.setAlign(Alignment.CENTER);
			mainGrid.setSelectionType(SelectionStyle.SIMPLE);
			mainGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
			mainGrid.setAutoFitData(Autofit.VERTICAL);
			mainGrid.setMargin(5);
			mainGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
				@Override
				public void onSelectionChanged(SelectionEvent event) {
					ListGrid grid = (ListGrid) event.getSource();
					ListGridRecord[] record = grid.getSelectedRecords();
					if (record.length > 0) {
						addFlagButton.setDisabled(false);
						modifyFlagButton.setDisabled(false);
						checkPreSelectedRecord();
					} else {
						addFlagButton.setDisabled(true);
						modifyFlagButton.setDisabled(true);
						checkPreSelectedRecord();
					}
				}
			});

			ListGridField tsField = new ListGridField("ts", "SeriesId");
			tsField.setHidden(true);
			ListGridField colorField = new ListGridField("color", "Series", 50);
			colorField.setCanSort(false);
			colorField.setAlign(Alignment.CENTER);
			ListGridField sensorField = new ListGridField("sensor", "Station");
			sensorField.setCanSort(false);
			ListGridField propField = new ListGridField("property", "Property");
			propField.setCanSort(false);
			mainGrid.setFields(colorField, sensorField, propField);
			updateListGridRecords();

			// mainGrid.setSort(new SortSpecifier[] { new
			// SortSpecifier("sensor", SortDirection.ASCENDING) });
			mainGrid.getField("color").setCellFormatter(new CellFormatter() {
				@Override
				public String format(Object value, ListGridRecord record, int rowNum, int colNum) {
					String color = record.getAttribute("color");
					return "<div style=\"width:17px;height:14px;background-color:" + color + ";\">" + "</div>";
				}
			});

			IButton clearFlagButton = new IButton("Clear");
			// clearFlagButton.setStartRow(false);
			// clearFlagButton.setEndRow(true);
			clearFlagButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					formRange.clearValues();
					mainGrid.deselectAllRecords();
					radioGroupItem.setDefaultValue("START");
					if (responseTextItem.isVisible()) {
						clearReponseTextItem();
					}
					// fire default selection
					SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.TIMERANGE);
					EventBus.getMainEventBus().fireEvent(setTimeRange);

					EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
					EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
				}
			});

			responseTextItem = new HTMLFlow();
			responseTextItem.setVisible(false);

			final HLayout buttonLayout = new HLayout();
			buttonLayout.setWidth100();
			buttonLayout.setLayoutMargin(8);
			buttonLayout.setMembersMargin(5);
			buttonLayout.setAlign(VerticalAlignment.CENTER);
			buttonLayout.addMember(addFlagButton);
			buttonLayout.addMember(modifyFlagButton);
			buttonLayout.addMember(approveButton);
			buttonLayout.addMember(clearFlagButton);

			Label space = new Label();
			space.setWidth("1%");

			formRange.setFields(radioGroupItem, startTxt, endTxt, flagSelect, flagSpecificSelect);
			messageLayout.addMember(formRange);
			messageLayout.addMember(label);
			messageLayout.addMember(mainGrid);
			messageLayout.addMember(formOffline);
			messageLayout.addMember(buttonLayout);
			messageLayout.addMember(exportLoadingSpinner);
			messageLayout.addMember(responseTextItem);
			this.setPane(messageLayout);
		}

	}

	private boolean isTimeZoneValid(String date) {
		int index = date.lastIndexOf(":") + 4; // 2014-01-26 22:15:00+0100
		String tzone = date.substring(index, date.length());
		if (tzone.length() == 4 && tzone.matches("[0-9]+")) // +0100
		{
			return true;
		} else {
			return false;
		}
	}

	public Date toDate(String date) {
		DateTimeFormat f = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ssZZZ");
		return f.parseStrict(date);
	}

	private static boolean stringContainsItemFromList(String inputString, String[] items) {
		for (int i = 0; i < items.length; i++) {
			if (items[i].equalsIgnoreCase(inputString)) {
				return true;
			}
		}
		return false;
	}

	public static String[] removeElements(ArrayList<String> input, String deleteMe) {
		List result = new LinkedList();
		for (String item : input) {
			if (!deleteMe.equals(item)) {
				result.add(item);
			}
		}

		String array[] = (String[]) result.toArray(new String[result.size()]);
		return array;
	}

	public DynamicForm getQualityForm() {
		return formRange;
	}

	public RadioGroupItem getRadioGroupItem() {
		return this.radioGroupItem;
	}

	public TextItem getStartTextITem() {
		return this.startTxt;
	}

	public TextItem getEndTextITem() {
		return this.endTxt;
	}

	public SelectItem getQualityFlag() {
		return this.flagSelect;
	}

	/*
	 * public void closeQualityFlagger() { formRange.reset();
	 * clearReponseTextItem(); EventBus.getMainEventBus().fireEvent(new
	 * ClearSelectedPointEvent()); // set quality control status
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetQualityControlStatusEvent(false)); //
	 * EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent()); }
	 */

	public String getActiveTextField() {
		return this.activeTextField;
	}

	public void updateReponseTextItem(QCResponseCode msg) {
		this.responseTextItem.setVisible(true);
		this.responseTextItem.setContents("<span style='color:" + msg.getColor() + ";'>" + msg.getResponseMessage() + "</span>");
	}

	public SelectItem getFlagSpecificSelect() {
		return flagSpecificSelect;
	}

	public void setFlagSpecificSelect(SelectItem flagSpecificSelect) {
		this.flagSpecificSelect = flagSpecificSelect;
	}

	public void clearReponseTextItem() {
		this.responseTextItem.setVisible(false);
		this.responseTextItem.setContents("");
	}

	public DynamicForm getFormRange() {
		return formRange;
	}

	public void setFormRange(DynamicForm formRange) {
		this.formRange = formRange;
	}

	private void createExportLoadingSpinner() {
		this.exportLoadingSpinner = new HLayout();
		this.exportLoadingSpinner.setWidth100();
		Img spinner = new Img("../img/loader_wide.gif", 60, 11);
		// this.exportLoadingSpinner.setWidth100();
		// this.exportLoadingSpinner.setHeight100();
		this.exportLoadingSpinner.setAlign(Alignment.CENTER);
		this.exportLoadingSpinner.addMember(spinner);
		this.exportLoadingSpinner.hide();
	}

	private class QualityFlaggerEventBroker implements DeleteAllTimeSeriesEventHandler, DeleteTimeSeriesEventHandler, FinishedAddFlagEventHandler, ChangeTimeSeriesStyleEventHandler, LegendElementSelectedEventHandler {
		QualityFlaggerEventBroker() {
			EventBus.getMainEventBus().addHandler(DeleteAllTimeSeriesEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(FinishedAddFlagEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(LegendElementSelectedEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(DeleteTimeSeriesEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(ChangeTimeSeriesStyleEvent.TYPE, this);
		}

		@Override
		public void onFinishedAddFlag(FinishedAddFlagEvent evt) {
			// TODO Auto-generated method stub
			if (exportLoadingSpinner.isVisible()) {
				exportLoadingSpinner.hide();
			}
		}

		@Override
		public void onSelected(LegendElementSelectedEvent evt) {
			if (evt.getElement().getDataWrapper() instanceof TimeSeries) {
				TimeSeries ts = (TimeSeries) evt.getElement().getDataWrapper();
				setSelectedTimeSeriesId(ts.getId());
				updateListGridRecords();
				checkPreSelectedRecord();
			}
		}

		@Override
		public void onDeleteTimeSeries(DeleteTimeSeriesEvent evt) {
			String idDeleted = evt.getId();
			ListGridRecord[] tRecords = mainGrid.getRecords();
			for (int tI = 0; tI < tRecords.length; tI++) {
				if (tRecords[tI].getAttribute("ts").toString().equalsIgnoreCase(idDeleted)) {
					mainGrid.removeData(tRecords[tI]);
				}
			}
			if (mainGrid.getRecords().length == 0) {
				mainGrid.setDisabled(true);
			}
		}

		@Override
		public void onChange(ChangeTimeSeriesStyleEvent evt) {
			setSelectedTimeSeriesId(evt.getID());
			updateListGridRecords();
			checkPreSelectedRecord();
		}

		@Override
		public void onDeleteAllTimeSeries(DeleteAllTimeSeriesEvent evt) {
			// TODO Auto-generated method stub
			ListGridRecord[] selectedRecords = mainGrid.getRecords();
			for (ListGridRecord rec : selectedRecords) {
				mainGrid.removeData(rec);
			}
			mainGrid.setDisabled(true);
		}
	}

	public Map<String, ArrayList<String>> getCodeList() {
		return codeList;
	}

	public void setCodeList(Map<String, ArrayList<String>> codeList) {
		this.codeList = codeList;
	}

	private HTMLFlow responseTextItem;

	public HTMLFlow getResponseTextItem() {
		return responseTextItem;
	}

	public void setResponseTextItem(HTMLFlow responseTextItem) {
		this.responseTextItem = responseTextItem;
	}

	public ListGridRecord[] updateListGridRecords() {
		Collection<TimeSeries> filteredSeries = QualityFlaggingController.getInstance().filterPermittedSitesAndProperties();
		TimeSeries[] ts = (TimeSeries[]) filteredSeries.toArray(new TimeSeries[filteredSeries.size()]);
		ListGridRecord[] records = new ListGridRecord[ts.length];
		if (filteredSeries.size() > 0) {
			for (int i = 0; i < ts.length; i++) {
				ListGridRecord record = new ListGridRecord();
				String id = ts[i].getId();
				String color = ts[i].getColor().trim();
				String proc = ts[i].getProcedureId().trim();
				String phen = ts[i].getPhenomenonId().trim();
				record.setAttribute("ts", id);
				record.setAttribute("color", color);
				record.setAttribute("sensor", proc);
				record.setAttribute("property", phen);
				records[i] = record;
				if (selectedTimeSeriesId != null) {
					if (id.equalsIgnoreCase(selectedTimeSeriesId)) {
						this.selectedTabRecord = records[i];
					}
				}
			}
			mainGrid.setDisabled(false);
			mainGrid.setData(records);
		} else {
			mainGrid.setDisabled(true);
		}

		return records;
	}

	public void checkPreSelectedRecord() {
		ListGridRecord[] selectedRecords = mainGrid.getSelectedRecords();
		if (selectedRecords.length > 0) {
			List<ListGridRecord> selectedRecordsList = Arrays.asList(selectedRecords);
			if (selectedRecordsList.contains(getSelectedTabRecord())) {
				// the record is already selected, so do nothing
			} else {
				// the record is already deselected, so select it.
				if (getSelectedTabRecord() != null) {
					mainGrid.selectRecord(getSelectedTabRecord());
				}
			}
		} else {
			// the record is already deselected, so select it.
			if (getSelectedTabRecord() != null) {
				mainGrid.selectRecord(getSelectedTabRecord());
			}
		}
	}

	public void setSelectedTimeSeriesId(String timeSeriesId) {
		this.selectedTimeSeriesId = timeSeriesId;
	}

	public String getSelectedTimeSeriesId() {
		return selectedTimeSeriesId;
	}

	public ListGridRecord getSelectedTabRecord() {
		return selectedTabRecord;
	}

	public void setSelectedTabRecord(ListGridRecord selectedRecord) {
		this.selectedTabRecord = selectedRecord;
	}

	public ListGrid getMainGrid() {
		return mainGrid;
	}

	public void setMainGrid(ListGrid mainGrid) {
		this.mainGrid = mainGrid;
	}

	public HLayout getExportLoadingSpinner() {
		return exportLoadingSpinner;
	}

	public void clearLocalTabValues() {
		//formRange.clearValues();
		radioGroupItem.clearValue();
		startTxt.clearValue();
		endTxt.clearValue();
		if (responseTextItem.isVisible()) {
			clearReponseTextItem();
		}
	}

}

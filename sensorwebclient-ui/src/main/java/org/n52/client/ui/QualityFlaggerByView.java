package org.n52.client.ui;
import static org.n52.client.sos.i18n.SosStringsAccessor.i18n;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.n52.client.bus.EventBus;
import org.n52.client.ctrl.TimeManager;
import org.n52.client.sos.ctrl.DataManagerSosImpl;
import org.n52.client.sos.ctrl.QCResponseCode;
import org.n52.client.sos.ctrl.QualityFlaggingController;
import org.n52.client.sos.event.ChangeTimeSeriesStyleEvent;
import org.n52.client.sos.event.DatesChangedEvent;
import org.n52.client.sos.event.LegendElementSelectedEvent;
import org.n52.client.sos.event.data.AddFlagEvent;
import org.n52.client.sos.event.data.ClearSelectedPointEvent;
import org.n52.client.sos.event.data.DeleteAllTimeSeriesEvent;
import org.n52.client.sos.event.data.DeleteTimeSeriesEvent;
import org.n52.client.sos.event.data.FinishedAddFlagEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent.FlaggingPointType;
import org.n52.client.sos.event.data.handler.DeleteAllTimeSeriesEventHandler;
import org.n52.client.sos.event.data.handler.DeleteTimeSeriesEventHandler;
import org.n52.client.sos.event.data.handler.FinishedAddFlagEventHandler;
import org.n52.client.sos.event.handler.ChangeTimeSeriesStyleEventHandler;
import org.n52.client.sos.event.handler.DatesChangedEventHandler;
import org.n52.client.sos.event.handler.LegendElementSelectedEventHandler;
import org.n52.client.sos.legend.TimeSeries;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyDownEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
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
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

//@ASD-2013
public class QualityFlaggerByView extends Tab {

	private static final String COMPONENT_ID = "qualityControlFlaggerByView";
	private Map<String, ArrayList<String>> codeList = null;
	private static QualityFlaggerByView instance;
	private HLayout exportLoadingSpinner;
	private DynamicForm formRange;
	private DynamicForm formRules;
	private DynamicForm formOffline;
	private long begin;
	private long end;
	private HTMLFlow responseTextItem;
	private SelectItem flagSelect;
	private SelectItem flagSpecificSelect;

	private TextItem startTxt;
	private TextItem endTxt;
	private static ListGrid mainGrid;
	private CheckboxItem offLineCheckboxItem;
	private CheckboxItem ruleCheckboxItem;
	private TextItem ruleBasedValue;
	private ComboBoxItem rulePropertyBox;
	private ComboBoxItem ruleOperatorBox;
	IButton approveButton;
	IButton addFlagButton;
	IButton modifyFlagButton;
	private String[] permittedProps;

	public HTMLFlow getResponseTextItem() {
		return responseTextItem;
	}

	public void setResponseTextItem(HTMLFlow responseTextItem) {
		this.responseTextItem = responseTextItem;
	}

	// private CheckboxItem checkboxItem;

	public static QualityFlaggerByView getInst() {

		if (instance == null) {
			instance = new QualityFlaggerByView();
		}
		return instance;
	}

	private QualityFlaggerByView() {
		new QualityFlaggerByViewEventBroker();

		setID(COMPONENT_ID);
		/*
		 * setIsModal(false); setCanDragResize(true);
		 * setShowMaximizeButton(true); setShowMinimizeButton(false);
		 * setMargin(0); setWidth(WIDTH_RANGE); setHeight(HEIGHT_RANGE);
		 */
		setTitle(i18n.qualityFlaggingView());
		initializeQualityRuleWindow();

		/*
		 * addCloseClickHandler(new CloseClickHandler() { public void
		 * onCloseClick(CloseClickEvent event) { closeQualityFlagger(); } });
		 */
	}

	private void initializeQualityRuleWindow() {

		VLayout messageLayout = new VLayout();
		messageLayout.setLayoutMargin(15);
		messageLayout.setMembersMargin(4);

		// onLoad
		this.begin = TimeManager.getInst().getBegin();
		this.end = TimeManager.getInst().getEnd();

		if (formRange == null) {
			formRange = new DynamicForm();
			formRange.setPadding(10);
			formRange.setAlign(Alignment.LEFT);
			formRange.setGroupTitle("<b>Time Period and Flags</b>");
			formRange.setIsGroup(true);
			formRange.setNumCols(4);
			formRange.setWidth100();
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

			// --------CUSTOM RULES ---------------
			formRules = new DynamicForm();
			formRules.setGroupTitle("<b>Custom Rules</b>");
			formRules.setIsGroup(true);
			formRules.setAlign(Alignment.LEFT);
			formRules.setLayoutAlign(VerticalAlignment.CENTER);
			formRules.setErrorOrientation(FormErrorOrientation.RIGHT);
			formRules.setPadding(10);
			formRules.setNumCols(8);

			ruleCheckboxItem = new CheckboxItem();
			ruleCheckboxItem.setTitle("Rules");
			ruleCheckboxItem.setStartRow(true);
			ruleCheckboxItem.setEndRow(true);
			ruleCheckboxItem.setTitle("Apply rules");

			ruleCheckboxItem.addChangeHandler(new ChangeHandler() {
				public void onChange(ChangeEvent event) {
					boolean checked = (Boolean) event.getValue();
					if (checked) {
						if (permittedProps.length > 0) {
							rulePropertyBox.setDisabled(false);
							ruleOperatorBox.setDisabled(false);
							ruleBasedValue.setDisabled(false);
							addFlagButton.setDisabled(false);
							modifyFlagButton.setDisabled(false);
							approveButton.setDisabled(true);
							mainGrid.deselectAllRecords();
							mainGrid.setDisabled(true);
							offLineCheckboxItem.setDisabled(true);
						}
					} else {
						if (permittedProps.length > 0) {
							formRules.resetValues();
							rulePropertyBox.setDisabled(true);
							ruleOperatorBox.setDisabled(true);
							ruleBasedValue.setDisabled(true);
							mainGrid.setDisabled(false);
							addFlagButton.setDisabled(true);
							modifyFlagButton.setDisabled(true);
							approveButton.setDisabled(true);
							offLineCheckboxItem.setDisabled(false);
						}
					}
				}
			});

			rulePropertyBox = new ComboBoxItem();
			rulePropertyBox.setTitle("Timeseries");
			rulePropertyBox.setType("comboBox");
			rulePropertyBox.setDisabled(true);
			rulePropertyBox.setStartRow(false);
			rulePropertyBox.setEndRow(false);
			rulePropertyBox.setRequired(true);
			rulePropertyBox.setWidth(250);
			// Flag data segments based on formal criteria
			updateRuleBasedTimeSeries();

			if (permittedProps.length > 0) {
				ruleCheckboxItem.setDisabled(false);
			} else {
				ruleCheckboxItem.setDisabled(true);
			}
			ruleOperatorBox = new ComboBoxItem();
			ruleOperatorBox.setTitle("Operator");
			ruleOperatorBox.setType("comboBox");
			ruleOperatorBox.setValueMap("==", ">", "<", ">=", "<=");
			ruleOperatorBox.setDisabled(true);
			ruleOperatorBox.setStartRow(false);
			ruleOperatorBox.setEndRow(false);
			ruleOperatorBox.setRequired(true);
			ruleOperatorBox.setWidth(50);

			ruleBasedValue = new TextItem();
			ruleBasedValue.setTitle("Value");
			ruleBasedValue.setKeyPressFilter("[0-9.-]");
			ruleBasedValue.setAlign(Alignment.LEFT);
			ruleBasedValue.setWidth(50);
			ruleBasedValue.setDisabled(true);
			ruleBasedValue.setStartRow(false);
			ruleBasedValue.setEndRow(false);
			ruleBasedValue.setRequired(true);
			ruleBasedValue.addChangedHandler(new com.smartgwt.client.widgets.form.fields.events.ChangedHandler(){
				@Override
				public void onChanged(ChangedEvent event) {
					// TODO Auto-generated method stub
					String input = ruleBasedValue.getDisplayValue();
			        if (!input.matches("[0-9.+-]*")) {
			        	return;
			        }
			        else {
			        }
				}
			});


			formRules.setFields(ruleCheckboxItem, rulePropertyBox, ruleOperatorBox, ruleBasedValue);

			// --------VIEW_BASED ---------------
			final HLayout buttonLayout = new HLayout();
			buttonLayout.setWidth100();
			buttonLayout.setLayoutMargin(8);
			buttonLayout.setMembersMargin(5);
			buttonLayout.setAlign(VerticalAlignment.CENTER);

			Label label = new Label();
			label.setHeight(20);
			label.setPadding(10);
			label.setAlign(Alignment.LEFT);
			label.setWrap(false);
			label.setContents("<p><b>Time Series </b> (only permitted series are listed)");

			startTxt = new TextItem();
			startTxt.setTitle("Start");
			startTxt.setShowTitle(true);
			startTxt.setName("Start");
			startTxt.setCanEdit(true);
			startTxt.setStartRow(false);
			startTxt.setEndRow(false);
			startTxt.setRequired(true);
			startTxt.setValue(convertTime(this.begin));

			endTxt = new TextItem();
			endTxt.setTitle("End");
			endTxt.setShowTitle(true);
			endTxt.setName("End");
			endTxt.setStartRow(false);
			endTxt.setEndRow(true);
			endTxt.setCanEdit(true);
			endTxt.setRequired(true);
			endTxt.setValue(convertTime(this.end));

			addFlagButton = new IButton("Add Flag");
			addFlagButton.setDisabled(true);
			addFlagButton.setTooltip("This button only updates data values with unevaluated flags.");
			modifyFlagButton = new IButton("Overwrite Flag");
			modifyFlagButton.setDisabled(true);
			modifyFlagButton.setTooltip("This button overwrites existing flags.");
			approveButton = new IButton("Approve Data Series");
			approveButton.setDisabled(true);
			approveButton.setTooltip("This button only updates the data level of non-unevaluated data. Their existing flags remain the same.");
			approveButton.setWidth(130);
			IButton clearFlagButton = new IButton("Clear");
			buttonLayout.addMember(addFlagButton);
			buttonLayout.addMember(modifyFlagButton);
			buttonLayout.addMember(approveButton);
			buttonLayout.addMember(clearFlagButton);

			createExportLoadingSpinner();

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

			mainGrid = new ListGrid();
			mainGrid.setAlign(Alignment.CENTER);
			mainGrid.setSelectionType(SelectionStyle.SIMPLE);
			mainGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
			mainGrid.setAutoFitData(Autofit.VERTICAL);
			mainGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
				@Override
				public void onSelectionChanged(SelectionEvent event) {
					ListGrid grid = (ListGrid) event.getSource();
					ListGridRecord[] record = grid.getSelectedRecords();
					// if (record.length > 0 && checkboxItem.getValueAsBoolean()
					// == true) {
					if (record.length > 0) {
						addFlagButton.setDisabled(false);
						modifyFlagButton.setDisabled(false);
						approveButton.setDisabled(false);
						ruleCheckboxItem.setDisabled(true);
					} else {
						addFlagButton.setDisabled(true);
						modifyFlagButton.setDisabled(true);
						approveButton.setDisabled(true);
						ruleCheckboxItem.setDisabled(false);
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

			addFlagButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					boolean dateComparison = false;
					boolean isFirstWellFormatted = false;
					boolean isSecondWellFormatted = false;
					String errorMsg = "";

					boolean valid = formRange.validate();
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
							boolean isRuleActive = ruleCheckboxItem.getValueAsBoolean();
							if (isRuleActive) {
								boolean validForm = formRules.validate();
								if (validForm) {
									// if (checked) {
									// EventBus.getMainEventBus().fireEvent(new
									// AddFlagEvent(false, true, true));
									// } else {
									EventBus.getMainEventBus().fireEvent(new AddFlagEvent(false, false, true));
									// }
								}
							} else {
								if (mainGrid.getSelectedRecords().length > 0) {
									if (checked) {
										EventBus.getMainEventBus().fireEvent(new AddFlagEvent(false, true, false));
									} else {
										EventBus.getMainEventBus().fireEvent(new AddFlagEvent(false, false, false));
									}
								}
							}
						} else {
							responseTextItem.setVisible(true);
							responseTextItem.setContents("<p style='color:" + "red" + ";'>" + errorMsg + "</p>");
						}

					}

				}

			});

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
							boolean isRuleActive = ruleCheckboxItem.getValueAsBoolean();
							if (isRuleActive) {
								boolean validForm = formRules.validate();
								if (validForm) {
									// if (checked) {
									// EventBus.getMainEventBus().fireEvent(new
									// AddFlagEvent(true, true, true));
									// } else {
									EventBus.getMainEventBus().fireEvent(new AddFlagEvent(true, false, true));
									// }
								}
							} else {
								if (checked) {
									EventBus.getMainEventBus().fireEvent(new AddFlagEvent(true, true, false));
								} else {
									EventBus.getMainEventBus().fireEvent(new AddFlagEvent(true, false, false));
								}
							}
						} else {
							responseTextItem.setVisible(true);
							responseTextItem.setContents("<p style='color:" + "red" + ";'>" + errorMsg + "</p>");
						}

					}

				}
			});

			approveButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					boolean dateComparison = false;
					boolean isFirstWellFormatted = false;
					boolean isSecondWellFormatted = false;
					boolean isSeriesSelected = false;
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

					if(mainGrid.getSelectedRecords().length>0) {
						isSeriesSelected = true;
					}
					else {
						errorMsg += QCResponseCode.TIMESERIESNULL.getResponseMessage() +"<br>";
						isSeriesSelected = false;
					}
					
					if (isFirstWellFormatted && isSecondWellFormatted && dateComparison && isSeriesSelected) {
						boolean checked = offLineCheckboxItem.getValueAsBoolean();
						String startView = convertTime(getBegin());
						String endView = convertTime(getEnd());
						QualityFlaggingController.getInstance().approveDataSeries(checked, startView,endView,getMainGrid().getSelectedRecords());
					} else {
						responseTextItem.setVisible(true);
						responseTextItem.setContents("<p style='color:" + "red" + ";'>" + errorMsg + "</p>");
					}
				}
			});
			
			clearFlagButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					// formRange.clearValues();
					flagSelect.clearValue();
					flagSpecificSelect.clearValue();
					formRules.clearValues();
					mainGrid.deselectAllRecords();
					if (responseTextItem.isVisible()) {
						clearReponseTextItem();
					}
					// fire default selection
					addFlagButton.setDisabled(true);
					modifyFlagButton.setDisabled(true);
					approveButton.setDisabled(true);
					mainGrid.setDisabled(false);

					SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.VIEW);
					EventBus.getMainEventBus().fireEvent(setTimeRange);
					EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
					// EventBus.getMainEventBus().fireEvent(new
					// LoadImageDataEvent());
				}
			});

			responseTextItem = new HTMLFlow();
			responseTextItem.setVisible(false);

			Label instuctions = new Label();
			instuctions.setHeight(20);
			instuctions.setPadding(10);
			instuctions.setAlign(Alignment.LEFT);
			instuctions.setWrap(false);
			instuctions.setContents("<b>Hints:</b></br>1. Choose either <b>Custom Rules</b> OR <b>Time Series</b>. <br>" + "2. The <b>Custom Rules</b> option allows you to flag ONE time series that meets the criteria you specify.<br> "
					+ "3. The offline flagging mode is NOT possible with <b>Custom Rules</b> option.<br> 4. The <b>Time Series</b> table allows you to flag more than one time series.<p>");

			formRange.setFields(startTxt, endTxt, flagSelect, flagSpecificSelect);
			messageLayout.addMember(instuctions);
			messageLayout.addMember(formRange);
			messageLayout.addMember(formRules);
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

	public void updateReponseTextItem(QCResponseCode msg) {
		this.responseTextItem.setVisible(true);
		this.responseTextItem.setContents("<span style='color:" + msg.getColor() + ";'>" + msg.getResponseMessage() + "</span>");
	}

	public class QualityFlaggerByViewEventBroker implements DeleteAllTimeSeriesEventHandler, DatesChangedEventHandler, ChangeTimeSeriesStyleEventHandler, FinishedAddFlagEventHandler, DeleteTimeSeriesEventHandler, LegendElementSelectedEventHandler {

		public QualityFlaggerByViewEventBroker() {
			EventBus.getMainEventBus().addHandler(DeleteAllTimeSeriesEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(DeleteTimeSeriesEvent.TYPE, this);
			// EventBus.getMainEventBus().addHandler(StoreTimeSeriesEvent.TYPE,
			// this);
			EventBus.getMainEventBus().addHandler(LegendElementSelectedEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(DatesChangedEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(FinishedAddFlagEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(ChangeTimeSeriesStyleEvent.TYPE, this);
		}

		@Override
		public void onDatesChanged(DatesChangedEvent evt) {

			// TODO Auto-generated method stub
			QualityFlaggerByView.this.begin = evt.getStart();
			QualityFlaggerByView.this.end = evt.getEnd();
			// update date textboxes
			QualityFlaggerByView.this.startTxt.setValue(convertTime(evt.getStart()));
			QualityFlaggerByView.this.endTxt.setValue(convertTime(evt.getEnd()));
		}

		/*
		 * @Override public void onStore(StoreTimeSeriesEvent evt) {
		 * ListGridRecord[] rec = updateListGridRecords();
		 * mainGrid.setData(rec); }
		 */

		@Override
		public void onSelected(LegendElementSelectedEvent evt) {
			updateListGridRecords();
			updateRuleBasedTimeSeries();
		}

		@Override
		public void onDeleteTimeSeries(DeleteTimeSeriesEvent evt) {
			String idDeleted = evt.getId();
			ListGridRecord[] tRecords = mainGrid.getRecords();
			if (tRecords.length == 1) {
				mainGrid.deselectAllRecords();
				mainGrid.setDisabled(true);
			}
			for (int tI = 0; tI < tRecords.length; tI++) {
				if (tRecords[tI].getAttribute("ts").toString().equalsIgnoreCase(idDeleted)) {
					mainGrid.removeData(tRecords[tI]);
				}
			}
			updateRuleBasedTimeSeries();
		}

		@Override
		public void onFinishedAddFlag(FinishedAddFlagEvent evt) {
			// TODO Auto-generated method stub
			if (exportLoadingSpinner.isVisible()) {
				exportLoadingSpinner.hide();
			}
		}

		@Override
		public void onChange(ChangeTimeSeriesStyleEvent evt) {
			updateListGridRecords();
		}

		@Override
		public void onDeleteAllTimeSeries(DeleteAllTimeSeriesEvent evt) {
			mainGrid.deselectAllRecords();
			mainGrid.setDisabled(true);
			ListGridRecord[] selectedRecords = mainGrid.getRecords();
			for (ListGridRecord rec : selectedRecords) {
				mainGrid.removeData(rec);
			}
			ruleCheckboxItem.setDisabled(true);
			rulePropertyBox.clearValue();
		}

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
			}
			mainGrid.setDisabled(false);
			mainGrid.setData(records);
		} else {
			mainGrid.setDisabled(true);
		}
		return records;
	}

	public long getBegin() {
		return begin;
	}

	public void setBegin(long begin) {
		this.begin = begin;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public String convertTime(long time) {
		Date date = new Date(time);
		DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ssZ");
		return format.format(date).toString();
	}

	public ListGrid getMainGrid() {
		return mainGrid;
	}

	public void setMainGrid(ListGrid mainGrid) {
		this.mainGrid = mainGrid;
	}

	public void clearReponseTextItem() {
		this.responseTextItem.setVisible(false);
		this.responseTextItem.setContents("");
	}

	private void createExportLoadingSpinner() {
		this.exportLoadingSpinner = new HLayout();
		this.exportLoadingSpinner.setWidth100();
		this.exportLoadingSpinner.setHeight(40);
		Img spinner = new Img("../img/loader_wide.gif", 60, 11);
		// this.exportLoadingSpinner.setWidth100();
		// this.exportLoadingSpinner.setHeight100();
		this.exportLoadingSpinner.setAlign(Alignment.CENTER);
		this.exportLoadingSpinner.addMember(spinner);
		this.exportLoadingSpinner.hide();
	}

	public SelectItem getQualityFlag() {
		return this.flagSelect;
	}

	public SelectItem getFlagSpecificSelect() {
		return flagSpecificSelect;
	}

	public static Date toDate(String date) {
		DateTimeFormat f = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ssZ");
		return f.parseStrict(date);
	}

	public HLayout getExportLoadingSpinner() {
		return exportLoadingSpinner;
	}

	public void clearLocalTabValues() {
		//flagSpecificSelect.clearValue();
		//flagSelect.clearValue();
		formRules.clearValues();
		mainGrid.deselectAllRecords();
		mainGrid.setDisabled(false);
		offLineCheckboxItem.clearValue();
		if (responseTextItem.isVisible()) {
			clearReponseTextItem();
		}
	}

	public CheckboxItem getRuleCheckboxItem() {
		return ruleCheckboxItem;
	}

	public void setRuleCheckboxItem(CheckboxItem ruleCheckboxItem) {
		this.ruleCheckboxItem = ruleCheckboxItem;
	}

	public TextItem getRuleBasedValue() {
		return ruleBasedValue;
	}

	public void setRuleBasedValue(TextItem ruleBasedValue) {
		this.ruleBasedValue = ruleBasedValue;
	}

	public ComboBoxItem getRulePropertyBox() {
		return rulePropertyBox;
	}

	public void setRulePropertyBox(ComboBoxItem rulePropertyBox) {
		this.rulePropertyBox = rulePropertyBox;
	}

	public ComboBoxItem getRuleOperatorBox() {
		return ruleOperatorBox;
	}

	public void setRuleOperatorBox(ComboBoxItem ruleOperatorBox) {
		this.ruleOperatorBox = ruleOperatorBox;
	}

	private void updateRuleBasedTimeSeries() {
		Collection<TimeSeries> filteredSeries = QualityFlaggingController.getInstance().filterPermittedSitesAndProperties();
		TimeSeries[] ts = (TimeSeries[]) filteredSeries.toArray(new TimeSeries[filteredSeries.size()]);
		permittedProps = new String[ts.length];
		if (filteredSeries.size() > 0) {
			for (int i = 0; i < ts.length; i++) {
				String proc = ts[i].getPhenomenonId().trim();
				String sense = ts[i].getProcedureId().trim();
				permittedProps[i] = sense + ":" + proc;
			}
			rulePropertyBox.setValueMap(permittedProps);
			ruleCheckboxItem.setDisabled(false);
		} else {
			ruleCheckboxItem.setDisabled(true);
		}
	}

	public TextItem getStartTxt() {
		return startTxt;
	}

	public void setStartTxt(TextItem startTxt) {
		this.startTxt = startTxt;
	}

	public TextItem getEndTxt() {
		return endTxt;
	}

	public void setEndTxt(TextItem endTxt) {
		this.endTxt = endTxt;
	}
}

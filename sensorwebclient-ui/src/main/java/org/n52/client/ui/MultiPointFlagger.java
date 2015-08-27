package org.n52.client.ui;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eesgmbh.gimv.client.event.LoadImageDataEvent;
import org.n52.client.bus.EventBus;
import org.n52.client.sos.ctrl.QCResponseCode;
import org.n52.client.sos.ctrl.QualityFlaggingController;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.event.DatesChangedEvent;
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
import org.n52.client.sos.event.handler.DatesChangedEventHandler;
import org.n52.client.sos.legend.TimeSeries;
import org.n52.client.sos.ui.EESTab;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;

//ASD-2013
public class MultiPointFlagger extends Tab {

	private static final String COMPONENT_ID = "MultiPointFlagger";
	private static MultiPointFlagger instance;
	// private DynamicForm formPoints;
	private HTMLFlow responseTextItem;
	// private CheckboxItem offLineCheckboxItem;
	// private CheckboxItem okCheckboxItem;
	private HLayout exportLoadingSpinner;
	private ListGridRecord selectedTabRecord;
	private ListGrid dataGrid = new ListGrid();

	private IButton addFlagButton;
	private IButton modifyFlagButton;

	// private TextItem startTxt;
	// private TextItem endTxt;
	// private long begin;
	// private long end;

	public static MultiPointFlagger getInst() {
		if (instance == null) {
			instance = new MultiPointFlagger();
		}
		return instance;
	}

	private MultiPointFlagger() {
		new OfflineFlaggerEventBroker();
		setID(COMPONENT_ID);
		setTitle("Multi-Point Flagging");
		initializeOfflineWindow();
	}

	private void initializeOfflineWindow() {

		VLayout messageLayout = new VLayout();
		messageLayout.setLayoutMargin(15);
		messageLayout.setMembersMargin(3);

		// this.begin = TimeManager.getInst().getBegin();
		// this.end = TimeManager.getInst().getEnd();

		createExportLoadingSpinner();

		dataGrid = new ListGrid();
		dataGrid.setAlign(Alignment.CENTER);
		dataGrid.setSelectionType(SelectionStyle.SIMPLE);
		dataGrid.setAutoFitData(Autofit.VERTICAL);

		/*
		 * Label label = new Label(); label.setHeight(20); label.setPadding(10);
		 * label.setAlign(Alignment.CENTER); label.setWrap(false);
		 * label.setContents(
		 * "Note: The following table only includes permitted timeseries: " );
		 */

		/*
		 * startTxt = new TextItem(); startTxt.setTitle("Start");
		 * startTxt.setShowTitle(true); startTxt.setName("Start");
		 * startTxt.setCanEdit(false); startTxt.setStartRow(true);
		 * startTxt.setEndRow(false);
		 * startTxt.setValue(convertTime(this.begin));
		 * 
		 * endTxt = new TextItem(); endTxt.setTitle("End");
		 * endTxt.setShowTitle(true); endTxt.setName("End");
		 * endTxt.setStartRow(false); endTxt.setEndRow(true);
		 * endTxt.setCanEdit(false); endTxt.setValue(convertTime(this.end));
		 */
		ListGridField tsField = new ListGridField("ts", "SeriesId");
		tsField.setHidden(true);
		ListGridField dateField = new ListGridField("date", "Date");
		dateField.setCanSort(false);
		dateField.setWidth(140);
		ListGridField sensorField = new ListGridField("sensor", "Station");
		sensorField.setCanSort(false);
		sensorField.setWidth(55);
		ListGridField propField = new ListGridField("property", "Property");
		propField.setCanSort(false);
		propField.setWidth(155);
		ListGridField flagField = new ListGridField("flag", "Generic & Specific Flags");
		flagField.setCanSort(false);
		ListGridField removeField = new ListGridField("removeAction", 20);
		removeField.setType(ListGridFieldType.ICON);
		removeField.setCellIcon("[SKIN]/actions/remove.png");
		removeField.setCanEdit(false);
		removeField.setCanFilter(true);
		removeField.setFilterEditorType(new SpacerItem());
		removeField.setCanGroupBy(false);
		removeField.setCanSort(false);
		removeField.addRecordClickHandler(new RecordClickHandler() {
			public void onRecordClick(final RecordClickEvent event) {
				ListGridRecord record = (ListGridRecord) event.getRecord();
				String id = record.getAttribute("ts").toString();
				String date = record.getAttribute("date").toString();
				QualityFlaggingController qcInstance = QualityFlaggingController.getInstance();
				qcInstance.getRenderTimePoint().remove(id + "#" + date);
				dataGrid.removeData(record);
				if (!MultiPointFlagger.getInst().getExportLoadingSpinner().isVisible()) {
					MultiPointFlagger.getInst().getExportLoadingSpinner().show();
				}
				EESTab.showLoadingSpinner();
				EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
				checkButtons();
			}
		});

		addFlagButton = new IButton("Add Flag");
		addFlagButton.setDisabled(true);
		addFlagButton.setTooltip("This button only updates data values with unevaluated flags.");
		modifyFlagButton = new IButton("Overwrite Flag");
		modifyFlagButton.setDisabled(true);
		modifyFlagButton.setTooltip("This button overwrites the flags of all selected data values.");
		IButton clearFlagButton = new IButton("Clear Table");
		final HLayout buttonLayout = new HLayout();
		buttonLayout.setWidth100();
		buttonLayout.setLayoutMargin(8);
		buttonLayout.setMembersMargin(5);
		buttonLayout.setAlign(VerticalAlignment.CENTER);
		buttonLayout.addMember(addFlagButton);
		buttonLayout.addMember(modifyFlagButton);
		buttonLayout.addMember(clearFlagButton);

		dataGrid.setFields(dateField, sensorField, propField, flagField, removeField);
		dataGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
			@Override
			public void onSelectionChanged(SelectionEvent event) {
				ListGrid grid = (ListGrid) event.getSource();
				ListGridRecord[] record = grid.getRecords();
				if (record.length > 0) {
					addFlagButton.setDisabled(false);
					modifyFlagButton.setDisabled(false);
				} else {
					addFlagButton.setDisabled(true);
					modifyFlagButton.setDisabled(true);
				}
			}
		});

		/*
		 * offLineCheckboxItem = new CheckboxItem();
		 * offLineCheckboxItem.setTitle("Offline");
		 * offLineCheckboxItem.setHeight(25);
		 * offLineCheckboxItem.setTitle("Enable Offline Flagging");
		 * offLineCheckboxItem.setStartRow(false);
		 * offLineCheckboxItem.setEndRow(true);
		 */

		/*
		 * okCheckboxItem = new CheckboxItem();
		 * okCheckboxItem.setTitle("OKAll"); okCheckboxItem.setHeight(25);
		 * okCheckboxItem.setTitle(
		 * "Set the remaining time series as displayed on the screen to OK");
		 * okCheckboxItem.setStartRow(true); okCheckboxItem.setEndRow(false);
		 * okCheckboxItem
		 * .setHint("This option only applies to permitted time series.");
		 */

		addFlagButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				EventBus.getMainEventBus().fireEvent(new AddFlagEvent(false, false,false));

			}
		});

		modifyFlagButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				EventBus.getMainEventBus().fireEvent(new AddFlagEvent(true, false,false));
			}
		});

		clearFlagButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				clearLocalTabValues();
				SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.MULTIPOINT);
				EventBus.getMainEventBus().fireEvent(setTimeRange);
				EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
				EESTab.showLoadingSpinner();
				EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
			}
		});

		responseTextItem = new HTMLFlow();
		responseTextItem.setVisible(false);

		Label label = new Label();
		label.setHeight(20);
		label.setPadding(10);
		label.setAlign(Alignment.LEFT);
		label.setWrap(false);
		label.setContents("<b>Hints:</b></br>" + "1. Points will be highlighted based on the selected timeseries on the legend (left window). <br> " + "2. You may select more than one time point on the timeseries window. <br>" + "3. Click on the same point to overwrite its flag information. <br> "
				+ "4. Click Add Flag or Overwrite Flag button to commit the changes to the database.<p>");

		Label space = new Label();
		space.setWidth("1px");
		// formPoints.setFields(okCheckboxItem, offLineCheckboxItem);
		messageLayout.addMember(label);
		messageLayout.addMember(dataGrid);
		// messageLayout.addMember(formPoints);
		messageLayout.addMember(buttonLayout);
		messageLayout.addMember(exportLoadingSpinner);
		messageLayout.addMember(responseTextItem);
		messageLayout.addMember(space);
		this.setPane(messageLayout);

	}

	public void updateDataTable(Map<String, String> responseFields) {
		dataGrid.setData(new ListGridRecord[] {});
		DataStoreTimeSeriesImpl dataManager = DataStoreTimeSeriesImpl.getInst();
		Iterator it = responseFields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String key = (String) pairs.getKey();
			String value = (String) pairs.getValue();
			String[] arrayKey = key.split("#");
			String[] arrayValue = value.split("#");
			TimeSeries ts = dataManager.getDataItem(arrayKey[0]);
			ListGridRecord record = new ListGridRecord();
			record.setAttribute("ts", ts.getId());
			record.setAttribute("date", arrayValue[0]);
			record.setAttribute("sensor", ts.getProcedureId());
			record.setAttribute("property", ts.getPhenomenonId());
			record.setAttribute("flag", arrayValue[1] + " , " + arrayValue[2]);
			dataGrid.addData(record);
		}
		checkButtons();
	}

	private void checkButtons() {
		ListGridRecord[] record = dataGrid.getRecords();
		if (record.length > 0) {
			addFlagButton.setDisabled(false);
			modifyFlagButton.setDisabled(false);
		} else {
			addFlagButton.setDisabled(true);
			modifyFlagButton.setDisabled(true);
		}
		if (MultiPointFlagger.getInst().getExportLoadingSpinner().isVisible()) {
			MultiPointFlagger.getInst().getExportLoadingSpinner().hide();
		}
	}

	public void updateReponseTextItem(QCResponseCode msg) {
		this.responseTextItem.setVisible(true);
		this.responseTextItem.setContents("<span style='color:" + msg.getColor() + ";'>" + msg.getResponseMessage() + "</span>");
	}

	public void clearReponseTextItem() {
		this.responseTextItem.setVisible(false);
		this.responseTextItem.setContents("");
	}

	private void createExportLoadingSpinner() {
		this.exportLoadingSpinner = new HLayout();
		this.exportLoadingSpinner.setWidth100();
		Img spinner = new Img("../img/loader_wide.gif", 60, 11);
		this.exportLoadingSpinner.setAlign(Alignment.CENTER);
		this.exportLoadingSpinner.addMember(spinner);
		this.exportLoadingSpinner.hide();
	}

	private class OfflineFlaggerEventBroker implements DeleteAllTimeSeriesEventHandler, DeleteTimeSeriesEventHandler, FinishedAddFlagEventHandler {
		OfflineFlaggerEventBroker() {
			EventBus.getMainEventBus().addHandler(FinishedAddFlagEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(DeleteTimeSeriesEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(DeleteAllTimeSeriesEvent.TYPE, this);
			// EventBus.getMainEventBus().addHandler(DatesChangedEvent.TYPE,
			// this);
		}

		@Override
		public void onFinishedAddFlag(FinishedAddFlagEvent evt) {
			if (exportLoadingSpinner.isVisible()) {
				exportLoadingSpinner.hide();
			}
		}

		@Override
		public void onDeleteTimeSeries(DeleteTimeSeriesEvent evt) {
			String idDeleted = evt.getId();
			ListGridRecord[] tRecords = dataGrid.getRecords();
			for (int tI = 0; tI < tRecords.length; tI++) {
				if (tRecords[tI].getAttribute("ts").toString().equalsIgnoreCase(idDeleted)) {
					dataGrid.removeData(tRecords[tI]);
				}
			}
		}

		@Override
		public void onDeleteAllTimeSeries(DeleteAllTimeSeriesEvent evt) {
			// TODO Auto-generated method stub
			ListGridRecord[] selectedRecords = dataGrid.getRecords();
			for (ListGridRecord rec : selectedRecords) {
				dataGrid.removeData(rec);
			}
		}

	}

	public HTMLFlow getResponseTextItem() {
		return responseTextItem;
	}

	public void setResponseTextItem(HTMLFlow responseTextItem) {
		this.responseTextItem = responseTextItem;
	}

	public ListGridRecord getSelectedTabRecord() {
		return selectedTabRecord;
	}

	public void setSelectedTabRecord(ListGridRecord selectedRecord) {
		this.selectedTabRecord = selectedRecord;
	}

	public HLayout getExportLoadingSpinner() {
		return exportLoadingSpinner;
	}

	public String convertTime(long time) {
		Date date = new Date(time);
		DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ssZ");
		return format.format(date).toString();
	}

	public void clearLocalTabValues() {
		dataGrid.setData(new ListGridRecord[]{});
		addFlagButton.setDisabled(true);
		modifyFlagButton.setDisabled(true);
		if (responseTextItem.isVisible()) {
			clearReponseTextItem();
		}
	}

	public ListGrid getDataGrid() {
		return dataGrid;
	}

	public void setDataGrid(ListGrid dataGrid) {
		this.dataGrid = dataGrid;
	}
}

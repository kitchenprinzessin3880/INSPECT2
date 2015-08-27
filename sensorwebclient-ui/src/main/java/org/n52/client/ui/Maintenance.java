package org.n52.client.ui;

import java.util.Date;
import java.util.TreeSet;

import org.n52.client.bus.EventBus;
import org.n52.client.ctrl.LoaderManager;
import org.n52.client.ctrl.TimeManager;
import org.n52.client.sos.ctrl.QCResponseCode;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.event.LegendElementSelectedEvent;
import org.n52.client.sos.event.data.AddFlagEvent;
import org.n52.client.sos.event.data.DeleteAllTimeSeriesEvent;
import org.n52.client.sos.event.data.DeleteTimeSeriesEvent;
import org.n52.client.sos.event.data.handler.DeleteAllTimeSeriesEventHandler;
import org.n52.client.sos.event.data.handler.DeleteTimeSeriesEventHandler;
import org.n52.client.sos.event.handler.LegendElementSelectedEventHandler;
import org.n52.client.sos.legend.TimeSeries;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DateDisplayFormat;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.RelativeDateItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;

public class Maintenance extends Tab {
	private RelativeDateItem startDate;
	private RelativeDateItem endDate;
	protected VLayout layout;
	private static FlexTable storageFlexTable;
	private static Maintenance instance;
	private HTMLFlow responseTextItem;
	VLayout wrapper;
	private DynamicForm dateForm;
	private boolean isDateValid = true;
	private SelectItem sensorBox = null;
	final IButton requestButton =null;
	public static Maintenance getInst() {
		// save the search query as a query template
		if (instance == null) {
			instance = new Maintenance();
		}
		return instance;
	}

	private Maintenance() {
		setTitle("Maintenance Info");
		createContent();
		new MaintenanceEventBroker();
	}

	private void createContent() {
		wrapper = new VLayout();
		wrapper.setMargin(5);

		responseTextItem = new HTMLFlow();
		responseTextItem.setVisible(false);

		long start = TimeManager.getInst().getBegin();
		long end = TimeManager.getInst().getEnd();

		// ASD
		startDate = new RelativeDateItem("STDATE");
		startDate.setTitle("Start");
		startDate.setDateFormatter(DateDisplayFormat.TOEUROPEANSHORTDATE);
		startDate.setValidateOnExit(true);
		startDate.setRequired(true);
		startDate.setRequiredMessage("This field cannot be blank");
		Date st = new Date(start);
		startDate.setValue(st);

		// ASD
		endDate = new RelativeDateItem("EDDATE");
		endDate.setTitle("End");
		endDate.setDateFormatter(DateDisplayFormat.TOEUROPEANSHORTDATE);
		endDate.setRequired(true);
		endDate.setRequiredMessage("This field cannot be blank");
		endDate.setValidateOnExit(true);
		Date ed = new Date(end);
		endDate.setValue(ed);

		startDate.setEndDate(new Date());
		startDate.addChangedHandler(new ChangedHandler() {
			@Override
			public void onChanged(ChangedEvent event) {
				String errorMsg = "";
				Date startDt = (Date) event.getValue();
				Date endDt = (Date) endDate.getValue();

				if (startDt.after(new Date())) {
					errorMsg = "Date must be today's date or prior.\n";
				}
				if (startDt.after(endDt) || startDt.equals(endDt)) {
					errorMsg += "The dates cannot be crossed or same.";
				}

				if (startDt.before(endDt) && startDt.before(new Date()) && !startDt.equals(endDt)) {
					isDateValid = true;
					startDate.getForm().clearFieldErrors("STDATE", true);
					endDate.getForm().clearFieldErrors("EDDATE", true);
					setFilterButtonActiv(true);
				} else {
					isDateValid = false;
					setFilterButtonActiv(false);
					startDate.getForm().setFieldErrors("STDATE", errorMsg, true);
				}
			}
		});

		endDate.addChangedHandler(new ChangedHandler() {
			@Override
			public void onChanged(ChangedEvent event) {
				String errorMsg = "";
				Date startDt = (Date) startDate.getValue();
				Date endDt = (Date) event.getValue();

				if (endDt.after(new Date())) {
					errorMsg = "Date must be today's date or prior.\n";
				}

				if (endDt.before(startDt) || endDt.equals(startDt)) {
					errorMsg += "The dates cannot be crossed or same.";
				}

				if (endDt.after(startDt) && endDt.before(new Date()) && !endDt.equals(startDt)) {
					isDateValid = true;
					endDate.getForm().clearFieldErrors("ENDDATE", true);
					startDate.getForm().clearFieldErrors("STARTDATE", true);
					setFilterButtonActiv(true);
				} else {
					isDateValid = false;
					setFilterButtonActiv(false);
					endDate.getForm().setFieldErrors("ENDDATE", errorMsg, true);
				}

			}
		});

		sensorBox = new SelectItem();
		sensorBox = new SelectItem("Stations", "Select Station");
		sensorBox.setAlign(Alignment.LEFT);
		sensorBox.setRequired(true);
		sensorBox.setStartRow(true);
		sensorBox.setEndRow(false);
		
		TimeSeries[] timeSeries = DataStoreTimeSeriesImpl.getInst().getTimeSeriesSorted();
		if(timeSeries.length>0) {
			
			String[] sensors = getSensorsFromSeries(timeSeries);
			sensorBox.setValueMap(sensors);
		}

		final IButton button = new IButton("Request");
		button.setWidth(55);
		button.setTooltip("Request maintenance information based on the specified time range and station.");
		button.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				
			}
		});
	
		
		dateForm = new DynamicForm();
		dateForm.setNumCols(4);
		dateForm.setStyleName("n52_sensorweb_client_jumpToTimeIntervalForm");
		dateForm.setFields(startDate, endDate, sensorBox);
		dateForm.setHeight("*");

		ScrollPanel scrollPanel = new ScrollPanel();
		scrollPanel.setHeight("400px");
		storageFlexTable = new FlexTable();
		storageFlexTable.setWidth("100%");
		generateTable();
		scrollPanel.add(storageFlexTable);

		Label space = new Label();
		space.setHeight(8);
		wrapper.addMember(dateForm);
		wrapper.addMember(button);
		wrapper.addMember(space);
		wrapper.addMember(scrollPanel);
		wrapper.addMember(space);
		wrapper.addMember(responseTextItem);
		this.setPane(wrapper);

	}

	private void generateTable() {
		storageFlexTable.getRowFormatter().addStyleName(0, "tereno_queryListHeader");
		storageFlexTable.setStyleName("tereno_queryList");
		storageFlexTable.setText(0, 0, "From");
		storageFlexTable.setText(0, 1, "To");
		storageFlexTable.setText(0, 2, "Details");
		storageFlexTable.setText(0, 3, "Flag");
		storageFlexTable.setText(0, 4, "User");
	}

	private String[] getSensorsFromSeries(TimeSeries[] series)
	{
		String[] result = null;
		TreeSet<String> hs = new TreeSet<String>();
		for(int i =0; i< series.length ; i++)
		{
			TimeSeries ts = series[i];
			String station = ts.getProcedureId();
			hs.add(station);
			result = hs.toArray(new String[hs.size()]);
		}
		return result;
	}
	
	public void clearReponseTextItem() {
		this.responseTextItem.setVisible(false);
		this.responseTextItem.setContents("");
	}

	public void updateReponseTextItem(QCResponseCode msg) {
		this.responseTextItem.setVisible(true);
		this.responseTextItem.setContents("<span style='color:" + msg.getColor() + ";'>" + msg.getResponseMessage() + "</span>");
	}

	public void updateTable(String results) {

	}

	public void updateSensors(String results) {

	}

	public void setFilterButtonActiv(boolean activ) {
		if (activ) {
			requestButton.setDisabled(false);
			requestButton.setStyleName("n52_sensorweb_client_legendbutton");
		} else {
			requestButton.setDisabled(true);
			requestButton.setStyleName("n52_sensorweb_client_legendbuttonDisabled");
		}
	}

	private class MaintenanceEventBroker implements DeleteAllTimeSeriesEventHandler, DeleteTimeSeriesEventHandler, LegendElementSelectedEventHandler {
		MaintenanceEventBroker() {
			EventBus.getMainEventBus().addHandler(DeleteAllTimeSeriesEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(LegendElementSelectedEvent.TYPE, this);
			EventBus.getMainEventBus().addHandler(DeleteTimeSeriesEvent.TYPE, this);
		}

		@Override
		public void onSelected(LegendElementSelectedEvent evt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDeleteTimeSeries(DeleteTimeSeriesEvent evt) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDeleteAllTimeSeries(DeleteAllTimeSeriesEvent evt) {
			// TODO Auto-generated method stub
			
		}

		

	}

}

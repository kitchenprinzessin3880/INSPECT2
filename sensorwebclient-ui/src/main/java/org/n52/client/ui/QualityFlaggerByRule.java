package org.n52.client.ui;

import static org.n52.client.sos.i18n.SosStringsAccessor.i18n;

import java.util.Date;
import org.eesgmbh.gimv.client.event.LoadImageDataEvent;
import org.n52.client.bus.EventBus;
import org.n52.client.sos.ctrl.QCResponseCode;
import org.n52.client.sos.ctrl.QualityFlaggingController;
import org.n52.client.sos.event.LegendElementSelectedEvent;
import org.n52.client.sos.event.data.AddFlagEvent;
import org.n52.client.sos.event.data.ClearSelectedPointEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent;
import org.n52.client.sos.event.data.SetQualityControlStatusEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent.FlaggingPointType;
import org.n52.client.sos.event.handler.LegendElementSelectedEventHandler;
import org.n52.client.sos.legend.TimeSeries;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.DateItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.form.validator.DateRangeValidator;
import com.smartgwt.client.widgets.layout.VLayout;

//@ASD-2013
public class QualityFlaggerByRule extends Window {

	private static final String COMPONENT_ID = "qualityControlFlaggerByRule";

	private static int WIDTH_RANGE = 400;

	private static int HEIGHT_RANGE = 300;

	private static QualityFlaggerByRule instance;

	private DynamicForm formRange;

	private Canvas responseTextItem;
	private static QualityFlaggerByRuleEventBroker eventBroker;
	private Date firstValueDate;
	private Date lastValueDate;
	private String timeSeriesId = "";
	private Canvas timeSeriesCanvas;
	private static QualityFlaggingController qcController;
	private String property = "";
	private String sensor = "";
	private DateItem endItem;
	private DateItem startItem;
	private boolean isStartDateAvailable = false;
	private boolean isEndDateAvailable = false;

	public static QualityFlaggerByRule getInst() {
		if (instance == null) {
			instance = new QualityFlaggerByRule();
		}

		return instance;
	}

	private QualityFlaggerByRule() {

		this.eventBroker = new QualityFlaggerByRuleEventBroker();
		this.qcController = QualityFlaggingController.getInstance();

		Date today = new java.util.Date();
		TimeSeries latestTS = this.qcController.getTimeSeriesProperties();
		GWT.log("QualityFlaggerByRule-latestTS" + " --" + latestTS.getFirstValueDate() + " --" + latestTS.getLastValueDate());

		if (latestTS.getFirstValueDate() != 0) {
			this.firstValueDate = new Date(latestTS.getFirstValueDate());
			this.isStartDateAvailable = true;
		} else {
			this.firstValueDate = today;
			this.isStartDateAvailable = false;
		}

		if (latestTS.getLastValueDate() != 0) {
			this.lastValueDate = new Date(latestTS.getLastValueDate());
			this.isEndDateAvailable = true;
		} else {
			this.lastValueDate = today;
			this.isEndDateAvailable = false;
		}

		this.timeSeriesId = latestTS.getId();
		this.property = latestTS.getPhenomenonId();
		this.sensor = latestTS.getProcedureId();

		// setShowModalMask(true);
		setID(COMPONENT_ID);
		setIsModal(false);
		setCanDragResize(true);
		setShowMaximizeButton(true);
		setShowMinimizeButton(false);
		setMargin(0);
		setWidth(WIDTH_RANGE);
		setHeight(HEIGHT_RANGE);
		setTitle(i18n.qualityFlaggingRule());
		initializeQualityRuleWindow();

		addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClickEvent event) {
				closeQualityFlagger();
			}
		});

	}

	private void initializeQualityRuleWindow() {

		VLayout messageLayout = new VLayout();
		messageLayout.setWidth(WIDTH_RANGE);
		messageLayout.setHeight(HEIGHT_RANGE);
		// messageLayout.setBorder("1px solid #6a6a6a");
		messageLayout.setLayoutMargin(5);

		if (formRange == null) {

			GWT.log("Date-initializeQualityRuleWindow" + " --" + this.firstValueDate.toString() + "," + this.lastValueDate.toString());
			formRange = new DynamicForm();
			formRange.setHeight100();
			formRange.setWidth100();
			formRange.setPadding(10);
			formRange.setLayoutAlign(VerticalAlignment.CENTER);

			String additionalTxt = "Sensor: " + this.sensor + "<BR>" + "Property: " + this.property + " <BR>";
			String dateTxt = "Data availability: <BR>" + convertTime(this.firstValueDate) + " -- " + convertTime(this.lastValueDate) + "<BR>";
			timeSeriesCanvas = new Canvas();
			timeSeriesCanvas.setPrefix("<b>Timeseries Info:</b><BR>");
			timeSeriesCanvas.setContents(this.timeSeriesCanvas.getPrefix() + additionalTxt + dateTxt);
			timeSeriesCanvas.setPadding(5);
			timeSeriesCanvas.setHeight(1);

			// start date
			startItem = new DateItem("START", "Start Date");
			startItem.setWrapTitle(false);
			startItem.setRequired(true);

			// end date
			endItem = new DateItem("END", "End Date");
			endItem.setWrapTitle(false);
			endItem.setRequired(true);

			// custom date range validator
			if (isStartDateAvailable) {
				DateRangeValidator dateRangeValidatorStart = new DateRangeValidator();
				dateRangeValidatorStart.setMax(this.lastValueDate);
				dateRangeValidatorStart.setMin(this.firstValueDate);
				dateRangeValidatorStart.setErrorMessage("The selected timeseries is only available from: " + firstValueDate.toString());
				startItem.setValidators(dateRangeValidatorStart);
				startItem.setValidateOnExit(false);
				startItem.setValidateOnChange(true);
				startItem.setDefaultChooserDate(firstValueDate);
				startItem.setValue(firstValueDate);
			}

			if (isEndDateAvailable) {
				DateRangeValidator dateRangeValidatorEnd = new DateRangeValidator();
				dateRangeValidatorEnd.setMax(this.lastValueDate);
				dateRangeValidatorEnd.setMin(this.firstValueDate);
				dateRangeValidatorEnd.setErrorMessage("The selected timeseries is only available until: " + lastValueDate.toString());
				endItem.setValidators(dateRangeValidatorEnd);
				endItem.setValidateOnExit(false);
				endItem.setValidateOnChange(true);
				endItem.setDefaultChooserDate(lastValueDate);
				endItem.setValue(lastValueDate);
			}

			final ButtonItem addFlagButton = new ButtonItem("saveBtn", "Save");
			addFlagButton.setStartRow(true);
			addFlagButton.setEndRow(false);
			addFlagButton.setDisabled(true);
			addFlagButton.setAlign(Alignment.RIGHT);
			addFlagButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					if (after(startItem.getValueAsDate(), endItem.getValueAsDate())) {
						EventBus.getMainEventBus().fireEvent(new AddFlagEvent(false,false,false));
					} else {
						// print error msg
						updateReponseTextItem(QCResponseCode.TIMERANGEINVALID);
					}

				}
			});

			CheckboxItem checkboxItem = new CheckboxItem();
			checkboxItem.setRequired(true);
			checkboxItem.setName("okFlag");
			checkboxItem.setTitle("Assign 'OK' flag to rest data points.");
			checkboxItem.setValue(false);
			checkboxItem.addChangeHandler(new ChangeHandler() {
				public void onChange(ChangeEvent event) {
					addFlagButton.setDisabled(!((Boolean) event.getValue()));
				}
			});

			ButtonItem clearFlagButton = new ButtonItem("cancelBtn", "Clear");
			clearFlagButton.setStartRow(false);
			clearFlagButton.setEndRow(true);
			clearFlagButton.setAlign(Alignment.LEFT);
			clearFlagButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					clearReponseTextItem();
					formRange.reset();
					// fire default selection
					addFlagButton.setDisabled(true);
					SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.RULE);
					EventBus.getMainEventBus().fireEvent(setTimeRange);
					EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
					EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
				}
			});
			responseTextItem = new Canvas();
			responseTextItem.setVisible(false);
			formRange.setFields(startItem, endItem, checkboxItem, addFlagButton, clearFlagButton);

			messageLayout.addMember(timeSeriesCanvas);
			messageLayout.addMember(formRange);
			messageLayout.addMember(responseTextItem);

			addItem(messageLayout);
		}

	}

	private String convertTime(Date date) {
		DateTimeFormat formatter = DateTimeFormat.getFormat("dd.MM.yyyy");
		return formatter.format(date).toString();
	}

	protected void closeQualityFlagger() {
		clearReponseTextItem();
		formRange.reset();
		hide();
		// set quality control status
		EventBus.getMainEventBus().fireEvent(new SetQualityControlStatusEvent(false));
		EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
	}

	public void updateReponseTextItem(QCResponseCode msg) {
		this.responseTextItem.setVisible(true);
		this.responseTextItem.setContents("<span style='color:" + msg.getColor() + ";'>" + msg.getResponseMessage() + "</span>");
	}

	private void clearReponseTextItem() {
		this.responseTextItem.setContents("");
	}

	public class QualityFlaggerByRuleEventBroker implements LegendElementSelectedEventHandler {

		public QualityFlaggerByRuleEventBroker() {
			EventBus.getMainEventBus().addHandler(LegendElementSelectedEvent.TYPE, this);

		}

		public void onSelected(LegendElementSelectedEvent evt) {
			// TODO Auto-generated method stub
			if (evt.getElement().getDataWrapper() instanceof TimeSeries) {
				TimeSeries ts = (TimeSeries) evt.getElement().getDataWrapper();
				QualityFlaggerByRule.this.timeSeriesId = ts.getId();
				QualityFlaggerByRule.this.property = ts.getPhenomenonId();
				QualityFlaggerByRule.this.sensor = ts.getProcedureId();
				setStartDate(new Date(ts.getFirstValueDate()));
				setEndDate(new Date(ts.getLastValueDate()));

				if (getStartDate() != null) {
					isStartDateAvailable = true;
				} else {
					isStartDateAvailable = false;

				}
				if (getEndDate() != null) {
					isEndDateAvailable = true;
				} else {
					isEndDateAvailable = false;
				}

				QualityFlaggerByRule.this.updateContents();
			}

		}

	}

	private String getTimeSeriesId() {
		return this.timeSeriesId;
	}

	public Canvas getTimeSeriesInfoCanvas() {
		return this.timeSeriesCanvas;
	}

	public void setStartDate(Date d) {
		this.firstValueDate = d;
	}

	public void setEndDate(Date d) {
		this.lastValueDate = d;
	}

	private String getStartDate() {
		return this.firstValueDate.toString();
	}

	private String getEndDate() {
		return this.lastValueDate.toString();
	}

	public DateItem getStartDateItem() {
		return this.startItem;
	}

	public DateItem getEndDateItem() {
		return this.endItem;
	}

	private void updateContents() {
		String additionalTxt = "Sensor: " + this.sensor + "<BR>" + "Property: " + this.property + " <BR>";
		String dateTxt = "Data availability: <BR>" + convertTime(this.firstValueDate) + " -- " + convertTime(this.lastValueDate) + "<BR>";
		this.timeSeriesCanvas.setContents(this.timeSeriesCanvas.getPrefix() + additionalTxt + dateTxt);
	}

	/**
	 * Compares two date with a precision of one second.
	 * 
	 * @param baseDate
	 *            The base date
	 * @param afterDate
	 *            The date supposed to be after.
	 * @return True if the afterDate is indeed after the baseDate.
	 */
	public static boolean after(final Date baseDate, final Date afterDate) {
		if ((baseDate == null) || (afterDate == null)) {
			throw new IllegalArgumentException("Can't compare the dates, at least one of them is null");
		}

		final long baseTime = baseDate.getTime() / 1000;
		final long afterTime = afterDate.getTime() / 1000;
		return baseTime < afterTime;
	}

	/**
	 * Compares two date with a precision of one second.
	 * 
	 * @param baseDate
	 *            The base date
	 * @param beforeDate
	 *            The date supposed to be before.
	 * @return True if the beforeDate is indeed before the baseDate.
	 */
	public static boolean before(final Date baseDate, final Date beforeDate) {
		if ((baseDate == null) || (beforeDate == null)) {
			throw new IllegalArgumentException("Can't compare the dates, at least one of them is null");
		}

		final long baseTime = baseDate.getTime() / 1000;
		final long beforeTime = beforeDate.getTime() / 1000;
		return beforeTime < baseTime;
	}

}

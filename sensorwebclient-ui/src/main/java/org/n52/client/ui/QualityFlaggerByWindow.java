package org.n52.client.ui;

import static org.n52.client.sos.i18n.SosStringsAccessor.i18n;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import org.eesgmbh.gimv.client.event.SetImageUrlEvent;
import org.eesgmbh.gimv.client.event.SetImageUrlEventHandler;
import org.n52.client.bus.EventBus;
import org.n52.client.sos.ctrl.DataManagerSosImpl;
import org.n52.client.sos.ctrl.QCResponseCode;
import org.n52.client.sos.ctrl.SOSRequestManager;
import org.n52.client.sos.event.data.AddFlagEvent;
import org.n52.client.sos.event.data.ClearSelectedPointEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent;
import org.n52.client.sos.event.data.SetQualityControlStatusEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent.FlaggingPointType;
//import com.google.code.gwt.crop.client.GWTCropper;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.FormErrorOrientation;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;

//ASD-2013
public class QualityFlaggerByWindow extends Tab {

	private static final String COMPONENT_ID = "qualityControlFlaggerByWindow";

	private static QualityFlaggerByWindow instance;

	private DynamicForm formRange;

	private String activeTextField = "";

	private TextItem startTxt;

	private TextItem endTxt;

	private SelectItem flagSelect;

	private SelectItem flagSpecificSelect;

	private HTMLFlow responseTextItem;

	private static String imageURL = "";

	//private GWTCropper cropper = null;
	
	
	public static QualityFlaggerByWindow getInst() {
		if (instance == null) {
			instance = new QualityFlaggerByWindow();
		}
		return instance;
	}

	private QualityFlaggerByWindow() {
		new QualityFlaggerByWindowEventBroker();
		setID(COMPONENT_ID);
		setTitle(i18n.qualityFlaggingWindow());
		initializeQualityRangeWindow();
	}

	public void showCroppingBox()
	{
		if(imageURL.equals(""))
		{
			imageURL = SOSRequestManager.getInstance().getMainImageURL();
		}
		//cropper = new GWTCropper(imageURL);
		//cropper.setAspectRatio(1); // square selection (optional)
		//EESTab.addCroppingBox(cropper);
	}
	
	public void hideCroppingBox()
	{
		//EESTab.removeCroppingBox(cropper);
	}
	
	
	private void initializeQualityRangeWindow() {
		VLayout messageLayout = new VLayout();
		messageLayout.setLayoutMargin(15);

		if (formRange == null) {
			formRange = new DynamicForm();
			formRange.setHeight100();
			formRange.setWidth100();
			formRange.setPadding(10);
			formRange.setLayoutAlign(VerticalAlignment.CENTER);
			formRange.setNumCols(3);
			formRange.setErrorOrientation(FormErrorOrientation.RIGHT);

			startTxt = new TextItem();
			startTxt.setShowTitle(false);
			startTxt.setName("Start");
			startTxt.setCanEdit(false);
			startTxt.setStartRow(false);
			startTxt.setEndRow(false);
			startTxt.setRequired(true);
			startTxt.setValue("");
			startTxt.setCanEdit(false);

			endTxt = new TextItem();
			endTxt.setShowTitle(false);
			endTxt.setName("End");
			endTxt.setStartRow(false);
			endTxt.setEndRow(false);
			endTxt.setCanEdit(false);
			endTxt.setRequired(true);
			endTxt.setValue("");
			endTxt.setCanEdit(false);

			DataManagerSosImpl dataManager = DataManagerSosImpl.getInst();
			final Map<String, ArrayList<String>> codeList = dataManager.getQualityCodeList();
			String[] genericFlags = codeList.keySet().toArray(new String[codeList.size()]);

			// get flag code
			flagSpecificSelect = new SelectItem("SpecificCode", "Specific Code");
			flagSpecificSelect.setAlign(Alignment.LEFT);
			flagSpecificSelect.setAddUnknownValues(false);
			flagSpecificSelect.setRequired(true);

			flagSelect = new SelectItem("FlagCode", "Flag Code");
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

			/*
			ButtonItem cropButton = new ButtonItem("cropBtn", "Crop");
			cropButton.setStartRow(false);
			cropButton.setEndRow(true);
			cropButton.setAlign(Alignment.LEFT);
			cropButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					GWT.log("Selected area from (" +
					cropper.getSelectionXCoordinate() + "," +
					cropper.getSelectionYCoordinate() + ") " + " with width=" +
					cropper.getSelectionWidth() + " and height=" +
					cropper.getSelectionHeight());
				}

			}); */

			ButtonItem addFlagButton = new ButtonItem("saveBtn", "Add Flag");
			addFlagButton.setStartRow(true);
			addFlagButton.setEndRow(false);
			addFlagButton.setAlign(Alignment.RIGHT);
			addFlagButton.addClickHandler(new ClickHandler() {

				boolean dateComparison = false;
				// HH:mm:ssZ
				DateTimeFormat f = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ssZ");

				public void onClick(ClickEvent event) {
					boolean valid = formRange.validate();
					if (valid) {
						// GWT doesnot support CET
						String startDate = startTxt.getValue().toString().replaceAll("CET", "").trim();
						String endDate = endTxt.getValue().toString().replaceAll("CET", "").trim();
						Date first = (Date) f.parse(startDate);
						Date end = (Date) f.parseStrict(endDate);
						dateComparison = first.before(end);
						if (dateComparison) {
							EventBus.getMainEventBus().fireEvent(new AddFlagEvent(true,false,false));
						}

					} else {
						if (valid) {
							updateReponseTextItem(QCResponseCode.TIMERANGEINVALID);
						}
					}

				}
			});
			ButtonItem clearFlagButton = new ButtonItem("cancelBtn", "Clear");
			clearFlagButton.setStartRow(false);
			clearFlagButton.setEndRow(false);
			clearFlagButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					formRange.clearValues();
					// fire default selection
					SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.WINDOW);
					EventBus.getMainEventBus().fireEvent(setTimeRange);
					EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());

				}
			});

			responseTextItem = new HTMLFlow();
			responseTextItem.setVisible(false);

			//formRange.setFields(startTxt, endTxt, cropButton, flagSelect, flagSpecificSelect, addFlagButton, clearFlagButton);
			messageLayout.addMember(formRange);
			messageLayout.addMember(responseTextItem);
			this.setPane(messageLayout);
		}

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

	public TextItem getStartTextITem() {
		return this.startTxt;
	}

	public TextItem getEndTextITem() {
		return this.endTxt;
	}

	public SelectItem getQualityFlag() {
		return this.flagSelect;
	}

	public void closeQualityFlagger() {
		formRange.reset();
		clearReponseTextItem();
		hideCroppingBox();
		EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
		// set quality control status
		EventBus.getMainEventBus().fireEvent(new SetQualityControlStatusEvent(false));
		// EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
	}

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

	public DynamicForm getFormWindow() {
		return formRange;
	}

	public void setFormWindow(DynamicForm formRange) {
		this.formRange = formRange;
	}

	public HTMLFlow getResponseTextItem() {
		return responseTextItem;
	}

	public void setResponseTextItem(HTMLFlow responseTextItem) {
		this.responseTextItem = responseTextItem;
	}
	
	private class QualityFlaggerByWindowEventBroker implements SetImageUrlEventHandler
	{
		QualityFlaggerByWindowEventBroker() {
			EventBus.getMainEventBus().addHandler(SetImageUrlEvent.TYPE, this);
		}

		@Override
		public void onSetImageUrl(SetImageUrlEvent event) {
			// TODO Auto-generated method stub
			//ASD
			GWT.log("QualityFlaggerByWindowEventBroker-SetImageUrlEvent : "+event.getUrl());
			imageURL = event.getUrl();
		}
	}

}

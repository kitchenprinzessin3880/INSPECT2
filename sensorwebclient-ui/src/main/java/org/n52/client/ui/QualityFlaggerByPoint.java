package org.n52.client.ui;

import static org.n52.client.sos.i18n.SosStringsAccessor.i18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eesgmbh.gimv.client.event.LoadImageDataEvent;
import org.n52.client.bus.EventBus;
import org.n52.client.sos.ctrl.DataManagerSosImpl;
import org.n52.client.sos.ctrl.QCResponseCode;
import org.n52.client.sos.event.data.AddFlagEvent;
import org.n52.client.sos.event.data.ClearSelectedPointEvent;
import org.n52.client.sos.event.data.FinishedAddFlagEvent;
import org.n52.client.sos.event.data.handler.FinishedAddFlagEventHandler;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.FormErrorOrientation;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;

//@ASD-2013
public class QualityFlaggerByPoint extends Tab {

	private static final String COMPONENT_ID = "qualityControlFlaggerByPoint";

	private static QualityFlaggerByPoint instance;

	private DynamicForm formPoint;
	private DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ssZZZ");

	private String activeTextField = "";
	private Map<String, ArrayList<String>> codeList = null;
	private TextItem timePointTxt;

	private HTMLFlow responseTextItem;
	private HLayout exportLoadingSpinner;


	private SelectItem flagSelect;

	private SelectItem flagSpecificSelect;

	public static QualityFlaggerByPoint getInst() {

		if (instance == null) {
			instance = new QualityFlaggerByPoint();
		}
		return instance;
	}

	private QualityFlaggerByPoint() {
		setID(COMPONENT_ID);
		setTitle(i18n.qualityFlaggingPoint());
		initializeQualityPointWindow();
		new QualityFlaggerByPointEventBroker();
	}

	private void initializeQualityPointWindow() {
		VLayout wrapper = new VLayout();
		wrapper.setMargin(30);
		wrapper.setAlign(Alignment.CENTER);
		wrapper.setWidth100();

		if (formPoint == null) {
			formPoint = new DynamicForm();
			formPoint.setHeight100();
			formPoint.setWidth100();
			formPoint.setPadding(10);
			formPoint.setNumCols(3);
			formPoint.setLayoutAlign(VerticalAlignment.CENTER);
			// formPoint.setNumCols(3);
			formPoint.setErrorOrientation(FormErrorOrientation.RIGHT);

			timePointTxt = new TextItem();
			timePointTxt.setTitle("DateTime");
			timePointTxt.setCanEdit(false);
			timePointTxt.setStartRow(false);
			timePointTxt.setEndRow(false);
			timePointTxt.setSelectOnFocus(true);
			timePointTxt.setRequired(true);

			createExportLoadingSpinner();
			Label space = new Label();
			space.setWidth("1%");

			codeList = new HashMap<String, ArrayList<String>>();
			codeList = DataManagerSosImpl.getInst().getQualityCodeList();

			Map<String, ArrayList<String>> codeListModified = new HashMap<String, ArrayList<String>>();
			codeListModified.putAll(codeList);
			codeListModified.remove("unevaluated");

			String[] genericFlags = codeListModified.keySet().toArray(new String[codeListModified.size()]);

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
					formPoint.getField("SpecificCode").setValueMap(specificArray);
					if (specificArray.length == 1 && specificArray[0].equalsIgnoreCase(selectedItem)) {
						formPoint.getField("SpecificCode").setValue(selectedItem);
						flagSpecificSelect.setDisabled(true);
					} else {
						flagSpecificSelect.setDisabled(false);
					}
				}
			});

			IButton addFlagButton = new IButton("Add Flag");
			addFlagButton.setTooltip("This button only updates data values with unevaluated flags");
			addFlagButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					boolean valid = formPoint.validate();
					if (valid) {
						//exportLoadingSpinner.show();
						EventBus.getMainEventBus().fireEvent(new AddFlagEvent(false,false,false));
					}
				}
			});

			IButton modifyFlagButton = new IButton("Overwrite Flag");
			modifyFlagButton.setTooltip("This button overwrite the flags of all selected values");
			// modifyFlagButton.setStartRow(true);
			// modifyFlagButton.setEndRow(false);
			modifyFlagButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					boolean valid = formPoint.validate();
					if (valid) {
						//exportLoadingSpinner.show();
						EventBus.getMainEventBus().fireEvent(new AddFlagEvent(true, true,false));
					}
				}
			});

			IButton clearFlagButton = new IButton("Clear");
			// clearFlagButton.setStartRow(false);
			// clearFlagButton.setEndRow(true);
			clearFlagButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					formPoint.clearValues();
					clearReponseTextItem();
					EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
					EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
				}
			});

			responseTextItem = new HTMLFlow();
			responseTextItem.setVisible(false);

			final HLayout buttonLayout = new HLayout();
			buttonLayout.setWidth100();
			buttonLayout.setMembersMargin(5);
			buttonLayout.setAlign(VerticalAlignment.CENTER);
			buttonLayout.addMember(addFlagButton);
			buttonLayout.addMember(modifyFlagButton);
			buttonLayout.addMember(clearFlagButton);

			formPoint.setFields(timePointTxt, flagSelect, flagSpecificSelect);
			// addItem(formPoint);
			wrapper.addMember(formPoint);
			wrapper.addMember(buttonLayout);
			wrapper.addMember(space);
			wrapper.addMember(exportLoadingSpinner);
			wrapper.addMember(responseTextItem);
			this.setPane(wrapper);
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

	public SelectItem getQualityFlag() {
		return this.flagSelect;
	}

	public TextItem getTimePointTextItem() {
		return this.timePointTxt;
	}

	/*
	public void closeQualityFlagger() {
		formPoint.clearValues();
		if (responseTextItem.isVisible()) {
			clearReponseTextItem();
		}
		EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
		// set quality control status
		EventBus.getMainEventBus().fireEvent(new SetQualityControlStatusEvent(false));
		// EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
	}*/

	public DynamicForm getQualityForm() {
		return formPoint;
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

	/*
	 * // Search for time series window private void
	 * initializeQualityFlagWindow() {
	 * 
	 * if (qualFormLayout == null) { Layout qualFormLayout = new Layout();
	 * //qualFormLayout = new InteractionWindow(layout);
	 * //qualFormLayout.setZIndex(1000000); qualFormLayout.setWidth(200);
	 * qualFormLayout.setHeight(180);
	 * qualFormLayout.setTitle(i18n.qualityFlagging() + " - "+ i18n.byRange());
	 * //setFlagbyRangeWindowPosition();
	 * 
	 * // start,end, flag horizontal panels flagListBox.addItem("OK", "OK");
	 * flagListBox.addItem("Min Error", "MIN"); flagListBox.addItem("Max Error",
	 * "MAX"); flagListBox.setVisibleItemCount(1);
	 * 
	 * formTable.setText(0, 0, "Start Time"); formTable.setWidget(0, 1,
	 * startTimeButton); formTable.setText(1, 0, "End Time");
	 * formTable.setWidget(1, 1, endTimeButton); formTable.setText(2, 0,
	 * "Flag Code"); formTable.setWidget(2, 1, flagListBox);
	 * formTable.setCellSpacing(5); formTable.setCellPadding(3);
	 * 
	 * 
	 * formTable.setStyleName("qualityTableHeader");
	 * 
	 * // add buttons //addPanelButton.add(addFlagButton);
	 * //addPanelButton.add(clearFlagButton);
	 * 
	 * formTable.setWidget(3, 0, addFlagButton); formTable.setWidget(3, 1,
	 * clearFlagButton);
	 * 
	 * //formTable.getColumnFormatter().addStyleName(col, styleName);
	 * 
	 * // vertical layout qualFormLayout.addMember(formTable);
	 * //qualFormLayout.addMember(addPanelButton);
	 * 
	 * addFlagButton.addClickHandler(this);
	 * clearFlagButton.addClickHandler(this);
	 * startTimeButton.addClickHandler(this);
	 * endTimeButton.addClickHandler(this); addItem(qualFormLayout); }
	 */

	// }

	public DynamicForm getFormPoint() {
		return formPoint;
	}

	public void setFormPoint(DynamicForm formPoint) {
		this.formPoint = formPoint;
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

	private class QualityFlaggerByPointEventBroker implements FinishedAddFlagEventHandler {
		QualityFlaggerByPointEventBroker() {
			EventBus.getMainEventBus().addHandler(FinishedAddFlagEvent.TYPE, this);
		}

		@Override
		public void onFinishedAddFlag(FinishedAddFlagEvent evt) {
			// TODO Auto-generated method stub
			if (exportLoadingSpinner.isVisible()) {
				exportLoadingSpinner.hide();
			}
		}
	}

	public Map<String, ArrayList<String>> getCodeList() {
		return codeList;
	}

	public void setCodeList(Map<String, ArrayList<String>> codeList) {
		this.codeList = codeList;
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
	
	public HLayout getExportLoadingSpinner() {
		return exportLoadingSpinner;
	}

	public HTMLFlow getResponseTextItem() {
		return responseTextItem;
	}

	public void setResponseTextItem(HTMLFlow responseTextItem) {
		this.responseTextItem = responseTextItem;
	}
}

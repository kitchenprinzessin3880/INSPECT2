package org.n52.client.sos.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.client.bus.EventBus;
import org.n52.client.sos.ctrl.DataManagerSosImpl;
import org.n52.client.sos.ctrl.QCResponseCode;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.event.data.CustomRenderingEvent;
import org.n52.client.sos.event.data.FinishedAddFlagEvent;
import org.n52.client.sos.event.data.handler.FinishedAddFlagEventHandler;
import org.n52.client.sos.legend.TimeSeries;

import com.google.gwt.core.shared.GWT;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortArrow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

public class FilteringTab extends Tab {
	private static final String COMPONENT_ID = "qualityControlFlaggerFilter";
	private DynamicForm formRender;
	protected VLayout layout;
	protected ArrayList<String> sosURLMap;
	protected LinkedHashMap<String, String> offeringMap = new LinkedHashMap<String, String>();
	private HTMLFlow responseTextItem;
	private HLayout exportLoadingSpinner;
	//private ListGrid mainGrid;
	private IButton renderButton;
	private ListGrid flagGrid;
	private int columnNumber;
	private int rowNumber;
	protected DynamicForm formTop;
	private static FilteringTab instance;
	private List<String> flagList;
	private static DataManagerSosImpl dataManager;
	private boolean isRedundant = false;

	public static FilteringTab getInst() {
		dataManager = DataManagerSosImpl.getInst();
		if (instance == null) {
			instance = new FilteringTab();
		}
		return instance;
	}

	private FilteringTab() {
		new FilteringTabEventBroker();
		setID(COMPONENT_ID);
		setTitle("Customize Flags");
		createFormContent();
	}

	private void createFormContent() {
		VStack vStack = new VStack();
		vStack.setWidth100();

		// get general flags
		final Map<String, ArrayList<String>> codeList = dataManager.getQualityCodeList();
		this.flagList = new ArrayList<String>(codeList.keySet());
		Collections.sort(this.flagList, Collections.reverseOrder());

		if (formRender == null) {
			formRender = new DynamicForm();

			/*
			mainGrid = new ListGrid();
			mainGrid.setAlign(Alignment.CENTER);
			mainGrid.setSelectionType(SelectionStyle.SIMPLE);
			mainGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
			mainGrid.setAutoFitData(Autofit.VERTICAL);
			mainGrid.setMargin(15);

			mainGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
				@Override
				public void onSelectionChanged(SelectionEvent event) {
					ListGrid grid = (ListGrid) event.getSource();
					ListGridRecord[] record = grid.getSelectedRecords();
					if (record.length > 0 && flagGrid.getSelectedRecords().length > 0 && !isRedundant) {
						renderButton.setDisabled(false);
					} else {
						renderButton.setDisabled(true);
					}
				}
			});
			 */
			ListGridField colorField = new ListGridField("color", "Series");
			colorField.setCanSort(false);
			colorField.setAlign(Alignment.CENTER);

			ListGridField sensorField = new ListGridField("sensor", "Sensor");
			sensorField.setCanSort(false);
			ListGridField propField = new ListGridField("property", "Property");
			propField.setCanSort(false);

			//mainGrid.setFields(colorField, sensorField, propField);
			//mainGrid.setData(updateListGridRecords());

			//mainGrid.setSort(new SortSpecifier[] { new SortSpecifier("sensor", SortDirection.ASCENDING) });
			/*mainGrid.getField("color").setCellFormatter(new CellFormatter() {
				@Override
				public String format(Object value, ListGridRecord record, int rowNum, int colNum) {
					String color = record.getAttribute("color");
					return "<div style=\"width:14px;height:14px;background-color:" + color + ";\">" + "</div>";
				}
			});

			ListGridField[] listGridFields = mainGrid.getFields();
			for (ListGridField listGridField : listGridFields) {
				listGridField.setWidth("100%");
			}
			
			mainGrid.getField("color").setWidth(40);

			HTMLFlow flagLabel = new HTMLFlow();
			flagLabel.setHeight(25);
			flagLabel.setWidth100();
			flagLabel.setOverflow(Overflow.VISIBLE);
			flagLabel.setContents("Click on a particular 'Series Shape' to modfiy the value.");
			 */
			
			createExportLoadingSpinner();
			
			flagGrid = new ListGrid();
			flagGrid.setAlign(Alignment.CENTER);
			flagGrid.setSelectionType(SelectionStyle.SIMPLE);
			flagGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
			flagGrid.setMargin(15);
			flagGrid.setEditEvent(ListGridEditEvent.CLICK);
			flagGrid.setEditByCell(true);
			flagGrid.setShowSortArrow(SortArrow.NONE);
			flagGrid.setShowHeaderContextMenu(false);
			flagGrid.setShowHeaderMenuButton(false);
			flagGrid.setData(updateFlagGridRecords(dataManager.getActiveFlagsAndShapes()));
			flagGrid.setAutoFitData(Autofit.VERTICAL);
			flagGrid.addCellClickHandler(new CellClickHandler() {
				public void onCellClick(CellClickEvent event) {
					//ListGridRecord record = event.getRecord();
					int colNum = event.getColNum();
					int rowNum = event.getRowNum();
					setColumnNumber(colNum);
					setRowNumber(rowNum);
				}
			});

			flagGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
				@Override
				public void onSelectionChanged(SelectionEvent event) {
					ListGrid grid = (ListGrid) event.getSource();
					ListGridRecord[] record = grid.getSelectedRecords();
					//if (record.length > 0 && mainGrid.getSelectedRecords().length > 0 && !isRedundant) {
					if (record.length > 0 && !isRedundant) {
						renderButton.setDisabled(false);
					} else {
						renderButton.setDisabled(true);
					}
				}
			});

			ListGridField flagField = new ListGridField("flag", "Flag Type");
			flagField.setCanSort(false);
			flagField.setAlign(Alignment.CENTER);
			flagField.setCanEdit(false);

			ListGridField exampleField = new ListGridField("example", "Example");
			exampleField.setCanSort(false);
			exampleField.setAlign(Alignment.CENTER);
			exampleField.setCanEdit(false);
			exampleField.setType(ListGridFieldType.IMAGE);
			exampleField.setImageURLPrefix("../img/icons/");
			exampleField.setImageURLSuffix(".png");

			final ComboBoxItem shapesItem = new ComboBoxItem();
			List<String> allShapes = dataManager.getAllDefaultShapes();

			String[] shapesArray = new String[allShapes.size()];
			
			shapesItem.setValueMap(allShapes.toArray(shapesArray));
			shapesItem.addChangedHandler(new ChangedHandler() {
				public void onChanged(ChangedEvent event) {
					int selectedRow = getRowNumber();
					int selectedCol = getColumnNumber();
					if (isShapesRedundant()) {
						updateReponseTextItem(QCResponseCode.REDUNDANTSHAPES);
						//if (mainGrid.getSelectedRecords().length > 0) {
							renderButton.setDisabled(true);
						//}
					} else {
						clearReponseTextItem();
						//if (mainGrid.getSelectedRecords().length > 0) {
							renderButton.setDisabled(false);
						//}
						// ComboBoxItem(event.getItem().getJsObj()).getSelectedRecord();
						// ListGridRecord record =
						// flagGrid.getRecord(selectedRow);
					}
					flagGrid.setEditValue(selectedRow, selectedCol + 1, event.getValue().toString());
				}
			});

			final ListGridField symbolField = new ListGridField("symbol", "Series Shape", 100);
			symbolField.setCanSort(false);
			symbolField.setAlign(Alignment.CENTER);
			symbolField.setEditorType(shapesItem);
			symbolField.setCanEdit(true);

			flagGrid.setFields(flagField, symbolField, exampleField);

			ListGridField[] defaultListGridFields = flagGrid.getFields();
			for (ListGridField listGridField : defaultListGridFields) {
				listGridField.setWidth("100%");
			}
			flagGrid.getField("example").setWidth(60);

			final HLayout buttonLayout = new HLayout();
			buttonLayout.setWidth100();
			buttonLayout.setLayoutMargin(8);
			buttonLayout.setMembersMargin(5);
			buttonLayout.setAlign(VerticalAlignment.CENTER);

			renderButton = new IButton("Update Series");
			renderButton.setDisabled(true);
			buttonLayout.addMember(renderButton);
			IButton resetButton = new IButton("Reset to Default Shapes");
			resetButton.setWidth(140);
			buttonLayout.addMember(resetButton);

			renderButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					exportLoadingSpinner.show();
					EventBus.getMainEventBus().fireEvent(new CustomRenderingEvent(flagGrid.getRecords()));
				}
			});

			resetButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					//mainGrid.deselectAllRecords();
					flagGrid.deselectAllRecords();
					clearReponseTextItem();
					// redraw flagGrid
					flagGrid.setData(updateFlagGridRecords(dataManager.getDefaultFlagsAndShapes()));
					EventBus.getMainEventBus().fireEvent(new CustomRenderingEvent(flagGrid.getRecords()));
					renderButton.setDisabled(true);
				}
			});

			responseTextItem = new HTMLFlow();
			responseTextItem.setVisible(false);

			Label space = new Label();
			space.setHeight("3%");
			//vStack.addMember(mainGrid);
			vStack.addMember(space);
			//vStack.addMember(flagLabel);
			vStack.addMember(exportLoadingSpinner);
			vStack.addMember(responseTextItem);
			vStack.addMember(flagGrid);
			vStack.addMember(buttonLayout);
			this.setPane(vStack);
		}

	}

	private boolean isShapesRedundant() {
		flagGrid.saveAllEdits();
		ArrayList<String> tempList = new ArrayList<String>();
		// validate unique shape
		ListGridRecord[] records = flagGrid.getRecords();
		for (int i = 0; i < records.length; i++) {
			Record record = records[i];
			tempList.add(record.getAttribute("symbol"));
			GWT.log("isShapesRedundant:" + record.getAttribute("symbol"));
		}
		Set<String> set = new HashSet<String>(tempList);
		if (set.size() < tempList.size()) {
			/* There are duplicates */
			isRedundant = true;
			return true;
		} else {
			isRedundant = false;
			return false;
		}
	}

	protected ListGridRecord[] updateListGridRecords() {
		TimeSeries[] ts = DataStoreTimeSeriesImpl.getInst().getTimeSeriesSorted();
		ListGridRecord[] records = new ListGridRecord[ts.length];

		for (int i = 0; i < ts.length; i++) {
			ListGridRecord record = new ListGridRecord();
			String color = ts[i].getColor().trim();
			String proc = ts[i].getProcedureId().trim();
			String phen = ts[i].getPhenomenonId().trim();
			record.setAttribute("color", color);
			record.setAttribute("sensor", proc);
			record.setAttribute("property", phen);
			records[i] = record;
		}

		return records;
	}

	/*
	public ListGrid getMainGrid() {
		return mainGrid;
	}

	public void setMainGrid(ListGrid mainGrid) {
		this.mainGrid = mainGrid;
	} */

	public ListGrid getFlagGrid() {
		return flagGrid;
	}

	public void setFlagGrid(ListGrid flagGrid) {
		this.flagGrid = flagGrid;
	}

	protected ListGridRecord[] updateFlagGridRecords(Map<String, String> shapes) {
		// remove generic flags: ok, missing
		Iterator<String> iter = this.flagList.iterator();
		while (iter.hasNext()) {
			String s = iter.next();
			if (s.equalsIgnoreCase("missing")) {
				iter.remove();
			}
		}

		ListGridRecord[] records = new ListGridRecord[this.flagList.size()];

		for (int i = 0; i < this.flagList.size(); i++) {
			ListGridRecord record = new ListGridRecord();
			record.setAttribute("flag", flagList.get(i));
			Map<String, String> flagShapePairs = shapes;
			record.setAttribute("symbol", flagShapePairs.get(flagList.get(i)));
			record.setAttribute("example", flagShapePairs.get(flagList.get(i)));
			records[i] = record;
		}
		return records;
	}
	
	
	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(int columnNumber) {
		this.columnNumber = columnNumber;
	}

	public void clearFilteringTab() {
		flagGrid.deselectAllRecords();
		flagGrid.setData(updateFlagGridRecords(dataManager.getActiveFlagsAndShapes()));
		clearReponseTextItem();
	}

	public class FilteringTabEventBroker implements FinishedAddFlagEventHandler {

		public FilteringTabEventBroker() {
			EventBus.getMainEventBus().addHandler(FinishedAddFlagEvent.TYPE, this);
		}

		@Override
		public void onFinishedAddFlag(FinishedAddFlagEvent evt) {
			// TODO Auto-generated method stub
			if(exportLoadingSpinner.isVisible())
			{
				exportLoadingSpinner.hide();
			}
		}

	}

	public HTMLFlow getResponseTextItem() {
		return responseTextItem;
	}

	public void setResponseTextItem(HTMLFlow responseTextItem) {
		this.responseTextItem = responseTextItem;
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

}

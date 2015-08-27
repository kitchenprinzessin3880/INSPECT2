/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.client.ui.legend;

import static org.n52.client.sos.i18n.SosStringsAccessor.i18n;

import java.util.ArrayList;
import java.util.Iterator;

import org.eesgmbh.gimv.client.event.LoadImageDataEvent;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.n52.client.bus.EventBus;
import org.n52.client.ctrl.TimeManager;
import org.n52.client.sos.ctrl.DataManagerSosImpl;
import org.n52.client.sos.ctrl.SOSRequestManager;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.event.ChangeTimeSeriesStyleEvent;
import org.n52.client.sos.event.data.DeleteAllTimeSeriesEvent;
import org.n52.client.sos.event.data.GetFeatureEvent;
import org.n52.client.sos.event.data.GetOfferingEvent;
import org.n52.client.sos.event.data.GetPhenomenonsEvent;
import org.n52.client.sos.event.data.GetProcedureEvent;
import org.n52.client.sos.event.data.GetStationsEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent;
import org.n52.client.sos.event.data.SetQualityControlStatusEvent;
import org.n52.client.sos.event.data.ExportEvent.ExportType;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent.FlaggingPointType;
import org.n52.client.sos.legend.TimeSeries;
import org.n52.client.sos.ui.QualityControlTab;
import org.n52.client.sos.ui.StationSelector;
import org.n52.client.ui.Impressum;
import org.n52.client.ui.QualityFlagger;
import org.n52.client.ui.QualityFlaggerByView;
import org.n52.client.ui.View;
import org.n52.client.ui.btn.ImageButton;
import org.n52.shared.serializable.pojos.BoundingBox;
import org.n52.shared.serializable.pojos.sos.SOSMetadata;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.HStack;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;

public class Legend extends VLayout {

	private ArrayList<LegendElement> legendEntries = new ArrayList<LegendElement>();

	private ArrayList<ImageButton> contributedButtons = new ArrayList<ImageButton>();

	private LegendController controller;

	private LegendElement selectedElement;

	private VStack legend;

	private String elemID;

	private HLayout topButtons;

	private Label exportButton;

	// ASd
	private Label qualityButton;

	private Label sesTabButton;

	private Label eesTabButton;

	private VLayout exportMenu;

	private VLayout qualityMenu;

	private VLayout timeSeriesMenu;

	private HLayout exportLoadingSpinner;

	private Label timeSeriesButton;

	public Legend(String id) {
		this.elemID = id;
		setStyleName("n52_sensorweb_client_legend");
		this.controller = new LegendController(this);
		generateLegend();
		this.legend.setCanAcceptDrop(true);
		// legend.setAnimateMembers(true);
	}

	private MapWidget createMapContent() {
		OverviewMapController controller = new OverviewMapController();
		return controller.createMap(); // TODO refactor
		// return new LegendMap().getLayout();
	}

	public LegendElement getSelectedLegendelement() {
		return selectedElement;
	}

	public void addLegendElement(LegendElement element) {
		element.setOrdering(legendEntries.size());
		legendEntries.add(element);

		// le.setTargetToDrag(this); // disable legend dragging
		legend.addMember(element.getLayout());

		// legend.addMember(le.getLegendEntry());

	}

	public void contributeTopButtons(ArrayList<ImageButton> buttons) {
		for (ImageButton ib : this.contributedButtons) {
			topButtons.removeMember(ib);
		}

		contributedButtons.clear();

		for (ImageButton button : buttons) {
			button.setMargin(0);
			// ib.setWidth(16);
			// ib.setHeight(16);
			button.setStyleName("n52_sensorweb_client_topButtons");
			topButtons.addMember(button);
		}
		this.contributedButtons.addAll(buttons);
	}

	public void generateLegend() {

		// TODO cleanup/extract method(s)

		setWidth("370px"); // TODO make configurable (Legend width)
		setHeight100();
		// setMargin(4);
		setMinWidth(220);
		// setOverflow(Overflow.CLIP_H);

		this.topButtons = new HLayout();
		this.topButtons.setTabIndex(-1);
		this.topButtons.setHeight(1);
		this.topButtons.setAlign(Alignment.RIGHT);
		this.topButtons.setReverseOrder(true);

		ImageButton us = new ImageButton("us_lang", "../img/icons/gb.png", i18n.usLang(), i18n.usLangExtended());
		View.getInstance().registerTooltip(us);
		ImageButton de = new ImageButton("de_lang", "../img/icons/de.png", i18n.deLang(), i18n.deLangExtended());
		View.getInstance().registerTooltip(de);
		ImageButton ttips = new ImageButton("ttips", "../img/icons/comment.png", i18n.ttips(), i18n.ttipsExtended());
		View.getInstance().registerTooltip(ttips);
		ImageButton help = new ImageButton("help", "../img/icons/help.png", i18n.help(), i18n.helpExtended());
		View.getInstance().registerTooltip(help);
		ImageButton logger = new ImageButton("logger", "../img/icons/report.png", i18n.logger(), i18n.loggerExtended());
		View.getInstance().registerTooltip(logger);

		ImageButton impressum = new ImageButton("impressum", "../img/icons/information.png", i18n.Impressum(), i18n.Impressum());

		Label imprint = new Label(i18n.Impressum());
		imprint.setTooltip(i18n.Impressum());
		imprint.setStyleName("label");
		imprint.setWidth(60);

		View.getInstance().registerTooltip(impressum);
		View.getInstance().switchDetailedTooltips();

		imprint.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				new Impressum().show();
			}
		});

		help.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				String helpUrl = GWT.getHostPageBaseURL() + i18n.helpPath();
				Window.open(helpUrl, "", "");
			}
		});

		// this.topButtons.addMember(us);
		// this.topButtons.addMember(de);
		// this.topButtons.addMember(ttips);
		// this.topButtons.addMember(help);
		// this.topButtons.addMember(logger);
		// this.topButtons.addMember(imprint);

		addMember(this.topButtons);

		// Button addTS = new Button(I18N.sosClient.addNewTimeseries());
		// addTS.setIcon("../img/icons/chart_curve_add_new.png");

		// ImageButton addTS = new ImageButton("addTSLegend",
		// "../img/icons/chart_curve_add_new.png",
		// I18N.sosClient.picker(),
		// I18N.sosClient.pickerExtended());
		// View.getInstance().registerTooltip(addTS);

		// addTS.addClickHandler(new ClickHandler() {
		// public void onClick(ClickEvent evt) {
		// StationSelector.getInst().show();
		// }
		// });

		// addTS.setPadding(2);
		// addTS.setSize("32px", "32px");
		// addTS.setAlign(Alignment.CENTER);

		// Label addTSLabel = new Label(I18N.sosClient.addNewTimeseries());
		// addTSLabel.setHeight(20);
		// addTSLabel.setWidth100();
		// addTSLabel.setPadding(8);
		// addTSLabel.setStyleName("legendAddBoxLabel");
		// addTSLabel.setCursor(Cursor.HAND);
		// addTSLabel.setAlign(Alignment.CENTER);

		// HLayout add = new HLayout();
		// add.setStyleName("legendAddBox");
		// add.setAutoHeight();
		// add.addMember(addTS);
		// add.addMember(addTSLabel);
		// add.addClickHandler(new ClickHandler() {
		// public void onClick(ClickEvent event) {
		// StationSelector.getInst().show();
		// }
		// });

		// zip-export CSV
		// this.exportZipCSV =
		// new ImageButton("diagExportZipCSV", "../img/icons/folder_csv.png",
		// i18nManager.i18nSOSClient
		// .exportZipCSV(), i18nManager.i18nSOSClient.exportZipCSVExtended());

		// View.getInstance().registerTooltip(this.exportZipCSV);

		// zip-export XLS
		// this.exportPDFallInOne =
		// new ImageButton("diagExportPDFallIneOne",
		// "../img/icons/page_white_acrobat_add.png",
		// i18nManager.i18nSOSClient.exportPDFallInOne(),
		// i18nManager.i18nSOSClient.exportPDFallInOneExtended());
		// View.getInstance().registerTooltip(this.exportPDFallInOne);

		Button exportZipCSV = new Button(i18n.csv());
		exportZipCSV.setIcon("../img/icons/table.png");
		exportZipCSV.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				controller.exportTo(ExportType.CSV_ZIP);
			}
		});

		Button exportPDFallInOne = new Button(i18n.pdf());
		exportPDFallInOne.setStyleName("input");
		exportPDFallInOne.setIcon("../img/icons/page_white_acrobat_add.png");
		exportPDFallInOne.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				controller.exportTo(ExportType.PDF_ALL_IN_ONE);
			}
		});

		createExportLoadingSpinner();

		HStack menuStack = new HStack();
		menuStack.setHeight100();
		Label space = new Label();
		space.setWidth("1%");
		// addTS = createAddTimeSeriesLabelButton();
		timeSeriesButton = createTimeSeriesLabelButton();
		timeSeriesButton.setWidth(130);
		exportButton = createExportLabelButton();
		exportButton.setWidth(90);
		eesTabButton = createEESTabLabelButton();
		// sesTabButton = createSESTabLabelButton();
		// ASD 22.09.2014
		qualityButton = createdDataFlaggingLabelButton();
		qualityButton.setWidth(120);
		exportLoadingSpinner.setTop(5);
		exportLoadingSpinner.setWidth("5%");

		menuStack.addMember(space);
		menuStack.addMember(timeSeriesButton);
		menuStack.addMember(space);
		/*
		 * if (SesUtil.isSesActiv()) { addTS.setWidth("28%");
		 * eesTabButton.setWidth("16%"); sesTabButton.setWidth("16%");
		 * menuStack.addMember(eesTabButton); menuStack.addMember(sesTabButton);
		 * menuStack.addMember(space); } else { addTS.setWidth("40%"); }
		 */

		menuStack.addMember(exportButton);
		menuStack.addMember(exportLoadingSpinner);
		menuStack.addMember(space);
		menuStack.addMember(qualityButton);
		menuStack.addMember(space);
		setExportButtonActiv(false);
		setQualityButtonActiv(false);

		VStack separator = new VStack();
		separator.setHeight("10px");

		VStack vMenuStack = new VStack();
		vMenuStack.addMember(menuStack);
		vMenuStack.setHeight(28);
		vMenuStack.addMember(separator);
		addMember(separator);
		addMember(vMenuStack);
		addMember(separator);
		createTimeSeriesMenu();
		addChild(timeSeriesMenu);

		createExportMenu();
		addChild(exportMenu);

		// create Flag manu
		//createQualityMenu();
		//addChild(qualityMenu);

		this.legend = new VStack();
		this.legend.setOverflow(Overflow.AUTO);
		this.legend.setHeight("*");
		this.legend.setShowResizeBar(true);

		addMember(this.legend);
		// this.intro = new HTMLFlow(I18N.sosClient.intro());
		// this.legend.addMember(this.intro);
		addMember(separator);

		Layout layout = new Layout();
		layout.addMember(createMapContent());
		layout.setWidth100();
		layout.setHeight("30%");
		addMember(layout);

		// this.footer = new HLayout();
		// this.footer.setTabIndex( -1);
		// addMember(this.footer);

	}

	private void createExportLoadingSpinner() {
		this.exportLoadingSpinner = new HLayout();
		Img spinner = new Img("../img/loader_wide.gif", 43, 11);
		// this.exportLoadingSpinner.setWidth100();
		// this.exportLoadingSpinner.setHeight100();
		this.exportLoadingSpinner.setAlign(Alignment.CENTER);
		this.exportLoadingSpinner.addMember(spinner);
		this.exportLoadingSpinner.hide();
	}

	/*
	 * commented out by ASD private Label createCSVLabel() { Label toCSV = new
	 * Label(i18n.toCSV()); toCSV.setWrap(false); toCSV.setAutoFit(true);
	 * toCSV.setPadding(3); toCSV.setWidth100();
	 * toCSV.setStyleName("n52_sensorweb_client_exportEntry");
	 * toCSV.addClickHandler(new ClickHandler() {
	 * 
	 * @Override public void onClick(ClickEvent event) {
	 * controller.exportTo(ExportType.CSV_ZIP); exportMenu.hide(); } }); return
	 * toCSV; }
	 */

	private Label createPDFLabel() {
		Label toPDF = new Label("to PDF");
		toPDF.setWrap(false);
		toPDF.setAutoFit(true);
		toPDF.setPadding(3);
		toPDF.setWidth100();
		toPDF.setStyleName("n52_sensorweb_client_exportEntry");
		toPDF.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				controller.exportTo(ExportType.PDF_ALL_IN_ONE);
				exportMenu.hide();
			}
		});
		return toPDF;
	}

	private Label createExportLabelButton() {
		// Label export = new Label(i18n.export());
		// ASD
		Label export = new Label("Export Series");
		export.setStyleName("n52_sensorweb_client_legendbuttonDisabled");
		export.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (exportMenu.isVisible()) {
					exportMenu.hide();
				} else {
					exportMenu.setLeft(exportButton.getAbsoluteLeft() - 2);
					exportMenu.setWidth(exportButton.getWidth());
					exportMenu.show();
				}
			}
		});
		return export;
	}

	// added by ASD
	private Label createQualityFlagLabelButton() {
		Label qual = new Label("Flag Data Series");
		qual.setWrap(false);
		qual.setAutoFit(true);
		qual.setPadding(3);
		qual.setWidth100();
		qual.setStyleName("n52_sensorweb_client_exportEntry");
		qual.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				EventBus.getMainEventBus().fireEvent(new SetQualityControlStatusEvent(true));

				if (QualityControlTab.getInst() != null) {
					int tabSelect = QualityControlTab.getInst().getTabSelected();
					if (tabSelect == 0) {
						SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.MULTIPOINT);
						EventBus.getMainEventBus().fireEvent(setTimeRange);
					} else if (tabSelect == 1) {
						SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.TIMERANGE);
						EventBus.getMainEventBus().fireEvent(setTimeRange);
					} else if (tabSelect == 2) {
						SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.VIEW);
						EventBus.getMainEventBus().fireEvent(setTimeRange);
					} else {
						SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.RENDER);
						EventBus.getMainEventBus().fireEvent(setTimeRange);
					}
				}
				QualityControlTab.getInst().show();
			}
		});
		return qual;
	}

	// added by ASD
	private Label createSaveHistoryLabelButton() {
		Label qual = new Label("Save Current View");
		qual.setWrap(false);
		qual.setAutoFit(true);
		qual.setPadding(3);
		qual.setWidth100();
		qual.setStyleName("n52_sensorweb_client_exportEntry");
		qual.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!DataStoreTimeSeriesImpl.getInst().getDataItems().isEmpty()) {
					String historyValues = "";
					for (TimeSeries ts : DataStoreTimeSeriesImpl.getInst().getDataItems().values()) {
						String sos = ts.getSosUrl();
						String offering = ts.getOfferingId();
						String sensor = ts.getProcedureId();
						String property = ts.getPhenomenonId();
						long start = TimeManager.getInst().getBegin();
						long end = TimeManager.getInst().getEnd();
						historyValues += sos + "," + offering + "," + sensor + "," + property + "," +start+"," +end+"#";
					}
					if (historyValues.charAt(historyValues.length() - 1) == '#') {
						historyValues = historyValues.substring(0, historyValues.length() - 1);
					}
					HistoryManager.getInst().saveHistory(historyValues);
				}
				else
				{
					Window.alert("The timeseries list is empty. Please add data series before saving the series view!");
				}
			}
		});
		return qual;
	}

	// added by ASD
	private Label createOpenHistoryLabelButton() {
		Label qual = new Label("Open Saved View");
		qual.setWrap(false);
		qual.setAutoFit(true);
		qual.setPadding(3);
		qual.setWidth100();
		qual.setStyleName("n52_sensorweb_client_exportEntry");
		qual.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				HistoryManager.getInst().openViewHistory();
			}
		});
		return qual;
	}

	/*
	 * private void createQualityMenu() { Label qcLabel = new
	 * Label(i18n.qualityFlagging()) ;
	 * qcLabel.setStyleName("n52_sensorweb_client_legendbutton");
	 * qcLabel.addClickHandler(new ClickHandler() {
	 * 
	 * @Override public void onClick(ClickEvent event) {
	 * QualityControlTab.getInst().show(); } }); return qcLabel;
	 * 
	 * qualityMenu = new VLayout();
	 * qualityMenu.setLeft(qualityButton.getAbsoluteLeft());
	 * qualityMenu.setTop(30);
	 * qualityMenu.setStyleName("n52_sensorweb_client_interactionmenu");
	 * qualityMenu.setAutoHeight(); qualityMenu.setZIndex(1000000);
	 * qualityMenu.addMember(createFlagByPointLabel());
	 * qualityMenu.addMember(createFlagByRangeLabel());
	 * qualityMenu.addMember(createFlagByView()); qualityMenu.setVisible(false);
	 * }
	 */

	// ASD
	private Label createTimeSeriesLabelButton() {
		Label label = new Label("Add or Delete Series");
		label.setStyleName("n52_sensorweb_client_legendbutton");
		label.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (timeSeriesMenu.isVisible()) {
					timeSeriesMenu.hide();
				} else {
					timeSeriesMenu.setLeft(timeSeriesButton.getAbsoluteLeft() - 2);
					timeSeriesMenu.setWidth(timeSeriesButton.getWidth());
					timeSeriesMenu.show();
				}
			}
		});
		return label;
	}

	//old
	/*
	private Label createdDataFlaggingLabelButton() {
		Label label = new Label("Data Flagging");
		label.setStyleName("n52_sensorweb_client_legendbutton");
		label.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (qualityMenu.isVisible()) {
					qualityMenu.hide();
				} else {
					qualityMenu.setLeft(qualityButton.getAbsoluteLeft() - 2);
					qualityMenu.setWidth(qualityButton.getWidth());
					qualityMenu.show();
				}
			}
		});
		return label;
	}*/

	// added by ASD
		private Label createdDataFlaggingLabelButton() {
			Label qual = new Label("Flag Data Series");
			qual.setStyleName("n52_sensorweb_client_legendbuttonDisabled");
			qual.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					EventBus.getMainEventBus().fireEvent(new SetQualityControlStatusEvent(true));
					QualityControlTab.getInst().show();
				}
			});
			return qual;
		}

	private void createExportMenu() {
		exportMenu = new VLayout();
		exportMenu.setLeft(qualityButton.getAbsoluteLeft());
		exportMenu.setTop(40);
		exportMenu.setStyleName("n52_sensorweb_client_interactionmenu");
		exportMenu.setAutoHeight();
		exportMenu.setZIndex(1000000);
		exportMenu.addMember(createPDFLabel());
		// commented out by ASD
		// exportMenu.addMember(createCSVLabel());
		exportMenu.setVisible(false);
	}

	/*
	private void createQualityMenu() {
		qualityMenu = new VLayout();
		qualityMenu.setLeft(qualityButton.getAbsoluteLeft());
		qualityMenu.setTop(40);
		qualityMenu.setStyleName("n52_sensorweb_client_interactionmenu");
		qualityMenu.setAutoHeight();
		qualityMenu.setZIndex(1000000);
		qualityMenu.addMember(createQualityFlagLabelButton());
		qualityMenu.addMember(createSaveHistoryLabelButton());
		qualityMenu.addMember(createOpenHistoryLabelButton());
		qualityMenu.setVisible(false);
	}*/

	private void createTimeSeriesMenu() {
		timeSeriesMenu = new VLayout();
		timeSeriesMenu.setLeft(timeSeriesButton.getAbsoluteLeft());
		timeSeriesMenu.setTop(40);
		timeSeriesMenu.setStyleName("n52_sensorweb_client_interactionmenu");
		timeSeriesMenu.setAutoHeight();
		timeSeriesMenu.setZIndex(1000000);
		timeSeriesMenu.addMember(createAddTimeSeriesLabelButton());
		timeSeriesMenu.addMember(deletAllTimeSeriesLabelButton());
		timeSeriesMenu.addMember(resetSeriesColorsButton());
		timeSeriesMenu.addMember(createSaveHistoryLabelButton());
		timeSeriesMenu.addMember(createOpenHistoryLabelButton());

		timeSeriesMenu.setVisible(false);
	}

	// ASD
	private Label deletAllTimeSeriesLabelButton() {
		Label delTS = new Label("Delete All Series");
		delTS.setWrap(false);
		delTS.setAutoFit(true);
		delTS.setPadding(3);
		delTS.setWidth100();
		delTS.setStyleName("n52_sensorweb_client_exportEntry");
		delTS.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				timeSeriesMenu.hide();
				DataStoreTimeSeriesImpl dataManager = DataStoreTimeSeriesImpl.getInst();

				TimeSeries[] ts = dataManager.getTimeSeriesSorted();
				for (int i = 0; i < ts.length; i++) {
					// String id = ts[i].getId();
					TimeSeries series = dataManager.getDataItem(ts[i].getId());
					LegendEntryTimeSeries le = (LegendEntryTimeSeries) series.getLegendElement();
					le.getEventBroker().unregister();
				}
				SOSRequestManager.getInstance().setChangeStyleActive(false);
				EventBus.getMainEventBus().fireEvent(new DeleteAllTimeSeriesEvent());
			}
		});
		return delTS;
	}

	// ASD
	private Label resetSeriesColorsButton() {
		Label resetTS = new Label("Reset Time Series Colors");
		resetTS.setWrap(false);
		resetTS.setAutoFit(true);
		resetTS.setPadding(3);
		resetTS.setWidth100();
		resetTS.setStyleName("n52_sensorweb_client_exportEntry");
		resetTS.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				timeSeriesMenu.hide();
				SOSRequestManager.getInstance().setResetColors(true);
				SOSRequestManager.getInstance().setChangeStyleActive(false);
				EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
			}
		});
		return resetTS;
	}

	private Label createAddTimeSeriesLabelButton() {
		Label addTS = new Label("Add Time Series");
		addTS.setWrap(false);
		addTS.setAutoFit(true);
		addTS.setPadding(3);
		addTS.setWidth100();
		addTS.setStyleName("n52_sensorweb_client_exportEntry");
		addTS.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				StationSelector.getInst().show();
				timeSeriesMenu.hide();
			}
		});
		return addTS;
	}

	private Label createEESTabLabelButton() {
		Label eesTabLabelButton = new Label(i18n.diagram());
		eesTabLabelButton.setStyleName("n52_sensorweb_client_legendbutton");
		eesTabLabelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				switchToSESButton();
			}
		});
		eesTabLabelButton.setVisible(false);
		return eesTabLabelButton;
	}

	private Label createSESTabLabelButton() {
		Label sesTabLabelButton = new Label(i18n.sesTabButton());
		sesTabLabelButton.setStyleName("n52_sensorweb_client_legendbutton");
		sesTabLabelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				switchToEESButton();
			}
		});
		sesTabLabelButton.setVisible(true);
		return sesTabLabelButton;
	}

	private void switchToEESButton() {
		controller.switchToSESTab();
		eesTabButton.setVisible(true);
		sesTabButton.setVisible(false);
	}

	private void switchToSESButton() {
		controller.switchToEESTab();
		eesTabButton.setVisible(false);
		sesTabButton.setVisible(true);
	}

	public VStack getLegendStack() {
		return this.legend;
	}

	public LegendElement[] getEntries() {
		LegendElement[] elements = new LegendElement[legendEntries.size()];
		return legendEntries.toArray(elements);
	}

	public void fill(ArrayList<LegendElement> elementsToFillIn) {

		// remove old elements
		Iterator<LegendElement> iterator = legendEntries.iterator();
		while (iterator.hasNext()) {
			LegendElement currentElement = iterator.next();
			if (!elementsToFillIn.contains(currentElement)) {
				iterator.remove(); // remove current
			}
		}

		// add new elements
		for (LegendElement legendElement : elementsToFillIn) {
			if (!legendEntries.contains(legendElement)) {
				addLegendElement(legendElement);
				if (!legendElement.equals(selectedElement)) {
					legendElement.hideFooter();
				}
			}
		}

		// TODO old code! .. remove if legend behaves correct
		// List<LegendElement> itemsToDelete = new ArrayList<LegendElement>();
		// List<LegendElement> itemsToAdd = new ArrayList<LegendElement>();
		// try {
		// // search deletions
		// for (LegendElement currentElement : legendEntries) {
		// boolean found = false;
		// for (LegendElement newElement : elementsToFillIn) {
		// if (newElement.getElemId().equals(currentElement.getElemId())) {
		// found = true;
		// }
		// }
		// if ( !found) {
		// itemsToDelete.add(currentElement);
		// }
		// }
		//
		// // search additions
		// for (LegendElement newElement : elementsToFillIn) {
		// boolean found = false;
		// for (LegendElement legendElement : this.legendEntries) {
		// if (legendElement.getElemId() != null &&
		// legendElement.equals(newElement)) {
		// found = true;
		// }
		// }
		// if ( !found) {
		// itemsToAdd.add(newElement);
		// }
		// }
		//
		// // del items
		// for (LegendElement oldElement : itemsToDelete) {
		// legendEntries.remove(oldElement);
		// }
		//
		// // add items
		// for (LegendElement newElement : itemsToAdd) {
		// addLegendElement(newElement);
		// if (selectedElement != null && !selectedElement.equals(newElement)) {
		// newElement.hideFooter();
		// }
		// }
		// }
		// catch (Exception e) {
		// GWT.log("Error filling legend entries", e);
		// }

		reorderAlong(elementsToFillIn);
	}

	/**
	 * reorders legend entries along the order given by the passed
	 * <code>elements</code>
	 */
	private void reorderAlong(ArrayList<LegendElement> elements) {
		if (legend.getMembers().length > 0) {
			legend.removeMembers(legend.getMembers());
			legendEntries.clear();
			for (int i = 0; i < elements.size(); i++) {
				legend.addMember(elements.get(i).getLayout());
				legendEntries.add(i, elements.get(i));
			}
		}
	}

	public String getId() {
		return this.elemID;
	}

	void setSelectedElement(LegendElement element) {
		this.selectedElement = element;
	}

	public void stopExportLoadingSpinner() {
		exportLoadingSpinner.hide();
		exportButton.show();
	}

	public void setExportButtonActiv(boolean activ) {
		if (activ) {
			exportButton.setDisabled(false);
			exportButton.setStyleName("n52_sensorweb_client_legendbutton");
		} else {
			exportButton.setDisabled(true);
			exportButton.setStyleName("n52_sensorweb_client_legendbuttonDisabled");
		}
	}

	public void setQualityButtonActiv(boolean activ) {
		if (activ) {
			qualityButton.setDisabled(false);
			qualityButton.setStyleName("n52_sensorweb_client_legendbutton");
		} else {
			qualityButton.setDisabled(true);
			qualityButton.setStyleName("n52_sensorweb_client_legendbuttonDisabled");
		}
	}

	public void startExportLoadingSpinner() {
		exportLoadingSpinner.show();
		exportButton.hide();
	}

}
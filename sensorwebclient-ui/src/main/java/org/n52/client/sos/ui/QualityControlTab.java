package org.n52.client.sos.ui;

import org.eesgmbh.gimv.client.event.LoadImageDataEvent;
import org.n52.client.bus.EventBus;
import org.n52.client.sos.ctrl.QualityFlaggingController;
import org.n52.client.sos.event.data.ClearSelectedPointEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent;
import org.n52.client.sos.event.data.SetQualityControlStatusEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent.FlaggingPointType;
import org.n52.client.ui.Maintenance;
import org.n52.client.ui.MultiPointFlagger;
import org.n52.client.ui.QualityFlagger;
import org.n52.client.ui.QualityFlaggerByView;

import com.google.gwt.core.shared.GWT;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;
import com.smartgwt.client.widgets.Window;

//ASD

public class QualityControlTab extends Window {
	private static QualityControlTab instance;
	private static int WIDTH = 600;
	private static int HEIGHT = 650;
	private static final String COMPONENT_ID = "qcTabWindow";
	final TabSet tabSet;
	private int tabSelected = 0;

	public static QualityControlTab getInst() {
		if (instance == null) {
			instance = new QualityControlTab();
		}
		return instance;
	}

	private QualityControlTab() {
		// ASD
		tabSet = new TabSet();
		tabSet.addTabSelectedHandler(new TabSelectedHandler() {
			@Override
			public void onTabSelected(TabSelectedEvent event) {
				// TODO Auto-generated method stub
				// 0 - point, 1-range, 2-custom window, 3-view, 4- customize
				// shapes
				int tabSelect = event.getTabNum();
				QualityControlTab.this.tabSelected = tabSelect;
				/*
				 * if(tabSelect==0) { SetQCTimeRangeEvent setTimeRange = new
				 * SetQCTimeRangeEvent(FlaggingPointType.TIMEPOINT);
				 * EventBus.getMainEventBus().fireEvent(setTimeRange);
				 * 
				 * QualityFlagger.getInst().clearReponseTextItem();
				 * QualityFlagger.getInst().getFormRange().clearValues();
				 * 
				 * EventBus.getMainEventBus().fireEvent(new
				 * ClearSelectedPointEvent());
				 * EventBus.getMainEventBus().fireEvent(new
				 * LoadImageDataEvent()); }
				 */
				if (tabSelect == 0) // multipoint
				{
					SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.MULTIPOINT);
					EventBus.getMainEventBus().fireEvent(setTimeRange);
					EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent()); // clear
																							// rendered
																							// point
																							// in
																							// QFlaggingCtlr
					QualityFlagger.getInst().clearLocalTabValues();
					QualityFlaggerByView.getInst().clearLocalTabValues();
					EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
				}
				else if (tabSelect == 1) // range
				{
					SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.TIMERANGE);
					EventBus.getMainEventBus().fireEvent(setTimeRange);
					EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
					MultiPointFlagger.getInst().clearLocalTabValues();
					QualityFlaggerByView.getInst().clearLocalTabValues();
					EESTab.showLoadingSpinner();
					EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
				}
				else if (tabSelect == 2) // view
				{
					SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.VIEW);
					EventBus.getMainEventBus().fireEvent(setTimeRange);
					EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
					MultiPointFlagger.getInst().clearLocalTabValues();
					QualityFlagger.getInst().clearLocalTabValues();
					EESTab.showLoadingSpinner();
					EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
				}
				else if (tabSelect == 3) {
					SetQCTimeRangeEvent setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.RENDER);
					EventBus.getMainEventBus().fireEvent(setTimeRange);
					EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
					MultiPointFlagger.getInst().clearLocalTabValues();
					QualityFlagger.getInst().clearLocalTabValues();
					QualityFlaggerByView.getInst().clearLocalTabValues();
					EESTab.showLoadingSpinner();
					EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
				}
			}

		});

		initializeWindow();
		initializeContent();

		addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClickEvent event) {
				closeQCTabWindow();
			}
		});
	}

	private void initializeWindow() {
		setShowModalMask(true);
		// ASD
		setID(COMPONENT_ID);
		setTitle("Flag Time Series");
		setWidth(WIDTH);
		setHeight(HEIGHT);
		setCanDragResize(true);
		setShowMaximizeButton(true);
		setShowMinimizeButton(false);
		setMargin(0);
		centerInPage();
		addResizedHandler(new ResizedHandler() {
			@Override
			public void onResized(ResizedEvent event) {
				WIDTH = QualityControlTab.this.getWidth();
				HEIGHT = QualityControlTab.this.getHeight();
			}
		});

	}

	private void initializeContent() {

		// tabSet.addTab(QualityFlaggerByPoint.getInst());
		tabSet.addTab(MultiPointFlagger.getInst());
		tabSet.addTab(QualityFlagger.getInst());
		tabSet.addTab(QualityFlaggerByView.getInst());
		tabSet.addTab(FilteringTab.getInst());
		tabSet.setSelectedTab(2); // by default : view-based
		addItem(tabSet);
	}

	public void closeQCTabWindow() {
		hide();
		// 13.08.2014
		if (!QualityFlaggingController.getInstance().isAddFlagRunning()) {
			QualityFlagger.getInst().clearLocalTabValues();
			QualityFlaggerByView.getInst().clearLocalTabValues();
			MultiPointFlagger.getInst().clearLocalTabValues();
			FilteringTab.getInst().clearFilteringTab();
			EventBus.getMainEventBus().fireEvent(new SetQualityControlStatusEvent(false));
			EventBus.getMainEventBus().fireEvent(new ClearSelectedPointEvent());
		}
		// EESTab.showLoadingSpinner();
		// EventBus.getMainEventBus().fireEvent(new LoadImageDataEvent());
	}

	/*
	 * private Label createFlagByPointLabel() { Label byPoint = new
	 * Label(i18n.byPoint()); byPoint.setWrap(false); byPoint.setAutoFit(true);
	 * byPoint.setPadding(3); byPoint.setWidth100();
	 * byPoint.setStyleName("n52_sensorweb_client_exportEntry");
	 * byPoint.addClickHandler(new ClickHandler() {
	 * 
	 * @Override public void onClick(ClickEvent event) { SetQCTimeRangeEvent
	 * setTimeRange = new SetQCTimeRangeEvent(FlaggingPointType.TIMEPOINT);
	 * EventBus.getMainEventBus().fireEvent(setTimeRange);
	 * QualityFlaggerByPoint.getInst().show();
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetQualityControlStatusEvent(true)); } }); return byPoint; }
	 */
	/*
	 * private Label createFlagByRangeLabel() { Label byRange = new
	 * Label(i18n.byRange()); byRange.setWrap(false); byRange.setAutoFit(true);
	 * byRange.setPadding(3); byRange.setWidth100();
	 * byRange.setStyleName("n52_sensorweb_client_exportEntry");
	 * byRange.addClickHandler(new ClickHandler() {
	 * 
	 * @Override public void onClick(ClickEvent event) {
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetQualityControlStatusEvent(true)); QualityFlagger.getInst().show(); }
	 * }); return byRange; }
	 */
	/*
	 * private Label createFlagByView() { Label byView = new
	 * Label(i18n.byView()); byView.setWrap(false); byView.setAutoFit(true);
	 * byView.setPadding(3); byView.setWidth100();
	 * byView.setStyleName("n52_sensorweb_client_exportEntry");
	 * byView.addClickHandler(new ClickHandler() { public void
	 * onClick(ClickEvent event) { SetQCTimeRangeEvent setTimeRange = new
	 * SetQCTimeRangeEvent(FlaggingPointType.VIEW);
	 * EventBus.getMainEventBus().fireEvent(setTimeRange);
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetQualityControlStatusEvent(true));
	 * QualityFlaggerByView.getInst().show(); } }); return byView; }
	 */

	/*
	 * private Label createFlagByRule() { Label byRule = new
	 * Label(i18n.byRule()); byRule.setWrap(false); byRule.setAutoFit(true);
	 * byRule.setPadding(3); byRule.setWidth100();
	 * byRule.setStyleName("n52_sensorweb_client_exportEntry");
	 * byRule.addClickHandler(new ClickHandler() { public void
	 * onClick(ClickEvent event) { SetQCTimeRangeEvent setTimeRange = new
	 * SetQCTimeRangeEvent(FlaggingPointType.RULE);
	 * EventBus.getMainEventBus().fireEvent(setTimeRange);
	 * EventBus.getMainEventBus().fireEvent(new
	 * SetQualityControlStatusEvent(true));
	 * QualityFlaggerByView.getInst().show(); } }); return byRule; }
	 */

	public TabSet getTabSet() {
		return tabSet;
	}

	public int getTabSelected() {
		return tabSelected;
	}

	public void setTabSelected(int tabSelected) {
		this.tabSelected = tabSelected;
	}

}

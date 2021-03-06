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

package org.n52.client.sos.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import org.eesgmbh.gimv.client.controls.KeystrokeControl;
import org.eesgmbh.gimv.client.event.SetDomainBoundsEvent;
import org.eesgmbh.gimv.client.event.SetDomainBoundsEventHandler;
import org.eesgmbh.gimv.client.event.SetViewportPixelBoundsEvent;
import org.eesgmbh.gimv.client.event.StateChangeEvent;
import org.eesgmbh.gimv.client.presenter.ImagePresenter;
import org.eesgmbh.gimv.client.presenter.TooltipPresenter;
import org.eesgmbh.gimv.client.view.GenericWidgetView;
import org.eesgmbh.gimv.client.view.GenericWidgetViewImpl;
import org.eesgmbh.gimv.client.view.ImageViewImpl;
import org.eesgmbh.gimv.client.widgets.Viewport;
import org.eesgmbh.gimv.shared.util.Bound;
import org.eesgmbh.gimv.shared.util.Bounds;
import org.eesgmbh.gimv.shared.util.Direction;
import org.n52.client.Application;
import org.n52.client.bus.EventBus;
import org.n52.client.ctrl.DataControls;
import org.n52.client.ctrl.ExceptionHandler;
import org.n52.client.ctrl.TimeManager;
import org.n52.client.sos.ctrl.DataManagerSosImpl;
import org.n52.client.sos.ctrl.EESTabController;
import org.n52.client.sos.ctrl.QualityFlaggingController;
import org.n52.client.sos.data.DataStoreTimeSeriesImpl;
import org.n52.client.sos.event.DatesChangedEvent;
import org.n52.client.sos.event.data.FilterXYCoordEvent;
import org.n52.client.sos.event.data.SetQCTimeRangeEvent.FlaggingPointType;
import org.n52.client.ui.DataPanelTab;
import org.n52.shared.Constants;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DragAppearance;
import com.smartgwt.client.types.FormErrorOrientation;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VStack;

public class EESTab extends DataPanelTab {

	private static EESTabController controller;

	/*
	 * TODO: monitor impact of setting this public
	 */
	public static Layout layout;

	private static Viewport mainChartViewport;

	private HTML verticalMousePointerLine;

	private static Viewport overviewChartViewport;

	private EventBus overviewEventBus = EventBus.getOverviewChartEventBus();

	private EventBus mainChartEventBus = EventBus.getMainEventBus();

	protected TooltipPresenter tooltipPresenter;

	private HorizontalPanel horizontalSlider; // TODO use a flowpanel instead

	private int lastSliderPosition;

	private HTML leftHandleWidget;

	private HTML rightHandleWidget;

	private HTML mainHandleWidget;

	private static Img mainChartLoadingSpinner;

	// ASD
	private static Img overviewChartLoadingSpinner;
	private static OverviewPresenter overviewPresenter;
	private DynamicForm formMini;
	private static Window flagWindow = null;
	private SelectItem flagSpecificSelect;
	private SelectItem flagSelect;
	private Map<String, ArrayList<String>> codeList = null;
	private int offLineXCoord;
	private int offLineYCoord;
	private static ImgButton rightInteractionMenuButton;
	private static ImgButton leftInteractionMenuButton;
	private static ImgButton centerInteractionMenuButton;

	public EESTab(String ID, String title) {
		super("DiagramTab");
		layout = new Layout();

		MousePointerDomainBoundsHandler listener = new MousePointerDomainBoundsHandler();
		this.mainChartEventBus.addHandler(SetDomainBoundsEvent.TYPE, listener);

		controller = new EESTabController(this);

		setID(ID);
		setTitle(title);
		setIcon("../img/icons/chart_curve.png");
	}

	public static int getPanelHeight() {
		// - 100 overview height - 5 margin correction
		// ASD height is adjusted due to the legend of flags
		// int height = layout.getParentElement().getHeight() - 100 - 5;
		int height = layout.getParentElement().getHeight() - 110 - 5;
		if (Application.isHasStarted() && controller.getControls().isVisible()) {
			height -= controller.getControls().getHeight();
		}
		return height;
	}

	public static int getPanelWidth() {
		int width = layout.getParentElement().getWidth() - 5;
		return width;
	}

	public void init() {
		try {
			setPane(layout);

			EESTab.layout.setVertical(true);
			// ASD
			EESTab.mainChartViewport = getMainChartViewport();
			this.overviewChartViewport = getOverviewChartViewport();

			rightInteractionMenuButton = new ImgButton();
			rightInteractionMenuButton.setTooltip("View data series for the next time period.");
			rightInteractionMenuButton.setWidth(18);
			rightInteractionMenuButton.setHeight(18);
			rightInteractionMenuButton.setSrc("../img/icons/right.png");
			rightInteractionMenuButton.setDisabled(true);
			rightInteractionMenuButton.setShowDown(false);
			rightInteractionMenuButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					long begin = TimeManager.getInst().getBegin();
					long end = TimeManager.getInst().getEnd();
					long inter = end - begin;
					long newEnd = end + inter;
					rightInteractionMenuButton.setDisabled(true);
					leftInteractionMenuButton.setDisabled(true);
					centerInteractionMenuButton.setDisabled(true);
					TimeManager.getInst().setIsOverviewTimeFixed(false);

					EventBus.getMainEventBus().fireEvent(new DatesChangedEvent(end, newEnd));
				}
			});

			leftInteractionMenuButton = new ImgButton();
			leftInteractionMenuButton.setWidth(18);
			leftInteractionMenuButton.setHeight(18);
			leftInteractionMenuButton.setTooltip("View data series for the previous time period.");
			leftInteractionMenuButton.setSrc("../img/icons/left.png");
			leftInteractionMenuButton.setDisabled(true);
			leftInteractionMenuButton.setShowDown(false);
			leftInteractionMenuButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					long begin = TimeManager.getInst().getBegin();
					long end = TimeManager.getInst().getEnd();
					long inter = end - begin;
					long newStart = begin - inter;
					rightInteractionMenuButton.setDisabled(true);
					leftInteractionMenuButton.setDisabled(true);
					centerInteractionMenuButton.setDisabled(true);
					TimeManager.getInst().setIsOverviewTimeFixed(false);
					EventBus.getMainEventBus().fireEvent(new DatesChangedEvent(newStart, begin));
				}
			});

			centerInteractionMenuButton = new ImgButton();
			centerInteractionMenuButton.setWidth(18);
			centerInteractionMenuButton.setHeight(18);
			centerInteractionMenuButton.setTooltip("View data series for current time period.");
			centerInteractionMenuButton.setSrc("../img/icons/center.png");
			centerInteractionMenuButton.setDisabled(true);
			centerInteractionMenuButton.setShowDown(false);
			centerInteractionMenuButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					long begin = TimeManager.getInst().getBegin();
					long end = TimeManager.getInst().getEnd();
					long interval = end - begin;
					Date today = new Date();
					long todayDate = today.getTime();
					long newStart = todayDate - interval;
					centerInteractionMenuButton.setDisabled(true);
					rightInteractionMenuButton.setDisabled(true);
					leftInteractionMenuButton.setDisabled(true);
					TimeManager.getInst().setIsOverviewTimeFixed(false);
					EventBus.getMainEventBus().fireEvent(new DatesChangedEvent(newStart, todayDate));
				}
			});

			Label space = new Label();
			space.setWidth("20%");
			Label space1 = new Label();
			space1.setWidth("4");

			HLayout buttonsLayout = new HLayout();
			buttonsLayout.setMembersMargin(3);
			buttonsLayout.setAlign(VerticalAlignment.BOTTOM);
			buttonsLayout.addMember(space);
			buttonsLayout.addMember(leftInteractionMenuButton);
			buttonsLayout.addMember(centerInteractionMenuButton);
			buttonsLayout.addMember(rightInteractionMenuButton);
			buttonsLayout.addMember(space);

			EESTab.layout.addMember(EESTab.mainChartViewport);
			EESTab.layout.addMember(buttonsLayout);
			EESTab.layout.addMember(this.overviewChartViewport);
			initKeyControls();
			initZooming();
			initTooltips();

			EESTab.mainChartLoadingSpinner = new Img("../img/loader.gif");
			EESTab.mainChartLoadingSpinner.setWidth(40);
			EESTab.mainChartLoadingSpinner.setHeight(40);
			EESTab.mainChartLoadingSpinner.setLeft(getPanelWidth() / 2);
			EESTab.mainChartLoadingSpinner.setTop(getPanelHeight() / 2);
			EESTab.mainChartLoadingSpinner.hide();
			EESTab.mainChartViewport.add(EESTab.mainChartLoadingSpinner);

			// ASD
			EESTab.overviewChartLoadingSpinner = new Img("../img/loader.gif");
			EESTab.overviewChartLoadingSpinner.setWidth(40);
			EESTab.overviewChartLoadingSpinner.setHeight(40);
			EESTab.overviewChartLoadingSpinner.setLeft(getPanelWidth() - 30);
			EESTab.overviewChartLoadingSpinner.setTop(getPanelHeight() / 2);
			EESTab.overviewChartLoadingSpinner.hide();
			EESTab.overviewChartViewport.add(EESTab.overviewChartLoadingSpinner);

			EESTab.mainChartViewport.setHandlerManager(this.mainChartEventBus);
			EESTab.overviewChartViewport.setHandlerManager(this.overviewEventBus);
			this.overviewEventBus.fireEvent(StateChangeEvent.createMove());
			// ASD 26.05.2014
			// this.mainChartEventBus.fireEvent(StateChangeEvent.createMove());
			// this.mainChartEventBus.fireEvent(StateChangeEvent.createZoom());

			int mainOffsetWidth = EESTab.mainChartViewport.getOffsetWidth();
			int mainOffsetHeight = EESTab.mainChartViewport.getOffsetHeight();
			Bounds mainBounds = new Bounds(mainOffsetWidth, 0, mainOffsetHeight, 0);
			int overviewOffsetWidth = this.overviewChartViewport.getOffsetWidth();
			int overviewOffsetHeight = this.overviewChartViewport.getOffsetHeight();
			Bounds overviewBounds = new Bounds(overviewOffsetWidth, 0, overviewOffsetHeight, 0);
			this.mainChartEventBus.fireEvent(new SetViewportPixelBoundsEvent(mainBounds));
			this.overviewEventBus.fireEvent(new SetViewportPixelBoundsEvent(overviewBounds));

		} catch (Exception e) {
			ExceptionHandler.handleUnexpectedException(e);
		}
	}

	private Viewport getMainChartViewport() {

		Image mainChartImage = new Image("img/blank.gif");
		Viewport mainchart = new Viewport("100%", "100%");
		// ASD
		// mainchart.setEnableZoomWhenShiftkeyPressed(true);
		mainchart.setEnableZoomWhenShiftkeyPressed(false);
		// ASD
		mainChartImage.addClickHandler(new com.google.gwt.event.dom.client.ClickHandler() {
			public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
				if (QualityFlaggingController.getInstance().getActiveTimeField().equals(FlaggingPointType.MULTIPOINT.toString())) {
					offLineXCoord = event.getX();
					offLineYCoord = event.getY();
					//10.11.2014 ASD
					if (QualityFlaggingController.getInstance().isQualityControlSelected())
					{
						createFlagsWindow();
					}
				} else {
					EventBus.getMainEventBus().fireEvent(new FilterXYCoordEvent(event.getX(), event.getY()));
				}
			}
		});
		mainchart.add(mainChartImage);

		// as it is focusable, we do not want to see an outline
		DOM.setStyleAttribute(mainchart.getElement(), "outline", "none");
		DOM.setStyleAttribute(mainchart.getElement(), "overflow", "visible");
		ImageViewImpl imageView = new ImageViewImpl(mainChartImage);

		new ImagePresenter(this.mainChartEventBus, imageView);
		// ASD 26.05.2014 comment out
		// new DragImageControl(this.mainChartEventBus);
		// new MouseWheelControl(this.mainChartEventBus);

		return mainchart;
	}

	private Viewport getOverviewChartViewport() {
		Image overviewChartImage = new Image("img/blank.gif");
		Viewport overview = new Viewport("100%", "100px");
		overview.add(overviewChartImage);

		DOM.setStyleAttribute(overview.getElement(), "outline", "none");
		this.horizontalSlider = createOverviewSlider();
		overview.add(this.horizontalSlider);

		ImageViewImpl imageView = new ImageViewImpl(overviewChartImage);
		new ImagePresenter(this.overviewEventBus, imageView);
		return overview;
	}

	/**
	 * Creates the Slider the user can interact with to change the shown time
	 * intervals of the given timeseries'.
	 * 
	 * @return the TimeSlider as a whole
	 */
	private HorizontalPanel createOverviewSlider() {
		HorizontalPanel horizontalSlider = new HorizontalPanel();
		DOM.setStyleAttribute(horizontalSlider.getElement(), "marginTop", "6px");
		horizontalSlider.setHeight("75px");
		// ASD
		// this.leftHandleWidget = buildSliderPart("8px", "75px", "w-resize",
		// "#6585d0", 0.5);
		// this.rightHandleWidget = buildSliderPart("8px", "75px", "e-resize",
		// "#6585d0", 0.5);
		this.leftHandleWidget = buildSliderPart("8px", "75px", "w-resize", "#6585d0", 0.5);
		this.rightHandleWidget = buildSliderPart("8px", "75px", "e-resize", "#6585d0", 0.5);
		this.mainHandleWidget = buildSliderPart("100%", "75px", "move", "#aaa", 0.5);

		horizontalSlider.add(this.leftHandleWidget);
		horizontalSlider.setCellWidth(this.leftHandleWidget, "10px");
		horizontalSlider.add(this.mainHandleWidget);
		horizontalSlider.setCellWidth(this.mainHandleWidget, "100%");
		horizontalSlider.add(this.rightHandleWidget);
		horizontalSlider.setCellWidth(this.rightHandleWidget, "10px");
		DOM.setStyleAttribute(horizontalSlider.getElement(), "visibility", "hidden");

		GenericWidgetViewImpl view = new GenericWidgetViewImpl(horizontalSlider);
		overviewPresenter = new OverviewPresenter(view, this.overviewEventBus, this.mainChartEventBus);

		// Define handles for overview control
		GenericWidgetView leftHandle = new GenericWidgetViewImpl(this.leftHandleWidget);
		GenericWidgetView mainHandle = new GenericWidgetViewImpl(this.mainHandleWidget);
		GenericWidgetView rightHandle = new GenericWidgetViewImpl(this.rightHandleWidget);

		overviewPresenter.addHandle(leftHandle, Bound.LEFT);
		overviewPresenter.addHandle(mainHandle, Bound.RIGHT, Bound.LEFT);
		overviewPresenter.addHandle(rightHandle, Bound.RIGHT);
		// ASD overviewPresenter.setMinClippingWidth(40);
		overviewPresenter.setMinClippingWidth(10); // min width
		// it's a horizontal slider, dont' let it move vertically
		overviewPresenter.setVerticallyLocked(true); // drag horizontally only

		return horizontalSlider;
	}

	private HTML buildSliderPart(String width, String height, String cursor, String color, double transparancy) {
		HTML container = new HTML();
		container.setWidth(width);
		container.setHeight(height);
		DOM.setStyleAttribute(container.getElement(), "cursor", cursor);
		DOM.setStyleAttribute(container.getElement(), "backgroundColor", color);

		// transparency styling (see also bug#449 and
		// http://www.quirksmode.org/css/opacity.html)
		// note: since GWT complains, '-msFilter' has to be in plain camelCase
		// (w/o '-')
		// ordering is important here
		DOM.setStyleAttribute(container.getElement(), "opacity", Double.toString(transparancy));
		DOM.setStyleAttribute(container.getElement(), "mozOpacity", Double.toString(transparancy));
		String opacity = "(opacity=" + Double.toString(transparancy * 100) + ")";
		DOM.setStyleAttribute(container.getElement(), "msFilter", "\"progid:DXImageTransform.Microsoft.Alpha" + opacity + "\"");
		DOM.setStyleAttribute(container.getElement(), "filter", "alpha" + opacity);
		return container;
	}

	private void initKeyControls() {
		KeystrokeControl kCtrl = new KeystrokeControl(this.mainChartEventBus);
		kCtrl.addTargetElement(EESTab.mainChartViewport.getElement());
		kCtrl.addTargetElement(this.overviewChartViewport.getElement());
		kCtrl.addDocumentAndBodyAsTarget();

		// 10px offset each
		kCtrl.registerKey(KeyCodes.KEY_LEFT, Direction.EAST, 10);
		kCtrl.registerKey(KeyCodes.KEY_UP, Direction.SOUTH, 10);
		kCtrl.registerKey(KeyCodes.KEY_RIGHT, Direction.WEST, 10);
		kCtrl.registerKey(KeyCodes.KEY_DOWN, Direction.NORTH, 10);

		// 30px offset if ctrl is pressed
		kCtrl.registerKey(KeyCodes.KEY_LEFT, true, false, false, false, Direction.EAST, 30);
		kCtrl.registerKey(KeyCodes.KEY_UP, true, false, false, false, Direction.NORTH, 30);
		kCtrl.registerKey(KeyCodes.KEY_RIGHT, true, false, false, false, Direction.WEST, 30);
		kCtrl.registerKey(KeyCodes.KEY_DOWN, true, false, false, false, Direction.SOUTH, 30);
	}

	private void initZooming() {
		HTML zoomBox = new HTML();
		DOM.setStyleAttribute(zoomBox.getElement(), "opacity", "0.15");
		DOM.setStyleAttribute(zoomBox.getElement(), "mozOpacity", "0.15");
		DOM.setStyleAttribute(zoomBox.getElement(), "msFilter", "\"progid:DXImageTransform.Microsoft.Alpha(Opacity=15)\"");
		DOM.setStyleAttribute(zoomBox.getElement(), "filter", "alpha(opacity=15)");

		DOM.setStyleAttribute(zoomBox.getElement(), "outline", "black dashed 1px");
		DOM.setStyleAttribute(zoomBox.getElement(), "backgroundColor", "blue");
		DOM.setStyleAttribute(zoomBox.getElement(), "visibility", "hidden");
		EESTab.mainChartViewport.add(zoomBox);

		GenericWidgetView zoomBoxView = new GenericWidgetViewImpl(zoomBox);
		new ZoomBoxPresenter(this.mainChartEventBus, zoomBoxView);
	}

	private void initTooltips() {

		// ASD

		/*
		Element mousePointerElement = getMousePointerLineElement();
		DOM.setStyleAttribute(mousePointerElement, "backgroundColor", "blue");
		DOM.setStyleAttribute(mousePointerElement, "width", "0px");
		DOM.setStyleAttribute(mousePointerElement, "height", "0px");
		DOM.setStyleAttribute(mousePointerElement, "visibility", "hidden");
		DOM.setStyleAttribute(mousePointerElement, "marginTop", "6px");
		this.mainChartViewport.add(this.verticalMousePointerLine);*/

		this.tooltipPresenter = new TooltipPresenter(this.mainChartEventBus);
		//ASD 13.09.2014
		this.tooltipPresenter.configureHoverMatch(true, false, false);
		this.tooltipPresenter.setTooltipZIndex(Constants.Z_INDEX_ON_TOP);

		/*
		GenericWidgetViewImpl widget = new GenericWidgetViewImpl(this.verticalMousePointerLine);
		MousePointerPresenter mpp = new MousePointerPresenter(this.mainChartEventBus, widget);
		mpp.configure(true, false); */

	}

	public void setVisibleSlider(boolean isVisible) {
		if (this.horizontalSlider != null) {
			this.horizontalSlider.setVisible(isVisible);
		}
	}

	public void addSlider() {
		if (this.horizontalSlider == null && this.overviewChartViewport != null) {
			this.horizontalSlider = createOverviewSlider();
			this.overviewChartViewport.add(this.horizontalSlider, this.lastSliderPosition, 0);
		}
	}

	public void removeSlider() {
		if (this.horizontalSlider != null) {
			this.lastSliderPosition = this.overviewChartViewport.getWidgetLeft(this.horizontalSlider);
			// int left = this.leftHandleWidget.getAbsoluteLeft() +
			// this.leftHandleWidget.getOffsetWidth();
			// int right = this.rightHandleWidget.getAbsoluteLeft();
			// this.lastMainHandleWidth = right - left;
			this.overviewChartViewport.remove(this.horizontalSlider);
			this.horizontalSlider = null;
		}
	}

	protected Element getMousePointerLineElement() {
		if (this.verticalMousePointerLine == null) {
			this.verticalMousePointerLine = new HTML();
		}
		return this.verticalMousePointerLine.getElement();
	}

	@Override
	public DataControls getDataControls() {
		return controller.getControls();
	}

	public void redraw() {
		layout.markForRedraw();
	}

	public EventBus getOvervieweventBus() {
		return this.overviewEventBus;
	}

	protected class MousePointerDomainBoundsHandler implements SetDomainBoundsEventHandler {

		/*
		 * (non-Javadoc)
		 * 
		 * @seeorg.eesgmbh.gimv.client.event.SetDomainBoundsEventHandler#
		 * onSetDomainBounds(org.eesgmbh.gimv.client.event.SetDomainBoundsEvent)
		 */
		public void onSetDomainBounds(SetDomainBoundsEvent event) {
			if (!DataStoreTimeSeriesImpl.getInst().getDataItems().isEmpty()) {
				String[] widthHeight = getBoundValues(event);

				Element mousePointerElement = EESTab.this.getMousePointerLineElement();
				DOM.setStyleAttribute(mousePointerElement, "width", widthHeight[0]);
				DOM.setStyleAttribute(mousePointerElement, "height", widthHeight[1]);

				setTooltipsOnTop(event);
			}
		}

		/**
		 * @return String array with width as 1st and height as 2nd element.
		 */
		private String[] getBoundValues(SetDomainBoundsEvent event) {
			String absWidth = (isWidthWiderOne(event)) ? "1px" : "0px";
			String absHeight = Double.toString(event.getBounds().getAbsHeight()) + "px";
			return new String[] { absWidth, absHeight };
		}

		private void setTooltipsOnTop(SetDomainBoundsEvent event) {
			if (isWidthWiderOne(event)) {
				EESTab.this.tooltipPresenter.setTooltipZIndex(Constants.Z_INDEX_ON_TOP);
			} else {
				EESTab.this.tooltipPresenter.setTooltipZIndex(0);
			}
		}

		private boolean isWidthWiderOne(SetDomainBoundsEvent event) {
			return event.getBounds().getAbsWidth() > 1;
		}
	}

	/**
     * 
     */
	public void hideTooltips() {
		DOM.setStyleAttribute(EESTab.this.verticalMousePointerLine.getElement(), "width", "0px");
		this.tooltipPresenter.setTooltipZIndex(0);
	}

	class DraggableVStack extends VStack {

		public DraggableVStack() {
			setSize("200", "30");
			setCanDrag(false);
			setBackgroundColor("#000000");
		}

	}

	public static class DragLabel extends Label {
		public DragLabel() {
			setAlign(Alignment.CENTER);
			setPadding(4);
			setShowEdges(true);
			setMinWidth(70);
			setMinHeight(70);
			setMaxWidth(300);
			setMaxHeight(200);
			setKeepInParentRect(true);
			setCanDragReposition(true);
			setDragAppearance(DragAppearance.TARGET);

		}
	}

	// ASD
	public void hideLoadingSpinner() {
		if (mainChartLoadingSpinner != null && mainChartLoadingSpinner.isVisible()) {
			mainChartLoadingSpinner.hide();
		}
		if (overviewChartLoadingSpinner != null && overviewChartLoadingSpinner.isVisible()) {
			overviewChartLoadingSpinner.hide();
		}
		// ASD
		EESTab.getOverviewPresenter().setHorizontallyLocked(false);
	}

	// ASD
	public static void showLoadingSpinner() {
		if (mainChartLoadingSpinner != null && !mainChartLoadingSpinner.isVisible()) {
			mainChartLoadingSpinner.show();
		}
		if (overviewChartLoadingSpinner != null && !overviewChartLoadingSpinner.isVisible()) {
			overviewChartLoadingSpinner.show();
		}
	}

	public static OverviewPresenter getOverviewPresenter() {
		return overviewPresenter;
	}

	public static void setOverviewPresenter(OverviewPresenter overviewPresenter) {
		EESTab.overviewPresenter = overviewPresenter;
	}

	protected void createFlagsWindow() {
		if (flagWindow == null) {
			flagWindow = new Window();
			flagWindow.setHeight(180);
			flagWindow.setWidth(240);
			flagWindow.setTitle("Select Flags");
			flagWindow.setShowMinimizeButton(false);
			flagWindow.setShowModalMask(true);
			flagWindow.centerInPage();
			flagWindow.setIsModal(true);

			formMini = new DynamicForm();
			formMini.setMargin(15);
			formMini.setLayoutAlign(VerticalAlignment.CENTER);
			formMini.setErrorOrientation(FormErrorOrientation.RIGHT);

			codeList = DataManagerSosImpl.getInst().getQualityCodeList();
			Map<String, ArrayList<String>> codeListModified = new HashMap<String, ArrayList<String>>();
			codeListModified.putAll(codeList);
			codeListModified.remove("unevaluated");
			codeList = codeListModified;
			String[] genericFlags = codeList.keySet().toArray(new String[codeList.size()]);
			flagSpecificSelect = new SelectItem("SpecificCode", "Specific Flag");
			flagSpecificSelect.setAlign(Alignment.LEFT);
			flagSpecificSelect.setAddUnknownValues(false);
			flagSpecificSelect.setRequired(true);
			flagSelect = new SelectItem("GenericCode", "Generic Flag");
			flagSelect.setAlign(Alignment.LEFT);
			flagSelect.setRequired(true);
			flagSelect.setValueMap(genericFlags);
			flagSelect.addChangeHandler(new ChangeHandler() {
				public void onChange(ChangeEvent event) {
					String selectedItem = (String) event.getValue();
					String[] specificArray = codeList.get(selectedItem).toArray(new String[codeList.get(selectedItem).size()]);
					formMini.getField("SpecificCode").setValueMap(specificArray);
					if (specificArray.length == 1 && specificArray[0].equalsIgnoreCase(selectedItem)) {
						formMini.getField("SpecificCode").setValue(selectedItem);
						flagSpecificSelect.setDisabled(true);
					} else {
						flagSpecificSelect.setDisabled(false);
					}
				}
			});

			IButton addFlagButton = new IButton("OK");
			addFlagButton.setTooltip("This button only updates data values with unevaluated flags");
			addFlagButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					boolean valid = formMini.validate();
					if (valid) {
						String genericFlag = flagSelect.getValueAsString();
						String specificFlag = flagSpecificSelect.getValueAsString();
						flagWindow.hide();
						EventBus.getMainEventBus().fireEvent(new FilterXYCoordEvent(offLineXCoord, offLineYCoord, genericFlag, specificFlag));
					}
				}
			});

			IButton clearFlagButton = new IButton("Reset");
			clearFlagButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					formMini.clearValues();
				}
			});

			final HLayout buttonLayout = new HLayout();
			buttonLayout.setWidth100();
			buttonLayout.setMembersMargin(5);
			buttonLayout.setAlign(VerticalAlignment.CENTER);
			buttonLayout.addMember(addFlagButton);
			buttonLayout.addMember(clearFlagButton);

			formMini.setFields(flagSelect, flagSpecificSelect);
			flagWindow.addItem(formMini);
			flagWindow.addItem(buttonLayout);
		}
		flagWindow.show();
	}

	public static ImgButton getRightInteractionMenuButton() {
		return rightInteractionMenuButton;
	}

	public static ImgButton getLeftInteractionMenuButton() {
		return leftInteractionMenuButton;
	}

	public static ImgButton getCenterInteractionMenuButton() {
		return centerInteractionMenuButton;
	}

	public static void setCenterInteractionMenuButton(ImgButton centerInteractionMenuButton) {
		EESTab.centerInteractionMenuButton = centerInteractionMenuButton;
	}
}

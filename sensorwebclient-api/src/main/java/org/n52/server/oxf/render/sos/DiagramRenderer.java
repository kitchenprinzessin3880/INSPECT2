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

package org.n52.server.oxf.render.sos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.Log;
import org.n52.oxf.feature.OXFFeature;
import org.n52.oxf.feature.OXFFeatureCollection;
import org.n52.oxf.feature.sos.ObservationSeriesCollection;
import org.n52.oxf.feature.sos.ObservedValueTuple;
import org.n52.oxf.util.JavaHelper;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.server.oxf.render.sos.CustomRenderingProperties.SHAPES;
import org.n52.server.oxf.render.sos.DesignDescriptionList.DesignDescription;
import org.n52.server.oxf.util.ConfigurationContext;
import org.n52.server.oxf.util.generator.EESGenerator;
import org.n52.server.oxf.util.generator.MetaDataInURLGenerator;
import org.n52.server.oxf.util.parser.TimeseriesFactory;
import org.n52.shared.serializable.pojos.Axis;
import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.TimeSeriesProperties;
import org.n52.shared.serializable.pojos.sos.Phenomenon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiagramRenderer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DiagramRenderer.class);

	private static final String DOTTED = "3";

	private static final String AREA = "2";

	private static final String LINE = "1";

	private static final int TICK_FONT_SIZE = 10;

	private static final int LABEL_FONT_SIZE = 12;

	private static final Color LABEL_COLOR = new Color(0, 0, 0);

	private static final String LABEL_FONT = "Arial";

	private final Font label = new Font(LABEL_FONT, Font.BOLD, LABEL_FONT_SIZE);

	private final Font tickLabelDomain = new Font(LABEL_FONT, Font.PLAIN, TICK_FONT_SIZE);

	private HashMap<String, Axis> axisMapping = new HashMap<String, Axis>();

	private boolean isOverview = false;

	private boolean isMapWithFlag = false;

	private EESGenerator eesGenerator;

	private Map<String, HashMap<String, String>> generalSpecificqQualifierMap = null;

	private XYPlot plot;
	
	private int counter = 0;

	Map<String, String> qualityFlagMapping;

	private boolean isExportRequested = false;

	protected static CustomRenderingProperties customRenderingInstance;

	private Map dataLevelsMap;

	private static Map genericFlagSymbolMap;

	private static Map<Long, String> flagMaps = new HashMap<Long, String>();

	private SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

	public DiagramRenderer(boolean isOverview, EESGenerator gen) {
		this.isOverview = isOverview;
		this.eesGenerator = gen;
		this.customRenderingInstance = CustomRenderingProperties.getInst();
		this.dataLevelsMap = customRenderingInstance.getDataProcLevels();
		this.genericFlagSymbolMap = customRenderingInstance.getActiveFlagSymbolPair();
		this.flagMaps = customRenderingInstance.getQualityFlagMappingWithId();
	}

	public DiagramRenderer getDiagramRenderer() {
	    return DiagramRenderer.this;
	}
	
	public DiagramRenderer(boolean isOverview) {
		this.isOverview = isOverview;
	}

	public DiagramRenderer(boolean isOverview, boolean isFileGenerated) {
		this.isOverview = isOverview;
		this.isExportRequested = isFileGenerated;
		this.customRenderingInstance = CustomRenderingProperties.getInst();
	}

	public HashMap<String, Axis> getAxisMapping() {
		return this.axisMapping;
	}

	/**
	 * builds up a DesignDescriptionList object which stores the information
	 * about the style of each timeseries.
	 * 
	 * @param options
	 *            the options
	 * @return the design description list
	 */
	private DesignDescriptionList buildUpDesignDescriptionList(DesignOptions options) {

		String domainAxisLabel;
		if (options.getLanguage() != null && options.getLanguage().equals("de")) {
			domainAxisLabel = "Zeit";
		} else { // default => "en"
			domainAxisLabel = "Time";
		}
		if (this.isOverview) {
			domainAxisLabel = null;
		}

		DesignDescriptionList ddList = new DesignDescriptionList(domainAxisLabel);

		String observedPropertyWithGrid = options.getProperties().get(0).getPhenomenon().getId();

		for (TimeSeriesProperties tsProperties : options.getProperties()) {

			Color c = JavaHelper.transformToColor(tsProperties.getHexColor());
			String phenomenonId = tsProperties.getPhenomenon().getId();
			boolean drawGrid = observedPropertyWithGrid.equals(phenomenonId);

			String procedureId = tsProperties.getProcedure().getId();
			String featureId = tsProperties.getFoi().getId();
			String phenomenonLabel = tsProperties.getPhenomenon().getLabel();
			String procedureLabel = tsProperties.getProcedure().getLabel();
			String featureLabel = tsProperties.getFoi().getLabel();
			
			ddList.add(phenomenonId, procedureId, featureId, phenomenonLabel, procedureLabel, featureLabel, tsProperties.getLabel(), tsProperties.getUnitOfMeasure(), new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) tsProperties.getOpacity() * 255 / 100), tsProperties.getLineStyle(), tsProperties.getLineWidth(), drawGrid);
		}
		return ddList;
	}

	// private static final XYAreaRenderer AREA_RENDERER = new XYAreaRenderer();
	// private static final XYLineAndShapeRenderer LINE_RENDERER = new
	// XYLineAndShapeRenderer(
	// true, false);
	// private static final XYLineAndShapeRenderer DASHED_RENDERER = new
	// XYLineAndShapeRenderer(
	// false, true);

	/**
	 * <pre>
	 * dataset :=  associated to one range-axis;
	 * corresponds to one observedProperty;
	 * may contain multiple series;
	 * series :=   corresponds to a time series for one foi
	 * </pre>
	 * 
	 * .
	 * 
	 * @param entireCollMap
	 *            the entire coll map
	 * @param options
	 *            the options
	 * @param begin
	 *            the begin
	 * @param end
	 *            the end
	 * @param compress
	 * @return the j free chart
	 */
	public JFreeChart renderChart(Map<String, OXFFeatureCollection> entireCollMap, DesignOptions options, Calendar begin, Calendar end, boolean compress) {

		DesignDescriptionList designDescriptions = buildUpDesignDescriptionList(options);

		// ASD - get render coordinates
		this.isMapWithFlag = options.isQCActivated();

		if (!this.isOverview) {
			this.generalSpecificqQualifierMap = new HashMap<String, HashMap<String, String>>();
		}

		/*** FIRST RUN ***/
		JFreeChart chart = initializeTimeSeriesChart();
		chart.setBackgroundPaint(Color.white);

		if (!this.isOverview) {
			chart.addProgressListener(new ChartProgressListener() {
				@Override
				public void chartProgress(final ChartProgressEvent event) {
					if (event.getType() == ChartProgressEvent.DRAWING_FINISHED && !isOverview && !isExportRequested) {
						counter++;
						//System.out.println("chart.addProgressListener.................."+ counter);
						if (counter == 3) {
							//System.out.println("chart.addProgressListener..................FINAL: "+ counter);
							eesGenerator.generateTimeSeriesCoordinates();
						}
					}
				}

			});
			// ASD
			// chart.addSubtitle(new TextTitle(ConfigurationContext.COPYRIGHT,
			// new Font(LABEL_FONT, Font.PLAIN, 9), Color.black,
			// RectangleEdge.BOTTOM, HorizontalAlignment.RIGHT,
			// VerticalAlignment.BOTTOM, new RectangleInsets(0, 0, 20, 20)));
		}

		plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(2.0, 2.0, 2.0, 2.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

		plot.setDomainGridlinesVisible(options.getGrid());
		plot.setRangeGridlinesVisible(options.getGrid());

		// add additional datasets:
		DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
		dateAxis.setRange(begin.getTime(), end.getTime());
		dateAxis.setDateFormatOverride(new SimpleDateFormat("dd/MM/yyyy hh:mm a"));

		// add all axes
		String[] phenomenaIds = options.getAllPhenomenIds();
		
		// all the axis indices to map them later
		HashMap<String, Integer> axes = new HashMap<String, Integer>();
	    
		for (int i = 0; i < phenomenaIds.length; i++) {
			//ASD actualproperty: SoilTemperature_0.05mSensor1
			String actualPropertyName = phenomenaIds[i];
			String propertyType="";
			if(actualPropertyName.contains("_")){
				propertyType = actualPropertyName.substring(0, actualPropertyName.indexOf("_"));
			}
			else{
				propertyType = actualPropertyName;
			}
			Integer prop = axes.get(propertyType);
			if (prop != null) { // prop exists
				//do nothing
			}
			else {
				axes.put(propertyType.trim(), i);
				plot.setRangeAxis(i, new NumberAxis(propertyType.trim()));
			}
		}
		
		// list range markers
		ArrayList<ValueMarker> referenceMarkers = new ArrayList<ValueMarker>();
		HashMap<String, double[]> referenceBounds = new HashMap<String, double[]>();

		// create all TS collections
		for (int i = 0; i < options.getProperties().size(); i++) {

			TimeSeriesProperties prop = options.getProperties().get(i); // prop.getTsID());
																		// //TS_1386803550597
			Phenomenon phenomenon = prop.getPhenomenon();
			String phenomenonId = phenomenon.getId();
			String phenomenonIdSimplified ="";
			if(phenomenonId.contains("_"))
			{
				phenomenonIdSimplified = phenomenonId.substring(0, phenomenonId.indexOf("_")).trim();
			}
			else
			{
				phenomenonIdSimplified = phenomenonId.trim();
			}
			
			TimeSeriesCollection dataset = createDataset(entireCollMap, prop, phenomenon.getId(), compress);
			dataset.setGroup(new DatasetGroup(prop.getTsID()));

			XYDataset additionalDataset = dataset;

			NumberAxis axe = (NumberAxis) plot.getRangeAxis(axes.get(phenomenonIdSimplified));

			if (this.isOverview) {
				axe.setAutoRange(true);
				axe.setAutoRangeIncludesZero(false);
			} else if (prop.getAxisUpperBound() == prop.getAxisLowerBound() || prop.isAutoScale()) {
				if (prop.isZeroScaled()) {
					axe.setAutoRangeIncludesZero(true);
				} else {
					axe.setAutoRangeIncludesZero(false);
				}
			} else {
				if (prop.isZeroScaled()) {
					if (axe.getUpperBound() < prop.getAxisUpperBound()) {
						axe.setUpperBound(prop.getAxisUpperBound());
					}
					if (axe.getLowerBound() > prop.getAxisLowerBound()) {
						axe.setLowerBound(prop.getAxisLowerBound());
					}
				} else {
					axe.setRange(prop.getAxisLowerBound(), prop.getAxisUpperBound());
					axe.setAutoRangeIncludesZero(false);
				}
			}

			plot.setDataset(i, additionalDataset);
			//ASD
			plot.mapDatasetToRangeAxis(i, axes.get(phenomenonIdSimplified));
			
			// set bounds new for reference values
			if (!referenceBounds.containsKey(phenomenonIdSimplified)) {
				double[] bounds = new double[] { axe.getLowerBound(), axe.getUpperBound() };
				referenceBounds.put(phenomenonIdSimplified, bounds);
			} else {
				double[] bounds = referenceBounds.get(phenomenonIdSimplified);
				if (bounds[0] >= axe.getLowerBound()) {
					bounds[0] = axe.getLowerBound();
				}
				if (bounds[1] <= axe.getUpperBound()) {
					bounds[1] = axe.getUpperBound();
				}
			}
			
			double[] bounds = referenceBounds.get(phenomenonIdSimplified);
			for (String string : prop.getReferenceValues()) {
				if (prop.getRefValue(string).show()) {
					Double value = prop.getRefValue(string).getValue();
					if (value <= bounds[0]) {
						bounds[0] = value;
					} else if (value >= bounds[1]) {
						bounds[1] = value;
					}
				}
			}

			Axis axis = prop.getAxis();
			if (axis == null) {
				axis = new Axis(axe.getUpperBound(), axe.getLowerBound());
			} else if (prop.isAutoScale()) {
				axis.setLowerBound(axe.getLowerBound());
				axis.setUpperBound(axe.getUpperBound());
				axis.setMaxY(axis.getMaxY());
				axis.setMinY(axis.getMinY());
			}
			prop.setAxisData(axis);
			this.axisMapping.put(prop.getTsID(), axis);

			for (String string : prop.getReferenceValues()) {
				if (prop.getRefValue(string).show()) {
					referenceMarkers.add(new ValueMarker(prop.getRefValue(string).getValue(), Color.decode(prop.getRefValue(string).getColor()), new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f)));
				}
			}
			plot.mapDatasetToRangeAxis(i, axes.get(phenomenonIdSimplified));
		}
		
		for (ValueMarker valueMarker : referenceMarkers) {
			plot.addRangeMarker(valueMarker);
		}

		// show actual time
		ValueMarker nowMarker = new ValueMarker(System.currentTimeMillis(), Color.orange, new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f));
		plot.addDomainMarker(nowMarker);

		if (!this.isOverview) {
			Iterator<Entry<String, double[]>> iterator = referenceBounds.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, double[]> boundsEntry = iterator.next();
				String phenId = boundsEntry.getKey();
				NumberAxis axe = (NumberAxis) plot.getRangeAxis(axes.get(phenId));
				axe.setAutoRange(true);
				// add a margin
				double marginOffset = (boundsEntry.getValue()[1] - boundsEntry.getValue()[0]) / 25;
				boundsEntry.getValue()[0] -= marginOffset;
				boundsEntry.getValue()[1] += marginOffset;
				axe.setRange(boundsEntry.getValue()[0], boundsEntry.getValue()[1]);
			}
		}

		/**** SECOND RUN ***/

		// set domain axis labels:
		plot.getDomainAxis().setLabelFont(label);
		plot.getDomainAxis().setLabelPaint(LABEL_COLOR);
		plot.getDomainAxis().setTickLabelFont(tickLabelDomain);
		plot.getDomainAxis().setTickLabelPaint(LABEL_COLOR);
		plot.getDomainAxis().setLabel(designDescriptions.getDomainAxisLabel());

		// define the design for each series: getDatasetCount > number of series
		for (int datasetIndex = 0; datasetIndex < plot.getDatasetCount(); datasetIndex++) {
			TimeSeriesCollection dataset = (TimeSeriesCollection) plot.getDataset(datasetIndex);
			for (int seriesIndex = 0; seriesIndex < dataset.getSeriesCount(); seriesIndex++) {

				String seriesID = (String) dataset.getSeries(seriesIndex).getKey(); // RU_EC_002___AirAbsoluteHumidity2mAvg30minSensor1___RU_EC_002
				
				String foiID = seriesID.split("___")[0];
				String obsPropID = seriesID.split("___")[1];
				String procID = seriesID.split("___")[2];
				
				DesignDescription dd = designDescriptions.get(obsPropID, procID, foiID);

				if (dd != null) {
					// LINESTYLE:
					String lineStyle = dd.getLineStyle();
					int width = dd.getLineWidth();
					if (this.isOverview) {
						width = width / 2;
						width = (width == 0) ? 1 : width;
					}
					final Color seriesColor = dd.getColor();
					final int index = datasetIndex;

					// "1" is lineStyle "line"
					if (lineStyle.equalsIgnoreCase(LINE)) {
						/*
						 * if (!this.isOverview) { XYLineAndShapeRenderer render
						 * = new XYLineAndShapeRenderer(true, true);
						 * render.setStroke(new BasicStroke(width));
						 * render.setSeriesShape(seriesIndex, square);
						 * plot.setRenderer(datasetIndex, render); } else {
						 * XYLineAndShapeRenderer render = new
						 * XYLineAndShapeRenderer(true, false);
						 * render.setStroke(new BasicStroke(width)); );
						 * plot.setRenderer(datasetIndex, render); }
						 */
						if (!this.isOverview) {
							MyRenderer render = new MyRenderer(true, true, index, seriesIndex, seriesColor);
							plot.setRenderer(datasetIndex, render);
							render.setStroke(new BasicStroke(width));
							// plot.getRendererForDataset(plot.getDataset(datasetIndex)).setSeriesPaint(seriesIndex,
							// dd.getColor());
							// plot.setRenderer(datasetIndex, render);

						} else {
							XYLineAndShapeRenderer render = new XYLineAndShapeRenderer(true, false);
							render.setStroke(new BasicStroke(width));
							plot.setRenderer(datasetIndex, render);
						}

					}
					// "2" is lineStyle "area"
					else if (lineStyle.equalsIgnoreCase(AREA)) {
						plot.setRenderer(datasetIndex, new XYAreaRenderer());
					}
					// "3" is lineStyle "dotted"
					else if (lineStyle.equalsIgnoreCase(DOTTED)) {
						XYLineAndShapeRenderer ren = new XYLineAndShapeRenderer(false, true);
						ren.setShape(new Ellipse2D.Double(-width, -width, 2 * width, 2 * width));
						plot.setRenderer(datasetIndex, ren);
					}
					// "4" is dashed
					else if (lineStyle.equalsIgnoreCase("4")) {
						XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
						renderer.setSeriesStroke(0, new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 4.0f * width, 4.0f * width }, 0.0f));
						plot.setRenderer(datasetIndex, renderer);
					} else if (lineStyle.equalsIgnoreCase("5")) {
						// lines and dots
						XYLineAndShapeRenderer ren = new XYLineAndShapeRenderer(true, true);
						int thickness = 2 * width;
						ren.setShape(new Ellipse2D.Double(-width, -width, thickness, thickness));
						ren.setStroke(new BasicStroke(width));
						plot.setRenderer(datasetIndex, ren);
					} else {
						// default is lineStyle "line"
						plot.setRenderer(datasetIndex, new XYLineAndShapeRenderer(true, false));
					}

					XYToolTipGenerator toolTipGenerator = StandardXYToolTipGenerator.getTimeSeriesInstance();
					XYURLGenerator urlGenerator = new MetaDataInURLGenerator(designDescriptions);
					plot.getRenderer(datasetIndex).setBaseToolTipGenerator(toolTipGenerator);
					plot.getRenderer(datasetIndex).setURLGenerator(urlGenerator);
					// plot.getRenderer(datasetIndex).setSeriesPaint(seriesIndex,dd.getColor());

					// GRID:
					// PROBLEM: JFreeChart only allows to switch the grid on/off
					// for the whole XYPlot. And the
					// grid will always be displayed for the first series in the
					// plot. I'll always show the
					// grid.
					// --> plot.setDomainGridlinesVisible(visible)

					// RANGE AXIS LABELS:
					if (isOverview) {
						plot.getRangeAxisForDataset(datasetIndex).setTickLabelsVisible(false);
						plot.getRangeAxisForDataset(datasetIndex).setTickMarksVisible(false);
						plot.getRangeAxisForDataset(datasetIndex).setVisible(false);
						// ASD
						plot.getRenderer(datasetIndex).setSeriesPaint(seriesIndex, dd.getColor());
					} else {
						plot.getRangeAxisForDataset(datasetIndex).setLabelFont(label);
						plot.getRangeAxisForDataset(datasetIndex).setLabelPaint(LABEL_COLOR);
						plot.getRangeAxisForDataset(datasetIndex).setTickLabelFont(tickLabelDomain);
						plot.getRangeAxisForDataset(datasetIndex).setTickLabelPaint(LABEL_COLOR);
						StringBuilder unitOfMeasure = new StringBuilder();
						//ASD
						String yAxisPropVal = dd.getObservedPropertyDesc();

						if(yAxisPropVal.contains("_"))
						{
							yAxisPropVal = yAxisPropVal.substring(0, yAxisPropVal.indexOf("_"));
						}
						//ASD
						unitOfMeasure.append(yAxisPropVal.trim());
						String uomLabel = dd.getUomLabel();
						if (uomLabel != null && !uomLabel.isEmpty()) {
							unitOfMeasure.append(" (").append(uomLabel).append(")");
						}
						else {
							System.out.println("DiagramRenderer - unitOfMeasure is missing for: "+ dd.getProcedureID()+" "+ dd.getObservedPropertyDesc());
						}
						plot.getRangeAxisForDataset(datasetIndex).setLabel(unitOfMeasure.toString());

					}
				}
			}
		}

		if (this.isMapWithFlag && !this.isOverview && !isExportRequested) {
			addClickedPoint(options);
		}

		if (!this.isOverview) {
			LegendItemCollection items = new LegendItemCollection();
			Map<String, String> flagSymbolMap = CustomRenderingProperties.getInst().getActiveFlagSymbolPair();
			// get default shapes, generate legend accordingly
			Iterator it = flagSymbolMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry) it.next();
				String flagName = pairs.getKey().toString();
				String shapeType = pairs.getValue().toString();
				Color c = null;
				if (flagName.equalsIgnoreCase("ok")) {
					c = Color.GREEN;
				}
				if (flagName.equalsIgnoreCase("unevaluated")) {
					c = Color.DARK_GRAY;
				}
				if (flagName.equalsIgnoreCase("baddata")) {
					c = Color.RED;
				}
				if (flagName.equalsIgnoreCase("suspicious")) {
					c = Color.ORANGE;
				}
				if (flagName.equalsIgnoreCase("gapfilled")) {
					c = Color.BLUE;
				}

				items.add(new LegendItem(flagName, "", null, null, CustomRenderingProperties.getInst().getShapesByName(SHAPES.valueOf(shapeType)), c, new BasicStroke(1.0f), c));
			}
			plot.setFixedLegendItems(items);
			plot.setInsets(new RectangleInsets(5, 5, 5, 20));
			LegendTitle legend = new LegendTitle(plot);
			legend.setPosition(RectangleEdge.TOP);
			chart.addSubtitle(legend);
			// XYTitleAnnotation ta = new XYTitleAnnotation(options.getWidth(),
			// options.getHeight(), legend,RectangleAnchor.TOP_RIGHT);
			// ta.setMaxWidth(0.48);
			// plot.addAnnotation(ta);
		}

		return chart;
	}

	private void addClickedPoint(DesignOptions options) {
		LinkedHashMap<String, String> responseCoord = new LinkedHashMap<String, String>();
		String t = null;
		String v = null;
		Iterator<Entry<String, ArrayList<String>>> iter = options.getSelectedCoordinates().entrySet().iterator();
		LOGGER.debug("DiagramRenderer - RENDERED COORDINATES: " + options.getSelectedCoordinates());
		while (iter.hasNext()) {
			double millis = 0;
			double yDb = 0;
			Map.Entry pairs = (Map.Entry) iter.next();
			String timeType = (String) pairs.getKey();
			ArrayList<String> selectedPairs = (ArrayList<String>) pairs.getValue();
			for (int m = 0; m < selectedPairs.size(); m++) {
				t = selectedPairs.get(0);
				v = selectedPairs.get(1);
				yDb = Double.parseDouble(v);
				Date date = null;
				try {
					date = sdformat.parse(t);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				millis = date.getTime();
			}
			CircleDrawer cd = new CircleDrawer(Color.yellow, new BasicStroke(1.0f), Color.yellow);
			XYAnnotation selectedPoint = new XYDrawableAnnotation(millis, yDb, 13, 13, cd);
			plot.getRenderer().addAnnotation(selectedPoint);
			// plot.getRenderer(seriesIndex).addAnnotation(selectedPoint);
			responseCoord.put(timeType, t);
		}

		eesGenerator.setResponseCoord(responseCoord);
	}

	private JFreeChart initializeTimeSeriesChart() {
		String title = "";
		String xLabel = "Date";
		String yLabel = "";
		XYDataset data = null;
		// ASD
		boolean isCreateLegend = true;
		boolean isCreateTooltips = true;
		boolean isCreateURLs = true;
		JFreeChart chart = ChartFactory.createTimeSeriesChart(title, xLabel, yLabel, data, isCreateLegend, isCreateTooltips, isCreateURLs);

		return chart;
	}

	protected JFreeChart renderPreChart(Map<String, OXFFeatureCollection> entireCollMap, String[] observedProperties, ArrayList<TimeSeriesCollection> timeSeries, Calendar begin, Calendar end) {

		JFreeChart chart = ChartFactory.createTimeSeriesChart(null, // title
				"Date", // x-axis label
				observedProperties[0], // y-axis label
				timeSeries.get(0), // data
				false, // create legend?
				true, // generate tooltips?
				false // generate URLs?
				);

		chart.setBackgroundPaint(Color.white);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.lightGray);
		plot.setAxisOffset(new RectangleInsets(2.0, 2.0, 2.0, 2.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

		// add additional datasets:
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setRange(begin.getTime(), end.getTime());
		axis.setDateFormatOverride(new SimpleDateFormat());
		for (int i = 1; i < observedProperties.length; i++) {
			XYDataset additionalDataset = timeSeries.get(i);
			plot.setDataset(i, additionalDataset);
			plot.setRangeAxis(i, new NumberAxis(observedProperties[i]));
			// plot.getRangeAxis(i).setRange((Double)
			// overAllSeriesCollection.getMinimum(i),
			// (Double) overAllSeriesCollection.getMaximum(i));
			plot.mapDatasetToRangeAxis(i, i);
			// plot.getDataset().getXValue(i, i);
		}

		return chart;
	}

	public TimeSeriesCollection createDataset(Map<String, OXFFeatureCollection> entireCollMap, TimeSeriesProperties prop, String observedProperty, boolean compress) {

		TimeSeriesCollection dataset = new TimeSeriesCollection();
		OXFFeatureCollection obsColl = entireCollMap.get(prop.getOffering().getId() + "@" + prop.getSosUrl());

		String foiID = prop.getFoi().getId();
		String obsPropID = prop.getPhenomenon().getId();
		String procID = prop.getProcedure().getId();
		// ASD
		String qualityControlObsPropId = prop.getPhenomenon().getId() + "QualityFlag";

		// only if the observation concerns the observedProperty, it
		// will be added to the dataset
		if (obsPropID.equals(observedProperty)) {

			String[] foiIds = new String[] { foiID };
			String[] procedureIds = new String[] { procID };
			String[] observedPropertyIds = new String[] { obsPropID };
			String[] observedPropertyIds2 = new String[] { obsPropID, qualityControlObsPropId };

			// get tuples***
			ObservationSeriesCollection seriesCollection = new ObservationSeriesCollection(obsColl, foiIds, observedPropertyIds, procedureIds, true);
			// Map<ITimePosition,ObservedValueTuple> maptest =
			// seriesCollection.getAllTuples();
			ObservationQualityControlCollection seriesCollectionwihtQuality = new ObservationQualityControlCollection(obsColl, foiIds, observedPropertyIds2, procedureIds, true);
			//
			// now let's put in the date-value pairs.
			// ! But put it only in if it differs from the previous
			// one !
			//
			HashMap<String, String> qualityTimeMap = null;

			TimeSeries timeSeries = new TimeSeries(foiID + "___" + obsPropID + "___" + procID, Second.class);
			if (seriesCollection.getSortedTimeArray().length > 0) {
				if (compress) {
					timeSeries = TimeseriesFactory.compressToTimeSeries(seriesCollection, foiID, obsPropID, procID, isOverview, prop.getGraphStyle());
				} else {
					qualityTimeMap = createTimeQualityMap(seriesCollectionwihtQuality, foiID, obsPropID, procID, prop.getGraphStyle());
					timeSeries = TimeseriesFactory.createTimeSeries(seriesCollection, foiID, obsPropID, procID, prop.getGraphStyle());
					if (!this.isOverview) {
						this.generalSpecificqQualifierMap.put(prop.getTsID(), qualityTimeMap); // timeseriesId,
																								// <2008-05-08
						//System.out.println("DDDDDDDDDDDD "+ generalSpecificqQualifierMap.toString());																// 03:00:00+0200=2_2>
					}
				}
			}
			dataset.addSeries(timeSeries);

		}

		dataset.setDomainIsPointsInTime(true);

		return dataset;
	}

	private static HashMap<String, String> createTimeQualityMap(ObservationQualityControlCollection seriesCollection2, String foiID, String obsPropID, String procID, String seriesType) {

		HashMap<String, String> qualMap = new HashMap<String, String>();
		LOGGER.debug("Matching time with quality information");

		ITimePosition timeArray[] = seriesCollection2.getSortedTimeArray();
		ObservedValueTuple nextObservation = seriesCollection2.getTuple(new OXFFeature(foiID, null), timeArray[0]);
		ObservedValueTuple observation = nextObservation;

		Double sum = 0.0;

		for (int i = 0; i < timeArray.length; i++) {

			final String OLD_FORMAT = "dd.M.yyyy H:mm:s.Sz"; // 20.1.2013
																// 0:30:0.0+01
			final String NEW_FORMAT = "yyyy-MM-dd HH:mm:ssZ";
			SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
			observation = nextObservation;

			if (i + 1 < timeArray.length) {
				nextObservation = seriesCollection2.getTuple(new OXFFeature(foiID, null), timeArray[i + 1]);
			}
			// 2008-05-09T12:00:00+02
			ITimePosition timePos = (ITimePosition) observation.getTime();
			Double resultVal = getValidDataNew(observation.getValue(0).toString());
			String qualVal = observation.getValue(1).toString();
			if (seriesType.equals("1")) {
				// nothing
			} else if (seriesType.equals("2")) {
				if (resultVal != null) {
					resultVal += sum;
				} else {
					resultVal = sum;
				}
			} else {
				// nothing
			}

			sum = resultVal;
			String reformattedStr = "";

			Date d = null;
			try {
				// to-do: the time zone formats available to SimpleDateFormat
				// are not ISO8601 compliant.
				d = sdf.parse(timePos.toString() + "00");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sdf.applyPattern(NEW_FORMAT);

			reformattedStr = sdf.format(d);
			// create HashMap: 07-05-2008 21:10:00 CEST=noData
			qualMap.put(reformattedStr, qualVal);

		}
		return qualMap;

	}

	/**
	 * @param obsVal
	 * @return
	 */
	private static Double getValidDataNew(String obsVal) {
		// XXX no data as double? double comparison via equals? switch to
		// NO_DATA value string
		Double tmp = null;
		try {
			tmp = new Double(obsVal);
			for (Double noData : ConfigurationContext.NO_DATA_VALUES) {
				if (tmp.equals(noData)) {
					return null;
				}
			}
		} catch (NumberFormatException e) {
			LOGGER.error("Not a double or integer value " + obsVal, e);
		}
		return tmp;
	}

	public String getQualifierList(String id, String time) {
		HashMap<String, String> tempMap = generalSpecificqQualifierMap.get(id);
		String qualityInfo = "";
		if (tempMap != null) {
			qualityInfo = tempMap.get(time).trim();
		} else {
			Log.debug("NO QUALIFIER is found for :" + time);
			qualityInfo = "N/A";
		}
		return qualityInfo;
	}

	private class MyRenderer extends XYLineAndShapeRenderer {
		int dIndex = 0;
		Color defSeriesColor = null;

		public MyRenderer(boolean lines, boolean shapes, int index, int seriesIndex, Color seriesColor) {
			super(lines, shapes);
			this.dIndex = index;
			this.defSeriesColor = seriesColor;
		}

		@Override
		public Paint getItemPaint(int row, int col) {
			Paint cpaint = getItemColor(row, col);
			if (cpaint == null) {
				cpaint = super.getItemPaint(row, col);
			}
			return cpaint;
		}

		@Override
		protected void drawFirstPassShape(Graphics2D g2, int pass, int series, int item, Shape shape) {
			g2.setStroke(getItemStroke(series, item));
			// Color c1 = getItemColor(series, item);
			// Color c2 = getItemColor(series, item - 1);
			// GradientPaint linePaint = new GradientPaint(0, 0, c1, 0, 300,
			// c2);
			g2.setPaint(defSeriesColor);
			g2.draw(shape);
		}

		public Color getItemColor(int row, int col) {
			//getPlot().getDataset(dIndex).getY(row, col);
			long time = (Long) getPlot().getDataset(dIndex).getX(row, col);
			Date date = new Date(time);

			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
			String dateText = df2.format(date); // formatted: 2008-05-08
												// 15:20:00+0200

			// identify timeseries id, get hashmap(time, data qualityinfo)
			String timeSeriesId = (String) getPlot().getDataset(dIndex).getGroup().getID();
			String qualityInfo = DiagramRenderer.this.getQualifierList(timeSeriesId, dateText);// qualityInfo

			if (qualityInfo.equalsIgnoreCase("noData") || qualityInfo==null || qualityInfo.equals("") || qualityInfo.equals("N/A")) {
				// empty shapes
				return Color.DARK_GRAY;
			} else {
				String[] parts = qualityInfo.split("_"); // 2_2

				String dataLevelId = parts[0];
				String genericFlgId = parts[1];

				if (dataLevelId.equalsIgnoreCase("1")) {
					return Color.DARK_GRAY;
				} 
				else { // rest (ok, suspicious,// missing,interpolated) 
					String genSpecFlags = flagMaps.get(genericFlgId).toString();

					String[] flg = genSpecFlags.split(","); // example:
															// baddata,irregular
					String genericFlag = flg[0];
					if (genericFlag.trim().equalsIgnoreCase("ok")) {
						return Color.GREEN;
					}
					if (genericFlag.trim().equalsIgnoreCase("suspicious")) {
						return Color.ORANGE;
					}
					if (genericFlag.trim().equalsIgnoreCase("baddata")) {
						return Color.RED;
					}
					if (genericFlag.trim().equalsIgnoreCase("gapfilled")) {
						return Color.BLUE;
					}
					if (genericFlag.trim().equalsIgnoreCase("unevaluated")) {
						return Color.DARK_GRAY;
					}
				}
			}
			return Color.DARK_GRAY;
		}

		@Override
		public Shape getItemShape(int row, int column) {
			getPlot().getDataset(dIndex).getY(row, column);
			long time = (Long) getPlot().getDataset(dIndex).getX(row, column);
			Date date = new Date(time);

			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
			String dateText = df2.format(date); 

			// identify timeseries id, get hashmap(time, data qualityinfo)
			String timeSeriesId = (String) getPlot().getDataset(dIndex).getGroup().getID();
			String qualityInfo = DiagramRenderer.this.getQualifierList(timeSeriesId, dateText);// qualityInfo:2_2 or noData

			String unevaluatedShape = genericFlagSymbolMap.get("unevaluated").toString();
			if (qualityInfo.equalsIgnoreCase("noData") || qualityInfo==null || qualityInfo.equals("") || qualityInfo.equals("N/A")) {
				// empty shapes
				return customRenderingInstance.getShapesByName(SHAPES.valueOf(unevaluatedShape));
			} else {
				// dissect quality info
				String[] parts = qualityInfo.split("_"); // 2_2
			
				String dataLevelId = parts[0];
				String genericFlgId = parts[1];

				if (dataLevelId.equalsIgnoreCase("1")) {
					return customRenderingInstance.getShapesByName(SHAPES.valueOf(unevaluatedShape));
				} else // rest (ok, suspicious,
						// missing,interpolated)
				{
					String genSpecFlags = flagMaps.get(genericFlgId).toString();
		
					String[] flg = genSpecFlags.split(","); // example:
			
					String genericFlag = flg[0];
					String shapeName = genericFlagSymbolMap.get(genericFlag).toString();
					Shape seriesShape = customRenderingInstance.getShapesByName(SHAPES.valueOf(shapeName));
					return seriesShape;
				}
			}
		} 
	}
	
	  private static JFreeChart createChart(XYDataset dataset) {

	        JFreeChart chart = ChartFactory.createTimeSeriesChart(
	            "Legal & General Unit Trust Prices",  // title
	            "Date",             // x-axis label
	            "Price Per Unit",   // y-axis label
	            dataset,            // data
	            true,               // create legend?
	            true,               // generate tooltips?
	            false               // generate URLs?
	        );

	        chart.setBackgroundPaint(Color.white);

	        XYPlot plot = (XYPlot) chart.getPlot();
	        plot.setBackgroundPaint(Color.lightGray);
	        plot.setDomainGridlinePaint(Color.white);
	        plot.setRangeGridlinePaint(Color.white);
	        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
	        plot.setDomainCrosshairVisible(true);
	        plot.setRangeCrosshairVisible(true);

	        XYItemRenderer r = plot.getRenderer();
	        if (r instanceof XYLineAndShapeRenderer) {
	            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
	            renderer.setBaseShapesVisible(true);
	            renderer.setBaseShapesFilled(true);
	            renderer.setDrawSeriesLineAsPath(true);
	        }

	        DateAxis axis = (DateAxis) plot.getDomainAxis();
	        axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));

	        return chart;

	    }
}
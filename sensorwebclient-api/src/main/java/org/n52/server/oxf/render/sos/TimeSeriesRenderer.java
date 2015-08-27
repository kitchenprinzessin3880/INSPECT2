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

import org.fzj.ibg.odm.tables.datavalues.DataValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.n52.oxf.util.JavaHelper;
import org.n52.server.oxf.render.sos.CustomRenderingProperties.SHAPES;
import org.n52.server.oxf.render.sos.DesignDescriptionList.DesignDescription;
import org.n52.server.oxf.util.access.ObservationData;
import org.n52.server.oxf.util.generator.DiagramGenerator;
import org.n52.server.oxf.util.generator.Generator;
import org.n52.server.oxf.util.generator.MetaDataInURLGenerator;
import org.n52.server.oxf.util.generator.TimeSeriesGenerator;
import org.n52.shared.serializable.pojos.Axis;
import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.TimeSeriesProperties;
import org.n52.shared.serializable.pojos.sos.Phenomenon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSeriesRenderer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimeSeriesRenderer.class);

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

	private Generator eesGenerator;

	private XYPlot plot;

	// private int counter = 0;

	Map<String, String> qualityFlagMapping;

	private boolean isExportRequested = false;

	protected static CustomRenderingProperties customRenderingInstance;

	private static Map<String, String> genericFlagSymbolMap = new HashMap<String, String>();

	private SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

	private Map<String, TimeSeriesCollection> datasetMap = null;

	Map<String, HashMap<Long, DataValue>> dataValueMap = null;

	private Map<String, Integer> renderInstances = new HashMap<String, Integer>();
	HashMap<String, Integer> axes = null;
	boolean isGenerated = false;

	public TimeSeriesRenderer(boolean isOverview, TimeSeriesGenerator gen, Map<String, TimeSeriesCollection> dataset2) {
		this.isOverview = isOverview;
		this.eesGenerator = gen;
		this.datasetMap = dataset2;
		this.customRenderingInstance = CustomRenderingProperties.getInst();
		this.genericFlagSymbolMap = customRenderingInstance.getActiveFlagSymbolPair(); // ok->filledsquare,
		this.axes = new HashMap<String, Integer>();
	}

	public TimeSeriesRenderer(boolean isOverview, DiagramGenerator gen, Map<String, TimeSeriesCollection> dataset2) {
		this.isOverview = isOverview;
		this.eesGenerator = gen;
		this.datasetMap = dataset2;
		this.customRenderingInstance = CustomRenderingProperties.getInst();
		this.genericFlagSymbolMap = customRenderingInstance.getActiveFlagSymbolPair(); // ok->filledsquare,
		//ASD 18.11.2014
		this.axes = new HashMap<String, Integer>();
	}

	public TimeSeriesRenderer getTimeSeriesRenderer() {
		return TimeSeriesRenderer.this;
	}

	public HashMap<String, Axis> getAxisMapping() {
		return this.axisMapping;
	}

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

	public JFreeChart renderChart(DesignOptions options, Calendar begin, Calendar end, boolean compress) {

		DesignDescriptionList designDescriptions = buildUpDesignDescriptionList(options);
		// ASD - get render coordinates
		this.isMapWithFlag = options.isQCActivated();

		/*** FIRST RUN ***/
		JFreeChart chart = initializeTimeSeriesChart();
		chart.setBackgroundPaint(Color.white);

		if (!this.isOverview) {
			chart.addProgressListener(new ChartProgressListener() {
				public void chartProgress(ChartProgressEvent event) {
					if (event.getType() != ChartProgressEvent.DRAWING_FINISHED) {
						return;
					}
					if (event.getType() == ChartProgressEvent.DRAWING_FINISHED && !isExportRequested) {
						if (!isGenerated) {
							((TimeSeriesGenerator) eesGenerator).generateTimeSeriesCoordinates(axes);
							isGenerated = true; // this is to ensure the method
												// is only called once
												// generateTimeSeriesCoordinates()
						}
					}
				}

			});
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

		String[] phenomenaIds = options.getAllPhenomenIds();
		
		// ASD 11.09.2014
		for (int i = 0; i < phenomenaIds.length; i++) {
			String actualPropertyName = phenomenaIds[i];
			String propertyType = "";
			if (actualPropertyName.contains("_")) {
				propertyType = actualPropertyName.substring(0, actualPropertyName.indexOf("_"));
			} else {
				propertyType = actualPropertyName;
			}
			Integer prop = axes.get(propertyType);
			if (prop != null) { // prop exists
			} else {
				axes.put(propertyType.trim(), i);
				plot.setRangeAxis(i, new NumberAxis(propertyType.trim()));
			}
		}

		// list range markers
		ArrayList<ValueMarker> referenceMarkers = new ArrayList<ValueMarker>();
		HashMap<String, double[]> referenceBounds = new HashMap<String, double[]>();

		// ASD sort timeseries; first series must be the current series selected
		// by the user; annotations directly added to the plot will be drawn on
		// the topmost layer.
		if (isMapWithFlag) {
			int index = 0;
			TimeSeriesProperties oldTimeSeries = null;
			for (int i = 0; i < options.getProperties().size(); i++) {
				oldTimeSeries = options.getProperties().get(0);
				if (options.getProperties().get(i).getLabel().equalsIgnoreCase(options.getSelectedSeriesId())) {
					index = i;
					options.getProperties().set(0, options.getProperties().get(i));
					options.getProperties().set(index, oldTimeSeries);
					break;
				}
			}
		}

		// create all TS collections
		for (int i = 0; i < options.getProperties().size(); i++) {

			TimeSeriesProperties prop = options.getProperties().get(i); // TS_1386803550597
			Phenomenon phenomenon = prop.getPhenomenon();
			String phenomenonId = phenomenon.getId();
			String phenomenonIdSimplified = "";
			if (phenomenonId.contains("_")) {
				phenomenonIdSimplified = phenomenonId.substring(0, phenomenonId.indexOf("_")).trim();
			} else {
				phenomenonIdSimplified = phenomenonId.trim();
			}

			TimeSeriesCollection dataset = datasetMap.get(prop.getTsID());
			dataset.setGroup(new DatasetGroup(prop.getTsID()));
			XYDataset additionalDataset = dataset;

			NumberAxis axe = (NumberAxis) plot.getRangeAxis(axes.get(phenomenonIdSimplified));
			if (this.isOverview) {
				axe.setAutoRange(true);
				axe.setAutoRangeIncludesZero(false);
			} 
			else {
				if (prop.isAutoScale()) {
				//if (prop.getAxisUpperBound() == prop.getAxisLowerBound() || prop.isAutoScale()) {
					/*
					if (prop.isZeroScaled()) {
						if (this.isOverview) {LOGGER.debug("A............................");}
						axe.setAutoRangeIncludesZero(true);
					} else {
						if (this.isOverview) {LOGGER.debug("B............................");}
						axe.setAutoRangeIncludesZero(false);
					}*/
					axe.setAutoRangeIncludesZero(false);
					axe.setAutoRange(true);
				}
				else if (prop.isZeroScaled()) {
					axe.setAutoRangeIncludesZero(true);
					if (axe.getUpperBound() < prop.getAxisUpperBound()) {
						axe.setUpperBound(prop.getAxisUpperBound());
					}
					if (axe.getLowerBound() > prop.getAxisLowerBound()) {
						axe.setLowerBound(prop.getAxisLowerBound());
					}
				} else if (prop.isCustomScale()) {
					axe.setLowerBound(prop.getCustomScaleMin());
					axe.setUpperBound(prop.getCustomScaleMax());
				}

			}

			plot.setDataset(i, additionalDataset);
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
			} else if (prop.isCustomScale()) {
				axis.setLowerBound(prop.getCustomScaleMin());
				axis.setUpperBound(prop.getCustomScaleMax());
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
			
		} // end for loop

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
					// commented out by ASD
					// if (this.isOverview) {
					width = width / 2;
					width = (width == 0) ? 1 : width;
					// }
					final Color seriesColor = dd.getColor();

					// "1" is lineStyle "line"
					if (lineStyle.equalsIgnoreCase(LINE)) {
						if (!this.isOverview) {
							// to-do: currently, one timeseries = each dataset
							// with one series
							MyRenderer render = new MyRenderer(true, true, dataset.getGroup().getID(), dataset, seriesColor);
							plot.setRenderer(datasetIndex, render);
							render.setStroke(new BasicStroke(width));
							renderInstances.put(dataset.getGroup().getID(), datasetIndex);
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

					if (isOverview) {
						plot.getRangeAxisForDataset(datasetIndex).setTickLabelsVisible(false);
						plot.getRangeAxisForDataset(datasetIndex).setTickMarksVisible(false);
						plot.getRangeAxisForDataset(datasetIndex).setVisible(false);
						// ASD
						plot.getRenderer(datasetIndex).setSeriesPaint(seriesIndex, dd.getColor());
					} else {
						// ASD 29.07.2014
						// XYToolTipGenerator toolTipGenerator =
						// StandardXYToolTipGenerator.getTimeSeriesInstance();
						XYURLGenerator urlGenerator = new MetaDataInURLGenerator(designDescriptions);
						// plot.getRenderer(datasetIndex).setBaseToolTipGenerator(toolTipGenerator);
						plot.getRenderer(datasetIndex).setURLGenerator(urlGenerator);

						plot.getRangeAxisForDataset(datasetIndex).setLabelFont(label);
						plot.getRangeAxisForDataset(datasetIndex).setLabelPaint(LABEL_COLOR);
						plot.getRangeAxisForDataset(datasetIndex).setTickLabelFont(tickLabelDomain);
						plot.getRangeAxisForDataset(datasetIndex).setTickLabelPaint(LABEL_COLOR);
						StringBuilder unitOfMeasure = new StringBuilder();
						// ASD
						String yAxisPropVal = dd.getObservedPropertyDesc();

						if (yAxisPropVal.contains("_")) {
							yAxisPropVal = yAxisPropVal.substring(0, yAxisPropVal.indexOf("_"));
						}
						// ASD
						unitOfMeasure.append(yAxisPropVal.trim());
						String uomLabel = dd.getUomLabel();
						if (uomLabel != null && !uomLabel.isEmpty()) {
							unitOfMeasure.append(" (").append(uomLabel).append(")");
						} else {
							LOGGER.debug("TimeSeriesRenderer - unitOfMeasure is missing for: " + dd.getProcedureID() + " " + dd.getObservedPropertyDesc());
						}
						plot.getRangeAxisForDataset(datasetIndex).setLabel(unitOfMeasure.toString());

					}
				}
			}
		}

		if (this.isMapWithFlag && !isExportRequested && !this.isOverview) {
			addClickedPoint(options);
		}

		if (!this.isOverview) {
			LegendItemCollection items = customRenderingInstance.getLegendItems();
			plot.setFixedLegendItems(items);
			plot.setInsets(new RectangleInsets(5, 5, 5, 20));
			LegendTitle legend = new LegendTitle(plot);
			legend.setPosition(RectangleEdge.TOP);
			chart.addSubtitle(legend);
		}

		return chart;
	}

	private void addClickedPoint(DesignOptions options) {
		XYAnnotation selectedPoint = null;
		LinkedHashMap<String, String> responseCoord = new LinkedHashMap<String, String>();
		String t = null;
		String v = null;

		Iterator<Entry<String, ArrayList<String>>> iter = options.getSelectedCoordinates().entrySet().iterator();
		LOGGER.debug("TimeSeriesRenderer - RENDERED COORDINATES: " + options.getSelectedCoordinates());
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
			selectedPoint = new XYDrawableAnnotation(millis, yDb, 6, 6, cd);
			int renderIndex;
			if (timeType.contains("#")) // offline mode -> multiple series
			{
				String[] id = timeType.split("#");
				renderIndex = renderInstances.get(id[0]);
				plot.getRenderer(renderIndex).addAnnotation(selectedPoint);
				responseCoord.put(timeType, t + "#" + selectedPairs.get(2) + "#" + selectedPairs.get(3));
			} else {
				plot.getRenderer().addAnnotation(selectedPoint);
				responseCoord.put(timeType, t);
			}
		}
		((TimeSeriesGenerator) eesGenerator).setResponseCoord(responseCoord);
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

	/*
	 * protected JFreeChart renderPreChart(Map<String, OXFFeatureCollection>
	 * entireCollMap, String[] observedProperties,
	 * ArrayList<TimeSeriesCollection> timeSeries, Calendar begin, Calendar end)
	 * {
	 * 
	 * JFreeChart chart = ChartFactory.createTimeSeriesChart(null, // title
	 * "Date", // x-axis label observedProperties[0], // y-axis label
	 * timeSeries.get(0), // data false, // create legend? true, // generate
	 * tooltips? false // generate URLs? );
	 * 
	 * chart.setBackgroundPaint(Color.white);
	 * 
	 * XYPlot plot = (XYPlot) chart.getPlot();
	 * plot.setBackgroundPaint(Color.white);
	 * plot.setDomainGridlinePaint(Color.lightGray);
	 * plot.setRangeGridlinePaint(Color.lightGray); plot.setAxisOffset(new
	 * RectangleInsets(2.0, 2.0, 2.0, 2.0));
	 * plot.setDomainCrosshairVisible(true);
	 * plot.setRangeCrosshairVisible(true);
	 * 
	 * // add additional datasets: DateAxis axis = (DateAxis)
	 * plot.getDomainAxis(); axis.setRange(begin.getTime(), end.getTime());
	 * axis.setDateFormatOverride(new SimpleDateFormat()); for (int i = 1; i <
	 * observedProperties.length; i++) { XYDataset additionalDataset =
	 * timeSeries.get(i); plot.setDataset(i, additionalDataset);
	 * plot.setRangeAxis(i, new NumberAxis(observedProperties[i]));
	 * plot.mapDatasetToRangeAxis(i, i); }
	 * 
	 * return chart; }
	 */

	/*
	 * public Long getDataIdByDate(String id, String time) { // HashMap<String,
	 * ArrayList<String>> tempMap = //
	 * ObservationData.getInstance().getDataWithQualifierMap().get(id);
	 * DataValue dataValue =
	 * ObservationData.getInstance().getLocalObservationData
	 * ().get(id).get(time); Long dId = null; if (dataValue != null) { dId =
	 * dataValue.getId(); } return dId; }
	 */

	public String getFlagByDate(String id, Date time) {

		// HashMap<String, ArrayList<String>> tempMap =
		// ObservationData.getInstance().getDataWithQualifierMap().get(id);
		DataValue dataValue = ObservationData.getInstance().getLocalObservationData().get(id).get(time);
		String genericFlag = "";
		if (dataValue != null) {
			genericFlag = dataValue.getQualifierGroup().getGroup().getCode();
		} else {
			LOGGER.debug("GetFlagByDate returns NULLL !!!!!!!! : " + id + " " + time);
			genericFlag = "N/A";
		}
		return genericFlag;
	}

	private class MyRenderer extends XYLineAndShapeRenderer {
		// int dIndex = 0;
		Color defSeriesColor = null;
		String timeSeriesId = null;
		XYDataset dataset = null;
		Map<String, ArrayList<String>> coordinates = null;

		public MyRenderer(boolean lines, boolean shapes, String tId, TimeSeriesCollection dataset2, Color seriesColor) {
			super(lines, shapes);
			this.defSeriesColor = seriesColor;
			this.timeSeriesId = tId;
			this.dataset = dataset2;
			// LOGGER.debug("Current Observation Data While Rendering: " +
			// ObservationData.getInstance().getLocalObservationData().get(timeSeriesId).size());
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
			g2.setPaint(defSeriesColor);
			g2.draw(shape);
		}

		public Color getItemColor(int row, int col) {
			long time = (Long) dataset.getX(row, col);
			// String timeStmpStr = sdformat.format(time);
			Color selectedColor = null;
			// identify timeseries id (TS_1400073767067), get hashmap by time
			// String timeSeriesId = (String)
			// getPlot().getDataset(dIndex).getGroup().getID();

			String genericFlag = getFlagByDate(timeSeriesId, new Date(time));// qualityInfo

			if (genericFlag.equals("unevaluated")) {
				selectedColor = Color.BLACK;
			} else if (genericFlag.trim().equalsIgnoreCase("ok")) {
				selectedColor = Color.GREEN;
			} else if (genericFlag.trim().equalsIgnoreCase("suspicious")) {
				selectedColor = Color.ORANGE;
			} else if (genericFlag.trim().equalsIgnoreCase("baddata")) {
				selectedColor = Color.RED;
			} else if (genericFlag.trim().equalsIgnoreCase("gapfilled")) {
				selectedColor = Color.BLUE;
			} else {
				selectedColor = Color.BLACK; // e.g., N/A
			}
			return selectedColor;
		}

		@Override
		public Shape getItemShape(int row, int column) {
			// double time = (Long) getPlot().getDataset(dIndex).getX(row,
			// column);

			long time = (Long) dataset.getX(row, column);
			// String timeStmpStr = sdformat.format(time);
			// String timeSeriesId = (String)
			// getPlot().getDataset(dIndex).getGroup().getID();
			String genericFlag = getFlagByDate(timeSeriesId, new Date(time));// qualityInfo
			String unevaluatedShape = genericFlagSymbolMap.get("unevaluated").toString();
			if (genericFlag.equals("N/A")) {
				// empty shapes
				return customRenderingInstance.getShapesByName(SHAPES.valueOf(unevaluatedShape));
			} else {
				String shapeName = genericFlagSymbolMap.get(genericFlag).toString();
				Shape seriesShape = customRenderingInstance.getShapesByName(SHAPES.valueOf(shapeName));
				return seriesShape;
			}
		}
	}

}
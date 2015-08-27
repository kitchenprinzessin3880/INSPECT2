package org.n52.server.oxf.render.sos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.util.ShapeUtilities;

public class CustomRenderingProperties {
	private static CustomRenderingProperties instance;
	private static Map<String, String> defaultFlagSymbolPair = new HashMap<String, String>();
	private Map<String, String> activeFlagSymbolPair = new HashMap<String, String>();
	private Map<Long, String> dataProcLevels = new HashMap<Long, String>();
	private Map<Long, String> qualityFlagMappingWithId = new HashMap<Long, String>();
	private EnumMap<SHAPES, Shape> shapesMap;
	private static LegendItemCollection legendItems = null;

	public enum SHAPES {
		cross("cross"), triangleup("triangleup"), triangledown("triangledown"), diamond("diamond"), filledsquare("filledsquare");
		private final String name;

		private SHAPES(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static CustomRenderingProperties getInst() {
		if (instance == null) {
			instance = new CustomRenderingProperties();
		}
		return instance;
	}

	private CustomRenderingProperties() {

		// create shapes instances
		shapesMap = new EnumMap<SHAPES, Shape>(SHAPES.class);
		// shapesMap.put(SHAPES.cross,
		// ShapeUtilities.createRegularCross(2.0f,0.01f));
		shapesMap.put(SHAPES.cross, new Cross(-1.5, -1.5, 3, 3));
		shapesMap.put(SHAPES.triangledown, ShapeUtilities.createDownTriangle(1.5f));
		shapesMap.put(SHAPES.triangleup, ShapeUtilities.createUpTriangle(1.5f));
		shapesMap.put(SHAPES.diamond, ShapeUtilities.createDiamond(1.5f));
		shapesMap.put(SHAPES.filledsquare, new Rectangle2D.Double(-1.5, -1.5, 3.0, 3.0));
	}

	public Shape getShapesByName(SHAPES shapeName) {
		return this.shapesMap.get(shapeName);
	}

	public void generateDefaultFlagShapePairs(ArrayList genericFlags) {
		for (int i = 0; i < genericFlags.size(); i++) {
			String flag = genericFlags.get(i).toString();
			if (flag.equalsIgnoreCase("ok")) {
				defaultFlagSymbolPair.put(flag, "filledsquare");
			}
			if (flag.equalsIgnoreCase("unevaluated")) {
				defaultFlagSymbolPair.put(flag, "cross");
			}
			if (flag.equalsIgnoreCase("baddata")) {
				defaultFlagSymbolPair.put(flag, "triangledown");
			}
			if (flag.equalsIgnoreCase("suspicious")) {
				defaultFlagSymbolPair.put(flag, "triangleup");
			}
			if (flag.equalsIgnoreCase("gapfilled")) {
				defaultFlagSymbolPair.put(flag, "diamond");
			}
		}
		setActiveFlagSymbolPair(defaultFlagSymbolPair);
	}

	public Map<String, String> getDefaultFlagsSymbolPair() {
		return defaultFlagSymbolPair;
	}

	public Map getDataProcLevels() {
		return dataProcLevels;
	}

	public void setDataProcLevels(Map dataProcLevels) {
		this.dataProcLevels = dataProcLevels;
	}

	public List getAllActiveShapesNames() {
		List shapesList = new ArrayList(this.activeFlagSymbolPair.values());
		return shapesList;
	}

	public Map<Long, String> getQualityFlagMappingWithId() {
		return qualityFlagMappingWithId;
	}

	public void setQualityFlagMappingWithId(Map<Long, String> qualityFlagMapping) {
		qualityFlagMappingWithId = qualityFlagMapping;
	}

	public EnumMap<SHAPES, Shape> getShapesMap() {
		return shapesMap;
	}

	public void setShapesMap(EnumMap<SHAPES, Shape> shapesMap) {
		this.shapesMap = shapesMap;
	}

	public Map<String, String> getActiveFlagSymbolPair() {
		return activeFlagSymbolPair;
	}

	public void setActiveFlagSymbolPair(Map<String, String> activeFlagSymbolPair) {
		this.activeFlagSymbolPair = activeFlagSymbolPair;
		
		legendItems = new LegendItemCollection();
		// get default shapes, generate legend accordingly
		Iterator it = activeFlagSymbolPair.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			String flagName = pairs.getKey().toString();
			String shapeType = pairs.getValue().toString();
			Color c = null;
			if (flagName.equalsIgnoreCase("ok")) {
				c = Color.GREEN;
			}
			if (flagName.equalsIgnoreCase("unevaluated")) {
				c = Color.BLACK;
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
			legendItems.add(new LegendItem(flagName, "", null, null, getShapesByName(SHAPES.valueOf(shapeType)), c, new BasicStroke(1.0f), c));
		}
	}

	public static LegendItemCollection getLegendItems() {
		return legendItems;
	}

	public static void setLegendItems(LegendItemCollection legendItems) {
		CustomRenderingProperties.legendItems = legendItems;
	}
}

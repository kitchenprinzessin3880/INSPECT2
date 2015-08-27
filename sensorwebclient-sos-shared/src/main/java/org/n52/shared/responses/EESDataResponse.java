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
package org.n52.shared.responses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eesgmbh.gimv.shared.util.Bounds;
import org.eesgmbh.gimv.shared.util.ImageEntity;
import org.n52.shared.serializable.pojos.Axis;
import org.n52.shared.serializable.pojos.DesignOptions;
import org.n52.shared.serializable.pojos.ResponseOptions;
import org.n52.shared.serializable.pojos.TimeSeriesProperties;

public class EESDataResponse extends RepresentationResponse {

    private static final long serialVersionUID = -1036816874508351530L;

    private String imageUrl;

    private Bounds plotArea;

    private ImageEntity[] imageEntities;

    private HashMap<String, Axis> tsAxis;

    private String overviewurl;

    private int width;

    private int height;

    private long begin;

    private long end;

    private ArrayList<TimeSeriesProperties> propertiesList;

    private Map<String, Map<Double, List<String>>> qualTimeMap = null;
    
    private ArrayList<String> flagList;
    
    private Map<String, String> renderCoord;
    
    private EESDataResponse() {
        // for serialization
    }

    public EESDataResponse(HashMap<String,ResponseOptions> options) {
        super(options);
    }

    public EESDataResponse(ArrayList<String> flags) {
        // for serialization
    	this.flagList = flags;
    }
    
    //ASD - overview diagram
    public EESDataResponse(String imageUrl, DesignOptions options, Bounds plotArea, HashMap<String, Axis> tsAxis) {
        this.imageUrl = imageUrl;
        this.begin = options.getBegin();
        this.end = options.getEnd();
        this.width = options.getWidth();
        this.height = options.getHeight();
        this.propertiesList = options.getProperties();
        this.plotArea = plotArea;
        this.tsAxis = tsAxis;
    }

    // main diagram w/o quality control
    public EESDataResponse(String imageUrl, DesignOptions options, Bounds plotArea, ImageEntity[] imageEntities, HashMap<String, Axis> tsAxis,
    		Map<String, Map<Double, List<String>>> qualMap) {
        this.imageUrl = imageUrl;
        this.begin = options.getBegin();
        this.end = options.getEnd();
        this.width = options.getWidth();
        this.height = options.getHeight();
        this.propertiesList = options.getProperties();
        this.imageEntities = imageEntities;
        this.plotArea = plotArea;
        this.tsAxis = tsAxis;
        this.qualTimeMap= qualMap;
    }
    
    //Quality control - main diagram
    public EESDataResponse(String imageUrl, DesignOptions options, Bounds plotArea, ImageEntity[] imageEntities, HashMap<String, Axis> tsAxis,
    		Map<String, Map<Double, List<String>>> qualMap, Map<String, String> r) {
        this.imageUrl = imageUrl;
        this.begin = options.getBegin();
        this.end = options.getEnd();
        this.width = options.getWidth();
        this.height = options.getHeight();
        this.propertiesList = options.getProperties();
        this.imageEntities = imageEntities;
        this.plotArea = plotArea;
        this.tsAxis = tsAxis;
        this.qualTimeMap= qualMap;
        this.renderCoord=r;
    }
    
    public EESDataResponse(String imageUrl, DesignOptions options, Bounds plotArea, ImageEntity[] imageEntities, HashMap<String, Axis> tsAxis,
    		Map<String, Map<Double, List<String>>> qualMap, List<String> timePoint) {
        this.imageUrl = imageUrl;
        this.begin = options.getBegin();
        this.end = options.getEnd();
        this.width = options.getWidth();
        this.height = options.getHeight();
        this.propertiesList = options.getProperties();
        this.imageEntities = imageEntities;
        this.plotArea = plotArea;
        this.tsAxis = tsAxis;
        this.qualTimeMap= qualMap;
    }
    
    public EESDataResponse(String imageUrl, DesignOptions options, Bounds plotArea, ImageEntity[] imageEntities, HashMap<String, Axis> tsAxis) {
       this.imageUrl = imageUrl;
       this.begin = options.getBegin();
       this.end = options.getEnd();
       this.width = options.getWidth();
       this.height = options.getHeight();
       this.propertiesList = options.getProperties();
       this.imageEntities = imageEntities;
       this.plotArea = plotArea;
       this.tsAxis = tsAxis;
   }
   
    
    public void destroy() {
        this.imageEntities = null;
        //ASD
        if( this.propertiesList !=null)
        {
        this.propertiesList.clear();}
        this.propertiesList = null;
        if( this.tsAxis !=null){
        this.tsAxis.clear();}
        this.tsAxis = null;
    }

    public ArrayList<TimeSeriesProperties> getPropertiesList() {
        return this.propertiesList;
    }
    
    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public String getOverviewUrl() {
        return this.overviewurl;
    }

    public HashMap<String, Axis> getAxis() {
        return this.tsAxis;
    }

    public Bounds getPlotArea() {
        return this.plotArea;
    }

    public ImageEntity[] getImageEntities() {
        return this.imageEntities;
    }
    
    public Map<String, Map<Double, List<String>>> getQualityTimeMap() {
        return this.qualTimeMap;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }
    
    public long getBegin() {
        return this.begin;
    }
    
    public long getEnd() {
        return this.end;
    }

    public ArrayList<String> getFlagsList() {
        return this.flagList;
    }
    
    public Map<String, String> getResponseCoord() {
        return this.renderCoord;
    }

}

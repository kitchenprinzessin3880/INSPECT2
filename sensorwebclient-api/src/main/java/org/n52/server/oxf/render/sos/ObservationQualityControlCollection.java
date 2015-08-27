package org.n52.server.oxf.render.sos;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.oxf.feature.OXFAbstractObservationType;
import org.n52.oxf.feature.OXFFeature;
import org.n52.oxf.feature.OXFFeatureCollection;
import org.n52.oxf.feature.OXFMeasurementType;
import org.n52.oxf.feature.dataTypes.IObservationResult;
import org.n52.oxf.feature.dataTypes.OXFPhenomenonPropertyType;
import org.n52.oxf.feature.sos.ObservedValueTuple;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.TimePosition;

public class ObservationQualityControlCollection {

    /**
     * associates a feature-id (String) with a Map<ITimePosition, ObservedValueTuple>.
     */
    private Map<String, Map<ITimePosition, ObservedValueTuple>> featureMap;

    private Set<ITimePosition> timeSet;

    private Comparable[] maximumValues;
    private Comparable[] minimumValues;

    private String[] observedPropertyArray;
    
    public ObservationQualityControlCollection(OXFFeatureCollection observationCollection,
                                       String[] featureIDArray,
                                       String[] observedProperties,
                                       String[] procedureNames,
                                       boolean onlyCompleteTuples) {
        if (observationCollection == null) {
            throw new NullPointerException("No observation given.");
        }
        timeSet = new HashSet<ITimePosition>();
        featureMap = findObservedValueTuples4FOI(observationCollection,
                                                 featureIDArray,
                                                 observedProperties,
                                                 procedureNames,
                                                 onlyCompleteTuples);
    }
    
    /**
     * 
     * @param indexOfObservedProperty
     * @return
     */
    public Comparable getMaximum(int indexOfObservedProperty) {
        return maximumValues[indexOfObservedProperty];
    }
    public Comparable getMaximum(String observedPropertyName) {
        for (int i=0; i<observedPropertyArray.length; i++) {
            if (observedPropertyArray[i].equals(observedPropertyName)) {
                return maximumValues[i];
            }
        }
        return null;
    }
    
    /**
     * 
     * @param indexOfObservedProperty
     * @return
     */
    public Comparable getMinimum(int indexOfObservedProperty) {
        return minimumValues[indexOfObservedProperty];
    }

    public Comparable getMinimum(String observedPropertyName) {
        for (int i=0; i<observedPropertyArray.length; i++) {
            if (observedPropertyArray[i].equals(observedPropertyName)) {
                return minimumValues[i];
            }
        }
        return null;
    }
    
    /**
     * Gibt sÔøΩmtliche <code>ObservedValueTuple</code> zurÔøΩck. D.h. alle <code>ObservedValueTuple</code>
     * fÔøΩr sÔøΩmtliche FOI-Zeitpunkt-Kombinationen.
     * 
     * @return
     */
    public List<ObservedValueTuple> getAllTuples() {
        List<ObservedValueTuple> res = new ArrayList<ObservedValueTuple>();

        for (String featureID : featureMap.keySet()) {
            Map<ITimePosition, ObservedValueTuple> timeMap = featureMap.get(featureID);
            res.addAll(timeMap.values());
        }

        return res;
    }

    /**
     * gibt zu dem spezifizierten OXFFeature sÔøΩmtliche observedValueTuples zurÔøΩck, und zwar sortiert zu den
     * zugehÔøΩrigen Mess-Zeitpunkten in einer Map.
     * 
     * @param feature
     * @return
     */
    public Map<ITimePosition, ObservedValueTuple> getAllTuples(OXFFeature foi) {
        return featureMap.get(foi.getID());
    }

    /**
     * gibt zu der spezifizierten OXFFeature-ID sÔøΩmtliche observedValueTuples zurÔøΩck, und zwar sortiert zu den
     * zugehÔøΩrigen Mess-Zeitpunkten in einer Map.
     * 
     * @param feature-ID
     * @return
     */
    public Map<ITimePosition, ObservedValueTuple> getAllTuples(String foiID) {
        return featureMap.get(foiID);
    }

    /**
     * Gibt fÔøΩr ein spezifiziertes FOI und einen bestimmten Zeitpunkt das PhÔøΩnomen-Werte-Tupel zurÔøΩck. <br>
     * Entspricht der Funktion: <br>
     * f(featureID, timePos) = phenTuple = (X1, X2, ... Xn) | mit Xi := beobachteter PhÔøΩnomenwert.
     * 
     * @param feature
     * @param timePos
     * @return
     */
    public ObservedValueTuple getTuple(OXFFeature feature, ITimePosition timePos) {
        for (String featureID : featureMap.keySet()) {
            if (feature.getID().equals(featureID)) {
                Map<ITimePosition, ObservedValueTuple> timeTupleMap = featureMap.get(featureID);
                for (ITimePosition timeKey : timeTupleMap.keySet()) {
                    if (timePos.equals(timeKey)) {
                        return timeTupleMap.get(timeKey);
                    }
                }
            }
        }
        return null;
    }

    /**
     * gibt sÔøΩmtliche Zeitpunkte in einem Array zurÔøΩck, fÔøΩr die bei mindestens einem FOI Messwerte fÔøΩr jedes
     * PhÔøΩnomen vorliegen.
     * 
     * @return
     */
    public ITimePosition[] getSortedTimeArray() {
        ITimePosition[] timeArray = new ITimePosition[timeSet.size()];
        timeSet.toArray(timeArray);
        Arrays.sort(timeArray);
        return timeArray;
    }


    
    /**
     * ÔøΩbergeben wird eine <code>observationCollection</code>, die observations fÔøΩr n PhÔøΩnomene enthÔøΩlt.
     * Die Namen dieser PhÔøΩnomene werden in dem Parameter <code>observedPropertyNames</code> angegeben. Dann
     * werden fÔøΩr sÔøΩmtliche FOI, deren ID in dem Parameter <code>featureIDArray</code> spezifiziert wurde,
     * die zugehÔøΩrigen observations gefunden. FÔøΩr jeden Zeitpunkt fÔøΩr den observations fÔøΩr dieses foi
     * existieren werden die zugehÔøΩrigen Messwerte zu den einzelnen PhÔøΩnomenen gefunden und zu Tupeln, sog.
     * <code>ObservedValueTuple</code>, zusammengefasst. <br>
     * <br>!! Falls (onlyCompleteTuples == true): Finden sich fÔøΩr ein FOI bei einem bestimmten Zeitpunkt
     * nicht fÔøΩr jedes PhÔøΩnomen Messwerte, so wird das unvollstÔøΩndig besetzte <code>ObservedValueTuple</code>
     * nicht ins Resultat mit aufgenommen!
     * 
     * @param observationCollection
     * @param featureSet
     * @param observedPropertyNames
     */
    protected Map<String, Map<ITimePosition, ObservedValueTuple>> findObservedValueTuples4FOI(OXFFeatureCollection observationCollection,
                                                                                              String[] featureIDArray,
                                                                                              String[] observedPropertyNames,
                                                                                              String[] procedureNames,
                                                                                              boolean onlyCompleteTuples) {
        Map<String, Map<ITimePosition, ObservedValueTuple>> resultMap = new HashMap<String, Map<ITimePosition, ObservedValueTuple>>();
        
        //
        // firstly, initialize maximum- and minimum-arrays:
        //
        maximumValues = new Comparable[observedPropertyNames.length];
        minimumValues = new Comparable[observedPropertyNames.length];

        //
        // now iterate over features:
        //
        for (String featureID : featureIDArray) {
            Map<String, ObservedValueTuple> tupleMap = new HashMap<String, ObservedValueTuple>();

            for (OXFFeature observation : observationCollection) {

                Object foiAttribute = observation.getAttribute(OXFAbstractObservationType.FEATURE_OF_INTEREST);
                if (foiAttribute != null) {
                    OXFFeature featureOfInterest = (OXFFeature) foiAttribute;

                    if (featureOfInterest.getID().equals(featureID)) {

                        ITimePosition time = (ITimePosition) observation.getAttribute(OXFAbstractObservationType.SAMPLING_TIME);
                        String timeString = time.toISO8601Format();

                        ObservedValueTuple tuple = new ObservedValueTuple(observedPropertyNames.length,
                                                                          observedPropertyNames,
                                                                          time);
                        if (tupleMap.containsKey(timeString)) {
                            tuple = tupleMap.get(timeString);
                        }

                        IObservationResult measureResult = (IObservationResult) observation.getAttribute(OXFMeasurementType.RESULT);
 
                        Object result = measureResult.getValue();

                        OXFPhenomenonPropertyType observedProperty = (OXFPhenomenonPropertyType) observation.getAttribute(OXFAbstractObservationType.OBSERVED_PROPERTY);

                        String procedure = (String)observation.getAttribute(OXFAbstractObservationType.PROCEDURE);

                        for (int i = 0; i < observedPropertyNames.length; i++) {
                            if (observedProperty.getURN().equals(observedPropertyNames[i])) { 
                                //if (procedure.equals(procedureNames[i])){
                                    tuple.setValue(i, result);
                               // }
                            }
                        }
                        // fÔøΩge das Tupel der tupleMap hinzu bzw. ersetze das alte Objekt:
                        tupleMap.put(timeString, tuple);

                    }
                }
            }

            //
            // nachschauen, welche tuple vollstÔøΩndig gesetzt sind:
            //
            Map<ITimePosition, ObservedValueTuple> tupleMap_corrected = new HashMap<ITimePosition, ObservedValueTuple>();
            for (String timeString : tupleMap.keySet()) {
                ObservedValueTuple tuple = tupleMap.get(timeString);

                boolean completeTuple = true;

                // check if all values are setted
                for (int i = 0; i < tuple.dimension(); i++) {
                    if (tuple.getValue(i) == null) {
                        completeTuple = false;
                        break;
                    }
                }

                // falls auch nicht-vollstÔøΩndige Tupel mit aufgenommen werden sollen, wird 'completeTuple'
                // wieder auf 'true' gesetzt:
                if (onlyCompleteTuples == false) {
                    completeTuple = true;
                }

                if (completeTuple) {
                    ITimePosition time = new TimePosition(timeString);

                    //
                    // das tuple darf rein
                    //
                    tupleMap_corrected.put(time, tuple);

                    //
                    // den Zeitpunkt der Menge der timeSet hinzufÔøΩgen, falls noch nicht enthalten:
                    //
                    boolean contained = false;
                    for (ITimePosition timePos : timeSet) {
                        if (timePos.equals(time)) {
                            contained = true;
                            break;
                        }
                    }
                    if ( !contained) {
                        timeSet.add(time);
                    }

                    //
                    // falls der value vom Typ Comparable --> maximum und minimum setzen:
                    //
                    for (int i = 0; i < tuple.dimension(); i++) {
                        if (tuple.getValue(i) instanceof Comparable) {
                            Comparable c = (Comparable) tuple.getValue(i);
                            if (maximumValues[i] == null || c.compareTo(maximumValues[i]) > 0) {
                                maximumValues[i] = c;
                            }
                            if (minimumValues[i] == null || c.compareTo(minimumValues[i]) < 0) {
                                minimumValues[i] = c;
                            }
                        }
                    }
                }
            }
            if (tupleMap_corrected.size() > 0) {
                resultMap.put(featureID, tupleMap_corrected);
            }
        }

        return resultMap;
    }
}
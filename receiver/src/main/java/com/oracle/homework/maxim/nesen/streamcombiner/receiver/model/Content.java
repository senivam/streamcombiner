package com.oracle.homework.maxim.nesen.streamcombiner.receiver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Element of the model which is not marshalled but only holds list of elements to be marshalled
 */
public class Content {

    /**
     * List of elements to be marshaled to JSON output
     * Wrapped by synchronized collection to gain minimal protection from concurrent threads access
     *
     * why not synchronized map or queue? Because here we store only about 100 elements
     * (so that shall work quite quick), XML processing performs direct writes of new objects to list
     * (without any casting or so, we compare only hash codes here)
     * And final JSON marshalling does not require additional adapter to map concurentMap to Data object
     * before marshalling
     */
    private final List<Data> jsonPrepare = Collections.synchronizedList(new ArrayList<Data>());

    public List<Data> getJsonPrepare() {
        return jsonPrepare;
    }

    /**
     * clears the list (marshalling occurs at runtime so sometimes buffer shall be cleared
     */
    public void clearContent() {
        jsonPrepare.clear();
    }

    /**
     * adds new elements to the list or performs sum of amounts for existing elements
     * @param data element to be added or increased (amount)
     */
    public synchronized void processNewData(Data data) {

        if (jsonPrepare.contains(data)) {
            int index = jsonPrepare.indexOf(data);
            final Data existingData = jsonPrepare.get(index);
            existingData.setAmount(existingData.getAmount().add(data.getAmount()));
        } else {
            jsonPrepare.add(data);
        }

    }
}

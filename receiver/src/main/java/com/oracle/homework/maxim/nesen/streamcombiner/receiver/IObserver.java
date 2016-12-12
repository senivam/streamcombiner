package com.oracle.homework.maxim.nesen.streamcombiner.receiver;

import com.oracle.homework.maxim.nesen.streamcombiner.receiver.model.Data;

/**
 * Simple observer interface to provide required functionality for recipients' monitoring
 */
public interface IObserver {

    /**
     * Each client stores parsed (from XML) values of timestamp and amount in shared map. That is the place for that.
     *
     * @param data bean with parsed timestamp and amount  (from XML)
     */
    void addParsedPair(Data data);

    /**
     * Each receiver calls that method when the work is done so observer knows that it's time to finish its own processing
     */
    void wakeup();
}

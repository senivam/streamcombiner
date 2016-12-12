package com.oracle.homework.maxim.nesen.streamcombiner.receiver;

import com.oracle.homework.maxim.nesen.streamcombiner.receiver.xml.XMLReceiver;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * initiates XML recipients (clients)
 */
public class Recipient implements Runnable {
    private final Executor recipientExecutor;
    private final List<String> connections;
    private final IObserver observer;


    public Recipient(List<String> connections) {
        this.recipientExecutor = Executors.newFixedThreadPool(connections.size());
        this.connections = connections;
        this.observer = new Observer(connections.size());
    }

    public void run() {
        //run all recipients
        for (final String connection : connections) {
            recipientExecutor.execute(new XMLReceiver(connection, observer));
        }
        //run observer
        Executors.newSingleThreadExecutor().execute((Runnable) observer);
    }

}

package com.oracle.homework.maxim.nesen.streamcombiner.receiver.xml;

import com.oracle.homework.maxim.nesen.streamcombiner.receiver.IObserver;

import java.io.IOException;
import java.net.Socket;

import javax.xml.stream.XMLStreamException;

/**
 * XML processor class. The class is instantiated per connection.
 */
public class XMLReceiver implements Runnable {

    private final String connection;

    private final IObserver observer;

    public XMLReceiver(String connection, IObserver observer) {
        this.connection = connection;
        this.observer = observer;
    }

    public void run() {
        final String host = connection.contains(":") ? connection.substring(0, connection.indexOf(":")) : connection;
        final int port = connection.contains(":") ? Integer.parseInt(connection.substring(connection.indexOf(":") + 1)) : 80;
        try {
            final Socket connection = new Socket(host, port);
            if (connection.isConnected() && !connection.isInputShutdown()) {
                new XMLParser(observer).parse(connection);
            }
            connection.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } finally {
            observer.wakeup();
        }

        System.out.printf("Thread is over [ %s ]%n", Thread.currentThread());
    }
}

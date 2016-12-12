package com.oracle.homework.maxim.nesen.streamcombiner.sender;

import com.oracle.homework.maxim.nesen.streamcombiner.sender.utils.XMLGenerator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Sender class. Handles incoming requests and starts new thread for each new request
 */
public class Sender implements Runnable {
    private static final Executor senderStreamExecutor = Executors.newCachedThreadPool();
    private int listenPort;
    private final Properties properties;

    public Sender(int listeningPort, Properties prop) {
        this.listenPort = listeningPort;
        this.properties = prop;
    }

    public void run() {
        try {
            final ServerSocket socket = new ServerSocket(listenPort);
            while (true) {
                final Socket connection = socket.accept();
                final Runnable xmlSender = new Runnable() {
                    public void run() {
                        new XMLGenerator(connection, properties);
                        try {
                            connection.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                connection.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                senderStreamExecutor.execute(xmlSender);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

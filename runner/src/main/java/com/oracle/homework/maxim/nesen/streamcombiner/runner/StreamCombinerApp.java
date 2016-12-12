package com.oracle.homework.maxim.nesen.streamcombiner.runner;

import com.oracle.homework.maxim.nesen.streamcombiner.common.MainConstants;
import com.oracle.homework.maxim.nesen.streamcombiner.receiver.Recipient;
import com.oracle.homework.maxim.nesen.streamcombiner.sender.Sender;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * Main class of the application
 *
 * Initiates the application - reads properties from property file and converts them to Properties map.
 * The map is afterwards passed to sender (if local sender is used) for initialization
 */
public class StreamCombinerApp
{

    public static void main( String[] args )
    {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = ClassLoader.getSystemResourceAsStream(MainConstants.PROPERTIES_FILE_NAME);

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            final List<String> connections = new ArrayList<String>();

            System.out.printf(MainConstants.PROPERTIES_DESCRIPTION_TEXT1, MainConstants.PROPERTIES_FILE_NAME);
            System.out.println(MainConstants.PROPERTIES_DESCRIPTION_TEXT2);

            for (final String propertyName :  prop.stringPropertyNames() ) {
                if (propertyName.contains(MainConstants.STREAM_URL_PROPERTY_NAME)) {
                    connections.add(prop.getProperty(propertyName));
                }
                System.out.printf("%s:%s%n", propertyName, prop.getProperty(propertyName));
            }

            System.out.printf(
                MainConstants.APPLICATION_DESCRIPTION_TEXT,
                connections.size(), prop.getProperty(MainConstants.XML_ELEMENTS_AMOUNT_PROPERTY_NAME),
                prop.getProperty(MainConstants.DATE_GENERATION_PROPERTY_NAME));

            System.in.read();
            System.out.println("The work is started please wait...");

            int listeningPort = Integer.parseInt(prop.getProperty(MainConstants.LISTENING_PORT_PROPERTY_NAME));
            final String useSender = prop.getProperty(MainConstants.USE_SENDER_PROPERTY_NAME);

            if (MainConstants.USE_SENDER_PROPERTIES_CHECK_VALUE.equalsIgnoreCase(useSender)) {
                Executors.newSingleThreadExecutor().execute(new Sender(listeningPort, prop));
            }
            Executors.newSingleThreadExecutor().execute(new Recipient(connections));


        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}

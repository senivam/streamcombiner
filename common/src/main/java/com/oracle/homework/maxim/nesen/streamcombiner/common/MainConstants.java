package com.oracle.homework.maxim.nesen.streamcombiner.common;

/**
 * Common texts and string constants for main class
 */
public class MainConstants {
    public final static String PROPERTIES_FILE_NAME = "stream.properties";
    public static final String USE_SENDER_PROPERTY_NAME = "useSender";
    public static final String LISTENING_PORT_PROPERTY_NAME = "listeningPort";
    public static final String DATE_GENERATION_PROPERTY_NAME = "dategenerationdense";
    public static final String XML_ELEMENTS_AMOUNT_PROPERTY_NAME = "amountofgeneratedelements";
    public static final String USE_SENDER_PROPERTIES_CHECK_VALUE = "true";
    public static final String STREAM_URL_PROPERTY_NAME = "stream.url";
    public static final String PROPERTIES_DESCRIPTION_TEXT2 = "Properties file content:";
    public static final String PROPERTIES_DESCRIPTION_TEXT1 = "Properties file %s loaded.%n";
    public static final String APPLICATION_DESCRIPTION_TEXT = "\n\n\n*********************************************\n\nThe application is aimed to combine several incoming XML streams into 1 JSON output. \nConfiguration is taken from properties file.\nThe application is configured for %d incoming XML threads, \neach thread contains %s data elements. \nDate will be changes using %s permille. \nAnd presumably internal XML generator will be used.\n\n***********************************************\n\nPress ENTER to start... \n%n";
}

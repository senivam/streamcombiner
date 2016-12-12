package com.oracle.homework.maxim.nesen.streamcombiner.receiver.xml;

import com.oracle.homework.maxim.nesen.streamcombiner.common.XmlConstants;
import com.oracle.homework.maxim.nesen.streamcombiner.receiver.IObserver;
import com.oracle.homework.maxim.nesen.streamcombiner.receiver.model.Data;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * XML stream parser (StAX based due to huge amount of expected input)
 */
public class XMLParser {

    private final IObserver observer;
    /**
     * local buffer to sum only one same date.
     * When bigger date comes buffer is flushed to common buffer.
     */
    private final Data localBuffer = new Data();

    public XMLParser(IObserver observer) {
        this.observer = observer;
    }

    /**
     * Starts parsing (obtains input stream and parses it)
     *
     * @param connection established connection instance (shall not be closed nor null)
     * @throws XMLStreamException parse failed for some reason
     * @throws IOException failed to obtain stream
     */
    public void parse(Socket connection) throws XMLStreamException, IOException {
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        final XMLStreamReader reader = inputFactory.createXMLStreamReader(connection.getInputStream());
        while (reader.hasNext()) {
            int eventType = reader.next();
            switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    if (XmlConstants.DATA_ELEMENT_NAME.equalsIgnoreCase(reader.getLocalName())) {
                        parseData(reader);
                    }
            }
        }
        flushData();
    }

    /**
     * Parses <data>...</data> XML tag.
     *
     * @param reader instance which is pointed to the beginning of the <data>...</data> tag
     */
    private void parseData(XMLStreamReader reader) throws XMLStreamException {
        String date = null;
        BigDecimal amount = null;
        while (reader.hasNext()) {
            int eventType = reader.next();
            switch (eventType) {
                case XMLStreamReader.START_ELEMENT:
                    final String elementName = reader.getLocalName();
                    if (elementName.equals(XmlConstants.TIMESTAMP_ELEMENT_NAME)) {
                        date = readCharacters(reader);
                    } else if (elementName.equals(XmlConstants.AMOUNT_ELEMENT_NAME)) {
                        amount = readBigDecimal(reader);
                    }
                    break;
                case XMLStreamReader.END_ELEMENT: //we are interested in the end of both amount and timestamp elements than we can fill a line in the Map
                    if ((date != null) && (amount != null)) {
                        processCollectedData(date, amount);
                        return; //presumably when amount and timestamp are found we have nothing to do so we switch to the next <data>..</data> tag (the parent node in that case)
                    }
            }
        }
    }

    /**
     * Obtain string value from an element
     *
     * @param reader reader pointed to start of a text element
     * @return value of the element
     * @throws XMLStreamException something went wrong
     */
    private String readCharacters(XMLStreamReader reader) throws XMLStreamException {
        StringBuilder result = new StringBuilder();
        while (reader.hasNext()) {
            int eventType = reader.next();
            switch (eventType) {
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.CDATA:
                    result.append(reader.getText());
                    break;
                case XMLStreamReader.END_ELEMENT:
                    return result.toString();
            }
        }
        //this shall newer happen. If this occurs there is an error in parsing (implementation error) or OutOfMemory error
        throw new XMLStreamException("Premature end of stream");
    }

    /**
     * BigDecimal representation of a string value obtained from text node.
     *
     * @param reader reader pointed the the start of an element
     * @return BigDecimal value
     * @throws XMLStreamException is thrown even if bad number value is given
     */
    private BigDecimal readBigDecimal(XMLStreamReader reader) throws XMLStreamException {
        final String characters = readCharacters(reader);
        try {
            return new BigDecimal(characters);
        } catch (NumberFormatException e) {
            throw new XMLStreamException(String.format("Invalid number %s", characters));
        }
    }

    private void processCollectedData(String date, BigDecimal amount) {
        if (localBuffer.getTimestamp() == null) {
            setData(date, amount);
        } else if (localBuffer.getTimestamp().equalsIgnoreCase(date)) {
            localBuffer.setAmount(localBuffer.getAmount().add(amount));
        } else {
            flushData();
            setData(date, amount);
        }
    }
    private void flushData() {
        observer.addParsedPair(localBuffer.copy());
        synchronized (observer) {
            observer.notify();
        }
    }
    private void setData(String date, BigDecimal amount) {
        localBuffer.setTimestamp(date);
        localBuffer.setAmount(amount);
    }
}

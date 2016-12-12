package com.oracle.homework.maxim.nesen.streamcombiner.sender.utils;

import com.oracle.homework.maxim.nesen.streamcombiner.common.MainConstants;
import com.oracle.homework.maxim.nesen.streamcombiner.common.SenderConstants;
import com.oracle.homework.maxim.nesen.streamcombiner.common.XmlConstants;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * XML generator class
 * This is hybrid implementation of StAX and JAXB marshalling which uses Stax to begin and end XML stream and JAXB is used to put the repeatedly
 * occured data into it. Hybrid is used because the pure StAX (as I was measuring) is slower than mixed with JAXB. For 2 Mln of XML entries pure
 * StAX works for 6:20 minutes and hybrid works 5:30 minutes.
 *
 * as decorator (beautifier) for the generated stream the IndentingXMLStreamWriter is used. This ensures intendations and \n at the EOL however
 * the generated document is much bigger than without beautifier. For same 2 Mln of entries the difference is 65 MB, where the raw stream is of
 * 165 MB size and the decorated stream is of 230 MB size (however that does not much reflect on generation time).
 */
public class XMLGenerator {


    /**
     * Constructor initializes required properties from property file or assigns defaults if property file is missing or incorrect
     * @param connection where to put generated XML
     * @param properties properties to be checked and used
     */
    public XMLGenerator(Socket connection, Properties properties) {
        try {
            int elements;
            int permille;
            try {
                elements = Integer.parseInt(properties.getProperty(MainConstants.XML_ELEMENTS_AMOUNT_PROPERTY_NAME));
                permille = Integer.parseInt(properties.getProperty(MainConstants.DATE_GENERATION_PROPERTY_NAME));
            } catch (NumberFormatException e) {
                elements = SenderConstants.MAX_XML_ELEMENTS;
                permille = SenderConstants.DIFFERENT_DATES_PERMILLE;
            }
            generateXMLEntities(elements, permille, connection);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

    }

    /**
     * Generates XML to provided socket connection
     * @param numberOfEntities number of entities to be generated
     * @param dencePremille how often date shell be changes (ex. once per 2000 of XML entries)
     * @param connection established connection to put generated XML
     * @throws JAXBException
     * @throws XMLStreamException
     * @throws IOException
     */
    private void generateXMLEntities(int numberOfEntities, Integer dencePremille, Socket connection) throws JAXBException, XMLStreamException, IOException {
        final DynamicJAXBContext
            jaxbContext =
            DynamicJAXBContextFactory.createContextFromXSD(ClassLoader.getSystemResourceAsStream(SenderConstants.XSD_SOURCE), null, null, null);
        final Marshaller marshaller = jaxbContext.createMarshaller();

        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        final XMLOutputFactory xof = XMLOutputFactory.newFactory();
        final XMLStreamWriter xsw = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(connection.getOutputStream()));

        xsw.writeStartDocument();
        xsw.writeStartElement(XmlConstants.START_ELEMENT_NAME);

        generateData(jaxbContext, numberOfEntities, dencePremille == null ? SenderConstants.DIFFERENT_DATES_PERMILLE : dencePremille, marshaller, xsw);
        xsw.writeEndDocument();
        xsw.close();
    }

    /**
     * Generates the main part of the xml - <data><amount>...</amount><timestamp>...</timestamp></data> block
     * @param jaxbContext context to be used for marshalling (generate new entities etc)
     * @param numberOfEntities amount of <data>...</data> blocks to be generated
     * @param dencePremille how often date (timestamp) shall be changes
     * @param marshaller marshall entities to stream
     * @param xsw     StAX writer
     * @throws JAXBException
     * @throws XMLStreamException
     */
    private void generateData(DynamicJAXBContext jaxbContext,
                              int numberOfEntities, int dencePremille,
                              Marshaller marshaller,
                              XMLStreamWriter xsw)
        throws JAXBException, XMLStreamException {
        final DynamicEntity data = jaxbContext.newDynamicEntity(XmlConstants.DATA_ELEMENT_TYPE);
        final JAXBElement<DynamicEntity> dataEntity = new JAXBElement<DynamicEntity>(
            new QName("", XmlConstants.DATA_ELEMENT_NAME), (Class<DynamicEntity>) data.getClass(), data);

        final int differentDatesDence = calculateDiferentDatesDense(dencePremille, numberOfEntities);

        XMLGregorianCalendar entryDate = generateDate();
        if (numberOfEntities > 0) {
            for (int i = 0; i < numberOfEntities; i++) {
                if (i%differentDatesDence == 0) {
                    entryDate = generateDate();
                }
                data.set(XmlConstants.AMOUNT_ELEMENT_NAME, BigDecimal.valueOf(i));
                data.set(XmlConstants.TIMESTAMP_ELEMENT_NAME, entryDate);
                marshaller.marshal(dataEntity, xsw);
            }
        }
    }

    /**
     * generates and converts date - very expensive operation - can slow down generation up to 6 times.
     * I was thinking about doing this by static string (any way that is only generation which is not the matter of the task)
     * however if that method is used whith avare of its performacne issues it still can be used so I've decided not to do that
     * by strings (any way that is not the matter of the task)
     * @return XML date
     */
    private XMLGregorianCalendar generateDate() {
        final GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Counts date change step by provided permille.
     * @param permille permille (from 1 to 1000) - 1 is very often and 1000 is never
     * @param amount number of total entries to be generated
     * @return step between generated entries when date changes
     */
    private int calculateDiferentDatesDense(int permille, int amount) {
        return (int)((double)permille / (double)1000 * amount);
    }
}

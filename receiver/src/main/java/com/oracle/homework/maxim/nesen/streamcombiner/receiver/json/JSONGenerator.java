package com.oracle.homework.maxim.nesen.streamcombiner.receiver.json;

import com.oracle.homework.maxim.nesen.streamcombiner.receiver.model.Content;
import com.oracle.homework.maxim.nesen.streamcombiner.receiver.model.Data;

import org.eclipse.persistence.jaxb.JAXBContext;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBMarshaller;
import org.eclipse.persistence.jaxb.MarshallerProperties;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * JSON generator class / generates JSON from mapped XML objects
 */
public class JSONGenerator {
    private final Content content;
    private final JAXBMarshaller jsonMarshaller;

    public JSONGenerator(Content content) throws JAXBException {
        this.content = content;
        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(MarshallerProperties.MEDIA_TYPE, "application/json");
        properties.put(MarshallerProperties.JSON_INCLUDE_ROOT, true);
        properties.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        properties.put(Marshaller.JAXB_FRAGMENT, true);
        final JAXBContext
            context = (JAXBContext) JAXBContextFactory.createContext(new Class[]{Data.class}, properties);
        jsonMarshaller = context.createMarshaller();
        jsonMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }

    /**
     * Performs JSON marshalling from provided data structure
     * @throws JAXBException something wen wrong
     */
    public void generate() throws JAXBException {

        if (content.getJsonPrepare().isEmpty()) {
            return;
        }
        jsonMarshaller.marshal(content.getJsonPrepare(), System.out);
    }
}

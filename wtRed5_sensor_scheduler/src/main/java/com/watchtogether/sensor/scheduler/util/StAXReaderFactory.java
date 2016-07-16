package com.watchtogether.sensor.scheduler.util;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

public class StAXReaderFactory {

	public static XMLStreamReader2 createReader(InputStream is) {
		XMLInputFactory2 factory = null;
		
		factory  = (XMLInputFactory2)XMLInputFactory2.newInstance();
		factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
		factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
		factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		factory.configureForSpeed();
		
		XMLStreamReader2 reader = null;
		try {
			reader = (XMLStreamReader2)factory.createXMLStreamReader(is);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return reader;
	}
	
}

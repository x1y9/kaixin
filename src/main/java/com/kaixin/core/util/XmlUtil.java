package com.kaixin.core.util;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.InputStream;

public class XmlUtil {

	public static Element loadXmlRoot(Object src) throws Exception
	{
	    return loadXmlRoot(src, false);
	}
	
	public static Element loadXmlRoot(Object src, boolean loadExternalDtd) throws Exception
	{
	    Document doc = loadXml(src, loadExternalDtd);
	    Element root = doc.getDocumentElement(); 
	    return root;
	}

	public static Document loadXml(Object src, boolean loadExternalDtd) throws Exception
	{
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    if (!loadExternalDtd)
	    	dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    Document doc = null;
	    if (src instanceof String)
	    	doc = dBuilder.parse((String)src);
	    else if (src instanceof File)
	    	doc = dBuilder.parse((File)src);
	    else if (src instanceof InputStream)
	    	doc = dBuilder.parse((InputStream)src);
	    else
	    	throw new Exception("xml src not valid");
	    
	    doc.getDocumentElement().normalize();
	    return doc;
	}
	
	public static void saveXml(Document doc, File file, boolean indent) throws TransformerException
	{
		  TransformerFactory transformerFactory = TransformerFactory.newInstance();
		  Transformer transformer = transformerFactory.newTransformer();
		  if (indent)			 
			  transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		  
		  DOMSource source = new DOMSource(doc);
		  StreamResult result =  new StreamResult(file);
		  transformer.transform(source, result);	
	}
	
	public static Attr createAttrib(Document doc, String name, String value)
	{
		Attr attr = doc.createAttribute(name);
		attr.setValue(value);
		return attr;
	}
	
	public static Element createSubElement(Document doc, Element element, String name)
	{
		Element subElement = doc.createElement(name);
		element.appendChild(subElement);
		return subElement;
	}
}

package org.nescent.phylr.lucene;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class NexmlNamespaceContext implements NamespaceContext {
    public String getNamespaceURI(String prefix) {
        if (prefix == null) throw new NullPointerException("Null prefix");
        else if ("xsi".equals(prefix)) return "http://www.w3.org/2001/XMLSchema-instance";
        else if ("nex".equals(prefix)) return "http://www.nexml.org/1.0"; 
        else if ("dc".equals(prefix)) return "http://purl.org/dc/terms/";
        else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
        return XMLConstants.NULL_NS_URI;
    }

	public String getPrefix(String namespaceURI) {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator getPrefixes(String namespaceURI) {
		// TODO Auto-generated method stub
		return null;
	}
}

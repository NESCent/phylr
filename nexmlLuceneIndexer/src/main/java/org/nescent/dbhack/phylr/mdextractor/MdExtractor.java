package org.nescent.dbhack.phylr.mdextractor;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;

import org.w3c.dom.*; 


public class MdExtractor {
	private XPathExpression expr_treeid, expr_otus, expr_abstract, expr_firstname, expr_lastname, 
													expr_citation, expr_datatype, expr_nodes; 

	public MdExtractor() {
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    xpath.setNamespaceContext(new MdNamespaceContext());

 	 //pre-compile XPath expressions
    try { 
    	expr_treeid = xpath.compile("/nex:nexml/nex:trees/nex:tree/@id");
    	expr_otus = xpath.compile("/nex:nexml/nex:otus/nex:otu/@label");
    	expr_abstract = xpath.compile("/nex:nexml/nex:external/phy:study/phy:abstract/text()");
    	expr_citation = xpath.compile("/nex:nexml/nex:external/phy:study/phy:citation/text()");
    	expr_firstname = xpath.compile("/nex:nexml/nex:external/phy:study/phy:author/@firstname"); 
    	expr_lastname = xpath.compile("/nex:nexml/nex:external/phy:study/phy:author/@lastname");
    	expr_datatype = xpath.compile("/nex:nexml/nex:trees/nex:tree/nex:external/phy:analysis/phy:inputmatrix/@datatype");
    	expr_nodes = xpath.compile("/nex:nexml/nex:trees/nex:tree/nex:node"); 
    	//++ 
    } catch (XPathExpressionException e) {
    	e.printStackTrace(); 
    }
   }
   
   public Document getXMLdoc(String filename) {
  	 Document doc = null;
  	 try { 
  	 DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
     domFactory.setNamespaceAware(true); // never forget this!
     DocumentBuilder builder = domFactory.newDocumentBuilder();
     doc = builder.parse(filename);  	 
  	 } catch (Exception e) {
  		 e.printStackTrace(); 
  	 }
  	 return doc; 
   }
   
   public String obtainTreeID(Document doc) {
  	 return obtain_nodevalues(doc, expr_treeid); 
   }
   
   public String obtainOTUs(Document doc) {
  	 return obtain_nodevalues(doc, expr_otus); 
   }
   
   public String obtainAbstract(Document doc) {
  	 return obtain_nodevalues(doc, expr_abstract); 
   }
   
   public String obtainCitation(Document doc) {
  	 return obtain_nodevalues(doc, expr_citation); 
   }

   public String obtainAuthors(Document doc) {
  	 return obtain_nodevalues(doc, expr_firstname) + 
  	        obtain_nodevalues(doc, expr_lastname); 
   }
   
   public String obtainDatatype(Document doc) {
  	 return obtain_nodevalues(doc, expr_datatype); 
   }
   
   public int obtainNodeCount (Document doc) {
   	NodeList nodes = obtain_nodes(doc, expr_nodes); 
   	return nodes.getLength(); 
   }

   private String obtain_nodevalues(Document doc, XPathExpression expr) {
    	NodeList nodes = obtain_nodes(doc, expr); 
    	String res = ""; 
    	int length = nodes.getLength(); 
    	for (int i = 0; i < length; i++) {
    		res += nodes.item(i).getNodeValue() + (i == length-1 ? "" : " "); 
    	}
    	return res;   	 
   }
    
   private NodeList obtain_nodes (Document doc, XPathExpression expr) {
  	 Object result = null;
  	 try {
  		 result = expr.evaluate(doc, XPathConstants.NODESET);
  	 } catch (XPathExpressionException e) {
			e.printStackTrace();
  	 }
     return (NodeList) result;
   }
   
}
package org.nescent.dbhack.phylr.lucene;

/**
 * program to illustrate how MdExtactor and MdNamespaceContext are used
 * 
 */
import org.nescent.dbhack.phylr.mdextractor.MdExtractor;
import org.w3c.dom.Document;


public class Usage {
	
	public static void main(String[] args) {
		String[] files = {"Tree000006"};
		for (String fb : files) {
			String filename = "./data/phylr-nexml/" + fb + ".nexml"; 
			System.out.println("Processing file...." +filename);
			process_file(filename); 
		}
	}
	
	private static void process_file(String filename) {
		prln("Extracting metadata from file '" + filename + "'" ); 
		MdExtractor extractor = new MdExtractor(); 
		Document doc = extractor.getXMLdoc(filename); 
		prln("TreeID: " + extractor.obtainTreeID(doc)); 
		prln("OTUs: " + extractor.obtainOTUs(doc)); 
		prln("Abstract: " + extractor.obtainAbstract(doc)); 
		prln("Citation: " + extractor.obtainCitation(doc)); 
		prln("Authors: " + extractor.obtainAuthors(doc)); 
		prln("Datatype: " + extractor.obtainDatatype(doc)); 
		prln("Node count: " + extractor.obtainNodeCount(doc)); 	
		//++ prln(": " + extractor); 		 
	}

	private static void prln(String str) {
		System.out.println(str);  
	}

}
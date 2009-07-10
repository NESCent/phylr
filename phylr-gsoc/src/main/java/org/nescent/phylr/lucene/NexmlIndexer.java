package org.nescent.phylr.lucene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.NodeList;

public class NexmlIndexer {
	private Map<Predicate, XPathExpression> xpaths;
	private static final String USAGE = "java org.nescent.phylr.lucene.NexmlIndexer [-h] -d <data_dir> -i <index_dir> -m <predicates_mapping_file>";

	private static final String HEADER = "NEXML Lucene Indexer";

	private static final String FOOTER = "For more instructions, please visit http://tinyurl.com/q2pchz";
	
	private static Log log = LogFactory.getLog(NexmlIndexer.class); 
		
	/**
	 * Pring usage of the command line tool, and exit
	 * 
	 * @param options
	 */
	private static void printUsageAndExit(Options options) {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.defaultWidth = 80;
		helpFormatter.printHelp(USAGE, HEADER, options, FOOTER);
		System.exit(0);
	}
	
	@SuppressWarnings("static-access")
	public static Options setupOptions() {
		Options options = new Options();
		options.addOption("m", "mappings", true, "Mappings for CQL predicates to XPaths");
		options.addOption("i", "index", true, "The lucene index directory");
		options.addOption("h", "help", false, "Optional. print usage information");
		options.addOption("d", "data", true, "Directory where NEXML files to be found");
		return options;
	}	
	
	public static void main(String[] args) {
		Options options = setupOptions();
		CommandLineParser cmdParser = new BasicParser();
		
		String index = null;
		String data = null;
		String mappings = null;
		
		try {
			CommandLine cmd = cmdParser.parse(options, args);
			
			if (cmd.hasOption('h')) {
				printUsageAndExit(options);
			}
			
			mappings = cmd.getOptionValue('m');
			index = cmd.getOptionValue('i');
			data = cmd.getOptionValue('d');
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (StringUtils.isBlank(mappings) || StringUtils.isBlank(index) || StringUtils.isBlank(data)) {
			printUsageAndExit(options);
		}
		
		File mappingsFile = new File(mappings);
		if (!mappingsFile.isFile()) {
			System.out.println("Can't open " + mappings);
			printUsageAndExit(options);
		}
		
		File indexDir = new File(index);
		File dataDir = new File(data);
		if (!indexDir.isDirectory()) {
			System.out.println("Can't open " + indexDir);
			printUsageAndExit(options);
		}
		
		if (!dataDir.isDirectory()) {
			System.out.println("Can't open " + dataDir);
			printUsageAndExit(options);
		}
		
		List<Predicate> list;
		try {
			list = NexmlIndexer.readPredicates(new FileReader(mappingsFile));
			NexmlIndexer indexer = new NexmlIndexer(list);
			indexer.index(dataDir, indexDir);
		} catch (FileNotFoundException e) {
			log.error("Can't find " + mappings, e);
		} catch (IOException e) {
			log.error("NexmlIndexer failed to index", e);
		} 
		
	}

	/**
	 * Create a new instance
	 * 
	 * @param fields
	 */
	public NexmlIndexer(List<Predicate> fields) {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(new NexmlNamespaceContext());
		xpaths = new HashMap<Predicate, XPathExpression>();
		for (Predicate field : fields) {
			try {
				xpaths.put(field, xpath.compile(field.getXpath()));
			} catch (XPathExpressionException ex) {
				log.error("Invalid xpath: " + field.getXpath(), ex);
			}
		}
	}

	/**
	 * Read the predicates definitions
	 * 
	 * @param reader
	 * @return
	 */
	public static List<Predicate> readPredicates(Reader reader) {
		List<Predicate> list = new ArrayList<Predicate>();
		LineIterator it = IOUtils.lineIterator(reader);
		while (it.hasNext()) {
			String line = it.nextLine();
			String[] tokens = StringUtils.split(line, '\t');
			Predicate predicate = new Predicate();
			predicate.setField(tokens[0]);
			predicate.setXpath(tokens[2]);
			if ("String".equalsIgnoreCase(tokens[1])) {
				predicate.setDataType(Predicate.DataType.STRING);
			} else if ("Date".equalsIgnoreCase(tokens[1])) {
				predicate.setDataType(Predicate.DataType.DATE);
			} else if ("Int".equalsIgnoreCase(tokens[1])) {
				predicate.setDataType(Predicate.DataType.INTEGER);
			} else {
				predicate.setDataType(Predicate.DataType.NONE);
			}
			list.add(predicate);
		}

		return list;
	}

	/**
	 * Index files in dataDir, and put the index in indexDir
	 * 
	 * @param dataDir
	 * @param indexDir
	 * @throws IOException
	 */
	public void index(File dataDir, File indexDir) throws IOException {
		File[] nexmlfiles = dataDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml") || name.endsWith(".nexml");
			}
		});
		// directory to store you index files
		FSDirectory directory = FSDirectory.getDirectory(indexDir);
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriter writer = new IndexWriter(directory, analyzer, true,
				IndexWriter.MaxFieldLength.LIMITED);

		// create Lucene Document and index tree model object
		for (File file : nexmlfiles) {
			Document document = buildIndex(file);
			try {
				writer.addDocument(document);
			} catch (CorruptIndexException e) {
				log.error(e.getMessage(), e);
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}

		// cleanup
		writer.optimize();
		writer.close();
	}

	/**
	 * Bulid index with data from a file
	 * 
	 * @param file
	 * @return
	 */
	public Document buildIndex(File file) {
		Document document = new Document();
		org.w3c.dom.Document xmlDoc = getXMLDoc(file);
		for (Map.Entry<Predicate, XPathExpression> entry : xpaths.entrySet()) {
			Predicate predicate = entry.getKey();
			XPathExpression xpath = entry.getValue();

			QName qname = null;

			switch (predicate.getDataType()) {
			case STRING:
				qname = XPathConstants.NODESET;
				break;
			case INTEGER:
				qname = XPathConstants.NUMBER;
				break;
			case DATE:
				qname = XPathConstants.NODESET;
				break;
			default:
				qname = XPathConstants.NODESET;
				break;
			}

			Object result = null;
			try {
				result = xpath.evaluate(xmlDoc, qname);
			} catch (XPathExpressionException ex) {
				log.error("Failed to evaluate xpath " + xpath.toString() + " in " + file, ex);
			}

			String value = null;
			Field.Index fieldIndex = null;
			switch (predicate.getDataType()) {
			case STRING:
				fieldIndex = Field.Index.ANALYZED;
				//value = (String) result;
				value = getNodeValues((NodeList)result);
				break;
			case INTEGER:
				fieldIndex = Field.Index.NOT_ANALYZED;
				value = String.valueOf((Double) result);
				value = StringUtils.substringBefore(value, ".");
				break;
			case DATE:
				fieldIndex = Field.Index.NOT_ANALYZED;
				//value = (String) result;
				value = getNodeValues((NodeList)result);
				break;
			default:
				fieldIndex = Field.Index.ANALYZED;
				value = getNodeValues((NodeList)result);
			}
			document.add(new Field(predicate.getField(), value,
					Field.Store.YES, fieldIndex));
		}
		try {
			document.add(new Field("nexml", IOUtils.toString(new FileReader(
					file)), Field.Store.YES, Field.Index.NO));
		} catch (FileNotFoundException e) {
			log.error("Can't find file " + file, e);
		} catch (IOException e) {
			log.error("Failed to add 'nexml' field", e);
		}
		return document;
	}

	/**
	 * Get a concatenated String from a NodeList values
	 * 
	 * @param nodes
	 * @return
	 */
	private String getNodeValues(NodeList nodes) {
		String res = "";
		int length = nodes.getLength();
		for (int i = 0; i < length; i++) {
			res += nodes.item(i).getNodeValue() + (i == length - 1 ? "" : " ");
		}
		return res;
	}

	/**
	 * Parses file and returns an org.w3c.dom.Document object
	 * 
	 * @param file
	 * @return
	 */
	public org.w3c.dom.Document getXMLDoc(File file) {
		org.w3c.dom.Document doc = null;
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			//domFactory.setNamespaceAware(true); 
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			doc = builder.parse(file);
		} catch (Exception e) {
			log.error("Can't parse " + file, e);
		}
		return doc;
	}

}

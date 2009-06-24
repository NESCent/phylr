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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
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

	public NexmlIndexer(List<Predicate> fields) {
		//System.setProperty("javax.xml.xpath.XPathFactory:"+ NamespaceConstant.OBJECT_MODEL_SAXON, "net.sf.saxon.xpath.XPathFactoryImpl");
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(new NexmlNamespaceContext());
		xpaths = new HashMap<Predicate, XPathExpression>();
		for (Predicate field : fields) {
			try {
				xpaths.put(field, xpath.compile(field.getXpath()));
			} catch (XPathExpressionException ex) {
				ex.printStackTrace();
			}
		}
	}

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
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// cleanup
		writer.optimize();
		writer.close();
	}

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
				ex.printStackTrace();
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return document;
	}

	private String getNodeValues(NodeList nodes) {
		String res = "";
		int length = nodes.getLength();
		for (int i = 0; i < length; i++) {
			res += nodes.item(i).getNodeValue() + (i == length - 1 ? "" : " ");
		}
		return res;
	}

	public org.w3c.dom.Document getXMLDoc(File file) {
		org.w3c.dom.Document doc = null;
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			domFactory.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			doc = builder.parse(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}

}
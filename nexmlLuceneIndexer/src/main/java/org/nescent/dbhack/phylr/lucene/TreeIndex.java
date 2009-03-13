package org.nescent.dbhack.phylr.lucene;

/**
 * this program illustrates how we can index a Tree object
 * (in nexml) using Lucene
 * 
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.nescent.dbhack.phylr.mdextractor.MdExtractor;
import org.nescent.dbhack.phylr.model.Tree;


public class TreeIndex {
	
	/**
	 *  directories to retrieve your data files and to 
	 *  store Lucene's indexed files 
	 *  TODO: make the directories configurable
	 */
	private static final String FILE_PATH="./data/phylr-nexml";
	private static final String INDEX_PATH="./data/phylr-index";
	
	/*
	 * main method to index Tree
	 */
	public void index() throws Exception
	{		
		File datadir = new File(FILE_PATH);
		File[] nexmlfiles = datadir.listFiles();
		// directory to store you index files
		FSDirectory directory = FSDirectory.getDirectory(INDEX_PATH);
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriter writer = new IndexWriter(directory, analyzer,
				true, IndexWriter.MaxFieldLength.LIMITED);
		
		// create Lucene Document and index tree model object
		for(int i = 0; i < nexmlfiles.length; i++)
		{
			Tree tree = buildTree(nexmlfiles[i].getPath());
			if (tree == null) continue;
			Document document = buildTreeIndex(tree);
			writer.addDocument(document);
		} // end for
		
		// cleanup
		writer.optimize();
		writer.close();
	}
	/**
	 * Build the Lucent Document object and index the fields
	 * @param tree
	 */
	private Document buildTreeIndex(Tree tree) throws Exception
	{
		// for each nexml file, generate a Tree object with indexable fields
		Document document = new Document();
		document.add(new Field("treeid",tree.getM_treeID(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		document.add(new Field("otus",tree.getM_OTUs(), Field.Store.YES, Field.Index.ANALYZED));
		document.add(new Field("authors",tree.getM_authors(), Field.Store.YES, Field.Index.ANALYZED));
		document.add(new Field("abstract",tree.getM_abstract(), Field.Store.YES, Field.Index.ANALYZED));
		document.add(new Field("datatype",tree.getM_datatype(), Field.Store.YES, Field.Index.ANALYZED));
		document.add(new Field("treesize",tree.getM_nodecount(), Field.Store.YES, Field.Index.NOT_ANALYZED));

		// for keyword search
		String keywords= tree.getM_treeID() + " " + tree.getM_authors() + " " + tree.getM_abstract() + " " + tree.getM_citation();
		document.add(new Field("keywords",keywords,Field.Store.YES,Field.Index.ANALYZED));
		
		//save the nexml content, but do not index
		document.add(new Field("nexml", tree.getM_nexml(), Field.Store.YES, Field.Index.NO));
		
		return document;
	}
	/**
	 * generate a list of tree objects
	 * @return
	 */
	private Tree buildTree(String filename)
	{
		System.out.println("Processing nexml file:::" + filename);
		MdExtractor extractor = new MdExtractor();
		org.w3c.dom.Document doc= extractor.getXMLdoc(filename);
		
		Tree tree = new Tree();
		tree.setM_treeID(extractor.obtainTreeID(doc));
		tree.setM_OTUs(extractor.obtainOTUs(doc));
		tree.setM_abstract(extractor.obtainAbstract(doc));
		tree.setM_citation(extractor.obtainCitation(doc));
		tree.setM_authors(extractor.obtainAuthors(doc));
		tree.setM_datatype(extractor.obtainDatatype(doc));
		tree.setM_nodecount(Integer.toString(extractor.obtainNodeCount(doc)));
		
		// get the content of the nexml file
		String content = readFile(filename);
		tree.setM_nexml(content);
		System.out.println("\n==============================================================");
		System.out.println(tree.toString());
		return tree;
		
	}// end getTrees()
	
	/**
	 * read  in the nexml file and return the content as a string
	 * @param file
	 * @return
	 */
	private String readFile(String filename)
	{
		StringBuffer content = new StringBuffer();
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(filename));
			String s;
			while ((s = reader.readLine()) != null)
			{
				content.append(s + "\n");
			}
		} catch (IOException ex)
		{
			System.out.println("IOException in readFile" + ex.getMessage());
		}
		finally
		{
			if (reader != null) try {reader.close();} catch (Exception ex) {;}
		}
		return content.toString();
	}
}

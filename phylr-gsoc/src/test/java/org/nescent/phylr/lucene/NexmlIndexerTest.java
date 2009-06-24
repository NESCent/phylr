package org.nescent.phylr.lucene;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class NexmlIndexerTest {
	static NexmlIndexer indexer;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		InputStream stream = NexmlIndexerTest.class.getClassLoader().getResourceAsStream("predicates.txt");
		List<Predicate> list = NexmlIndexer.readPredicates(new InputStreamReader(stream));
		indexer = new NexmlIndexer(list);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testIndex() throws IOException {
		String path = NexmlIndexerTest.class.getProtectionDomain()
		.getCodeSource().getLocation().getPath();
		String root = path.substring(0, path.indexOf("target/test-class"));
		File dataDir = new File(root + "data/phylr-nexml");
		File indexDir = new File(root + "data/phylr-index");
		
		indexer.index(dataDir, indexDir);
		
		Directory directory = FSDirectory.getDirectory(indexDir);
		IndexReader indexReader = IndexReader.open(directory);
		int maxDocs = indexReader.maxDoc();
		assertEquals(1, maxDocs);
		assertEquals("T. J. Ayers", indexReader.document(0).get("dc.contributor"));
	}

	@Test
	public void testBuildIndex() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetXMLDoc() {
		fail("Not yet implemented");
	}

}

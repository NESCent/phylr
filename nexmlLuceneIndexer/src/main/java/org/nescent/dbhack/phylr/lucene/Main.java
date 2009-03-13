package org.nescent.dbhack.phylr.lucene;

/**
 * standalone program to read in nexml files from a directory
 * and generated a list of indexable fields using lucene
 * 
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		TreeIndex treeindex = new TreeIndex();
		treeindex.index();
	}

}

/**
 * model object for Tree class
 */
package org.nescent.dbhack.phylr.model;

/**
 * @author lcchan
 *
 */
public class Tree {
	private String m_treeID;
	private String m_OTUs;
	private String m_abstract;
	private String m_citation;
	private String m_authors;
	private String m_datatype;
	private String m_nodecount;
	private String m_nexml;
	
	
	public String toString()
	{
		String s = "\n treeid = " +getM_treeID() +
						   "\n otus = " + getM_OTUs() +
						   "\n abstract = " + getM_abstract() +
						   "\n citation = " + getM_citation() +
						   "\n authors = " + getM_authors() +
						   "\n datatype = " + getM_datatype() +
						   "\n nodecount = " + getM_nodecount() + 
						   "\n nexml =" + getM_nexml();
	
		return s;
	}
	
	public String getM_nexml() {
		return m_nexml;
	}

	public void setM_nexml(String m_nexml) {
		this.m_nexml = m_nexml;
	}

	public String getM_treeID() {
		return m_treeID;
	}

	public void setM_treeID(String m_treeid) {
		m_treeID = m_treeid;
	}

	public String getM_OTUs() {
		return m_OTUs;
	}

	public void setM_OTUs(String us) {
		m_OTUs = us;
	}

	public String getM_abstract() {
		return m_abstract;
	}

	public void setM_abstract(String m_abstract) {
		this.m_abstract = m_abstract;
	}

	public String getM_citation() {
		return m_citation;
	}

	public void setM_citation(String m_citation) {
		this.m_citation = m_citation;
	}

	public String getM_authors() {
		return m_authors;
	}

	public void setM_authors(String m_authors) {
		this.m_authors = m_authors;
	}

	public String getM_datatype() {
		return m_datatype;
	}

	public void setM_datatype(String m_datatype) {
		this.m_datatype = m_datatype;
	}

	public String getM_nodecount() {
		return m_nodecount;
	}

	public void setM_nodecount(String m_nodecount) {
		this.m_nodecount = m_nodecount;
	}
	
	/**
	 * default constructor
	 */
	public Tree() {}
	
	
}
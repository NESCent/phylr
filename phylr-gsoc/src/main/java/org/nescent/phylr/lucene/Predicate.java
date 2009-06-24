package org.nescent.phylr.lucene;

public class Predicate {
	public enum DataType {INTEGER, STRING, DATE, NONE};
	
	private String field;
	private String xpath;
	private DataType dataType;
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getXpath() {
		return xpath;
	}
	public void setXpath(String xpath) {
		this.xpath = xpath;
	}
	public DataType getDataType() {
		return dataType;
	}
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}
}

package org.nescent.phylr.relational;

import gov.loc.www.zing.srw.ExtraDataType;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oclc.os.SRW.Record;

public class BioSQLDCRecordResolver implements RecordResolver {
	static Log log = LogFactory.getLog(BioSQLDCRecordResolver.class);
	static final String LUCENE_SCHEMA_ID = "info:srw/schema/1/LuceneDocument";

	/** Creates a new instance of BasicLuceneRecordResolver */
	public BioSQLDCRecordResolver() {
	}

	public void init(Properties properties) {
	}

	public Record resolve(Object hit, String IdFieldName,
			ExtraDataType extraDataType) {
		Object[] result = (Object[])hit;
		String str = null;
		String xml = (String) result[1]; // it expects a list dc elements
		StringBuffer sb = new StringBuffer(
				"<dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
		sb.append(xml);
		sb.append("</dc>");
		str = sb.toString();

		return new Record(str, "info:srw/schema/1/dc-v1.1");
	}
}

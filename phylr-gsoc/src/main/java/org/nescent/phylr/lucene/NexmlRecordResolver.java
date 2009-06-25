package org.nescent.phylr.lucene;

import gov.loc.www.zing.srw.ExtraDataType;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.oclc.os.SRW.Record;
import org.oclc.os.SRW.Lucene.RecordResolver;


public class NexmlRecordResolver implements RecordResolver {
    static Log log=LogFactory.getLog(DCRecordResolver.class);
    static final String SCHEMA_ID="info:http://www.nexml.org/1.0";
    
	public void init(Properties properties) {
		// TODO Auto-generated method stub		
	}

	public Record resolve(Document doc, String IdFieldName,
			ExtraDataType extraDataType) {
        return new Record(doc.getField("nexml").stringValue(), SCHEMA_ID);
	}

}

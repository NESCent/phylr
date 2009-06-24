package ORG.oclc.os.SRW.Lucene;

import gov.loc.www.zing.srw.ExtraDataType;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import ORG.oclc.os.SRW.Record;

public class DCRecordResolver implements RecordResolver {
    static Log log=LogFactory.getLog(DCRecordResolver.class);
    static final String SCHEMA_ID="info:srw/schema/1/dc-v1.1";
    
	public void init(Properties properties) {
		// TODO Auto-generated method stub
		
	}

	public Record resolve(Document doc, String IdFieldName,
			ExtraDataType extraDataType) {
        StringBuffer sb = new StringBuffer("<dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
        
        sb.append("<dc:creator>");
        sb.append(doc.getField("authors").stringValue()); 
        sb.append("</dc:creator>");
        sb.append("</dc>");
        return new Record(sb.toString(), SCHEMA_ID);
	}

}

package org.nescent.phylr.lucene;

import gov.loc.www.zing.srw.ExtraDataType;

import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.oclc.os.SRW.Record;
import org.oclc.os.SRW.Lucene.RecordResolver;


public class DCRecordResolver implements RecordResolver {
    static Log log=LogFactory.getLog(DCRecordResolver.class);
    static final String SCHEMA_ID="info:srw/schema/1/dc-v1.1";
    
	public void init(Properties properties) {
		// TODO Auto-generated method stub
		
	}

	public Record resolve(Document doc, String IdFieldName,
			ExtraDataType extraDataType) {
        StringBuffer sb = new StringBuffer("<dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
        List fields = doc.getFields();
        for (Object object : fields) {
			Field field = (Field)object;
			if (field.name().startsWith("dc.")) {
				sb.append("<");
				sb.append(field.name().replace('.', ':'));
				sb.append(">");
				sb.append(field.stringValue());
				sb.append("</");
				sb.append(field.name().replace('.', ':'));
				sb.append(">");
			}
		}
        sb.append("</dc>");
        return new Record(sb.toString(), SCHEMA_ID);
	}

}

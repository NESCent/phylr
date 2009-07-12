/**
 * Copyright 2006 OCLC Online Computer Library Center, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * BasicLuceneRecordResolver.java
 *
 * Created on November 1, 2006, 1:40 PM
 */

package org.nescent.phylr.relational;

import gov.loc.www.zing.srw.ExtraDataType;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Properties;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oclc.os.SRW.Record;
import org.oclc.os.SRW.Utilities;


/**
 *
 * @author levan
 */
public class BasicRelationalRecordResolver implements RecordResolver {
    static Log log=LogFactory.getLog(BasicRelationalRecordResolver.class);
    static final String LUCENE_SCHEMA_ID="info:srw/schema/1/LuceneDocument";

    /** Creates a new instance of BasicLuceneRecordResolver */
    public BasicRelationalRecordResolver() {
    }

// Not legal until JDK 6    @Override
    public void init(Properties properties) {
    }

// Not legal until JDK 6    @Override
    public Record resolve(ResultSet result, String IdFieldName, ExtraDataType extraDataType) {
    	/*
        // Enumeration fields=doc.fields(); // lucene 1.4
        Iterator fields=doc.getFields().iterator();
        Field field;
        StringBuffer sb=new StringBuffer("<LuceneDocument xmlns=\"http://www.oclc.org/LuceneDocument\">");
        // while(fields.hasMoreElements()) {
        while(fields.hasNext()) {
            // field=(Field)fields.nextElement();
            field=(Field)fields.next();
            sb.append("<field name=\"").append(field.name()).append("\">");

	    String fieldVal = field.stringValue();
	    int firstNewline = fieldVal.indexOf("\n");
	    log.error("Trimmed: " + fieldVal.substring(0,firstNewline));
	    
	    //            sb.append(fieldVal.substring(firstNewline));
            sb.append("</field>");
        }
        sb.append("</LuceneDocument>");
        return new Record(sb.toString(), LUCENE_SCHEMA_ID);
        */
    	return null;
    }
}

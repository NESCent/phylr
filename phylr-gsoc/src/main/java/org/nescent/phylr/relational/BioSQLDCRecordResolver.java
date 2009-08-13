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
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oclc.os.SRW.Record;

/**
 * 
 * @author djiao
 */
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
		int id = (Integer) result[0];
		String xml = (String) result[1]; // it expects a list dc elements
		StringBuffer sb = new StringBuffer(
				"<dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
		sb.append(xml);
		sb.append("</dc>");
		str = sb.toString();

		return new Record(str, "info:srw/schema/1/dc-v1.1");
	}
}

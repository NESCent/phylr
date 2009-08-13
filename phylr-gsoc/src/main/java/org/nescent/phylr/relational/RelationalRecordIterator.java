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
 * LuceneRecordIterator.java
 *
 * Created on October 20, 2006, 3:03 PM
 */

package org.nescent.phylr.relational;

import gov.loc.www.zing.srw.ExtraDataType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oclc.os.SRW.Record;
import org.oclc.os.SRW.RecordIterator;

/**
 *
 * @author levan
 */
public class RelationalRecordIterator implements RecordIterator {
    static final Log log=LogFactory.getLog(RelationalRecordIterator.class);

    ExtraDataType edt;
    RecordResolver resolver;
    String schemaId;
    List result;
    ListIterator iterator;
    String idFieldName;

    /** Creates a new instance of LuceneRecordIterator */
    public RelationalRecordIterator(List result, String schemaId, ExtraDataType edt, RecordResolver resolver, String idFIeldName) {
        log.debug("schemaId="+schemaId+", result="+result+", resolver="+resolver+", edt="+edt);
        this.schemaId=schemaId;
        this.result=result;
        this.resolver=resolver;
        this.edt=edt;
        this.idFieldName = idFIeldName;
        this.iterator = result.listIterator();
    }

    public void close() {
    }

    public boolean hasNext() {
    	return iterator.hasNext();
    }

    public Object next() throws NoSuchElementException {
        return nextRecord();
    }

    public Record nextRecord() throws NoSuchElementException {
        try {
            return resolver.resolve(iterator.next(), idFieldName, edt);
        } catch(Exception e) {
            log.error(e, e);
            throw new NoSuchElementException(e.getMessage());
        }
    }

	public void remove() {
		// TODO Auto-generated method stub
		
	}
}

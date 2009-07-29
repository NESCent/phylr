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
    int numRecs;
    long whichRec;
    RelationalQueryResult result;
    RecordResolver resolver;
    String schemaId;

    /** Creates a new instance of LuceneRecordIterator */
    public RelationalRecordIterator(long whichRec, String schemaId,
      RelationalQueryResult result, RecordResolver resolver, ExtraDataType edt) {
        log.debug("whichRec="+whichRec+", schemaId="+schemaId+", result="+result+", resolver="+resolver+", edt="+edt);
        this.whichRec=whichRec;
        this.schemaId=schemaId;
        this.result=result;
        this.resolver=resolver;
        this.edt=edt;
    }
    
    public void go(long whichRec) {
    	ResultSet hits = result.hits;
    	try {
			hits.absolute(new Long(whichRec-1).intValue());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void close() {
    	try {
    		if (this.result.hits != null) {
    			if (!this.result.hits.isClosed())
    				this.result.hits.close();
    		}
    	} catch (Exception ex) {
    		log.error("Failed to close ResultSet object: " + ex.getMessage());
    	}
    }

    public boolean hasNext() {
        log.debug("whichRec="+whichRec+", result.getNumberOfRecords()="+result.getNumberOfRecords());
    	try {
			if (result.hits.isAfterLast()) {
				return false;
			} else {
				return true;
			}
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		return true;
    }

    public Object next() throws NoSuchElementException {
        return nextRecord();
    }

    public Record nextRecord() throws NoSuchElementException {
    	ResultSet hits = result.hits;
        try {
        	hits.next();
            return resolver.resolve(hits, result.ldb.idFieldName, edt);
        } catch(Exception e) {
            log.error(e, e);
            log.error("whichRec="+whichRec);
            throw new NoSuchElementException(e.getMessage());
        }
        finally {
            whichRec++;
        }
    }

    public void remove() {
    }
}

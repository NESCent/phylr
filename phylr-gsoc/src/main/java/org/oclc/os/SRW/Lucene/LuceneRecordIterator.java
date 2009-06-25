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

package org.oclc.os.SRW.Lucene;

import gov.loc.www.zing.srw.ExtraDataType;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.oclc.os.SRW.Record;
import org.oclc.os.SRW.RecordIterator;

/**
 *
 * @author levan
 */
public class LuceneRecordIterator implements RecordIterator {
    static final Log log=LogFactory.getLog(LuceneRecordIterator.class);

    ExtraDataType edt;
    int numRecs;
    long whichRec;
    LuceneQueryResult result;
    RecordResolver resolver;
    String schemaId;

    /** Creates a new instance of LuceneRecordIterator */
    public LuceneRecordIterator(long whichRec, String schemaId,
      LuceneQueryResult result, RecordResolver resolver, ExtraDataType edt) {
        log.debug("whichRec="+whichRec+", schemaId="+schemaId+", result="+result+", resolver="+resolver+", edt="+edt);
        this.whichRec=whichRec;
        this.schemaId=schemaId;
        this.result=result;
        this.resolver=resolver;
        this.edt=edt;
    }

    public void close() {
    }

    public boolean hasNext() {
        log.debug("whichRec="+whichRec+", result.getNumberOfRecords()="+result.getNumberOfRecords());
        if(whichRec<=result.getNumberOfRecords())
            return true;
        return false;
    }

    public Object next() throws NoSuchElementException {
        return nextRecord();
    }

    public Record nextRecord() throws NoSuchElementException {
        Hits hits=result.getHits();
        try {
            Document doc=hits.doc((int)whichRec-1);
            log.debug("doc="+doc);
            return resolver.resolve(doc, result.ldb.idFieldName, edt);
        }
        catch(Exception e) {
            log.error(e, e);
            log.error("whichRec="+whichRec+", hits.length()="+hits.length());
            throw new NoSuchElementException(e.getMessage());
        }
        finally {
            whichRec++;
        }
    }

    public void remove() {
    }
}

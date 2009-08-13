package org.nescent.phylr.relational;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import gov.loc.www.zing.srw.ExtraDataType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oclc.os.SRW.QueryResult;
import org.oclc.os.SRW.RecordIterator;

public class RelationalQueryResult extends QueryResult {
    static final Log log=LogFactory.getLog(RelationalQueryResult.class);

    List hits=null;
    SRWRelationalDatabase ldb=null;
    long count = -1;
    RelationalRecordIterator localIterator = null;

    /** Creates a new instance of LuceneQueryResult */
    public RelationalQueryResult() {
    }

    /** Creates a new instance of LuceneQueryResult */
    public RelationalQueryResult(SRWRelationalDatabase ldb, List hits) {
        this.ldb=ldb;
        this.hits=hits;
    }
    
    @Override
    public void close() {
    	super.close();
    }

    @Override
    public long getNumberOfRecords() {
    	if (count != -1)
    		return count;
        if(hits==null) {
        	count = 0;
        } else {
        	count = hits.size();
        }
        return count;
    }

    @Override
    public QueryResult getSortedResult(String sortKeys) {
        return this;
    }
    
    @Override
    public RecordIterator newRecordIterator(long whichRec, int numRecs, String schemaId, ExtraDataType edt) throws InstantiationException {
        log.debug("whichRec="+whichRec+", numRecs="+numRecs+", schemaId="+schemaId+", edt="+edt);
        List sublist = this.hits.subList(new Long(whichRec).intValue()-1, new Long(whichRec + numRecs).intValue()-1);
        return new RelationalRecordIterator(sublist, schemaId, edt, (RecordResolver)ldb.resolvers.get(schemaId), null);
    }
}

package org.nescent.phylr.relational;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.loc.www.zing.srw.ExtraDataType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oclc.os.SRW.QueryResult;
import org.oclc.os.SRW.RecordIterator;

public class RelationalQueryResult extends QueryResult {
    static final Log log=LogFactory.getLog(RelationalQueryResult.class);

    ResultSet hits=null;
    SRWRelationalDatabase ldb=null;
    long count = -1;

    /** Creates a new instance of LuceneQueryResult */
    public RelationalQueryResult() {
    }

    /** Creates a new instance of LuceneQueryResult */
    public RelationalQueryResult(SRWRelationalDatabase ldb, ResultSet hits) {
        this.ldb=ldb;
        this.hits=hits;
    }

    @Override
    public long getNumberOfRecords() {
    	if (count != -1)
    		return count;
        if(hits==null) {
        	count = 0;
        } else {
        	try {
        		hits.last();
        		count = hits.getRow();
        	} catch (SQLException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        }
        return count;
    }

    @Override
    public QueryResult getSortedResult(String sortKeys) {
        return this;
    }

    @Override
    public RecordIterator newRecordIterator(long whichRec, int numRecs,
      String schemaId, ExtraDataType edt) throws InstantiationException {
        log.debug("whichRec="+whichRec+", numRecs="+numRecs+", schemaId="+schemaId+", edt="+edt);
        return new RelationalRecordIterator(whichRec, schemaId, this, (RecordResolver)ldb.resolvers.get(schemaId), edt);
    }
}

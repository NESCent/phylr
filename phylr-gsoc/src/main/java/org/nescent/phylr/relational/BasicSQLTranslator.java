package org.nescent.phylr.relational;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oclc.os.SRW.SRWDiagnostic;
import org.oclc.os.SRW.SortTool;
import org.z3950.zing.cql.CQLNode;

public class BasicSQLTranslator implements CqlQueryTranslator {
    private static Log log= LogFactory.getLog(BasicSQLTranslator.class);

// Not legal until JDK 6    @Override
    public void init(Properties properties, SRWRelationalDatabase ldb) throws InstantiationException {
    }

// Not legal until JDK 6    @Override
    public String makeQuery(CQLNode node, SortTool sortTool) throws SRWDiagnostic {
    	return "";
    }
    
    
}

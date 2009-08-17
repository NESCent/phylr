/*
 * CqlToLuceneQueryTranslator.java
 *
 * Created on October 31, 2006, 11:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.nescent.phylr.relational;

import java.util.Properties;

import org.oclc.os.SRW.SRWDiagnostic;
import org.oclc.os.SRW.SortTool;
import org.z3950.zing.cql.CQLNode;

public interface CqlQueryTranslator {
    void init(Properties properties, SRWRelationalDatabase ldb) throws InstantiationException;

    String makeQuery(CQLNode node, SortTool sortTool) throws SRWDiagnostic;

}

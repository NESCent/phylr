package org.nescent.phylr.relational;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oclc.os.SRW.SRWDiagnostic;
import org.oclc.os.SRW.SortTool;
import org.z3950.zing.cql.CQLAndNode;
import org.z3950.zing.cql.CQLBooleanNode;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLNotNode;
import org.z3950.zing.cql.CQLOrNode;
import org.z3950.zing.cql.CQLTermNode;

public class BioSQLQueryTranslator implements CqlQueryTranslator {
    private static Log log= LogFactory.getLog(BioSQLQueryTranslator.class);
    SRWRelationalDatabase database = null;
    
    private String outSql = "select tree_id, xmlagg(xvalue) from ( " +
    		"SELECT tree_id, xmlforest(tq.value as \"dc:identifier\") as xvalue " +
    		"FROM tree_qualifier_value tq, term te " +
    		"WHERE tq.term_id = te.term_id and te.name='dc.identifier' " +
    		"UNION ALL " +
    		"SELECT tq.tree_id, xmlforest(tq.value as \"dc:title\") as xvalue " +
    		"FROM tree_qualifier_value tq, term te " +
    		"WHERE tq.term_id = te.term_id and te.name='dc.title' " +
    		"UNION ALL " +
    		"SELECT tq.tree_id, xmlforest(tq.value as \"dc:abstract\") as xvalue " +
    		"FROM tree_qualifier_value tq, term te " +
    		"WHERE tq.term_id = te.term_id and te.name='dc.abstract' " +
    		"UNION ALL " +
    		"SELECT tq.tree_id, xmlforest(tq.value as \"dc:contributor\") as xvalue " +
    		"FROM tree_qualifier_value tq, term te " +
    		"WHERE tq.term_id = te.term_id and te.name='dc.contributor' " +
    		") as tab " +
    		"WHERE tree_id in (?) " + 
    		"GROUP BY tree_id";
    
    private String ftSql = "select tq.tree_id from tree_qualifier_value as tq, term as te " +
    		"where to_tsvector('english', value) @@ to_tsquery('english', ?) " +
    		"and tq.term_id = te.term_id and te.name=?";
    
    private String eqSql = "select tq.tree_id from tree_qualifier_value as tq, term as te " +
			"where value=? " +
			"and tq.term_id = te.term_id and te.name=?";    

// Not legal until JDK 6    @Override
    public void init(Properties properties, SRWRelationalDatabase ldb) throws InstantiationException {
    	this.database = ldb;
    }

// Not legal until JDK 6    @Override
    public String makeQuery(CQLNode node, SortTool sortTool) throws SRWDiagnostic {
    	String sql = outSql;
    	StringBuffer sb =new StringBuffer();
    	makeSQLQuery(node, sb);
    	sql = sql.replaceFirst("\\?", sb.toString());
    	return sql;
    }
    
    public void makeSQLQuery(CQLNode node, StringBuffer sb) {
        if(node instanceof CQLBooleanNode) {
            CQLBooleanNode cbn=(CQLBooleanNode)node;
            makeSQLQuery(cbn.left, sb);
            if(node instanceof CQLAndNode)
                sb.append(" INTERSECT ");
            else if(node instanceof CQLNotNode)
                sb.append(" EXCEPT ");
            else if(node instanceof CQLOrNode)
                sb.append(" UNION ");
            else sb.append(" UnknownBoolean("+cbn+") ");
            makeSQLQuery(cbn.right, sb);
        }
        else if(node instanceof CQLTermNode) {
        	String sql = null;
            CQLTermNode ctn=(CQLTermNode)node;
            String index=ctn.getIndex();
            if(index.equals(""))
               index = "dc.title"; // set default field to dc:title

            String term = ctn.getTerm();

            if(ctn.getRelation().getBase().equals("=") ||
              ctn.getRelation().getBase().equals("scr")) {
            	sql = eqSql.replaceFirst("\\?", "'" + term + "'");
            } else {
            	sql = ftSql;
                if(ctn.getRelation().getBase().equals("any")) {
                	sql = sql.replaceFirst("\\?", "'" + StringUtils.join(StringUtils.split(term), " | ") + "'");
                }
                else if(ctn.getRelation().getBase().equals("all")) {
                	sql = sql.replaceFirst("\\?", "'" + StringUtils.join(StringUtils.split(term), " & ") + "'");
                } else if (ctn.getRelation().getBase().equals("exact")) {
                	sql = sql.replaceFirst("\\?", "'" + term + "'");
                } else
                    sql = "Unsupported Relation: "+ctn.getRelation().getBase();
            }
            sql = sql.replaceFirst("\\?", "'" + index + "'");
            sb.append(sql);
        }
        else sb.append("UnknownCQLNode("+node+")");
    	
    }
}

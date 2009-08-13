package org.nescent.phylr.relational;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

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
    Map<String, String> indexMapping = null;
    String outTreeSql = null;
    String outNodeSql = null;
    
    public void init(Properties properties, SRWRelationalDatabase ldb) throws InstantiationException {
    	this.database = ldb;
    	this.indexMapping = new HashMap<String, String>();
    	
        Enumeration enumer=properties.propertyNames();
        while(enumer.hasMoreElements()) {
            String prop=(String)enumer.nextElement();
            if(prop.startsWith("qualifier.")) {
            	indexMapping.put(prop.substring(10), properties.getProperty(prop));
            }
        }
        
        this.outTreeSql = properties.getProperty("SRWRelationalDatabase.query.tree");
        this.outNodeSql = properties.getProperty("SRWRelationalDatabase.query.node");
    }

    public String makeQuery(CQLNode node, SortTool sortTool) throws SRWDiagnostic {
    	StringBuffer sb =new StringBuffer();
    	makeSQLQuery(node, sb);
    	return outTreeSql.replaceFirst("\\?", sb.toString());
    }
    
    public void makeSQLQuery(CQLNode node, StringBuffer sb) throws SRWDiagnostic {
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
        } else if(node instanceof CQLTermNode) {
        	String sql = null;
            CQLTermNode ctn=(CQLTermNode)node;
            String index=ctn.getIndex();
            if(index.equals("") || index.equals("cql.serverChoice")) {
            	index = "dc.title"; // set default field to dc:title
            }

            String term = ctn.getTerm();
            if (!indexMapping.containsKey(index)) {
            	throw new SRWDiagnostic(SRWDiagnostic.UnsupportedIndex, index);
            }
        	sql = indexMapping.get(index);
            if(ctn.getRelation().getBase().equals("=") ||
              ctn.getRelation().getBase().equals("scr")) {
            	sql = sql.replaceFirst("\\?", term);
            } else if(ctn.getRelation().getBase().equals("any")) {
            	sql = sql.replaceFirst("\\?", StringUtils.join(StringUtils.split(term), " | ") );
            } else if(ctn.getRelation().getBase().equals("all")) {
            	sql = sql.replaceFirst("\\?", StringUtils.join(StringUtils.split(term), " & "));
            } else if (ctn.getRelation().getBase().equals("exact")) {
            	sql = sql.replaceFirst("\\?", term);
            } else {
            	sql = "Unsupported Relation: "+ctn.getRelation().getBase();
            }
            sb.append(sql);
        }
        else sb.append("UnknownCQLNode("+node+")");
    	
    }
}

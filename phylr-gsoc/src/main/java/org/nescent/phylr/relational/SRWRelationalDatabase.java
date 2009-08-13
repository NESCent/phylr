package org.nescent.phylr.relational;

import gov.loc.www.zing.srw.ExtraDataType;
import gov.loc.www.zing.srw.ScanRequestType;
import gov.loc.www.zing.srw.SearchRetrieveRequestType;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.oclc.os.SRW.QueryResult;
import org.oclc.os.SRW.Record;
import org.oclc.os.SRW.SRWDatabase;
import org.oclc.os.SRW.SRWDiagnostic;
import org.oclc.os.SRW.SortTool;
import org.oclc.os.SRW.TermList;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLTermNode;

public class SRWRelationalDatabase extends SRWDatabase {
	static Log log = LogFactory.getLog(SRWRelationalDatabase.class);

	CqlQueryTranslator translator = null;
	Hashtable<String, RecordResolver> resolvers = new Hashtable<String, RecordResolver>();
	String idFieldName = null, indexInfo = null;

	@Override
	public void addRenderer(String schemaName, String schemaID, Properties props)
			throws InstantiationException {
		RecordResolver resolver = null;
		String resolverName = dbProperties
				.getProperty(schemaName + ".resolver");
		if (resolverName == null) {
			log.debug("creating BasicRelationalResolver");
			resolver = new BioSQLDCRecordResolver();
			resolver.init(props);
		} else {
			try {
				log.debug("creating resolver " + resolverName);
				Class resolverClass = Class.forName(resolverName);
				log.debug("creating instance of class " + resolverClass);
				resolver = (RecordResolver) resolverClass.newInstance();
				resolver.init(dbProperties);
			} catch (Exception e) {
				log.error("Unable to create RecordResolver class "
						+ resolverName + " for database " + dbname);
				log.error(e, e);
				throw new InstantiationException(e.getMessage());
			}
		}
		resolvers.put(schemaName, resolver);
		resolvers.put(schemaID, resolver);
	}

	@Override
	public String getExtraResponseData(QueryResult result,
			SearchRetrieveRequestType request) {
		return null;
	}

	@Override
	public String getIndexInfo() {
		if (indexInfo == null) {
			indexInfo = makeIndexInfo(dbProperties, null);
		}
		return indexInfo;
	}
	
    public static String makeIndexInfo(Properties props, Hashtable<String, String> indexMappings) {
        Hashtable<String, String> sets=new Hashtable<String, String>();
        int             indexNum=0;
        String          index, indexSet, luceneIndex, prop;
        StringBuffer    sb=new StringBuffer("        <indexInfo>\n");
        StringTokenizer st;

        makeUnqualifiedIndexes(props);

        Enumeration enumer=props.propertyNames();
        while(enumer.hasMoreElements()) {
            prop=(String)enumer.nextElement();
            if(prop.startsWith("qualifier.")) {
                st=new StringTokenizer(prop.substring(10));
                index=st.nextToken();
                st=new StringTokenizer(index, ".");
                if(st.countTokens()==1) {
                    indexSet="local";
                    index=prop.substring(10);
                }
                else {
                    indexSet=st.nextToken();
                    index=prop.substring(10+indexSet.length()+1);
                }
                
                if(log.isDebugEnabled())log.debug("indexSet="+indexSet+", index="+index);
                if(sets.get(indexSet)==null) {  // new set
                    sb.append("          <set identifier=\"")
                      .append(props.getProperty("indexSet."+indexSet))
                      .append("\" name=\"").append(indexSet).append("\"/>\n");
                    sets.put(indexSet, indexSet);
                }
                sb.append("          <index>\n")
                  .append("            <title>").append(indexSet).append('.').append(index).append("</title>\n")
                  .append("            <map>\n")
                  .append("              <name set=\"").append(indexSet).append("\">").append(index).append("</name>\n")
                  .append("              </map>\n")
                  .append("            </index>\n");

                if(indexMappings!=null) {
                    // now for a bit of trickery for the CQL parser
                    // the line we just read isn't in the format the parser
                    // expect.  we just read:
                    // qualifier.<indexSet>.indexName=luceneIndexName
                    // the parser is expecting:
                    // qualifier.<indexSet>.indexName=1=<z39.50UseAttribute>
                    // it doesn't really care what Use attribute we provide,
                    // so we'll make up Use attribute numbers to correspond
                    // with the lucene indexes.
                    luceneIndex=props.getProperty(prop);
                    indexMappings.put(indexSet+"."+index, luceneIndex);
                    if(log.isDebugEnabled())
                        log.debug("mapping "+indexSet+"."+index+" to "+luceneIndex);
                    props.put(prop, "1="+(++indexNum));
                }
            }
            else if(prop.startsWith("hiddenQualifier.")) {
                st=new StringTokenizer(prop.substring(16));
                index=st.nextToken();
                if(indexMappings!=null) {
                    // now for a bit of trickery for the CQL parser
                    // the line we just read isn't in the format the parser
                    // expect.  we just read:
                    // qualifier.<indexSet>.indexName=luceneIndexName
                    // the parser is expecting:
                    // qualifier.<indexSet>.indexName=1=<z39.50UseAttribute>
                    // it doesn't really care what Use attribute we provide,
                    // so we'll make up Use attribute numbers to correspond
                    // with the lucene indexes.
                    luceneIndex=props.getProperty(prop);
                    indexMappings.put(index, luceneIndex);
                    if(log.isDebugEnabled())
                        log.debug("mapping "+index+" to "+luceneIndex);
                    props.put(prop, "1="+(++indexNum));
                }
            }
        }
        sb.append("          </indexInfo>\n");
        return sb.toString();
    }
    
    private void setupDriver(String poolName, String connectURI, String username, String passwd) throws ClassNotFoundException, SQLException {
    	ObjectPool connectionPool = new GenericObjectPool(null);
    	ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI,username, passwd);
    	PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
        Class.forName("org.apache.commons.dbcp.PoolingDriver");
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        driver.registerPool(poolName,connectionPool);
    }

	@Override
	public QueryResult getQueryResult(String queryStr,
			SearchRetrieveRequestType request) throws InstantiationException {
		log.debug("entering SRWRelationalDatabase.getQueryResult");
		
		Connection connection = null;
		ResultSet results = null;
		Statement stmt = null;		

		try {
			if (log.isDebugEnabled())
				log.debug("query=" + queryStr);
			CQLNode queryRoot = parser.parse(queryStr);

			String sortKey = request.getSortKeys();
			SortTool sortInfo = null;
			if (sortKey != null && sortKey.length() > 0) {
				log.info("sortKey=" + sortKey);
				sortInfo = new SortTool(sortKey);
			}

			// convert the CQL search to lucene search
			String query = translator.makeQuery(queryRoot, sortInfo);
			log.info("lucene search=" + query);

			// perform search
			connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:" + dbname);
			/*
			stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
			*/
			QueryRunner run = new QueryRunner();
			
			// Create a ResultSetHandler implementation to convert the
			// first row into an Object[].
			String handlerName = dbProperties.getProperty(
					"SRWRelationalDatabase.ResultSetHandler",
					"org.nescent.phylr.relational.BioSQLResultSetHandler");			
			Class resultSetHandler = Class.forName(handlerName);
			log.debug("creating instance of class " + resultSetHandler);
			ResultSetHandler handler = (ResultSetHandler) resultSetHandler.newInstance();			

			//results = stmt.executeQuery(query);
			List result = (List)run.query(connection, query, handler); 
			return new RelationalQueryResult(this, result);
			
		} catch (SRWDiagnostic e) {
			RelationalQueryResult lqr = new RelationalQueryResult();
			lqr.addDiagnostic(e.getCode(), e.getAddInfo());
			return lqr;
		} catch (CQLParseException e) {
			log.error(e, e);
			RelationalQueryResult lqr = new RelationalQueryResult();
			lqr.addDiagnostic(SRWDiagnostic.QuerySyntaxError, e.getMessage());
			return lqr;
		} catch (IOException e) {
			log.error(e, e);
			RelationalQueryResult lqr = new RelationalQueryResult();
			lqr.addDiagnostic(SRWDiagnostic.QuerySyntaxError, e.getMessage());
			return lqr;
		} catch (SQLException e) {
			log.error(e, e);
			RelationalQueryResult lqr = new RelationalQueryResult();
			lqr.addDiagnostic(SRWDiagnostic.GeneralSystemError, e.getMessage());
			return lqr;
		} catch (ClassNotFoundException e) {
			log.error(e, e);
			RelationalQueryResult lqr = new RelationalQueryResult();
			lqr.addDiagnostic(SRWDiagnostic.GeneralSystemError, e.getMessage());
			return lqr;
		} catch (IllegalAccessException e) {
			log.error(e, e);
			RelationalQueryResult lqr = new RelationalQueryResult();
			lqr.addDiagnostic(SRWDiagnostic.GeneralSystemError, e.getMessage());
			return lqr;
		} finally {
			try {
				DbUtils.close(connection);
			} catch (SQLException e) {}
		}
	}

	@Override
	public TermList getTermList(CQLTermNode cqlTermNode, int position,
			int maxTerms, ScanRequestType scanRequestType) {
		log.debug("in getTermList: cqlTermNode=" + cqlTermNode + ", position="
				+ position + ", maxTerms=" + maxTerms);
		TermList list = new TermList();
		if (position > 1) {
			log.debug("unsupported responsePosition=" + position);
			list.addDiagnostic(SRWDiagnostic.ResponsePositionOutOfRange,
					Integer.toString(position));
		} else {
			// TODO: to implement
		}
		return list;
	}

	@Override
	public boolean hasaConfigurationFile() {
		return false; // a configuration file is not required
	}

	@Override
	public void init(String dbname, String srwHome, String dbHome,
			String dbPropertiesFileName, Properties dbProperties)
			throws InstantiationException {
		if (log.isDebugEnabled())
			log.debug("entering SRWRelationalDatabase.init, dbname=" + dbname);

		String xmlSchemaList = dbProperties.getProperty("xmlSchemas");
		if (xmlSchemaList == null) {
			log.info("No schemas specified in SRWDatabase.props ("
					+ dbPropertiesFileName + ")");
			log
					.info("The LuceneDocument schema will be automatically provided");
			dbProperties.put("xmlSchemas", "LuceneDocument");
			dbProperties.put("LuceneDocument.identifier",
					"info:srw/schema/1/LuceneDocument");
			dbProperties
					.put("LuceneDocument.location",
							"http://www.oclc.org/standards/Lucene/schema/LuceneDocument.xsd");
			dbProperties.put("LuceneDocument.namespace",
					"http://www.oclc.org/LuceneDocument");
			dbProperties.put("LuceneDocument.title",
					"Lucene records in their internal format");
		}
		super.initDB(dbname, srwHome, dbHome, dbPropertiesFileName,
				dbProperties);

		maxTerms = 10;
		position = 1;
		
		String url = dbProperties.getProperty("SRWRelationalDatabase.url");		
		String username = dbProperties.getProperty("SRWRelationalDatabase.user");
		String password = dbProperties.getProperty("SRWRelationalDatabase.password");
		String driver = dbProperties.getProperty("SRWRelationalDatabase.driver");
		try {
			Class.forName(driver);
			setupDriver(dbname, url, username, password);
		} catch (ClassNotFoundException e1) {
			log.error("Database driver not found: " + driver);
			throw new InstantiationException("Database driver not found: " + driver);
		} catch (SQLException e) {
			log.error("Can't setup database pool: ", e);
			throw new InstantiationException("Database connection pool can't be initialized");
		}

		log.debug("db url=" + url);

		if (url == null) {
			log.error("Database Connection URL not specified for database " + dbname);
			throw new InstantiationException("Database Connection URL not specified");
		}

		String translatorName = dbProperties.getProperty(
				"SRWRelationalDatabase.CqlToSQLTranslator",
				"org.nescent.phylr.relational.BioSQLQueryTranslator");
		try {
			log.debug("creating translator " + translatorName);
			Class translatorClass = Class.forName(translatorName);
			log.debug("creating instance of class " + translatorClass);
			translator = (CqlQueryTranslator) translatorClass.newInstance();
			translator.init(dbProperties, this);
		} catch (Exception e) {
			log.error("Unable to create CqlToLuceneQueryTranslator class "
					+ translatorName + " for database " + dbname);
			log.error(e, e);
			throw new InstantiationException(e.getMessage());
		}
		if (log.isDebugEnabled())
			log.debug("leaving SRWLuceneDatabase.init");
	}

	/**
	 * Resolves a record from the identifier
	 * 
	 * @param Id
	 *            - identifier
	 * @param extraDataType
	 *            - nonstandard search parameters
	 * @return record if found.
	 */
	public Record resolve(String Id, ExtraDataType extraDataType) {
		// need to create a resolver in the init step from config info
		return null;
	}

	@Override
	public boolean supportsSort() {
		return true;
	}
}
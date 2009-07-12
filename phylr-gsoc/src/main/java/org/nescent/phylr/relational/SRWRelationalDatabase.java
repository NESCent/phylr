package org.nescent.phylr.relational;

import gov.loc.www.zing.srw.ExtraDataType;
import gov.loc.www.zing.srw.ScanRequestType;
import gov.loc.www.zing.srw.SearchRetrieveRequestType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oclc.os.SRW.QueryResult;
import org.oclc.os.SRW.Record;
import org.oclc.os.SRW.SRWDatabase;
import org.oclc.os.SRW.SRWDiagnostic;
import org.oclc.os.SRW.SortTool;
import org.oclc.os.SRW.TermList;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLTermNode;

public class SRWRelationalDatabase extends SRWDatabase {
	static Log log = LogFactory.getLog(SRWRelationalDatabase.class);

	CqlQueryTranslator translator = null;
	// Hashtable indexSynonyms=new Hashtable();
	Hashtable<String, RecordResolver> resolvers = new Hashtable<String, RecordResolver>();
	String idFieldName = null, indexInfo = null;
	
	Connection connection = null;

	@Override
	public void addRenderer(String schemaName, String schemaID, Properties props)
			throws InstantiationException {
		RecordResolver resolver = null;
		String resolverName = dbProperties
				.getProperty(schemaName + ".resolver");
		if (resolverName == null) {
			log.debug("creating BasicRelationalResolver");
			resolver = new BasicRelationalRecordResolver();
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
			// TODO: to implement
		}
		return indexInfo;
	}

	@Override
	public QueryResult getQueryResult(String queryStr,
			SearchRetrieveRequestType request) throws InstantiationException {
		log.debug("entering SRWRelationalDatabase.getQueryResult");
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
			stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
			
			results = stmt.executeQuery(query);
			return new RelationalQueryResult(this, results);
		} catch (SRWDiagnostic e) {
			RelationalQueryResult lqr = new RelationalQueryResult();
			lqr.addDiagnostic(e.getCode(), e.getAddInfo());
			return lqr;
		} catch (Exception e) {
			log.error(e, e);
			RelationalQueryResult lqr = new RelationalQueryResult();
			lqr.addDiagnostic(SRWDiagnostic.GeneralSystemError, e.getMessage());
			return lqr;
		} finally {
			/*
			if (results != null) {
				try { results.close(); } catch (Exception ex) {}  
			}
			if (stmt != null) {
				try { stmt.close(); } catch (Exception ex) {}
			}
			*/
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
		log.debug("db url=" + url);

		if (url == null) {
			log.error("Database Connection URL not specified for database " + dbname);
			throw new InstantiationException("Database Connection URL not specified");
		}

		try {
			connection = DriverManager.getConnection(url);
		} catch (Exception e) {
			log.error("Unable to create connection with url=" + url
					+ " for database " + dbname);
			log.error(e, e);
			throw new InstantiationException(e.getMessage());
		}

		String translatorName = dbProperties.getProperty(
				"SRWRelationalDatabase.CqlToSQLTranslator",
				"org.nescent.phylr.relational.BasicSQLTranslator");
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
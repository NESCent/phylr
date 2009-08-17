package org.nescent.phylr.relational;

import gov.loc.www.zing.srw.ExtraDataType;

import java.util.Properties;

import org.oclc.os.SRW.Record;

/**
 * Interface for resolving records and schemas.
 */
public interface RecordResolver {

    /**
     * Resolves a record from the identifier
     *
     * @param hit - The search result that is trying to be resolved
     * @param IdFieldName - the name of the field that contains the identifier
     * @param extraDataType - nonstandard SRW request parameters
     * @return record if found.
     */
    public Record resolve(Object hit, String IdFieldName, ExtraDataType extraDataType);

    /**
     * Initialize the resolver.
     * 
     * @param properties
     */
    public void init(Properties properties);
}

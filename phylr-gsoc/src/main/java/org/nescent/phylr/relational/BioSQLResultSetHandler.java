package org.nescent.phylr.relational;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;

public class BioSQLResultSetHandler implements ResultSetHandler {

	public List<Object> handle(ResultSet rs) throws SQLException {
		List<Object> result = new ArrayList<Object>();
		
		while (rs.next()) {
			Object[] row = new Object[2];
			row[0] = rs.getInt(1);
			row[1] = rs.getString(2);
			result.add(row);
		}
		return result;
	}

}

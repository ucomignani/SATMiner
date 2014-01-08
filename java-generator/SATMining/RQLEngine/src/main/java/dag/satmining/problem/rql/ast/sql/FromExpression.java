/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author ecoquery
 */
public abstract class FromExpression implements SQLRenderer {

    public void buildSQLQueryNoName(StringBuilder output) {
        buildSQLQuery(output);
    }
    
    public abstract TupleFetcher getTupleFetcher(Connection c) throws SQLException, IOException;
    
}

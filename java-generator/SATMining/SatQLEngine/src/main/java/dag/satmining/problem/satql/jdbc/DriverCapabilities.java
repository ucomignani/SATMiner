package dag.satmining.problem.satql.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public class DriverCapabilities {

    public static boolean supportsNBooleanAtts(Connection c, int required) {
        try {
            String driverName = c.getMetaData().getDriverName();
            if ("Apache Derby Embedded JDBC Driver".equals(driverName)) {
                return required < 1012;
            } else if ("H2 JDBC Driver".equals(driverName)) {
                return required < 60000;
            } else if ("PostgreSQL Native Driver".equals(driverName)) {
                return required < 1650;
            } else if ("SQLite driver".equals(driverName)) {
                return required < 2000;
            } else {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

}

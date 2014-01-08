/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 *
 * @author ecoquery
 */
public class SQLScript {

    private static final Logger LOG = LoggerFactory.getLogger(SQLScript.class);

    // taken from http://stackoverflow.com/questions/1497569/how-to-execute-sql-script-file-using-jdbc
    public static void importSQL(Connection conn, InputStream in) throws SQLException {
        Scanner s = new Scanner(in);
        s.useDelimiter("(;.*(\r)?\n)|(--(\r)?\n)");
        Statement st = null;
        try {
            st = conn.createStatement();
            while (s.hasNext()) {
                String line = s.next();
                if (line.startsWith("/*!") && line.endsWith("*/")) {
                    int i = line.indexOf(' ');
                    line = line.substring(i + 1, line.length() - " */".length());
                }

                if (line.trim().length() > 0) {
                    st.execute(line);
                }
            }
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

    public static void importCSVWithTypes(Connection conn, String tableName, InputStream in) throws IOException, SQLException {
        CSVReader csv = new CSVReader(new InputStreamReader(in));
        String[] data = csv.readNext();
        Statement stat = conn.createStatement();
        int line = 0;
        if (data != null) {
            line++;
            StringBuilder sql = new StringBuilder("CREATE TABLE ");
            sql.append(tableName);
            sql.append("(");
            for (int i = 0; i < data.length; i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(data[i]);
            }
            sql.append(")");
            LOG.info("Creating table: {}",sql.toString());
            stat.addBatch(sql.toString());
            while ((data = csv.readNext()) != null) {
                line++;
                sql.setLength(0);
                sql.append("INSERT INTO ");
                sql.append(tableName);
                sql.append(" VALUES (");
                for (int i = 0; i < data.length; i++) {
                    if (i > 0) {
                        sql.append(", ");
                    }
                    sql.append("'");
                    sql.append(data[i].replace("'", "''")); // escape '
                    sql.append("'");
                }
                sql.append(")");
                try {
                    stat.addBatch(sql.toString());
                } catch (SQLException e) {
                    LOG.error("Error while handling line {}: {}", line, e.getLocalizedMessage());
                    csv.close();
                    throw e;
                }
            }
            stat.executeBatch();
            stat.close();
        }
        csv.close();
        conn.commit();
    }
}
/* ./satmining-utils/src/main/java/dag/satmining/utils/SQLScript.java

   Copyright (C) 2013, 2014 Emmanuel Coquery.

This file is part of SATMiner

SATMiner is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

SATMiner is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with SATMiner; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

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
/* SatQLEngine/src/test/java/dag/satmining/problem/satql/jdbc/DriverCapabilitiesTest.java

   Copyright (C) 2014 Emmanuel Coquery.

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

package dag.satmining.problem.satql.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import org.apache.derby.jdbc.EmbeddedDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class DriverCapabilitiesTest extends TestCase {

    private final static Logger LOG = LoggerFactory
            .getLogger(DriverCapabilitiesTest.class);

    private Connection pgConnect() {
        Driver d = new org.postgresql.Driver();
        try {
            return d.connect(
                    "jdbc:postgresql://localhost/satql?user=satql&password=satql",
                    null);
        } catch (SQLException e) {
            return null;
        }
    }

    public void testPGInfos() throws SQLException {
        Connection c = pgConnect();
        if (c != null) {
            LOG.info("PG driver: {}, version {}", c.getMetaData()
                    .getDriverName(), c.getMetaData().getDriverVersion());
            c.close();
        } else {
            LOG.warn("skipped PG test");
        }
    }

    public void testDerbyInfos() throws SQLException {
        Driver d = new EmbeddedDriver();
        Connection c = d.connect("jdbc:derby:target/test.db", null);
        LOG.info("Derby driver: {}, version {}", c.getMetaData()
                .getDriverName(), c.getMetaData().getDriverVersion());
        c.close();
    }

    public void testH2Infos() throws SQLException {
        Driver d = new org.h2.Driver();
        Connection c = d.connect("jdbc:h2:target/testh2", null);
        LOG.info("H2 driver: {}, version {}", c.getMetaData().getDriverName(),
                c.getMetaData().getDriverVersion());
        c.close();
    }

    public void testSQLiteInfos() throws SQLException {
        Driver d = new org.sqlite.JDBC();
        Connection c = d.connect("jdbc:sqlite:target/test-sqlite",
                new Properties());
        LOG.info("SQLite driver: {}, version {}", c.getMetaData()
                .getDriverName(), c.getMetaData().getDriverVersion());
        c.close();
    }

    private void runBoolTest(Connection c) throws SQLException {
        Statement stat = c.createStatement();
        try {
            stat.execute("CREATE TABLE A(a INTEGER)");
            stat.executeUpdate("INSERT INTO A VALUES(1)");
            ResultSet rs = stat
                    .executeQuery("SELECT CASE WHEN A=1 THEN 1 ELSE 0 END FROM A");
            assertTrue(rs.next());
            assertTrue(rs.getBoolean(1));
        } finally {
            stat.execute("DROP TABLE A");
            c.close();
        }
    }

    public void testH2Bool() throws SQLException {
        Driver d = new org.h2.Driver();
        runBoolTest(d.connect("jdbc:h2:target/testh2", null));
    }

    public void testDerbyBool() throws SQLException {
        Driver d = new EmbeddedDriver();
        runBoolTest(d.connect("jdbc:derby:target/test.db", null));
    }

    public void testSQLiteBool() throws SQLException {
        Driver d = new org.sqlite.JDBC();
        runBoolTest(d.connect("jdbc:sqlite:target/test-sqlite",
                new Properties()));
    }
    
    public void testPGBool() throws SQLException {
        runBoolTest(pgConnect());
    }

    //
    //
    //
    //
    // public void testSQLiteMaxColumnsInQuery() throws SQLException {
    // LOG.info("maxint: {}",Integer.MAX_VALUE);
    // Driver d = new org.sqlite.JDBC();
    // Connection c = d.connect("jdbc:sqlite:target/test-sqlite",new
    // Properties());
    // assertNotNull(c);
    // Statement stat = c.createStatement();
    // try {
    // stat.execute("CREATE TABLE A(a INTEGER)");
    // stat.executeUpdate("INSERT INTO A VALUES(1)");
    // int [] nbatts = {100,1000,2000};
    // String baseAtt = "(CASE WHEN a = 0 THEN 0 ELSE 1 END) AS A";
    // for(int nbatt : nbatts) {
    // LOG.info("Testing for {}",nbatt);
    // StringBuilder sb = new StringBuilder("SELECT ");
    // sb.append(baseAtt).append(0);
    // for(int i = 1; i < nbatt; ++i) {
    // sb.append(',').append(baseAtt).append(i);
    // }
    // sb.append(" FROM A");
    // ResultSet rs = stat.executeQuery(sb.toString());
    // assertTrue(rs.next());
    // LOG.info("Passed for {}",nbatt);
    // }
    // } finally {
    // stat.execute("DROP TABLE A");
    // c.close();
    // }
    // }

}

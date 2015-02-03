/*/Benchmark/src/test/java/org/Benchmark/ConnectionDBTests.java

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

package org.Benchmark;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

/**
*
* @author ucomignani
*/
public class ConnectionDBTests 
    extends TestCase
{
    private final static Logger LOG = LoggerFactory
            .getLogger(ConnectionDBTests.class);
            		
    private Connection pgConnect() {
        Driver d = new org.postgresql.Driver();
        try {
            return d.connect(
                    "jdbc:postgresql://localhost/benchs_satql?user=satql&password=satql",
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
}
/* JDBCConnectionHelper.java

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

package dag.satmining.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCConnectionHelper {

    private static final Logger LOG = LoggerFactory
            .getLogger(JDBCConnectionHelper.class);

    /**
     * Connect using JDBC
     * 
     * @param jars
     *            the list of jar files to load, or null if there is no jar to
     *            load.
     * @param driverClass
     *            the name of the JDBC driver class or null to let the
     *            drivermanager guess.
     * @param jdbcurl
     *            the url of the JDBC connection
     * @return the JDBC connection to the DBMS
     * @throws MalformedURLException
     *             if the jar files do not have the right format
     * @throws ClassNotFoundException
     *             if the driver class cannot be fount
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     *             if a connection error occurs.
     */
    public static Connection connect(String jars, String driverClass, String jdbcurl)
            throws MalformedURLException, InstantiationException,
            IllegalAccessException, ClassNotFoundException, SQLException {
        ClassLoader loader = JDBCConnectionHelper.class.getClassLoader();
        if (jars != null) {
            String[] jarArray = jars.split(":");
            URL[] urlArray = new URL[jarArray.length];
            for (int i = 0; i < jarArray.length; i++) {
                urlArray[i] = new URL("file://" + jarArray[i]);
            }
            loader = URLClassLoader.newInstance(urlArray, loader);
            LOG.info("Loaded jars {}", (Object) urlArray);
        }
        if (driverClass != null) {
            Driver d = (Driver) Class.forName(driverClass, true, loader)
                    .newInstance();
            LOG.info("Loaded driver {}", driverClass);
            return d.connect(jdbcurl, new Properties());
        } else {
            return DriverManager.getConnection(jdbcurl);
        }
    }

}

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

/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/SATQL.java

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
package dag.satmining.problem.satql;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import dag.satmining.constraints.Literal;
import dag.satmining.constraints.mining.Generator;
import dag.satmining.constraints.mining.UsageException;
import dag.satmining.output.Limiter;
import dag.satmining.output.PatternConverter;
import dag.satmining.problem.satql.ast.MiningQuery;
import dag.satmining.problem.satql.ast.sql.BitSetFetcher;
import dag.satmining.problem.satql.ast.sql.SingleStatementBitSetFetcher;
import dag.satmining.problem.satql.parser.ParseException;

/**
 * 
 * @author ecoquery
 */
public class SatQL<L extends Literal<L>> extends Generator<L> implements
        Limiter {

    private static final String NOCACHE_OPT = "nocache";
    private static final String DRIVER_OPT = "driver";
    private static final String JDBC_OPT = "jdbc";
    private MiningQuery<L> _query;
    private Class<L> _litClazz;

    /**
     * Constructor using the class of Literals of internal configuration.
     * 
     * @param clazz
     */
    public SatQL(Class<L> clazz) {
        this._litClazz = clazz;
    }

    @Override
    public PatternConverter getPatternConverter() {
        return _query;
    }

    @Override
    public void configure(Reader inputData, CommandLine opts)
            throws IOException, UsageException {
        try {
            _query = MiningQuery.parse(_litClazz, inputData);
            if (opts.hasOption(NOCACHE_OPT)) {
                _query.enableCache(false);
            }
            if (opts.hasOption(DRIVER_OPT)) {
                // load the driver to enable it in jdbc urls
                Class.forName(opts.getOptionValue(DRIVER_OPT));
            }
            Connection connection = DriverManager.getConnection(opts
                    .getOptionValue(JDBC_OPT));
            BitSetFetcher bsr;
            // Possibilitity to choose BitSetFetcher here
            bsr = new SingleStatementBitSetFetcher(connection);
            _query.setBitSetFetcher(bsr);
            addConstraint(_query);
        } catch (ParseException e) {
            throw new UsageException("Error in mining query: "
                    + e.getLocalizedMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new UsageException(e);
        } catch (SQLException e) {
            throw new UsageException(e);
        }
    }

    @Override
    public String getTitle() {
        return "SATQL SAT runtime";
    }

    @SuppressWarnings("static-access")
    @Override
    public Options getOptions() {
        Options opts = new Options();
        opts.addOption(OptionBuilder.hasArg().withArgName("url").isRequired()
                .withDescription("JDBC connection url").create(JDBC_OPT));
        opts.addOption(OptionBuilder
                .hasArg()
                .withArgName("className")
                .withDescription(
                        "JDBC driver class, may be required for driver loading")
                .create(DRIVER_OPT));
        opts.addOption(OptionBuilder
                .withDescription(
                        "disable cache when producing SAT formula (use only for benchmark purpose)")
                .create(NOCACHE_OPT));
        return opts;
    }

    @Override
    public long getLimit() {
        return _query.getLimit();
    }

}

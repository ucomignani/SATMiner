/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql;

import dag.satmining.constraints.Literal;
import dag.satmining.constraints.mining.Generator;
import dag.satmining.constraints.mining.UsageException;
import dag.satmining.output.PatternConverter;
import dag.satmining.problem.rql.ast.MiningQuery;
import dag.satmining.problem.rql.ast.sql.BitSetFetcher;
import dag.satmining.problem.rql.ast.sql.InMemoryNestedLoopBitSetFetcher;
import dag.satmining.problem.rql.ast.sql.NestedLoopBitSetFetcher;
import dag.satmining.problem.rql.ast.sql.SingleStatementBitSetFetcher;
import dag.satmining.problem.rql.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * 
 * @author ecoquery
 */
public class RQL<L extends Literal<L>> extends Generator<L> {

	private static final String NESTEDLOOP_MEM_OPT = "memnloop";
	private static final String NESTEDLOOP_OPT = "nloop";
	private static final String DRIVER_OPT = "driver";
	private static final String JDBC_OPT = "jdbc";
	private MiningQuery<L> _query;
	private Class<L> _litClazz;

	/**
	 * Constructor using the class of Literals of internal configuration.
	 * 
	 * @param clazz
	 */
	public RQL(Class<L> clazz) {
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
			if (opts.hasOption(DRIVER_OPT)) {
				// load the driver to enable it in jdbc urls
				Class.forName(opts.getOptionValue(DRIVER_OPT));
			}
			Connection connection = DriverManager.getConnection(opts
					.getOptionValue(JDBC_OPT));
			BitSetFetcher bsr;
			if (opts.hasOption(NESTEDLOOP_OPT)) {
				bsr = new NestedLoopBitSetFetcher(connection);
			} else if (opts.hasOption(NESTEDLOOP_MEM_OPT)) {
				bsr = new InMemoryNestedLoopBitSetFetcher(connection);
			} else {
				bsr = new SingleStatementBitSetFetcher(connection);
			}
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
		return "RQL SAT runtime";
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
		opts.addOption(OptionBuilder.withDescription(
				"use Java based cartesian product for tuples").create(
				NESTEDLOOP_OPT));
		opts.addOption(OptionBuilder
				.withDescription(
						"use Java based cartesian product for tuples, keeping each relation in memory")
				.create(NESTEDLOOP_MEM_OPT));
		return opts;
	}

}

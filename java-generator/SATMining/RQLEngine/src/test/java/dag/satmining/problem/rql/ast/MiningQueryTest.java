/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.sat4j.SAT4JPBBuilder;
import dag.satmining.problem.rql.ast.sql.InMemoryNestedLoopBitSetFetcher;
import dag.satmining.problem.rql.ast.sql.NestedLoopBitSetFetcher;
import dag.satmining.problem.rql.ast.sql.SingleStatementBitSetFetcher;
import dag.satmining.problem.rql.parser.ParseException;
import dag.satmining.utils.SQLScript;

/**
 * 
 * @author ecoquery
 */
public class MiningQueryTest extends TestCase {

	private static final Logger LOG = LoggerFactory
			.getLogger(MiningExpressionTest.class);

	private static final String DB_FILE = "target/test.db";
	Connection c;
	EmbeddedDataSource ds;
	SAT4JPBBuilder sat4jHandler;

	public MiningQueryTest() throws SQLException {
		ds = new EmbeddedDataSource();
		ds.setDatabaseName(DB_FILE);
		ds.setConnectionAttributes("create=true");
	}

	@Override
	protected void setUp() throws Exception {
		c = ds.getConnection();
		Statement stat = c.createStatement();
		String checkTable = "SELECT * FROM SYS.SYSTABLES WHERE TABLENAME = 'R'";
		ResultSet rs = stat.executeQuery(checkTable);
		if (!rs.next()) {
			SQLScript.importCSVWithTypes(c, "R", getClass()
					.getResourceAsStream("/funct_deps.csv"));
		}
		stat.close();
	}

	@Override
	protected void tearDown() throws Exception {
		c.close();
	}

	public void testFunctionnalDependenciesSSBF() throws ParseException,
			NoSolutionException {
		MiningQuery<DimacsLiteral> query = MiningQuery.parse(
				DimacsLiteral.class, new InputStreamReader(getClass()
						.getResourceAsStream("/funct_deps.rql")));
		query.setBitSetFetcher(new SingleStatementBitSetFetcher(c));
		LOG.debug("before parser");
		sat4jHandler = new SAT4JPBBuilder(SAT4JPBBuilder.SMALL);
		LOG.debug("made parser");
		query.addClauses(sat4jHandler);
		LOG.debug("added clauses");
		sat4jHandler.endProblem();
		int nbModels = 0;
		while (sat4jHandler.getNext()) {
			LOG.debug("found: {}",
					query.getPattern(sat4jHandler.getCurrentInterpretation()));
			nbModels++;
		}
		assertEquals(64, nbModels);
	}

	public void testFunctionnalDependenciesNLBF() throws ParseException,
			NoSolutionException {
		MiningQuery<DimacsLiteral> query = MiningQuery.parse(
				DimacsLiteral.class, new InputStreamReader(getClass()
						.getResourceAsStream("/funct_deps.rql")));
		query.setBitSetFetcher(new NestedLoopBitSetFetcher(c));
		sat4jHandler = new SAT4JPBBuilder(SAT4JPBBuilder.SMALL);
		query.addClauses(sat4jHandler);
		sat4jHandler.endProblem();
		int nbModels = 0;
		while (sat4jHandler.getNext()) {
			LOG.debug("found: {}",
					query.getPattern(sat4jHandler.getCurrentInterpretation()));
			nbModels++;
		}
		assertEquals(64, nbModels);
	}

	public void testFunctionnalDependenciesIMNLBF() throws ParseException,
			NoSolutionException {
		MiningQuery<DimacsLiteral> query = MiningQuery.parse(DimacsLiteral.class,new InputStreamReader(getClass()
				.getResourceAsStream("/funct_deps.rql")));
		query.setBitSetFetcher(new InMemoryNestedLoopBitSetFetcher(c));
		sat4jHandler = new SAT4JPBBuilder(SAT4JPBBuilder.SMALL);
		query.addClauses(sat4jHandler);
		sat4jHandler.endProblem();
		int nbModels = 0;
		while(sat4jHandler.getNext()) {
			LOG.debug("found: {}", query.getPattern(sat4jHandler.getCurrentInterpretation()));
			nbModels++;
		}
		assertEquals(64, nbModels);
	}

	public void testFunctionnalDependenciesMinXSingY() throws ParseException,
			NoSolutionException {
		MiningQuery<DimacsLiteral> query = MiningQuery.parse(DimacsLiteral.class, new InputStreamReader(getClass()
				.getResourceAsStream("/funct_deps_min_x_singleton_y.rql")));
		query.setBitSetFetcher(new InMemoryNestedLoopBitSetFetcher(c));
		sat4jHandler = new SAT4JPBBuilder(SAT4JPBBuilder.SMALL);
		query.addClauses(sat4jHandler);
		sat4jHandler.endProblem();
		int nbModels = 0;
		while(sat4jHandler.getNext()) {
			LOG.debug("found: {}", query.getPattern(sat4jHandler.getCurrentInterpretation()));
			nbModels++;
		}
		assertEquals(13, nbModels);
	}

	public void testFunctionnalDependenciesWithSingleton()
			throws ParseException, NoSolutionException {
		MiningQuery<DimacsLiteral> query = MiningQuery.parse(DimacsLiteral.class, new InputStreamReader(getClass()
				.getResourceAsStream("/funct_deps_y_singleton.rql")));
		query.setBitSetFetcher(new SingleStatementBitSetFetcher(c));
		sat4jHandler = new SAT4JPBBuilder(SAT4JPBBuilder.SMALL);
		query.addClauses(sat4jHandler);
		sat4jHandler.endProblem();
		int nbModels = 0;
		while(sat4jHandler.getNext()) {
			LOG.debug("found: {}", query.getPattern(sat4jHandler.getCurrentInterpretation()));
			nbModels++;
		}
		assertEquals(39, nbModels);
	}
}

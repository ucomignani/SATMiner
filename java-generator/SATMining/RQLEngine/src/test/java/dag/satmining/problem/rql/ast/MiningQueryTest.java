/* ./RQLEngine/src/test/java/dag/satmining/problem/rql/ast/MiningQueryTest.java

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

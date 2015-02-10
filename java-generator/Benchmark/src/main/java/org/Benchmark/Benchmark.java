/* /Benchmark/src/main/java/org/Benchmark/Benchmark.java

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.log4j.BasicConfigurator;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.sat4j.SAT4JPBBuilder;
import dag.satmining.backend.sat4j.SAT4JPBBuilderPRAND;
import dag.satmining.problem.satql.ast.MiningQuery;
import dag.satmining.problem.satql.ast.sql.SingleStatementBitSetFetcher;
import dag.satmining.problem.satql.parser.ParseException;
import dag.satmining.utils.SQLScript;

/**
 *
 * @author ucomignani
 */
public class Benchmark {
	
	private static final String DB_FILE = "target/testDiversificationAbalo.db";
	Connection c;
	EmbeddedDataSource ds;
	static SAT4JPBBuilder sat4jHandler;

	public Benchmark(SAT4JPBBuilder sat4jHandler){
		this.sat4jHandler = sat4jHandler;
		try {
			setUp();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	public void bench(String fichierSatQL){
		Connection connection = this.c;
		if(connection == null)
		{
			System.out.println("Connection a la base echouee");
			System.exit(1);
		}
		else 
		{
			System.out.println("Connection OK");
		}

		String resultatSatQL = "";


		// LOG de l'existence du fichier
		File f = new File( System.getProperty("user.dir")+fichierSatQL);
	    System.out.println("Working Directory = " + System.getProperty("user.dir"));
		if(!f.exists()) { System.out.println("Erreur, le fichier " + fichierSatQL + " n'existe pas. "); System.exit(1); }
		else {System.out.println(f.toString());}
		
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(f);	
		} catch (FileNotFoundException e) {
			System.out.println("Erreur au niveau de l'InputStream du fichier SatQL: " + e.toString());
			System.exit(1);
		}
		MiningQuery<DimacsLiteral> query = initQuery(connection, inputStream);
		
		int nbModels = 0;
		while (sat4jHandler.getNext()) {
			resultatSatQL = query.getPatternForBench(sat4jHandler.getCurrentInterpretation()).toString();

			System.out.println("found: "+ resultatSatQL);

			nbModels++;
		}	
		
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private MiningQuery<DimacsLiteral> initQuery(Connection connection, InputStream inputStream) {
		MiningQuery<DimacsLiteral> query = null;

		try {
			query = MiningQuery.parse(
					DimacsLiteral.class, new InputStreamReader(inputStream));
			query.setBitSetFetcher(new SingleStatementBitSetFetcher(connection));
			query.addClauses(sat4jHandler);
			sat4jHandler.endProblem();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSolutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return query;
	}

	protected void setUp() throws Exception {
		ds = new EmbeddedDataSource();
		ds.setDatabaseName(DB_FILE);
		ds.setConnectionAttributes("create=true");
		c = ds.getConnection();

		// abalone
		Statement stat = c.createStatement();
		String checkTable = "SELECT * FROM SYS.SYSTABLES WHERE TABLENAME = 'ABALONE'";
		ResultSet rs = stat.executeQuery(checkTable);
		if (!rs.next()) {
			String fichierBdD = System.getProperty("user.dir") + "/abalone.csv";
	        System.out.println(fichierBdD);
			InputStream inputStream = new FileInputStream(new File(fichierBdD));

			SQLScript.importCSVWithTypes(c, "abalone", inputStream);
		}

		stat.close();
		
		stat = c.createStatement();
		checkTable = "SELECT * FROM SYS.SYSTABLES WHERE TABLENAME = 'FUNCTDEPS'";
		rs = stat.executeQuery(checkTable);
		if (!rs.next()) {
			String fichierBdD = System.getProperty("user.dir") + "/functDeps.csv";
	        System.out.println(fichierBdD);
			InputStream inputStream = new FileInputStream(new File(fichierBdD));

			SQLScript.importCSVWithTypes(c, "FunctDeps", inputStream);
		}

		stat.close();
	}
}
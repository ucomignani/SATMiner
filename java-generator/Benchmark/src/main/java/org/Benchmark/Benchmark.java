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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.apache.log4j.BasicConfigurator;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.sat4j.SAT4JPBBuilderPRAND;
import dag.satmining.problem.satql.ast.MiningQuery;
import dag.satmining.problem.satql.ast.sql.SingleStatementBitSetFetcher;
import dag.satmining.problem.satql.parser.ParseException;

/**
*
* @author ucomignani
*/
public class Benchmark {
	private static final String DB_FILE = "target/test.db";

	EmbeddedDataSource ds;
	static SAT4JPBBuilderPRAND sat4jHandlerPRAND;

	
	//TODO ecrire bench performances
	public void benchPerformances(int nbIterations){
		
		Connection connection = null;      
		connection = pgConnect();	
		
		for(int l=0; l<nbIterations; l++){
			/*
			 * debut test
			 */
			
			testPRAND(connection);
			
			/*
			 * fin test
			 */	
		}
        
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//TODO ecrire bench qualite
	public void benchQualite(int nbIterations){
		PrintWriter writer = null;
		File fichier = null;
		try{
			fichier = new File("bench_" + System.currentTimeMillis() + ".txt");
			writer = new PrintWriter(fichier, "UTF-8");
		}catch(FileNotFoundException e){
			System.out.println("not found");
		}catch(UnsupportedEncodingException e){
			System.out.println("encoding error");
		}
		for(int l=0; l<nbIterations; l++){
			// fonction a evaluer
			writer.println("test");
		}
		
		writer.close();
	}
	
	private void testPRAND(Connection connection){
		Statement st = initStatement(connection);
        ResultSet rs = null;
        String querySQL = null;
        
		float debut = 0;
		float fin = 0;
		float tpsExec = -1;
		
        InputStream inputStream = initInputStream("/quantifier_percent.satql");		
		MiningQuery<DimacsLiteral> query = initQuery(connection, inputStream);
			
		/*
		 * debut mesure
		 */
		int nbModels = 0;
		float tpsDepartExec = System.nanoTime();

		debut = System.nanoTime();
		while (sat4jHandlerPRAND.getNext()) {
	
			System.out.println("found: {}"+
					query.getPattern(sat4jHandlerPRAND.getCurrentInterpretation()));
			
			nbModels++;
		}
		fin = System.nanoTime();

		/*
		 * fin mesure
		 */
		try {
			tpsExec = ((float) (fin-debut)) / 1000f;
			querySQL = "INSERT INTO bench_diversification(id_exec, id_model, algo_utilise, temps_exec) VALUES (" + tpsDepartExec + "," + nbModels+ ", 'PRAND' " +","+ Float.toString(tpsExec) + ");" ;
			rs = st.executeQuery(querySQL);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
    private InputStream initInputStream(String satqlFileDir) {
    	return getClass().getResourceAsStream(satqlFileDir);
	}

	private MiningQuery<DimacsLiteral> initQuery(Connection connection, InputStream inputStream) {
    	MiningQuery<DimacsLiteral> query = null;
    	
    	try {
			query = MiningQuery.parse(
					DimacsLiteral.class, new InputStreamReader(inputStream));
			query.setBitSetFetcher(new SingleStatementBitSetFetcher(connection));
			sat4jHandlerPRAND = new SAT4JPBBuilderPRAND(SAT4JPBBuilderPRAND.SMALL);
			query.addClauses(sat4jHandlerPRAND);
			sat4jHandlerPRAND.endProblem();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSolutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return query;
	}

	private Statement initStatement(Connection connection) {
    	Statement st = null;
    	
    	try {
			st = connection.createStatement();	        
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
    	
    	return st;
	}

	private static Connection pgConnect() {
        Driver d = new org.postgresql.Driver();
        try {
            return d.connect(
                    "jdbc:postgresql://localhost/benchs_satql?user=satql&password=satql",
                    null);
        } catch (SQLException e) {
            return null;
        }
    }
}
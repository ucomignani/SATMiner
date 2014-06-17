/* ./SATMiner/src/test/java/dag/satmining/problem/seq/CompareSeqImplTest.java

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
package dag.satmining.problem.seq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.backend.ExternalSolverModelReader;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.dimacs.FileDimacsBackend;
import dag.satmining.backend.minisat.MinisatModelReader;
import dag.satmining.backend.pb.gen.CardNetworksPBFactory;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
import dag.satmining.constraints.mining.Generator;
import dag.satmining.constraints.mining.UsageException;
import dag.satmining.output.FileModelOutput;
import dag.satmining.output.GeneratorOutput;

/**
 * 
 * @author ecoquery
 */
public class CompareSeqImplTest extends TestCase {

	private static final Logger LOG = LoggerFactory
			.getLogger(CompareSeqImplTest.class);

	private Set<String> runOnInput(String data, int maxSize, int minFreq,
			Generator<DimacsLiteral> gen) throws IOException,
			NoSolutionException, ParseException, UsageException {
		String[] args = { "-m", String.valueOf(maxSize), "-f",
				String.valueOf(minFreq) };
		gen.configure(new StringReader(data),
				new BasicParser().parse(gen.getOptions(), args));
		File cnfFile = File.createTempFile("satminer_junit_", ".cnf");
		cnfFile.deleteOnExit();
		LOG.info("Output CNF to {}", cnfFile);
		ReifiedWeightedPBBuilder<DimacsLiteral> handler = new CardNetworksPBFactory<DimacsLiteral>(
				new FileDimacsBackend(cnfFile));
		File rawOutput = File.createTempFile("satminer_junit_seq_", ".txt");
		LOG.info("Output patterns to {}", rawOutput);
		GeneratorOutput output = new FileModelOutput(gen.getPatternConverter(),
				new ExternalSolverModelReader(new MinisatModelReader(),
						cnfFile, "./minisat.sh", "#in", "-o", "#out"),
				rawOutput);
		gen.buildModel(handler);
		handler.endProblem();
		output.writeOutput();
		Set<String> lines = new HashSet<String>();
		BufferedReader rawInput = new BufferedReader(new FileReader(rawOutput));
		String line = rawInput.readLine();
		while (line != null) {
			lines.add(line);
			line = rawInput.readLine();
		}
		rawInput.close();
		return lines;
	}

	private void checkSameOutput(String data, int maxSize, int minFreq)
			throws IOException, NoSolutionException, ParseException,
			UsageException {
		Generator<DimacsLiteral> gen1 = new SequenceMiningGenerator1<DimacsLiteral>();
		Set<String> res1 = runOnInput(data, maxSize, minFreq, gen1);
		Generator<DimacsLiteral> gen2 = GSProblem.newProblem(
				DimacsLiteral.class, GSProblem.Predefined.Char);
		Set<String> res2 = runOnInput(data, maxSize, minFreq, gen2);
		LOG.debug("res1: {} results",res1.size());
		LOG.debug("res2: {} results",res2.size());
		assertTrue(res1.containsAll(res2));
		assertTrue(res2.containsAll(res1));
	}

	public void testSeq1() throws IOException, NoSolutionException,
			ParseException, UsageException {
		checkSameOutput("abcabcabc", 5, 3);
	}

	public void testSeqEmpty() throws IOException, NoSolutionException,
			ParseException, UsageException {
		checkSameOutput("abcdefg", 7, 2);
	}

	public void testSeqFull() throws IOException, NoSolutionException,
			ParseException, UsageException {
		checkSameOutput("abcdefg", 7, 1);
	}
}

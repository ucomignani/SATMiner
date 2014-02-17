/* ./SATMiner/src/test/java/dag/satmining/OutputTest.java

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

package dag.satmining;

import junit.framework.TestCase;
import boolvar.model.Variable;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.dimacs.FileDimacsBackend;
import dag.satmining.backend.pb.gen.CardNetworksPBFactory;
import dag.satmining.backend.sat4j.SAT4JPBBuilder;
import dag.satmining.run.Main;

public class OutputTest extends TestCase {

	@Override
	protected final void setUp() throws Exception {
		Variable.setUsed(0);
	}

	public final void testMainWithGeneratedData() throws Exception {
		String[] args = { "-f", "5", "-m", "3", "-genseq", "15", "3", "-o",
				"test.output" };
		Main<DimacsLiteral> pgm = new Main<DimacsLiteral>(DimacsLiteral.class,
				new CardNetworksPBFactory<DimacsLiteral>(
						new FileDimacsBackend()), null);
		pgm.parseArgs(args);
		pgm.run();
	}

	public final void testSolutionOutput() throws Exception {
		String[] args = { "-f", "5", "-m", "3", "-genseq", "15", "3", "-sat4j" };
		SAT4JPBBuilder sat4j = new SAT4JPBBuilder(SAT4JPBBuilder.SMALL);
		Main<DimacsLiteral> pgm = new Main<DimacsLiteral>(DimacsLiteral.class,
				sat4j, sat4j);
		pgm.parseArgs(args);
		pgm.run();
		System.out.flush();
		System.err.flush();
	}

}

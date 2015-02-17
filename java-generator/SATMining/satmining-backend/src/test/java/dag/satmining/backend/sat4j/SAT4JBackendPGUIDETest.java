/* ./satmining-backend/src/test/java/dag/satmining/backend/sat4j/SAT4JBackendTestPGUIDE.java

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
package dag.satmining.backend.sat4j;

import static org.sat4j.core.LiteralsUtils.negLit;
import static org.sat4j.core.LiteralsUtils.posLit;
import dag.satmining.NoSolutionException;
import dag.satmining.backend.BackendTest;
import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.sat4j.minisat.orders.PGUIDESelectionStrategy;


/**
 *
 * @author ucomignani
 */
public class SAT4JBackendPGUIDETest extends BackendTest<DimacsLiteral> {

	@Override
	protected void initHandler() {
		SAT4JPBBuilderPGUIDE sat4jHandler = new SAT4JPBBuilderPGUIDE(SAT4JPBBuilderPGUIDE.SMALL); // use a light solver to avoid heap space problems
		_handler = sat4jHandler;
		_modelReader = sat4jHandler;
	}

	@Override
	protected void destroyHandler() {
		_handler = null;
		_modelReader = null;
	}

	public void testPGUIDESelectionStrategy() throws NoSolutionException {
		PGUIDESelectionStrategy testPhaseSelection = new PGUIDESelectionStrategy();

		int[] nbPos = {-1,0,1,2};
		int[] nbNeg = {-1,2,1,0};

		for(int i=0; i<5;i++){
			testPhaseSelection.init(4);
			int testBCPGUIDE_var1 = testPhaseSelection.select(nbPos, nbNeg, 1);
			int testBCPGUIDE_var2 = testPhaseSelection.select(nbPos, nbNeg, 2);
			int testBCPGUIDE_var3 = testPhaseSelection.select(nbPos, nbNeg, 3);

			System.out.println();
			assertEquals(testBCPGUIDE_var1, posLit(1) );
			assertTrue( (testBCPGUIDE_var2 == posLit(2)) || (testBCPGUIDE_var2 == negLit(2)) );
			assertEquals(testBCPGUIDE_var3, negLit(3) );
		}

	}
}

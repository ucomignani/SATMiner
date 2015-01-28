/* ./satmining-backend/src/main/java/dag/satmining/backend/sat4j/SAT4JPBBuilderPBCPGUIDE.java

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

package dag.satmining.backend.sat4j;

import org.sat4j.minisat.learning.MiniSATLearning;
import org.sat4j.minisat.restarts.MiniSATRestarts;
import org.sat4j.pb.constraints.PBMaxDataStructure;
import org.sat4j.pb.core.PBDataStructureFactory;
import org.sat4j.tools.ModelIterator;

import dag.satmining.backend.dimacs.DimacsLiteral;
import dag.satmining.backend.sat4j.minisat.orders.PBCPGUIDESelectionStrategy;
import dag.satmining.backend.sat4j.pb.core.PBSolverResolution_PBCPGUIDE;
import dag.satmining.constraints.impl.PBReifier;
import dag.satmining.constraints.impl.WeightedPBReifier;

/**
*
* @author ucomignani
*/
public class SAT4JPBBuilderPBCPGUIDE extends SAT4JPBBuilder{

	public SAT4JPBBuilderPBCPGUIDE(int initNbVar) {
		super();
		_pbReifier = new PBReifier<DimacsLiteral>(this);
		_wpbReifier = new WeightedPBReifier<DimacsLiteral>(this, true);
        _sat4jInitNbVar = initNbVar;
        initSolver();
	}

	private void initSolver() {
		// Taken from SolverFactory.PBSolverWithImpliedClause() to use
		// specialized order for strong backdoor.
		_varOrder = new StrongBackdoorVarOrderHeapWithPhaseSelectionChoice(_strongBackdoor, new PBCPGUIDESelectionStrategy());
		MiniSATLearning<PBDataStructureFactory> learning = new MiniSATLearning<PBDataStructureFactory>();
		PBSolverResolution_PBCPGUIDE solver = new PBSolverResolution_PBCPGUIDE(learning,
				new PBMaxDataStructure(), _varOrder, new MiniSATRestarts());
		learning.setDataStructureFactory(solver.getDSFactory());
		learning.setVarActivityListener(solver);
		_solver = solver;
		_solver.newVar(_sat4jInitNbVar);
		_iteratorOnSolver = new ModelIterator(_solver);
	}
}

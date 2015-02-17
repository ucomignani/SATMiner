/* ./satmining-backend/src/main/java/dag/satmining/backend/sat4j/StrongBackdoorVarOrderHeapWithLearningChoice_PBCPGUIDE.java

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

import java.util.BitSet;

import org.sat4j.minisat.core.ILits;

import dag.satmining.backend.sat4j.minisat.orders.PBCPGUIDESelectionStrategy;
import dag.satmining.backend.sat4j.minisat.orders.PGUIDESelectionStrategy;

/**
 *
 * @author ucomignani
 */
public class StrongBackdoorVarOrderHeap_PBCPGUIDE extends StrongBackdoorVarOrderHeapWithPhaseSelectionChoice {

	private static final long serialVersionUID = 1L;

	public StrongBackdoorVarOrderHeap_PBCPGUIDE(BitSet backDoor) {
		super(backDoor,new PBCPGUIDESelectionStrategy());
	}

	public int select(int nbPos[], int nbNeg[]) {
		// recodage avec double pile, très lié à l'implementation de la super classe
		while (!get_strongHeap().empty()) {
			int var = get_strongHeap().getmin();
			int next = ( (PBCPGUIDESelectionStrategy) phaseStrategy).select(nbPos, nbNeg, var);
			if (lits.isUnassigned(next)) {
				return next;
			}
		}

		while (!this.heap.empty()) {
			int var = this.heap.getmin();
			int next = this.phaseStrategy.select(var);
			if (this.lits.isUnassigned(next)) {
				if (this.activity[var] < 0.0001) {
					this.setNullchoice(this.getNullchoice() + 1);
				}
				return next;
			}
		}
		return ILits.UNDEFINED;
	}

}

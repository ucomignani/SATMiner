/* ./satmining-backend/src/main/java/dag/satmining/backend/sat4j/PGUIDESelectionStrategy.java

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

package dag.satmining.backend.sat4j.minisat.orders;

import static org.sat4j.core.LiteralsUtils.posLit;
import static org.sat4j.core.LiteralsUtils.negLit;
import static org.sat4j.core.LiteralsUtils.var;

import java.util.Random;

import org.sat4j.minisat.core.IPhaseSelectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ucomignani
 */
public final class PGUIDESelectionStrategy implements IPhaseSelectionStrategy {

	private static final Logger LOG = LoggerFactory
			.getLogger(PGUIDESelectionStrategy.class);

	private static final long serialVersionUID = 1L;

	protected int[] phase;
	protected int[] nbPos;
	protected int[] nbNeg;
	protected boolean premiereInitModeleAct = true;
	
	public void init(int nlength) {
		boolean nouveauCompteur = false;
		if (this.phase == null || this.phase.length < nlength) {
			this.phase = new int[nlength];

		}
		if(this.nbPos == null || this.nbNeg == null || this.nbPos.length < nlength || this.nbNeg.length < nlength){
			this.nbPos = new int[nlength];
			this.nbNeg = new int[nlength];
			nouveauCompteur = true;
		}
		for (int i = 1; i < nlength; i++) {
			// on incremente les compteurs en fonction des valuations du dernier modele ou on initialise a wero si l'on vient de demarrer

			if(nouveauCompteur){
				this.nbPos[i] = 0;
				this.nbNeg[i] = 0;	
			}
			else if(premiereInitModeleAct && this.phase[i]%2 == 0)
			{
				this.nbPos[i] ++;
			}
			else if(premiereInitModeleAct && this.phase[i]%2 == 1)
			{
				this.nbNeg[i]++;
			}
			LOG.info("Var:{}; Phase choisie:{}; Nb pos:{}; Nb neg:{}", i, this.phase[i],this.nbPos[i],this.nbNeg[i]);

			this.phase[i] = negLit(i);

		}
		
		this.premiereInitModeleAct = false;//afin d'eviter de compter plusieurs fois si init() est appelle plusieurs fois de suite (comme c'est le cas dans actuellement))
	}

	public void init(int var, int p) {
		this.phase[var] = p;
	}

	public int select(int var) {
		this.premiereInitModeleAct = true; // pour rendre de nouveau le comptage possible au prochain appel a init()

		int pi = this.nbPos[var] - this.nbNeg[var];

		if(pi>0){
			return negLit(var);
		}else if(pi<0){
			return posLit(var);
		}else{
			return  new Random().nextBoolean() ? posLit(var) : negLit(var);
		}
	}

	public void updateVar(int p) {
		this.premiereInitModeleAct = true; // pour rendre de nouveau le comptage possible au prochain appel a init()

		int var = var(p);
//		LOG.info("UpVar:{}; Phase choisie:{}; Nb pos:{}; Nb neg:{}", var, this.phase[var],this.nbPos[var],this.nbNeg[var]);
		this.phase[var] = p;
	}

	@Override
	public String toString() {
		return "guided phase selection";
	}

	public void assignLiteral(int p) {
	}

	public void updateVarAtDecisionLevel(int q) {
	}
}


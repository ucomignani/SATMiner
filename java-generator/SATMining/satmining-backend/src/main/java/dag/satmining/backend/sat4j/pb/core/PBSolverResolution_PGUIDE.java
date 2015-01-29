/* ./satmining-backend/src/main/java/dag/satmining/backend/sat4j/pb/core/PBSolverResolution_PGUIDE.java

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

package dag.satmining.backend.sat4j.pb.core;

import static org.sat4j.core.LiteralsUtils.toDimacs;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.pb.core.PBDataStructureFactory;
import org.sat4j.pb.core.PBSolverResolution;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.TimeoutException;

import dag.satmining.backend.sat4j.StrongBackdoorVarOrderHeap_PGUIDE;

/**
 *
 * @author ucomignani
 */
public class PBSolverResolution_PGUIDE extends PBSolverResolution{

	protected int[] nbPos;
	protected int[] nbNeg;
	
	   public PBSolverResolution_PGUIDE(LearningStrategy<PBDataStructureFactory> learner,
	            PBDataStructureFactory dsf, SearchParams params, IOrder order,
	            RestartStrategy restarter) {
	        super(learner, dsf, params, order, restarter);
	    }
	

    public PBSolverResolution_PGUIDE(LearningStrategy<PBDataStructureFactory> learner,
            PBDataStructureFactory dsf, IOrder order, RestartStrategy restarter) {
        super(learner, dsf, order, restarter);
    }

	public void initOuUpdateCompteurs(){
		boolean nouveauCompteur = false;
		
		if(this.nbPos == null || this.nbNeg == null || this.nbPos.length < nVars() || this.nbNeg.length < nVars()){
			this.nbPos = new int[nVars()+1];
			this.nbNeg = new int[nVars()+1];
			nouveauCompteur = true;
		}

		if(nouveauCompteur){
			for (int i = 1; i < nVars(); i++) {
				this.nbPos[i] = 0;
				this.nbNeg[i] = 0;	
			}
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected Lbool search(IVecInt assumps) {
		assert this.rootLevel == decisionLevel();
		this.stats.starts++;
		int backjumpLevel;

		// varDecay = 1 / params.varDecay;
		this.getOrder().setVarDecay(1 / this.getParams().getVarDecay());
		this.setClaDecay(1 / this.getParams().getClaDecay());

		do {
			this.slistener.beginLoop();
			// propage les clauses unitaires
			Constr confl = propagate();
			assert this.trail.size() == this.getQhead();

			if (confl == null) {
				// No conflict found
				// simpliFYDB() prevents a correct use of
				// constraints removal.
				if (decisionLevel() == 0 && this.isDBSimplificationAllowed()) {
					// // Simplify the set of problem clause
					// // iff rootLevel==0
					this.stats.rootSimplifications++;
					boolean ret = simplifyDB();
					assert ret;
				}
				// was learnts.size() - nAssigns() > nofLearnts
				// if (nofLearnts.obj >= 0 && learnts.size() > nofLearnts.obj) {
				assert nAssigns() <= this.voc.realnVars();
				if (nAssigns() == this.voc.realnVars()) {
					modelFound();
					this.slistener
					.solutionFound((this.getFullmodel() != null) ? this.getFullmodel()
							: this.getModel(), this);
					if (this.sharedConflict == null) {
						cancelUntil(this.rootLevel);
						return Lbool.TRUE;
					} else {
						// listener called ISolverService.backtrack()
						confl = this.sharedConflict;
						this.sharedConflict = null;
					}
				} else {
					if (this.getRestarter().shouldRestart()) {
						// Reached bound on number of conflicts
						// Force a restart
						cancelUntil(this.rootLevel);
						return Lbool.UNDEFINED;
					}
					if (this.needToReduceDB) {
						reduceDB();
						this.needToReduceDB = false;
						// Runtime.getRuntime().gc();
					}
					if (this.sharedConflict == null) {
						// New variable decision
						this.stats.decisions++;

						this.initOuUpdateCompteurs(); //si les compteurs ne sont pas deja init on les cree pour avoir le bon nombre de variables
						int p = ((StrongBackdoorVarOrderHeap_PGUIDE) this.getOrder()).select(this.nbPos,this.nbNeg);
						if (p == ILits.UNDEFINED) {
							confl = preventTheSameDecisionsToBeMade();
							this.setLastConflictMeansUnsat(false);
						} else {
							assert p > 1;
							this.slistener.assuming(toDimacs(p));
							boolean ret = assume(p);
							assert ret;
						}
					} else {
						// listener called ISolverService.backtrack()
						confl = this.sharedConflict;
						this.sharedConflict = null;
					}
				}
			}
			if (confl != null) {
				// un conflit apparait
				this.stats.conflicts++;
				this.slistener.conflictFound(confl, decisionLevel(),
						this.trail.size());
				this.getConflictCount().newConflict();

				if (decisionLevel() == this.rootLevel) {
					if (this.isLastConflictMeansUnsat()) {
						// conflict at root level, the formula is inconsistent
						this.setUnsatExplanationInTermsOfAssumptions(analyzeFinalConflictInTermsOfAssumptions(
								confl, assumps, ILits.UNDEFINED));
						return Lbool.FALSE;
					}
					return Lbool.UNDEFINED;
				}
				int conflictTrailLevel = this.trail.size();
				// analyze conflict
				try {
					analyze(confl, this.getAnalysisResult());
				} catch (TimeoutException e) {
					return Lbool.UNDEFINED;
				}
				assert this.getAnalysisResult().backtrackLevel < decisionLevel();
				backjumpLevel = Math.max(this.getAnalysisResult().backtrackLevel,
						this.rootLevel);
				this.slistener.backjump(backjumpLevel);
				cancelUntil(backjumpLevel);
				if (backjumpLevel == this.rootLevel) {
					this.getRestarter().onBackjumpToRootLevel();
				}
				assert decisionLevel() >= this.rootLevel
						&& decisionLevel() >= this.getAnalysisResult().backtrackLevel;
						if (this.getAnalysisResult().reason == null) {
							return Lbool.FALSE;
						}
						record(this.getAnalysisResult().reason);
						this.getRestarter().newLearnedClause(this.getAnalysisResult().reason,
								conflictTrailLevel);
						this.getAnalysisResult().reason = null;
						decayActivities();
			}
		} while (this.undertimeout);
		return Lbool.UNDEFINED; // timeout occured
	}

	/**
	 * 
	 */
	protected void modelFound() {
		IVecInt tempmodel = new VecInt(nVars());
		this.setUserbooleanmodel(new boolean[realNumberOfVariables()]);
		this.setFullmodel(null);
		for (int i = 1; i <= nVars(); i++) {
			if (this.voc.belongsToPool(i)) {
				int p = this.voc.getFromPool(i);
				if (!this.voc.isUnassigned(p)) {

					/*
					 * On incremente ici les compteurs pour les calculs de distance entre modeles
					 */
					if(this.voc.isSatisfied(p))
						this.nbPos[i]++;
					else
						this.nbNeg[i]++;

					tempmodel.push(this.voc.isSatisfied(p) ? i : -i);                  
					this.getUserbooleanmodel()[i - 1] = this.voc.isSatisfied(p);
					if (this.voc.getReason(p) == null && voc.getLevel(p) > 0) {
						this.getDecisions().push(tempmodel.last());
					} else {
						this.getImplied().push(tempmodel.last());
					}
				}
			}
		}
		this.setModel(new int[tempmodel.size()]);
		tempmodel.copyTo(this.getModel());
		if (realNumberOfVariables() > nVars()) {
			for (int i = nVars() + 1; i <= realNumberOfVariables(); i++) {
				if (this.voc.belongsToPool(i)) {
					int p = this.voc.getFromPool(i);
					if (!this.voc.isUnassigned(p)) {
						tempmodel.push(this.voc.isSatisfied(p) ? i : -i);
						this.getUserbooleanmodel()[i - 1] = this.voc.isSatisfied(p);
						if (this.voc.getReason(p) == null) {
							this.getDecisions().push(tempmodel.last());
						} else {
							this.getImplied().push(tempmodel.last());
						}
					}
				}
			}
			this.setFullmodel(new int[tempmodel.size()]);
			tempmodel.moveTo(this.getFullmodel());
		} else {
			this.setFullmodel(this.getModel());
		}
	}

}

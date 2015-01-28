package dag.satmining.backend.sat4j.pb.core;

import static org.sat4j.core.LiteralsUtils.toDimacs;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.RestartStrategy;
import org.sat4j.pb.core.PBDataStructureFactory;
import org.sat4j.pb.core.PBSolverResolution;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.TimeoutException;

public class PBSolverResolution_PBCPGUIDE extends PBSolverResolution{

	public PBSolverResolution_PBCPGUIDE(
			LearningStrategy<PBDataStructureFactory> learner,
			PBDataStructureFactory dsf, IOrder order, RestartStrategy restarter) {
		super(learner, dsf, order, restarter);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


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
                        int p = this.getOrder().select();
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

   
}

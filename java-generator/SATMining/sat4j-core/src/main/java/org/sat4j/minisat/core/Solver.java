/*******************************************************************************
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004, 2012 Artois University and CNRS
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU Lesser General Public License Version 2.1 or later (the
 * "LGPL"), in which case the provisions of the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL, and not to allow others to use your version of
 * this file under the terms of the EPL, indicate your decision by deleting
 * the provisions above and replace them with the notice and other provisions
 * required by the LGPL. If you do not delete the provisions above, a recipient
 * may use your version of this file under the terms of the EPL or the LGPL.
 *
 * Based on the original MiniSat specification from:
 *
 * An extensible SAT solver. Niklas Een and Niklas Sorensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 *
 * See www.minisat.se for the original solver in C++.
 *
 * Contributors:
 *   CRIL - initial API and implementation
 *******************************************************************************/
package org.sat4j.minisat.core;

import static org.sat4j.core.LiteralsUtils.neg;
import static org.sat4j.core.LiteralsUtils.toDimacs;
import static org.sat4j.core.LiteralsUtils.toInternal;
import static org.sat4j.core.LiteralsUtils.var;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.sat4j.core.ConstrGroup;
import org.sat4j.core.LiteralsUtils;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ILogAble;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.ISolverService;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.sat4j.specs.Lbool;
import org.sat4j.specs.SearchListener;
import org.sat4j.specs.TimeoutException;

/**
 * The backbone of the library providing the modular implementation of a MiniSAT
 * (Chaff) like solver.
 * 
 * @author leberre
 */
public class Solver<D extends DataStructureFactory> implements ISolverService,
        ICDCL<D> {

    private static final long serialVersionUID = 1L;

    private static final double CLAUSE_RESCALE_FACTOR = 1e-20;

    private static final double CLAUSE_RESCALE_BOUND = 1 / CLAUSE_RESCALE_FACTOR;

    protected ILogAble out;

    /**
     * Set of original constraints.
     */
    protected final IVec<Constr> constrs = new Vec<Constr>();

    /**
     * Set of learned constraints.
     */
    protected final IVec<Constr> learnts = new Vec<Constr>();

    /**
     * Increment for clause activity.
     */
    private double claInc = 1.0;

    /**
     * decay factor pour l'activit? des clauses.
     */
    private double claDecay = 1.0;

    /**
     * propagation queue
     */
    // head of the queue in trail ... (taken from MiniSAT 1.14)
    private int qhead = 0;

    /**
     * variable assignments (literals) in chronological order.
     */
    protected final IVecInt trail = new VecInt();

    /**
     * position of the decision levels on the trail.
     */
    protected final IVecInt trailLim = new VecInt();

    /**
     * position of assumptions before starting the search.
     */
    protected int rootLevel;

    private int[] model = null;

    protected ILits voc;

    private IOrder order;

    private final ActivityComparator comparator = new ActivityComparator();

    private SolverStats stats = new SolverStats();

    private LearningStrategy<D> learner;

    protected volatile boolean undertimeout;

    private long timeout = Integer.MAX_VALUE;

    private boolean timeBasedTimeout = true;

    protected D dsfactory;

    private SearchParams params;

    private final IVecInt __dimacs_out = new VecInt();

    protected SearchListener slistener = new VoidTracing();

    private RestartStrategy restarter;

    private final Map<String, Counter> constrTypes = new HashMap<String, Counter>();

    private boolean isDBSimplificationAllowed = false;

    private final IVecInt learnedLiterals = new VecInt();

    private boolean verbose = false;

    private boolean keepHot = false;

    private String prefix = "c ";
    private int declaredMaxVarId = 0;

    protected IVecInt dimacs2internal(IVecInt in) {
        this.__dimacs_out.clear();
        this.__dimacs_out.ensure(in.size());
        int p;
        for (int i = 0; i < in.size(); i++) {
            p = in.get(i);
            if (p == 0) {
                throw new IllegalArgumentException(
                        "0 is not a valid variable identifier");
            }
            this.__dimacs_out.unsafePush(this.voc.getFromPool(p));
        }
        return this.__dimacs_out;
    }

    /*
     * @since 2.3.1
     */
    @Override
	public void registerLiteral(int p) {
        this.voc.getFromPool(p);
    }

    /**
     * creates a Solver without LearningListener. A learningListener must be
     * added to the solver, else it won't backtrack!!! A data structure factory
     * must be provided, else it won't work either.
     */

    public Solver(LearningStrategy<D> learner, D dsf, IOrder order,
            RestartStrategy restarter) {
        this(learner, dsf, new SearchParams(), order, restarter);
    }

    public Solver(LearningStrategy<D> learner, D dsf, SearchParams params,
            IOrder order, RestartStrategy restarter) {
        this(learner, dsf, params, order, restarter, ILogAble.CONSOLE);
    }

    public Solver(LearningStrategy<D> learner, D dsf, SearchParams params,
            IOrder order, RestartStrategy restarter, ILogAble logger) {
        this.order = order;
        this.setParams(params);
        this.setRestarter(restarter);
        this.out = logger;
        setDataStructureFactory(dsf);
        // should be called after dsf has been set up
        setLearningStrategy(learner);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.ICDCL#setDataStructureFactory(D)
     */
    @Override
	public final void setDataStructureFactory(D dsf) {
        this.dsfactory = dsf;
        this.dsfactory.setUnitPropagationListener(this);
        this.dsfactory.setLearner(this);
        this.voc = dsf.getVocabulary();
        this.order.setLits(this.voc);
    }

    /**
     * @since 2.2
     */
    @Override
	public boolean isVerbose() {
        return this.verbose;
    }

    /**
     * @param value
     * @since 2.2
     */
    @Override
	public void setVerbose(boolean value) {
        this.verbose = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sat4j.minisat.core.ICDCL#setSearchListener(org.sat4j.specs.SearchListener
     * )
     */
    @Override
	public <S extends ISolverService> void setSearchListener(
            SearchListener<S> sl) {
        this.slistener = sl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.ICDCL#getSearchListener()
     */
    @Override
	public <S extends ISolverService> SearchListener<S> getSearchListener() {
        return this.slistener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.ICDCL#setLearner(org.sat4j.minisat.core.
     * LearningStrategy)
     */
    @Override
	public void setLearner(LearningStrategy<D> strategy) {
        setLearningStrategy(strategy);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sat4j.minisat.core.ICDCL#setLearningStrategy(org.sat4j.minisat.core.
     * LearningStrategy)
     */
    @Override
	public void setLearningStrategy(LearningStrategy<D> strategy) {
        if (this.learner != null) {
            this.learner.setSolver(null);
        }
        this.learner = strategy;
        strategy.setSolver(this);
    }

    @Override
	public void setTimeout(int t) {
        this.timeout = t * 1000L;
        this.timeBasedTimeout = true;
    }

    @Override
	public void setTimeoutMs(long t) {
        this.timeout = t;
        this.timeBasedTimeout = true;
    }

    @Override
	public void setTimeoutOnConflicts(int count) {
        this.timeout = count;
        this.timeBasedTimeout = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.ICDCL#setSearchParams(org.sat4j.minisat.core.
     * SearchParams)
     */
    @Override
	public void setSearchParams(SearchParams sp) {
        this.setParams(sp);
    }

    @Override
	public SearchParams getSearchParams() {
        return this.getParams();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sat4j.minisat.core.ICDCL#setRestartStrategy(org.sat4j.minisat.core
     * .RestartStrategy)
     */
    @Override
	public void setRestartStrategy(RestartStrategy restarter) {
        this.setRestarter(restarter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.ICDCL#getRestartStrategy()
     */
    @Override
	public RestartStrategy getRestartStrategy() {
        return this.getRestarter();
    }

    @Override
	public void expireTimeout() {
        this.undertimeout = false;
        if (this.timeBasedTimeout) {
            if (this.timer != null) {
                this.timer.cancel();
                this.timer = null;
            }
        } else {
            if (this.getConflictCount() != null) {
                this.setConflictCount(null);
            }
        }
    }

    protected int nAssigns() {
        return this.trail.size();
    }

    @Override
	public int nConstraints() {
        return this.constrs.size();
    }

    @Override
	public void learn(Constr c) {
        this.slistener.learn(c);
        this.learnts.push(c);
        c.setLearnt();
        c.register();
        this.stats.learnedclauses++;
        switch (c.size()) {
        case 2:
            this.stats.learnedbinaryclauses++;
            break;
        case 3:
            this.stats.learnedternaryclauses++;
            break;
        default:
            // do nothing
        }
    }

    public final int decisionLevel() {
        return this.trailLim.size();
    }

    @Override
	@Deprecated
    public int newVar() {
        int index = this.voc.nVars() + 1;
        this.voc.ensurePool(index);
        return index;
    }

    @Override
	public int newVar(int howmany) {
        this.voc.ensurePool(howmany);
        this.declaredMaxVarId = howmany;
        return howmany;
    }

    @Override
	public IConstr addClause(IVecInt literals) throws ContradictionException {
        IVecInt vlits = dimacs2internal(literals);
        return addConstr(this.dsfactory.createClause(vlits));
    }

    @Override
	public boolean removeConstr(IConstr co) {
        if (co == null) {
            throw new IllegalArgumentException(
                    "Reference to the constraint to remove needed!"); //$NON-NLS-1$
        }
        Constr c = (Constr) co;
        c.remove(this);
        this.constrs.remove(c);
        clearLearntClauses();
        String type = c.getClass().getName();
        this.constrTypes.get(type).dec();
        return true;
    }

    /**
     * @since 2.1
     */
    @Override
	public boolean removeSubsumedConstr(IConstr co) {
        if (co == null) {
            throw new IllegalArgumentException(
                    "Reference to the constraint to remove needed!"); //$NON-NLS-1$
        }
        if (this.constrs.last() != co) {
            throw new IllegalArgumentException(
                    "Can only remove latest added constraint!!!"); //$NON-NLS-1$
        }
        Constr c = (Constr) co;
        c.remove(this);
        this.constrs.pop();
        String type = c.getClass().getName();
        this.constrTypes.get(type).dec();
        return true;
    }

    @Override
	public void addAllClauses(IVec<IVecInt> clauses)
            throws ContradictionException {
        for (Iterator<IVecInt> iterator = clauses.iterator(); iterator
                .hasNext();) {
            addClause(iterator.next());
        }
    }

    @Override
	public IConstr addAtMost(IVecInt literals, int degree)
            throws ContradictionException {
        int n = literals.size();
        IVecInt opliterals = new VecInt(n);
        for (IteratorInt iterator = literals.iterator(); iterator.hasNext();) {
            opliterals.push(-iterator.next());
        }
        return addAtLeast(opliterals, n - degree);
    }

    @Override
	public IConstr addAtLeast(IVecInt literals, int degree)
            throws ContradictionException {
        IVecInt vlits = dimacs2internal(literals);
        return addConstr(this.dsfactory.createCardinalityConstraint(vlits,
                degree));
    }

    @Override
	public IConstr addExactly(IVecInt literals, int n)
            throws ContradictionException {
        ConstrGroup group = new ConstrGroup(false);
        group.add(addAtMost(literals, n));
        group.add(addAtLeast(literals, n));
        return group;
    }

    @SuppressWarnings("unchecked")
    public boolean simplifyDB() {
        // Simplifie la base de clauses apres la premiere propagation des
        // clauses unitaires
        IVec<Constr>[] cs = new IVec[] { this.constrs, this.learnts };
        for (int type = 0; type < 2; type++) {
            int j = 0;
            for (int i = 0; i < cs[type].size(); i++) {
                if (cs[type].get(i).simplify()) {
                    // enleve les contraintes satisfaites de la base
                    cs[type].get(i).remove(this);
                } else {
                    cs[type].moveTo(j++, i);
                }
            }
            cs[type].shrinkTo(j);
        }
        return true;
    }

    /**
     * Si un mod?le est trouv?, ce vecteur contient le mod?le.
     * 
     * @return un mod?le de la formule.
     */
    @Override
	public int[] model() {
        if (this.getModel() == null) {
            throw new UnsupportedOperationException(
                    "Call the solve method first!!!"); //$NON-NLS-1$
        }
        int[] nmodel = new int[this.getModel().length];
        System.arraycopy(this.getModel(), 0, nmodel, 0, this.getModel().length);
        return nmodel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.ICDCL#enqueue(int)
     */
    @Override
	public boolean enqueue(int p) {
        return enqueue(p, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.ICDCL#enqueue(int,
     * org.sat4j.minisat.core.Constr)
     */
    @Override
	public boolean enqueue(int p, Constr from) {
        assert p > 1;
        if (this.voc.isSatisfied(p)) {
            // literal is already satisfied. Skipping.
            return true;
        }
        if (this.voc.isFalsified(p)) {
            // conflicting enqueued assignment
            return false;
        }
        // new fact, store it
        this.voc.satisfies(p);
        this.voc.setLevel(p, decisionLevel());
        this.voc.setReason(p, from);
        this.trail.push(p);
        if (from != null && from.learnt()) {
            this.learnedConstraintsDeletionStrategy.onPropagation(from);
        }
        return true;
    }

    private boolean[] mseen = new boolean[0];

    private final IVecInt mpreason = new VecInt();

    private final IVecInt moutLearnt = new VecInt();

    /**
     * @throws TimeoutException
     *             if the timeout is reached during conflict analysis.
     */
    public void analyze(Constr confl, Pair results) throws TimeoutException {
        assert confl != null;

        final boolean[] seen = this.mseen;
        final IVecInt outLearnt = this.moutLearnt;
        final IVecInt preason = this.mpreason;

        outLearnt.clear();
        assert outLearnt.size() == 0;
        for (int i = 0; i < seen.length; i++) {
            seen[i] = false;
        }

        int counter = 0;
        int p = ILits.UNDEFINED;

        outLearnt.push(ILits.UNDEFINED);
        // reserve de la place pour le litteral falsifie
        int outBtlevel = 0;
        IConstr prevConfl = null;

        do {
            preason.clear();
            assert confl != null;
            if (prevConfl != confl) {
                confl.calcReason(p, preason);
                this.learnedConstraintsDeletionStrategy
                        .onConflictAnalysis(confl);
                // Trace reason for p
                for (int j = 0; j < preason.size(); j++) {
                    int q = preason.get(j);
                    this.order.updateVar(q);
                    if (!seen[q >> 1]) {
                        seen[q >> 1] = true;
                        if (this.voc.getLevel(q) == decisionLevel()) {
                            counter++;
                            this.order.updateVarAtDecisionLevel(q);
                        } else if (this.voc.getLevel(q) > 0) {
                            // only literals assigned after decision level 0
                            // part of
                            // the explanation
                            outLearnt.push(q ^ 1);
                            outBtlevel = Math.max(outBtlevel,
                                    this.voc.getLevel(q));
                        }
                    }
                }
            }
            prevConfl = confl;
            // select next reason to look at
            do {
                p = this.trail.last();
                confl = this.voc.getReason(p);
                undoOne();
            } while (!seen[p >> 1]);
            // seen[p.var] indique que p se trouve dans outLearnt ou dans
            // le dernier niveau de d?cision
        } while (--counter > 0);

        outLearnt.set(0, p ^ 1);
        this.simplifier.simplify(outLearnt);

        Constr c = this.dsfactory.createUnregisteredClause(outLearnt);
        // slistener.learn(c);
        this.learnedConstraintsDeletionStrategy.onClauseLearning(c);
        results.reason = c;

        assert outBtlevel > -1;
        results.backtrackLevel = outBtlevel;
    }

    /**
     * Derive a subset of the assumptions causing the inconistency.
     * 
     * @param confl
     *            the last conflict of the search, occuring at root level.
     * @param assumps
     *            the set of assumption literals
     * @param conflictingLiteral
     *            the literal detected conflicting while propagating
     *            assumptions.
     * @return a subset of assumps causing the inconsistency.
     * @since 2.2
     */
    public IVecInt analyzeFinalConflictInTermsOfAssumptions(Constr confl,
            IVecInt assumps, int conflictingLiteral) {
        if (assumps.size() == 0) {
            return null;
        }
        while (!this.trailLim.isEmpty()
                && this.trailLim.last() == this.trail.size()) {
            // conflict detected when assuming a value
            this.trailLim.pop();
        }
        final boolean[] seen = this.mseen;
        final IVecInt outLearnt = this.moutLearnt;
        final IVecInt preason = this.mpreason;

        outLearnt.clear();
        if (this.trailLim.size() == 0) {
            // conflict detected on unit clauses
            return outLearnt;
        }

        assert outLearnt.size() == 0;
        for (int i = 0; i < seen.length; i++) {
            seen[i] = false;
        }

        if (confl == null) {
            seen[conflictingLiteral >> 1] = true;
        }

        int p = ILits.UNDEFINED;
        while (confl == null && this.trail.size() > 0
                && this.trailLim.size() > 0) {
            p = this.trail.last();
            confl = this.voc.getReason(p);
            undoOne();
            if (confl == null && p == (conflictingLiteral ^ 1)) {
                outLearnt.push(toDimacs(p));
            }
            if (this.trail.size() <= this.trailLim.last()) {
                this.trailLim.pop();
            }
        }
        if (confl == null) {
            return outLearnt;
        }
        do {

            preason.clear();
            confl.calcReason(p, preason);
            // Trace reason for p
            for (int j = 0; j < preason.size(); j++) {
                int q = preason.get(j);
                if (!seen[q >> 1]) {
                    seen[q >> 1] = true;
                    if (this.voc.getReason(q) == null
                            && this.voc.getLevel(q) > 0) {
                        assert assumps.contains(toDimacs(q));
                        outLearnt.push(toDimacs(q));
                    }
                }
            }

            // select next reason to look at
            do {
                p = this.trail.last();
                confl = this.voc.getReason(p);
                undoOne();
                if (decisionLevel() > 0
                        && this.trail.size() <= this.trailLim.last()) {
                    this.trailLim.pop();
                }
            } while (this.trail.size() > 0 && decisionLevel() > 0
                    && (!seen[p >> 1] || confl == null));
        } while (decisionLevel() > 0);
        return outLearnt;
    }

    public static final ISimplifier NO_SIMPLIFICATION = new ISimplifier() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
		public void simplify(IVecInt outLearnt) {
        }

        @Override
        public String toString() {
            return "No reason simplification"; //$NON-NLS-1$
        }
    };

    public final ISimplifier SIMPLE_SIMPLIFICATION = new ISimplifier() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
		public void simplify(IVecInt conflictToReduce) {
            simpleSimplification(conflictToReduce);
        }

        @Override
        public String toString() {
            return "Simple reason simplification"; //$NON-NLS-1$
        }
    };

    public final ISimplifier EXPENSIVE_SIMPLIFICATION = new ISimplifier() {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
		public void simplify(IVecInt conflictToReduce) {
            expensiveSimplification(conflictToReduce);
        }

        @Override
        public String toString() {
            return "Expensive reason simplification"; //$NON-NLS-1$
        }
    };

    public final ISimplifier EXPENSIVE_SIMPLIFICATION_WLONLY = new ISimplifier() {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
		public void simplify(IVecInt conflictToReduce) {
            expensiveSimplificationWLOnly(conflictToReduce);
        }

        @Override
        public String toString() {
            return "Expensive reason simplification specific for WL data structure"; //$NON-NLS-1$
        }
    };

    private ISimplifier simplifier = NO_SIMPLIFICATION;

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.ICDCL#setSimplifier(java.lang.String)
     */
    @Override
	public void setSimplifier(SimplificationType simp) {
        Field f;
        try {
            f = Solver.class.getDeclaredField(simp.toString());
            this.simplifier = (ISimplifier) f.get(this);
        } catch (Exception e) {
            e.printStackTrace();
            this.simplifier = NO_SIMPLIFICATION;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sat4j.minisat.core.ICDCL#setSimplifier(org.sat4j.minisat.core.Solver
     * .ISimplifier)
     */
    @Override
	public void setSimplifier(ISimplifier simp) {
        this.simplifier = simp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.ICDCL#getSimplifier()
     */
    @Override
	public ISimplifier getSimplifier() {
        return this.simplifier;
    }

    // MiniSat -- Copyright (c) 2003-2005, Niklas Een, Niklas Sorensson
    //
    // Permission is hereby granted, free of charge, to any person obtaining a
    // copy of this software and associated documentation files (the
    // "Software"), to deal in the Software without restriction, including
    // without limitation the rights to use, copy, modify, merge, publish,
    // distribute, sublicense, and/or sell copies of the Software, and to
    // permit persons to whom the Software is furnished to do so, subject to
    // the following conditions:
    //
    // The above copyright notice and this permission notice shall be included
    // in all copies or substantial portions of the Software.
    //
    // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
    // OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
    // MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
    // NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    // LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
    // OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
    // WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

    // Taken from MiniSAT 1.14: Simplify conflict clause (a little):
    private void simpleSimplification(IVecInt conflictToReduce) {
        int i, j, p;
        final boolean[] seen = this.mseen;
        IConstr r;
        for (i = j = 1; i < conflictToReduce.size(); i++) {
            r = this.voc.getReason(conflictToReduce.get(i));
            if (r == null || r.canBePropagatedMultipleTimes()) {
                conflictToReduce.moveTo(j++, i);
            } else {
                for (int k = 0; k < r.size(); k++) {
                    p = r.get(k);
                    if (!seen[p >> 1] && this.voc.isFalsified(p)
                            && this.voc.getLevel(p) != 0) {
                        conflictToReduce.moveTo(j++, i);
                        break;
                    }
                }
            }
        }
        conflictToReduce.shrink(i - j);
        this.stats.reducedliterals += i - j;
    }

    private final IVecInt analyzetoclear = new VecInt();

    private final IVecInt analyzestack = new VecInt();

    // Taken from MiniSAT 1.14
    private void expensiveSimplification(IVecInt conflictToReduce) {
        // Simplify conflict clause (a lot):
        //
        int i, j;
        // (maintain an abstraction of levels involved in conflict)
        this.analyzetoclear.clear();
        conflictToReduce.copyTo(this.analyzetoclear);
        for (i = 1, j = 1; i < conflictToReduce.size(); i++) {
            if (this.voc.getReason(conflictToReduce.get(i)) == null
                    || !analyzeRemovable(conflictToReduce.get(i))) {
                conflictToReduce.moveTo(j++, i);
            }
        }
        conflictToReduce.shrink(i - j);
        this.stats.reducedliterals += i - j;
    }

    // Check if 'p' can be removed.' min_level' is used to abort early if
    // visiting literals at a level that cannot be removed.
    //
    private boolean analyzeRemovable(int p) {
        assert this.voc.getReason(p) != null;
        ILits lvoc = this.voc;
        IVecInt lanalyzestack = this.analyzestack;
        IVecInt lanalyzetoclear = this.analyzetoclear;
        lanalyzestack.clear();
        lanalyzestack.push(p);
        final boolean[] seen = this.mseen;
        int top = lanalyzetoclear.size();
        while (lanalyzestack.size() > 0) {
            int q = lanalyzestack.last();
            assert lvoc.getReason(q) != null;
            Constr c = lvoc.getReason(q);
            lanalyzestack.pop();
            if (c.canBePropagatedMultipleTimes()) {
                for (int j = top; j < lanalyzetoclear.size(); j++) {
                    seen[lanalyzetoclear.get(j) >> 1] = false;
                }
                lanalyzetoclear.shrink(lanalyzetoclear.size() - top);
                return false;
            }
            for (int i = 0; i < c.size(); i++) {
                int l = c.get(i);
                if (!seen[var(l)] && lvoc.isFalsified(l)
                        && lvoc.getLevel(l) != 0) {
                    if (lvoc.getReason(l) == null) {
                        for (int j = top; j < lanalyzetoclear.size(); j++) {
                            seen[lanalyzetoclear.get(j) >> 1] = false;
                        }
                        lanalyzetoclear.shrink(lanalyzetoclear.size() - top);
                        return false;
                    }
                    seen[l >> 1] = true;
                    lanalyzestack.push(l);
                    lanalyzetoclear.push(l);
                }
            }

        }

        return true;
    }

    // Taken from MiniSAT 1.14
    private void expensiveSimplificationWLOnly(IVecInt conflictToReduce) {
        // Simplify conflict clause (a lot):
        //
        int i, j;
        // (maintain an abstraction of levels involved in conflict)
        this.analyzetoclear.clear();
        conflictToReduce.copyTo(this.analyzetoclear);
        for (i = 1, j = 1; i < conflictToReduce.size(); i++) {
            if (this.voc.getReason(conflictToReduce.get(i)) == null
                    || !analyzeRemovableWLOnly(conflictToReduce.get(i))) {
                conflictToReduce.moveTo(j++, i);
            }
        }
        conflictToReduce.shrink(i - j);
        this.stats.reducedliterals += i - j;
    }

    // Check if 'p' can be removed.' min_level' is used to abort early if
    // visiting literals at a level that cannot be removed.
    //
    private boolean analyzeRemovableWLOnly(int p) {
        assert this.voc.getReason(p) != null;
        this.analyzestack.clear();
        this.analyzestack.push(p);
        final boolean[] seen = this.mseen;
        int top = this.analyzetoclear.size();
        while (this.analyzestack.size() > 0) {
            int q = this.analyzestack.last();
            assert this.voc.getReason(q) != null;
            Constr c = this.voc.getReason(q);
            this.analyzestack.pop();
            for (int i = 1; i < c.size(); i++) {
                int l = c.get(i);
                if (!seen[var(l)] && this.voc.getLevel(l) != 0) {
                    if (this.voc.getReason(l) == null) {
                        for (int j = top; j < this.analyzetoclear.size(); j++) {
                            seen[this.analyzetoclear.get(j) >> 1] = false;
                        }
                        this.analyzetoclear.shrink(this.analyzetoclear.size()
                                - top);
                        return false;
                    }
                    seen[l >> 1] = true;
                    this.analyzestack.push(l);
                    this.analyzetoclear.push(l);
                }
            }
        }

        return true;
    }

    // END Minisat 1.14 cut and paste

    /**
     * 
     */
    protected void undoOne() {
        // gather last assigned literal
        int p = this.trail.last();
        assert p > 1;
        assert this.voc.getLevel(p) >= 0;
        int x = p >> 1;
        // unassign variable
        this.voc.unassign(p);
        this.voc.setReason(p, null);
        this.voc.setLevel(p, -1);
        // update heuristics value
        this.order.undo(x);
        // remove literal from the trail
        this.trail.pop();
        // update constraints on backtrack.
        // not used if the solver uses watched literals.
        IVec<Undoable> undos = this.voc.undos(p);
        assert undos != null;
        for (int size = undos.size(); size > 0; size--) {
            undos.last().undo(p);
            undos.pop();
        }
    }

    /**
     * Propagate activity to a constraint
     * 
     * @param confl
     *            a constraint
     */
    @Override
	public void claBumpActivity(Constr confl) {
        confl.incActivity(this.claInc);
        if (confl.getActivity() > CLAUSE_RESCALE_BOUND) {
            claRescalActivity();
            // for (int i = 0; i < confl.size(); i++) {
            // varBumpActivity(confl.get(i));
            // }
        }
    }

    @Override
	public void varBumpActivity(int p) {
        this.order.updateVar(p);
    }

    private void claRescalActivity() {
        for (int i = 0; i < this.learnts.size(); i++) {
            this.learnts.get(i).rescaleBy(CLAUSE_RESCALE_FACTOR);
        }
        this.claInc *= CLAUSE_RESCALE_FACTOR;
    }

    private final IVec<Propagatable> watched = new Vec<Propagatable>();

    /**
     * @return null if not conflict is found, else a conflicting constraint.
     */
    public final Constr propagate() {
        IVecInt ltrail = this.trail;
        SolverStats lstats = this.stats;
        IOrder lorder = this.order;
        SearchListener lslistener = this.slistener;
        // ltrail.size() changes due to propagation
        // cannot cache that value.
        while (this.getQhead() < ltrail.size()) {
            lstats.propagations++;
            int p = ltrail.get(this.qhead++);
            lslistener.propagating(toDimacs(p), null);
            lorder.assignLiteral(p);
            Constr confl = reduceClausesForFalsifiedLiteral(p);
            if (confl != null) {
                return confl;
            }
        }
        return null;
    }

    private Constr reduceClausesForFalsifiedLiteral(int p) {
        // p is the literal to propagate
        // Moved original MiniSAT code to dsfactory to avoid
        // watches manipulation in counter Based clauses for instance.
        assert p > 1;
        IVec<Propagatable> lwatched = this.watched;
        lwatched.clear();
        this.voc.watches(p).moveTo(lwatched);
        final int size = lwatched.size();
        for (int i = 0; i < size; i++) {
            this.stats.inspects++;
            // try shortcut
            // shortcut = shortcuts.get(i);
            // if (shortcut != ILits.UNDEFINED && voc.isSatisfied(shortcut))
            // {
            // voc.watch(p, watched.get(i), shortcut);
            // stats.shortcuts++;
            // continue;
            // }
            if (!lwatched.get(i).propagate(this, p)) {
                // Constraint is conflicting: copy remaining watches to
                // watches[p]
                // and return constraint
                final int sizew = lwatched.size();
                for (int j = i + 1; j < sizew; j++) {
                    this.voc.watch(p, lwatched.get(j));
                }
                this.setQhead(this.trail.size()); // propQ.clear();
                return lwatched.get(i).toConstraint();
            }
        }
        return null;
    }

    protected void record(Constr constr) {
        constr.assertConstraint(this);
        this.slistener.adding(toDimacs(constr.get(0)));
        if (constr.size() == 1) {
            this.stats.learnedliterals++;
        } else {
            this.learner.learns(constr);
        }
    }

    /**
     * @return false ssi conflit imm?diat.
     */
    public boolean assume(int p) {
        // Precondition: assume propagation queue is empty
        assert this.trail.size() == this.getQhead();
        assert !this.trailLim.contains(this.trail.size());
        this.trailLim.push(this.trail.size());
        return enqueue(p);
    }

    /**
     * Revert to the state before the last assume()
     */
    private void cancel() {
        // assert trail.size() == qhead || !undertimeout;
        int decisionvar = this.trail.unsafeGet(this.trailLim.last());
        this.slistener.backtracking(toDimacs(decisionvar));
        for (int c = this.trail.size() - this.trailLim.last(); c > 0; c--) {
            undoOne();
        }
        this.trailLim.pop();
        this.setQhead(this.trail.size());
    }

    /**
     * Restore literals
     */
    private void cancelLearntLiterals(int learnedLiteralsLimit) {
        this.learnedLiterals.clear();
        // assert trail.size() == qhead || !undertimeout;
        while (this.trail.size() > learnedLiteralsLimit) {
            this.learnedLiterals.push(this.trail.last());
            undoOne();
        }
        // qhead = 0;
        // learnedLiterals = 0;
    }

    /**
     * Cancel several levels of assumptions
     * 
     * @param level
     */
    protected void cancelUntil(int level) {
        while (decisionLevel() > level) {
            cancel();
        }
    }

    private final Pair analysisResult = new Pair();

    private boolean[] userbooleanmodel;

    private IVecInt unsatExplanationInTermsOfAssumptions;

    protected Lbool search(IVecInt assumps) {
        assert this.rootLevel == decisionLevel();
        this.stats.starts++;
        int backjumpLevel;

        // varDecay = 1 / params.varDecay;
        this.order.setVarDecay(1 / this.getParams().getVarDecay());
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
                if (decisionLevel() == 0 && this.isDBSimplificationAllowed) {
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
                        int p = this.order.select();
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

    protected Constr preventTheSameDecisionsToBeMade() {
        IVecInt clause = new VecInt(nVars());
        int p;
        for (int i = this.trail.size() - 1; i >= this.rootLevel; i--) {
            p = this.trail.get(i);
            if (this.voc.getReason(p) == null) {
                clause.push(p ^ 1);
            }
        }
        return this.dsfactory.createUnregisteredClause(clause);
    }

    protected void analyzeAtRootLevel(Constr conflict) {
    }

    private final IVecInt implied = new VecInt();
    private final IVecInt decisions = new VecInt();

    private int[] fullmodel;

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

    /**
     * Forget a variable in the formula by falsifying both its positive and
     * negative literals.
     * 
     * @param var
     *            a variable
     * @return a conflicting constraint resulting from the disparition of those
     *         literals.
     */
    private Constr forget(int var) {
        boolean satisfied = this.voc.isSatisfied(toInternal(var));
        this.voc.forgets(var);
        Constr confl;
        if (satisfied) {
            confl = reduceClausesForFalsifiedLiteral(LiteralsUtils
                    .toInternal(-var));
        } else {
            confl = reduceClausesForFalsifiedLiteral(LiteralsUtils
                    .toInternal(var));
        }
        return confl;
    }

    /**
     * Assume literal p and perform unit propagation
     * 
     * @param p
     *            a literal
     * @return true if no conflict is reached, false if a conflict is found.
     */
    private boolean setAndPropagate(int p) {
        if (voc.isUnassigned(p)) {
            assert !trail.contains(p);
            assert !trail.contains(neg(p));
            return assume(p) && propagate() == null;
        }
        return voc.isSatisfied(p);
    }

    private int[] prime;

    @Override
	public int[] primeImplicant() {
        assert this.getQhead() == this.trail.size() + this.learnedLiterals.size();
        if (this.learnedLiterals.size() > 0) {
            this.setQhead(trail.size());
        }
        System.out.printf("%s implied: %d, decision: %d %n", getLogPrefix(),
                getImplied().size(), getDecisions().size());
        this.prime = new int[realNumberOfVariables() + 1];
        int p, d;
        for (int i = 0; i < this.prime.length; i++) {
            this.prime[i] = 0;
        }
        boolean noproblem;
        for (IteratorInt it = this.getImplied().iterator(); it.hasNext();) {
            d = it.next();
            p = toInternal(d);
            this.prime[Math.abs(d)] = d;
            noproblem = setAndPropagate(p);
            assert noproblem;
        }
        boolean canBeRemoved;
        int rightlevel;
        int removed = 0;
        int propagated = 0;
        int tested = 0;
        int l2propagation = 0;

        for (int i = 0; i < this.getDecisions().size(); i++) {
            d = this.getDecisions().get(i);
            assert !this.voc.isFalsified(toInternal(d));
            if (this.voc.isSatisfied(toInternal(d))) {
                // d has been propagated
                this.prime[Math.abs(d)] = d;
                propagated++;
            } else if (setAndPropagate(toInternal(-d))) {
                canBeRemoved = true;
                tested++;
                rightlevel = currentDecisionLevel();
                for (int j = i + 1; j < this.getDecisions().size(); j++) {
                    l2propagation++;
                    if (!setAndPropagate(toInternal(this.getDecisions().get(j)))) {
                        canBeRemoved = false;
                        break;
                    }
                }
                cancelUntil(rightlevel);
                if (canBeRemoved) {
                    // it is not a necessary literal
                    forget(Math.abs(d));
                    IConstr confl = propagate();
                    assert confl == null;
                    removed++;
                } else {
                    this.prime[Math.abs(d)] = d;
                    cancel();
                    assert voc.isUnassigned(toInternal(d));
                    noproblem = setAndPropagate(toInternal(d));
                    assert noproblem;
                }
            } else {
                // conflict, literal is necessary
                this.prime[Math.abs(d)] = d;
                cancel();
                noproblem = setAndPropagate(toInternal(d));
                assert noproblem;
            }
        }
        cancelUntil(0);
        int[] implicant = new int[this.prime.length - removed - 1];
        int index = 0;
        for (int i : this.prime) {
            if (i != 0) {
                implicant[index++] = i;
            }
        }
        if (isVerbose()) {
            System.out.printf("%s prime implicant computation statistics%n",
                    getLogPrefix());
            System.out
                    .printf("%s implied: %d, decision: %d (removed %d, tested %d, propagated %d), l2 propagation:%d%n",
                            getLogPrefix(), getImplied().size(), getDecisions().size(),
                            removed, tested, propagated, l2propagation);
        }
        return implicant;
    }

    @Override
	public boolean primeImplicant(int p) {
        if (p == 0 || Math.abs(p) > realNumberOfVariables()) {
            throw new IllegalArgumentException(
                    "Use a valid Dimacs var id as argument!"); //$NON-NLS-1$
        }
        if (this.prime == null) {
            throw new UnsupportedOperationException(
                    "Call the primeImplicant method first!!!"); //$NON-NLS-1$
        }
        return this.prime[Math.abs(p)] == p;
    }

    @Override
	public boolean model(int var) {
        if (var <= 0 || var > realNumberOfVariables()) {
            throw new IllegalArgumentException(
                    "Use a valid Dimacs var id as argument!"); //$NON-NLS-1$
        }
        if (this.getUserbooleanmodel() == null) {
            throw new UnsupportedOperationException(
                    "Call the solve method first!!!"); //$NON-NLS-1$
        }
        return this.getUserbooleanmodel()[var - 1];
    }

    @Override
	public void clearLearntClauses() {
        for (Iterator<Constr> iterator = this.learnts.iterator(); iterator
                .hasNext();) {
            iterator.next().remove(this);
        }
        this.learnts.clear();
        this.learnedLiterals.clear();
    }

    protected final void reduceDB() {
        this.stats.reduceddb++;
        this.slistener.cleaning();
        this.learnedConstraintsDeletionStrategy.reduce(this.learnts);
        System.gc();
    }

    /**
     * @param learnts
     */
    protected void sortOnActivity() {
        this.learnts.sort(this.comparator);
    }

    /**
     * 
     */
    protected void decayActivities() {
        this.order.varDecayActivity();
        claDecayActivity();
    }

    /**
     * 
     */
    private void claDecayActivity() {
        this.claInc *= this.getClaDecay();
    }

    /**
     * @return true iff the set of constraints is satisfiable, else false.
     */
    @Override
	public boolean isSatisfiable() throws TimeoutException {
        return isSatisfiable(VecInt.EMPTY);
    }

    /**
     * @return true iff the set of constraints is satisfiable, else false.
     */
    @Override
	public boolean isSatisfiable(boolean global) throws TimeoutException {
        return isSatisfiable(VecInt.EMPTY, global);
    }

    private double timebegin = 0;

    protected boolean needToReduceDB;

    private ConflictTimerContainer conflictCount;

    private transient Timer timer;

    @Override
	public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        return isSatisfiable(assumps, false);
    }

    public final LearnedConstraintsDeletionStrategy fixedSize(final int maxsize) {
        return new LearnedConstraintsDeletionStrategy() {

            private static final long serialVersionUID = 1L;
            private final ConflictTimer aTimer = new ConflictTimerAdapter(
                    maxsize) {

                private static final long serialVersionUID = 1L;

                @Override
                public void run() {
                    Solver.this.needToReduceDB = true;
                }
            };

            @Override
			public void reduce(IVec<Constr> learnedConstrs) {
                int i, j, k;
                for (i = j = k = 0; i < Solver.this.learnts.size()
                        && Solver.this.learnts.size() - k > maxsize; i++) {
                    Constr c = Solver.this.learnts.get(i);
                    if (c.locked() || c.size() == 2) {
                        Solver.this.learnts
                                .set(j++, Solver.this.learnts.get(i));
                    } else {
                        c.remove(Solver.this);
                        k++;
                    }
                }
                for (; i < Solver.this.learnts.size(); i++) {
                    Solver.this.learnts.set(j++, Solver.this.learnts.get(i));
                }
                if (Solver.this.verbose) {
                    Solver.this.out.log(getLogPrefix()
                            + "cleaning " + (Solver.this.learnts.size() - j) //$NON-NLS-1$
                            + " clauses out of " + Solver.this.learnts.size()); //$NON-NLS-1$ 
                    // out.flush();
                }
                Solver.this.learnts.shrinkTo(j);
            }

            @Override
			public void onConflictAnalysis(Constr reason) {
                // TODO Auto-generated method stub

            }

            @Override
			public void onClauseLearning(Constr outLearnt) {
                // TODO Auto-generated method stub

            }

            @Override
            public String toString() {
                return "Fixed size (" + maxsize
                        + ") learned constraints deletion strategy";
            }

            @Override
			public void init() {
            }

            @Override
			public ConflictTimer getTimer() {
                return this.aTimer;
            }

            @Override
			public void onPropagation(Constr from) {
                // TODO Auto-generated method stub

            }
        };
    }

    private LearnedConstraintsDeletionStrategy activityBased(
            final ConflictTimer timer) {
        return new LearnedConstraintsDeletionStrategy() {

            private static final long serialVersionUID = 1L;

            private final ConflictTimer freeMem = timer;

            @Override
			public void reduce(IVec<Constr> learnedConstrs) {
                sortOnActivity();
                int i, j;
                for (i = j = 0; i < Solver.this.learnts.size() / 2; i++) {
                    Constr c = Solver.this.learnts.get(i);
                    if (c.locked() || c.size() == 2) {
                        Solver.this.learnts
                                .set(j++, Solver.this.learnts.get(i));
                    } else {
                        c.remove(Solver.this);
                    }
                }
                for (; i < Solver.this.learnts.size(); i++) {
                    Solver.this.learnts.set(j++, Solver.this.learnts.get(i));
                }
                if (Solver.this.verbose) {
                    Solver.this.out.log(getLogPrefix()
                            + "cleaning " + (Solver.this.learnts.size() - j) //$NON-NLS-1$
                            + " clauses out of " + Solver.this.learnts.size()); //$NON-NLS-1$ 
                    // out.flush();
                }
                Solver.this.learnts.shrinkTo(j);
            }

            @Override
			public ConflictTimer getTimer() {
                return this.freeMem;
            }

            @Override
            public String toString() {
                return "Memory based learned constraints deletion strategy";
            }

            @Override
			public void init() {
                // do nothing
            }

            @Override
			public void onClauseLearning(Constr constr) {
                // do nothing

            }

            @Override
			public void onConflictAnalysis(Constr reason) {
                if (reason.learnt()) {
                    claBumpActivity(reason);
                }
            }

            @Override
			public void onPropagation(Constr from) {
                // do nothing
            }
        };
    }

    private final ConflictTimer memoryTimer = new ConflictTimerAdapter(500) {
        private static final long serialVersionUID = 1L;
        final long memorybound = Runtime.getRuntime().freeMemory() / 10;

        @Override
        public void run() {
            long freemem = Runtime.getRuntime().freeMemory();
            // System.out.println("c Free memory "+freemem);
            if (freemem < this.memorybound) {
                // Reduce the set of learnt clauses
                Solver.this.needToReduceDB = true;
            }
        }
    };

    /**
     * @since 2.1
     */
    public final LearnedConstraintsDeletionStrategy memory_based = activityBased(this.memoryTimer);

    private class GlucoseLCDS implements LearnedConstraintsDeletionStrategy {

        private static final long serialVersionUID = 1L;
        private int[] flags = new int[0];
        private int flag = 0;
        // private int wall = 0;

        private final ConflictTimer clauseManagement;

        GlucoseLCDS(ConflictTimer timer) {
            this.clauseManagement = timer;
        }

        @Override
		public void reduce(IVec<Constr> learnedConstrs) {
            sortOnActivity();
            int i, j;
            for (i = j = learnedConstrs.size() / 2; i < learnedConstrs.size(); i++) {
                Constr c = learnedConstrs.get(i);
                if (c.locked() || c.getActivity() <= 2.0) {
                    learnedConstrs.set(j++, Solver.this.learnts.get(i));
                } else {
                    c.remove(Solver.this);
                }
            }
            if (Solver.this.verbose) {
                Solver.this.out
                        .log(getLogPrefix()
                                + "cleaning " + (learnedConstrs.size() - j) //$NON-NLS-1$
                                + " clauses out of " + learnedConstrs.size() + " with flag " + this.flag + "/" + Solver.this.stats.conflicts); //$NON-NLS-1$ //$NON-NLS-2$
                // out.flush();
            }
            Solver.this.learnts.shrinkTo(j);

        }

        @Override
		public ConflictTimer getTimer() {
            return this.clauseManagement;
        }

        @Override
        public String toString() {
            return "Glucose learned constraints deletion strategy";
        }

        @Override
		public void init() {
            final int howmany = Solver.this.voc.nVars();
            // wall = constrs.size() > 10000 ? constrs.size() : 10000;
            if (this.flags.length <= howmany) {
                this.flags = new int[howmany + 1];
            }
            this.flag = 0;
            this.clauseManagement.reset();
        }

        @Override
		public void onClauseLearning(Constr constr) {
            int nblevel = computeLBD(constr);
            constr.incActivity(nblevel);
        }

        protected int computeLBD(Constr constr) {
            int nblevel = 1;
            this.flag++;
            int currentLevel;
            for (int i = 1; i < constr.size(); i++) {
                currentLevel = Solver.this.voc.getLevel(constr.get(i));
                if (this.flags[currentLevel] != this.flag) {
                    this.flags[currentLevel] = this.flag;
                    nblevel++;
                }
            }
            return nblevel;
        }

        @Override
		public void onConflictAnalysis(Constr reason) {

        }

        @Override
		public void onPropagation(Constr from) {

        }
    }

    private class Glucose2LCDS extends GlucoseLCDS {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        Glucose2LCDS(ConflictTimer timer) {
            super(timer);
        }

        @Override
        public String toString() {
            return "Glucose 2 learned constraints deletion strategy";
        }

        @Override
        public void onPropagation(Constr from) {
            if (from.getActivity() > 2.0) {
                int nblevel = computeLBD(from);
                if (nblevel < from.getActivity()) {
                    Solver.this.stats.updateLBD++;
                    from.setActivity(nblevel);
                }
            }
        }

    }

    private final ConflictTimer lbdTimer = new ConflictTimerAdapter(1000) {
        private static final long serialVersionUID = 1L;
        private int nbconflict = 0;
        private static final int MAX_CLAUSE = 5000;
        private static final int INC_CLAUSE = 1000;
        private int nextbound = MAX_CLAUSE;

        @Override
        public void run() {
            this.nbconflict += bound();
            if (this.nbconflict >= this.nextbound) {
                this.nextbound += INC_CLAUSE;
                // if (nextbound > wall) {
                // nextbound = wall;
                // }
                this.nbconflict = 0;
                Solver.this.needToReduceDB = true;
            }
        }

        @Override
        public void reset() {
            super.reset();
            this.nextbound = MAX_CLAUSE;
            if (this.nbconflict >= this.nextbound) {
                this.nbconflict = 0;
                Solver.this.needToReduceDB = true;
            }
        }
    };

    /**
     * @since 2.1
     */
    public final LearnedConstraintsDeletionStrategy glucose = new Glucose2LCDS(
            this.lbdTimer);

    protected LearnedConstraintsDeletionStrategy learnedConstraintsDeletionStrategy = this.glucose;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sat4j.minisat.core.ICDCL#setLearnedConstraintsDeletionStrategy(org
     * .sat4j.minisat.core.Solver.LearnedConstraintsDeletionStrategy)
     */
    @Override
	public void setLearnedConstraintsDeletionStrategy(
            LearnedConstraintsDeletionStrategy lcds) {
        if (this.getConflictCount() != null) {
            this.getConflictCount().add(lcds.getTimer());
            assert this.learnedConstraintsDeletionStrategy != null;
            this.getConflictCount().remove(this.learnedConstraintsDeletionStrategy
                    .getTimer());
        }
        this.learnedConstraintsDeletionStrategy = lcds;
    }

    private boolean lastConflictMeansUnsat;

    @Override
	public boolean isSatisfiable(IVecInt assumps, boolean global)
            throws TimeoutException {
        Lbool status = Lbool.UNDEFINED;
        boolean alreadylaunched = this.getConflictCount() != null;
        final int howmany = this.voc.nVars();
        if (this.mseen.length <= howmany) {
            this.mseen = new boolean[howmany + 1];
        }
        this.trail.ensure(howmany);
        this.trailLim.ensure(howmany);
        this.learnedLiterals.ensure(howmany);
        this.getDecisions().clear();
        this.getImplied().clear();
        this.slistener.init(this);
        this.slistener.start();
        this.setModel(null); // forget about previous model
        this.setUserbooleanmodel(null);
        this.prime = null;
        this.setUnsatExplanationInTermsOfAssumptions(null);
        if (!alreadylaunched || !this.keepHot) {
            this.order.init();
        }
        this.learnedConstraintsDeletionStrategy.init();
        int learnedLiteralsLimit = this.trail.size();

        // Fix for Bug SAT37
        this.setQhead(0);
        // Apply undos on unit literals because they are getting propagated
        // again now that qhead is 0.
        for (int i = learnedLiteralsLimit - 1; i >= 0; i--) {
            int p = this.trail.get(i);
            IVec<Undoable> undos = this.voc.undos(p);
            assert undos != null;
            for (int size = undos.size(); size > 0; size--) {
                undos.last().undo(p);
                undos.pop();
            }
        }
        // push previously learned literals
        for (IteratorInt iterator = this.learnedLiterals.iterator(); iterator
                .hasNext();) {
            enqueue(iterator.next());
        }

        // propagate constraints
        Constr confl = propagate();
        if (confl != null) {
            analyzeAtRootLevel(confl);
            this.slistener.conflictFound(confl, 0, 0);
            this.slistener.end(Lbool.FALSE);
            cancelUntil(0);
            cancelLearntLiterals(learnedLiteralsLimit);
            return false;
        }

        // push incremental assumptions
        for (IteratorInt iterator = assumps.iterator(); iterator.hasNext();) {
            int assump = iterator.next();
            int p = this.voc.getFromPool(assump);
            if (!this.voc.isSatisfied(p) && !assume(p)
                    || (confl = propagate()) != null) {
                if (confl == null) {
                    this.slistener.conflictFound(p);
                    this.setUnsatExplanationInTermsOfAssumptions(analyzeFinalConflictInTermsOfAssumptions(
                            null, assumps, p));
                    this.getUnsatExplanationInTermsOfAssumptions().push(assump);
                } else {
                    this.slistener.conflictFound(confl, decisionLevel(),
                            this.trail.size());
                    this.setUnsatExplanationInTermsOfAssumptions(analyzeFinalConflictInTermsOfAssumptions(
                            confl, assumps, ILits.UNDEFINED));
                }

                this.slistener.end(Lbool.FALSE);
                cancelUntil(0);
                cancelLearntLiterals(learnedLiteralsLimit);
                return false;
            }
        }
        this.rootLevel = decisionLevel();
        // moved initialization here if new literals are added in the
        // assumptions.
        if (!alreadylaunched || !this.keepHot) {
            this.order.init(); // duplicated on purpose
        }
        this.learner.init();

        if (!alreadylaunched) {
            this.setConflictCount(new ConflictTimerContainer());
            this.getConflictCount().add(this.getRestarter());
            this.getConflictCount().add(this.learnedConstraintsDeletionStrategy
                    .getTimer());
        }
        boolean firstTimeGlobal = false;
        if (this.timeBasedTimeout) {
            if (!global || this.timer == null) {
                firstTimeGlobal = true;
                this.undertimeout = true;
                TimerTask stopMe = new TimerTask() {
                    @Override
                    public void run() {
                        Solver.this.undertimeout = false;
                    }
                };
                this.timer = new Timer(true);
                this.timer.schedule(stopMe, this.timeout);

            }
        } else {
            if (!global || !alreadylaunched) {
                firstTimeGlobal = true;
                this.undertimeout = true;
                ConflictTimer conflictTimeout = new ConflictTimerAdapter(
                        (int) this.timeout) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void run() {
                        Solver.this.undertimeout = false;
                    }
                };
                this.getConflictCount().add(conflictTimeout);
            }
        }
        if (!global || firstTimeGlobal) {
            this.getRestarter().init(this.getParams(), this.stats);
            this.timebegin = System.currentTimeMillis();
        }
        this.needToReduceDB = false;
        // this is used to allow the solver to be incomplete,
        // when using a heuristics limited to a subset of variables
        this.setLastConflictMeansUnsat(true);
        // Solve
        while (status == Lbool.UNDEFINED && this.undertimeout
                && this.isLastConflictMeansUnsat()) {
            status = search(assumps);
            if (status == Lbool.UNDEFINED) {
                this.getRestarter().onRestart();
                this.slistener.restarting();
            }
        }

        cancelUntil(0);
        cancelLearntLiterals(learnedLiteralsLimit);
        if (!global && this.timeBasedTimeout && this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        this.slistener.end(status);
        if (!this.undertimeout) {
            String message = " Timeout (" + this.timeout
                    + (this.timeBasedTimeout ? "s" : " conflicts")
                    + ") exceeded";
            throw new TimeoutException(message);
        }
        if (status == Lbool.UNDEFINED && !this.isLastConflictMeansUnsat()) {
            throw new TimeoutException("Cannot decide the satisfiability");
        }
        // When using a search enumerator (to compute all models)
        // the final answer is FALSE, however we are aware of at least one model
        // (the last one)
        return getModel() != null;
    }

    @Override
	public void printInfos(PrintWriter out) {
        printInfos(out, prefix);
    }

    @Override
	public void printInfos(PrintWriter out, String prefix) {
        out.print(prefix);
        out.println("constraints type ");
        long total = 0;
        for (Map.Entry<String, Counter> entry : this.constrTypes.entrySet()) {
            out.println(prefix + entry.getKey() + " => " + entry.getValue());
            total += entry.getValue().getValue();
        }
        out.print(prefix);
        out.print(total);
        out.println(" constraints processed.");
    }

    /**
     * @since 2.1
     */
    public void printLearntClausesInfos(PrintWriter out, String prefix) {
        Map<String, Counter> learntTypes = new HashMap<String, Counter>();
        for (Iterator<Constr> it = this.learnts.iterator(); it.hasNext();) {
            String type = it.next().getClass().getName();
            Counter count = learntTypes.get(type);
            if (count == null) {
                learntTypes.put(type, new Counter());
            } else {
                count.inc();
            }
        }
        out.print(prefix);
        out.println("learnt constraints type ");
        for (Map.Entry<String, Counter> entry : learntTypes.entrySet()) {
            out.println(prefix + entry.getKey() + " => " + entry.getValue());
        }
    }

    @Override
	public SolverStats getStats() {
        return this.stats;
    }

    /**
     * 
     * @param myStats
     * @since 2.2
     */
    protected void initStats(SolverStats myStats) {
        this.stats = myStats;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.ICDCL#getOrder()
     */
    @Override
	public IOrder getOrder() {
        return this.order;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.ICDCL#setOrder(org.sat4j.minisat.core.IOrder)
     */
    @Override
	public void setOrder(IOrder h) {
        this.order = h;
        this.order.setLits(this.voc);
    }

    public ILits getVocabulary() {
        return this.voc;
    }

    @Override
	public void reset() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        this.trail.clear();
        this.trailLim.clear();
        this.setQhead(0);
        for (Iterator<Constr> iterator = this.constrs.iterator(); iterator
                .hasNext();) {
            iterator.next().remove(this);
        }
        this.constrs.clear();
        clearLearntClauses();
        this.voc.resetPool();
        this.dsfactory.reset();
        this.stats.reset();
        this.constrTypes.clear();
    }

    @Override
	public int nVars() {
        if (this.declaredMaxVarId == 0) {
            return this.voc.nVars();
        }
        return this.declaredMaxVarId;
    }

    /**
     * @param constr
     *            a constraint implementing the Constr interface.
     * @return a reference to the constraint for external use.
     */
    protected IConstr addConstr(Constr constr) {
        if (constr == null) {
            Counter count = this.constrTypes
                    .get("ignored satisfied constraints");
            if (count == null) {
                this.constrTypes.put("ignored satisfied constraints",
                        new Counter());
            } else {
                count.inc();
            }
        } else {
            this.constrs.push(constr);
            String type = constr.getClass().getName();
            Counter count = this.constrTypes.get(type);
            if (count == null) {
                this.constrTypes.put(type, new Counter());
            } else {
                count.inc();
            }
        }
        return constr;
    }

    public DataStructureFactory getDSFactory() {
        return this.dsfactory;
    }

    public IVecInt getOutLearnt() {
        return this.moutLearnt;
    }

    /**
     * returns the ith constraint in the solver.
     * 
     * @param i
     *            the constraint number (begins at 0)
     * @return the ith constraint
     */
    public IConstr getIthConstr(int i) {
        return this.constrs.get(i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.ISolver#printStat(java.io.PrintStream,
     * java.lang.String)
     */
    @Override
	public void printStat(PrintStream out, String prefix) {
        printStat(new PrintWriter(out, true), prefix);
    }

    @Override
	public void printStat(PrintWriter out) {
        printStat(out, prefix);
    }

    @Override
	public void printStat(PrintWriter out, String prefix) {
        this.stats.printStat(out, prefix);
        double cputime = (System.currentTimeMillis() - this.timebegin) / 1000;
        out.println(prefix
                + "speed (assignments/second)\t: " + this.stats.propagations //$NON-NLS-1$
                / cputime);
        this.order.printStat(out, prefix);
        printLearntClausesInfos(out, prefix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString(String prefix) {
        StringBuffer stb = new StringBuffer();
        Object[] objs = { this.dsfactory, this.learner, this.getParams(),
                this.order, this.simplifier, this.getRestarter(),
                this.learnedConstraintsDeletionStrategy };
        stb.append(prefix);
        stb.append("--- Begin Solver configuration ---"); //$NON-NLS-1$
        stb.append("\n"); //$NON-NLS-1$
        for (Object o : objs) {
            stb.append(prefix);
            stb.append(o.toString());
            stb.append("\n"); //$NON-NLS-1$
        }
        stb.append(prefix);
        stb.append("timeout=");
        if (this.timeBasedTimeout) {
            stb.append(this.timeout / 1000);
            stb.append("s\n");
        } else {
            stb.append(this.timeout);
            stb.append(" conflicts\n");
        }
        stb.append(prefix);
        stb.append("DB Simplification allowed=");
        stb.append(this.isDBSimplificationAllowed);
        stb.append("\n");
        stb.append(prefix);
        if (isSolverKeptHot()) {
            stb.append("Heuristics kept accross calls (keep the solver \"hot\")\n");
            stb.append(prefix);
        }
        stb.append("Listener: ");
        stb.append(slistener);
        stb.append("\n");
        stb.append(prefix);
        stb.append("--- End Solver configuration ---"); //$NON-NLS-1$
        return stb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return toString(""); //$NON-NLS-1$
    }

    @Override
	public int getTimeout() {
        return (int) (this.timeBasedTimeout ? this.timeout / 1000
                : this.timeout);
    }

    /**
     * @since 2.1
     */
    @Override
	public long getTimeoutMs() {
        if (!this.timeBasedTimeout) {
            throw new UnsupportedOperationException(
                    "The timeout is given in number of conflicts!");
        }
        return this.timeout;
    }

    @Override
	public void setExpectedNumberOfClauses(int nb) {
        this.constrs.ensure(nb);
    }

    @Override
	public Map<String, Number> getStat() {
        return this.stats.toMap();
    }

    @Override
	public int[] findModel() throws TimeoutException {
        if (isSatisfiable()) {
            return model();
        }
        // DLB findbugs ok
        // A zero length array would mean that the formula is a tautology.
        return null;
    }

    @Override
	public int[] findModel(IVecInt assumps) throws TimeoutException {
        if (isSatisfiable(assumps)) {
            return model();
        }
        // DLB findbugs ok
        // A zero length array would mean that the formula is a tautology.
        return null;
    }

    @Override
	public boolean isDBSimplificationAllowed() {
        return this.isDBSimplificationAllowed;
    }

    @Override
	public void setDBSimplificationAllowed(boolean status) {
        this.isDBSimplificationAllowed = status;
    }

    /**
     * @since 2.1
     */
    @Override
	public int nextFreeVarId(boolean reserve) {
        return this.voc.nextFreeVarId(reserve);
    }

    /**
     * @since 2.1
     */
    @Override
	public IConstr addBlockingClause(IVecInt literals)
            throws ContradictionException {
        return addClause(literals);
    }

    /**
     * @since 2.1
     */
    @Override
	public void unset(int p) {
        // the literal might already have been
        // removed from the trail.
        if (this.voc.isUnassigned(p) || this.trail.isEmpty()) {
            return;
        }
        int current = this.trail.last();
        while (current != p) {
            undoOne();
            if (this.trail.isEmpty()) {
                return;
            }
            current = this.trail.last();
        }
        undoOne();
        this.setQhead(this.trail.size());
    }

    /**
     * @since 2.2
     */
    @Override
	public void setLogPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @since 2.2
     */
    @Override
	public String getLogPrefix() {
        return this.prefix;
    }

    /**
     * @since 2.2
     */
    @Override
	public IVecInt unsatExplanation() {
        IVecInt copy = new VecInt(
                this.getUnsatExplanationInTermsOfAssumptions().size());
        this.getUnsatExplanationInTermsOfAssumptions().copyTo(copy);
        return copy;
    }

    /**
     * @since 2.3.1
     */
    @Override
	public int[] modelWithInternalVariables() {
        if (this.getModel() == null) {
            throw new UnsupportedOperationException(
                    "Call the solve method first!!!"); //$NON-NLS-1$
        }
        int[] nmodel;
        if (nVars() == realNumberOfVariables()) {
            nmodel = new int[this.getModel().length];
            System.arraycopy(this.getModel(), 0, nmodel, 0, nmodel.length);
        } else {
            nmodel = new int[this.getFullmodel().length];
            System.arraycopy(this.getFullmodel(), 0, nmodel, 0, nmodel.length);
        }

        return nmodel;
    }

    /**
     * @since 2.3.1
     */
    @Override
	public int realNumberOfVariables() {
        return this.voc.nVars();
    }

    /**
     * @since 2.3.2
     */
    @Override
	public void stop() {
        expireTimeout();
    }

    protected Constr sharedConflict;

    /**
     * @since 2.3.2
     */
    @Override
	public void backtrack(int[] reason) {
        IVecInt clause = new VecInt(reason.length);
        for (int d : reason) {
            clause.push(LiteralsUtils.toInternal(d));
        }
        this.sharedConflict = this.dsfactory.createUnregisteredClause(clause);
        learn(this.sharedConflict);
    }

    /**
     * @since 2.3.2
     */
    @Override
	public Lbool truthValue(int literal) {
        int p = LiteralsUtils.toInternal(literal);
        if (this.voc.isFalsified(p)) {
            return Lbool.FALSE;
        }
        if (this.voc.isSatisfied(p)) {
            return Lbool.TRUE;
        }
        return Lbool.UNDEFINED;
    }

    /**
     * @since 2.3.2
     */
    @Override
	public int currentDecisionLevel() {
        return decisionLevel();
    }

    /**
     * @since 2.3.2
     */
    @Override
	public int[] getLiteralsPropagatedAt(int decisionLevel) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    /**
     * @since 2.3.2
     */
    @Override
	public void suggestNextLiteralToBranchOn(int l) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    protected boolean isNeedToReduceDB() {
        return this.needToReduceDB;
    }

    @Override
	public void setNeedToReduceDB(boolean needToReduceDB) {
        this.needToReduceDB = needToReduceDB;
    }

    @Override
	public void setLogger(ILogAble out) {
        this.out = out;
    }

    @Override
	public ILogAble getLogger() {
        return this.out;
    }

    @Override
	public double[] getVariableHeuristics() {
        return this.order.getVariableHeuristics();
    }

    @Override
	public IVec<Constr> getLearnedConstraints() {
        return this.learnts;
    }

    /**
     * @since 2.3.2
     */
    @Override
	public void setLearnedConstraintsDeletionStrategy(ConflictTimer timer,
            LearnedConstraintsEvaluationType evaluation) {
        if (this.getConflictCount() != null) {
            this.getConflictCount().add(timer);
            this.getConflictCount().remove(this.learnedConstraintsDeletionStrategy
                    .getTimer());
        }
        switch (evaluation) {
        case ACTIVITY:
            this.learnedConstraintsDeletionStrategy = activityBased(timer);
            break;
        case LBD:
            this.learnedConstraintsDeletionStrategy = new GlucoseLCDS(timer);
            break;
        case LBD2:
            this.learnedConstraintsDeletionStrategy = new Glucose2LCDS(timer);
            break;
        }
        if (this.getConflictCount() != null) {
            this.learnedConstraintsDeletionStrategy.init();
        }
    }

    /**
     * @since 2.3.2
     */
    @Override
	public void setLearnedConstraintsDeletionStrategy(
            LearnedConstraintsEvaluationType evaluation) {
        ConflictTimer aTimer = this.learnedConstraintsDeletionStrategy
                .getTimer();
        switch (evaluation) {
        case ACTIVITY:
            this.learnedConstraintsDeletionStrategy = activityBased(aTimer);
            break;
        case LBD:
            this.learnedConstraintsDeletionStrategy = new GlucoseLCDS(aTimer);
            break;
        case LBD2:
            this.learnedConstraintsDeletionStrategy = new Glucose2LCDS(aTimer);
            break;
        }
        if (this.getConflictCount() != null) {
            this.learnedConstraintsDeletionStrategy.init();
        }
    }

    @Override
	public boolean isSolverKeptHot() {
        return this.keepHot;
    }

    @Override
	public void setKeepSolverHot(boolean keepHot) {
        this.keepHot = keepHot;
    }

    @Override
	public IConstr addClauseOnTheFly(int[] literals) {
        IVecInt clause = new VecInt(literals.length);
        for (int d : literals) {
            clause.push(LiteralsUtils.toInternal(d));
        }
        this.sharedConflict = this.dsfactory.createUnregisteredClause(clause);
        this.sharedConflict.register();
        addConstr(this.sharedConflict);
        IVecInt reason = new VecInt();
        this.sharedConflict.calcReasonOnTheFly(ILits.UNDEFINED, trail, reason);
        while (!trail.isEmpty() && !reason.contains(trail.last())) {
            undoOne();
            if (!trailLim.isEmpty() && trailLim.last() == trail.size()) {
                trailLim.pop();
            }
        }
        return this.sharedConflict;
    }

    @Override
	public ISolver getSolvingEngine() {
        return this;
    }

    /**
     * 
     * @param literals
     */
    @Override
	public IConstr addAtMostOnTheFly(int[] literals, int degree) {
        IVecInt clause = new VecInt(literals.length);
        for (int d : literals) {
            clause.push(LiteralsUtils.toInternal(-d));
        }
        IVecInt copy = new VecInt(clause.size());
        clause.copyTo(copy);
        this.sharedConflict = this.dsfactory
                .createUnregisteredCardinalityConstraint(copy, literals.length
                        - degree);
        this.sharedConflict.register();
        addConstr(this.sharedConflict);
        // backtrack to the first decision level with a reason
        // for falsifying that constraint
        IVecInt reason = new VecInt();
        this.sharedConflict.calcReasonOnTheFly(ILits.UNDEFINED, trail, reason);
        while (!trail.isEmpty() && !reason.contains(trail.last())) {
            undoOne();
            if (!trailLim.isEmpty() && trailLim.last() == trail.size()) {
                trailLim.pop();
            }
        }
        return this.sharedConflict;
    }

	public ConflictTimerContainer getConflictCount() {
		return conflictCount;
	}

	public void setConflictCount(ConflictTimerContainer conflictCount) {
		this.conflictCount = conflictCount;
	}

	public boolean isLastConflictMeansUnsat() {
		return lastConflictMeansUnsat;
	}

	public void setLastConflictMeansUnsat(boolean lastConflictMeansUnsat) {
		this.lastConflictMeansUnsat = lastConflictMeansUnsat;
	}

	public IVecInt getUnsatExplanationInTermsOfAssumptions() {
		return unsatExplanationInTermsOfAssumptions;
	}

	public void setUnsatExplanationInTermsOfAssumptions(
			IVecInt unsatExplanationInTermsOfAssumptions) {
		this.unsatExplanationInTermsOfAssumptions = unsatExplanationInTermsOfAssumptions;
	}

	public Pair getAnalysisResult() {
		return analysisResult;
	}

	public RestartStrategy getRestarter() {
		return restarter;
	}

	public void setRestarter(RestartStrategy restarter) {
		this.restarter = restarter;
	}

	public int[] getModel() {
		return model;
	}

	public void setModel(int[] model) {
		this.model = model;
	}

	public int[] getFullmodel() {
		return fullmodel;
	}

	public void setFullmodel(int[] fullmodel) {
		this.fullmodel = fullmodel;
	}

	public int getQhead() {
		return qhead;
	}

	public void setQhead(int qhead) {
		this.qhead = qhead;
	}

	public SearchParams getParams() {
		return params;
	}

	public void setParams(SearchParams params) {
		this.params = params;
	}

	public double getClaDecay() {
		return claDecay;
	}

	public void setClaDecay(double claDecay) {
		this.claDecay = claDecay;
	}

	public boolean[] getUserbooleanmodel() {
		return userbooleanmodel;
	}

	public void setUserbooleanmodel(boolean[] userbooleanmodel) {
		this.userbooleanmodel = userbooleanmodel;
	}

	public IVecInt getDecisions() {
		return decisions;
	}

	public IVecInt getImplied() {
		return implied;
	}
}
/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/SQLBinding.java

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
package dag.satmining.problem.satql.ast;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
import dag.satmining.problem.satql.ast.intermediate.BFormula;
import dag.satmining.problem.satql.ast.intermediate.BNeg;
import dag.satmining.problem.satql.ast.intermediate.LiteralOrValue;
import dag.satmining.problem.satql.ast.sql.BitSetFetcher;
import dag.satmining.problem.satql.ast.sql.BitSetWithRowNumbers;
import dag.satmining.problem.satql.ast.sql.SQLBooleanValue;
import dag.satmining.problem.satql.ast.sql.QuantifierGeneralInformations;


/**
 * 
 * @author ecoquery
 */
public class SQLBinding {

    private static final Logger LOG = LoggerFactory.getLogger(SQLBinding.class);

    private final MiningExpression _mainExpression;
    private final List<AttributeConstant> _attributes;
    private final Map<SchemaVariable, Map<AttributeConstant, Integer>> _domain;
    private final List<SQLBooleanValue> _selectStatements = new ArrayList<SQLBooleanValue>();
    private final ASTDictionnary _dict;
    private final boolean _doCache;


    public SQLBinding(
            MiningExpression e,
            List<AttributeConstant> atts,
            Map<SchemaVariable, Map<AttributeConstant, Integer>> providedDomain,
            ASTDictionnary dict, boolean doCache) {
        this._mainExpression = e;
        this._attributes = atts;
        this._domain = providedDomain;
        this._dict = dict;
        this._doCache = doCache;
        int maxAttId = 0;
        for (AttributeConstant att : atts) {
            maxAttId = Math.max(maxAttId, att.getId());
        }
        e.registerSQLExpressions(_attributes, maxAttId, this, _dict);
    }

    public MiningExpression getMainExpression() {
        return _mainExpression;
    }

    private class CacheEnabler extends AbstractMiningExpressionVisitor {
       
        @Override
        public void exists(MiningExpression e, AttributeVariable av,
                SchemaVariable sv, MiningExpression a) {
            e.enableCache(_doCache || e.isDataIndependant());
        }

        @Override
        public void forall(MiningExpression e, AttributeVariable av,
                SchemaVariable sv, MiningExpression a) {
            e.enableCache(_doCache || e.isDataIndependant());
        }

    }

    public <L extends Literal<L>> void runEvaluation(PBBuilder<L> handler,
            BitSetFetcher bsr, List<SchemaVariable> toMinimize,
            List<SchemaVariable> toMaximize) throws NoSolutionException,
            SQLException, IOException {
        if (_doCache) {
            LOG.debug("Enabling cache in main expression and quantifiers");
            _mainExpression.enableCache(true);
        } else {
            LOG.debug("Caching only mining expressions independant of data");
        }
        // CacheEnabler enables cache only for data independant formulas if _doCache is false   
        _mainExpression.acceptPrefix(new CacheEnabler()); 
        LOG.debug("Generating intermediate formulas");
        int nbConfigs = 1 + _attributes.size()
                * (toMinimize.size() + toMaximize.size());
        ForceAttributeSchema[] minMaxCfg = new ForceAttributeSchema[nbConfigs];
        minMaxCfg[0] = new ForceAttributeSchema(null, null, true);
        int cfg = 0;
        for (SchemaVariable sv : toMinimize) {
            for (AttributeConstant att : _attributes) {
                minMaxCfg[++cfg] = new ForceAttributeSchema(att, sv, false);
            }
        }
        for (SchemaVariable sv : toMaximize) {
            for (AttributeConstant att : _attributes) {
                minMaxCfg[++cfg] = new ForceAttributeSchema(att, sv, true);
            }
        }
        BFormula[] intermediateFormulas = new BFormula[nbConfigs];
        @SuppressWarnings("unchecked")
        Set<LiteralOrValue>[] minMaxLitSets = new Set[nbConfigs];

        for (cfg = 0; cfg < nbConfigs; cfg++) {
            minMaxLitSets[cfg] = new HashSet<LiteralOrValue>();
            ForceAttributeSchema fas = minMaxCfg[cfg];
            intermediateFormulas[cfg] = _mainExpression.getIntermediateFormula(
                    new AttributeValuation(_dict), _attributes, fas, _domain);
            if (cfg > 0) {
                intermediateFormulas[cfg] = new BNeg(intermediateFormulas[cfg]);
            }
        }
        LOG.debug("Running query for generating SAT problem ...");
        int nbTupleComb = 0;
        BFormula.cacheHits = 0;
        int nbCachedTuples = 0;
                
        while (bsr.next()) { // for all tuple combination
            BitSetWithRowNumbers bswrn = bsr.getBitSet();
        	BitSet data = bswrn.getBitSet();
        	
        	nbTupleComb++;
            int oldCacheHits = BFormula.cacheHits;
            for (cfg = 0; cfg < nbConfigs; cfg++) {
                minMaxLitSets[cfg].add(intermediateFormulas[cfg].getRepresentation(handler, data));
             
            }
            if (oldCacheHits != BFormula.cacheHits) {
                nbCachedTuples++;
            }
        }
        LOG.info(
                "Generated formula from {} tuple combinations, {} cache hits ({} full hits) on {} combinations",
                new Object[] { nbTupleComb, BFormula.cacheHits,
                        intermediateFormulas[0].getCacheHits(), nbCachedTuples });
        if (minMaxLitSets[0].contains(LiteralOrValue.FALSE)) {
            throw new NoSolutionException(
                    "Formula is always false for some tuple combinaition");
        } else {
            minMaxLitSets[0].remove(LiteralOrValue.TRUE);
            for (LiteralOrValue lv : minMaxLitSets[0]) {
                handler.addClause(lv.getLiteral(handler));
            }
        }
        for (cfg = 1; cfg < nbConfigs; cfg++) {
            ForceAttributeSchema fas = minMaxCfg[cfg];
            Set<LiteralOrValue> lvs = minMaxLitSets[cfg];
            lvs.remove(LiteralOrValue.FALSE);
            L repr = handler.fromDimacs(_domain.get(fas.getSchemaVariable())
                    .get(fas.getAttribute()));
            if (lvs.contains(LiteralOrValue.TRUE)) {
                // nothing to do, the implication is trivially satisfied
            } else {
                Collection<L> clause = new ArrayList<L>();
                clause.add(fas.isInSet() ? repr : repr.getOpposite());
                for (LiteralOrValue lv : lvs) {
                    clause.add(lv.getLiteral(handler));
                }
                handler.addClause(clause);
            }
        }
    }
    public <L extends Literal<L>> void runEvaluation(ReifiedWeightedPBBuilder<L> handler, List<QuantifierGeneralInformations> quantifierInformationsList,
            BitSetFetcher bsr, List<SchemaVariable> toMinimize,
            List<SchemaVariable> toMaximize) throws NoSolutionException,
            SQLException, IOException {
        if (_doCache) {
            LOG.debug("Enabling cache in main expression and quantifiers");
            _mainExpression.enableCache(true);
        } else {
            LOG.debug("Caching only mining expressions independant of data");
        }
        // CacheEnabler enables cache only for data independant formulas if _doCache is false   
        _mainExpression.acceptPrefix(new CacheEnabler()); 
        LOG.debug("Generating intermediate formulas");
        ForceAttributeSchema minMaxCfg = new ForceAttributeSchema(null, null, true);
        
        BFormula intermediateFormulas = _mainExpression.getIntermediateFormula(
        		new AttributeValuation(_dict), _attributes, minMaxCfg, _domain);
        
        QuantifierMap<L> quantifierMap = new QuantifierMap<L>();

        LOG.debug("Running query for generating SAT problem ...");
        int nbTupleComb = 0;
        BFormula.cacheHits = 0;
        int nbCachedTuples = 0;
                
        while (bsr.next()) { // for all tuple combination
            BitSetWithRowNumbers bitSetWithRowNumbers = bsr.getBitSet();
        	LiteralOrValue insertedLiteralOrValue = intermediateFormulas.getRepresentation(handler, bitSetWithRowNumbers.getBitSet());
        	L insertedLiteral = insertedLiteralOrValue.getLiteral(handler);
        	
        	quantifierMap.addLiteral(1, bitSetWithRowNumbers, insertedLiteral);
        	LOG.debug("taille niveau 1 hashmap: " + quantifierMap.getQuantifierMap().size());
        	
        	nbTupleComb++;
            int oldCacheHits = BFormula.cacheHits;

            if (oldCacheHits != BFormula.cacheHits) {
                nbCachedTuples++;
            }
        }
        LOG.info(
                "Generated formula from {} tuple combinations, {} cache hits ({} full hits) on {} combinations",
                new Object[] { nbTupleComb, BFormula.cacheHits,
                        intermediateFormulas.getCacheHits(), nbCachedTuples });
        
		L res = handler.newLiteral(true, false);
    	quantifierMap.createFormula(handler, quantifierInformationsList, res);
    	LOG.info("Literal a la racine: " + res.toString());
          
    	handler.addClause(res);
    }
    
    public List<SQLBooleanValue> getSelectStatements() {
        return _selectStatements;
    }

    public int registerSQLStatement(SQLBooleanValue statement) {
        if (statement == null)
            throw new IllegalArgumentException(
                    "trying to register null statement");
        int idx = _selectStatements.size();
        _selectStatements.add(statement);
        return idx;
    }
}

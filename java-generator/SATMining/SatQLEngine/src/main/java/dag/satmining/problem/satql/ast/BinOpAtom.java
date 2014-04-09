/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/BinOpAtom.java

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dag.satmining.problem.satql.ast.intermediate.AtomicHolder;
import dag.satmining.problem.satql.ast.intermediate.BFormula;
import dag.satmining.problem.satql.ast.sql.Comparison;
import dag.satmining.problem.satql.ast.sql.SQLBooleanValue;
import dag.satmining.problem.satql.ast.sql.SQLValue;
import dag.satmining.utils.BoundedIntPrefixTree;
import dag.satmining.utils.UnreachableException;

/**
 * 
 * @author ecoquery
 */
public final class BinOpAtom extends AtomicMiningExpression {

    private final Op _op;
    private final MiningValue _a;
    private final MiningValue _b;
    private final AttributeVariable[] _variables;
    private BoundedIntPrefixTree<AtomicHolder> _intermediateLiterals;

    private BinOpAtom(MiningValue a, MiningValue b, Op op) {
        this._a = a;
        this._b = b;
        this._op = op;
        Set<AttributeVariable> var = new HashSet<AttributeVariable>();
        var.addAll(a.getAttributeVariables());
        var.addAll(b.getAttributeVariables());
        this._variables = var.toArray(new AttributeVariable[var.size()]);
    }

    @Override
    public MiningExpression pushDown(Quantifier q, AttributeVariable av,
            SchemaVariable s) {
        if (_a.getAttributeVariables().contains(av)
                || _b.getAttributeVariables().contains(av)) {
            return new AttributeQuantifier(q, av, s, this);
        } else {
            return this;
        }
    }

    @Override
    public MiningExpression pushDown() {
        return this;
    }

    @Override
    public Set<AttributeVariable> getFreeAttVariables() {
        Set<AttributeVariable> s = _a.getAttributeVariables();
        s.addAll(_b.getAttributeVariables());
        return s;
    }

    @Override
    public String toString() {
        return _op.applyTo(_a.toString(), _b.toString());
    }

    @Override
    public <E> E accept(Visitor<E> v) {
        return v.eq(_a, _b);
    }

    public enum Op {

        Eq, Like;

        public String applyTo(String fst, String snd) {
            switch (this) {
            case Eq:
                return fst + " = " + snd;
            case Like:
                return fst + " LIKE " + snd;
            }
            return null;
        }
    }

    public static BinOpAtom eq(MiningValue a, MiningValue b) {
        return new BinOpAtom(a, b, Op.Eq);
    }

    public static BinOpAtom like(MiningValue a, MiningValue b) {
        return new BinOpAtom(a, b, Op.Like);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BinOpAtom other = (BinOpAtom) obj;
        if (_a == null) {
            if (other._a != null)
                return false;
        } else if (!_a.equals(other._a))
            return false;
        if (_b == null) {
            if (other._b != null)
                return false;
        } else if (!_b.equals(other._b))
            return false;
        if (_op != other._op)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_a == null) ? 0 : _a.hashCode());
        result = prime * result + ((_b == null) ? 0 : _b.hashCode());
        result = prime * result + ((_op == null) ? 0 : _op.hashCode());
        return result;
    }

    @Override
    public void acceptPrefix(VoidVisitor v) {
        switch (_op) {
        case Eq:
            v.eq(this, _a, _b);
            break;
        case Like:
            v.like(this, _a, _b);
            break;
        default:
            throw new UnreachableException();
        }
    }

    @Override
    void registerSQLExpressions(Collection<AttributeConstant> atts,
            int maxAttId, SQLBinding binding, ASTDictionnary dict) {
        if (_intermediateLiterals == null) {
            _intermediateLiterals = new BoundedIntPrefixTree<AtomicHolder>(0,
                    maxAttId + 1);
        }
        registerSQLExpressionsAndBuildBFormulas(atts, binding, 0,
                new int[_variables.length], new AttributeValuation(dict));
    }

    private void registerSQLExpressionsAndBuildBFormulas(
            Collection<AttributeConstant> atts, SQLBinding binding, int i,
            int[] valuation, AttributeValuation attVal) {
        if (i < _variables.length) {
            for (AttributeConstant att : atts) {
                valuation[i] = att.getId();
                attVal.set(_variables[i], att);
                registerSQLExpressionsAndBuildBFormulas(atts, binding, i + 1,
                        valuation, attVal);
            }
        } else {
            SQLValue a = _a.generateSQLExpression(attVal);
            SQLValue b = _b.generateSQLExpression(attVal);
            SQLBooleanValue cmp = null;
            switch (_op) {
            case Eq:
                cmp = Comparison.eq(a, b);
                break;
            case Like:
                cmp = Comparison.like(a, b);
                break;
            default:
                throw new UnreachableException();
            }
            int exprIdx = binding.registerSQLStatement(cmp);
            AtomicHolder h = new AtomicHolder(exprIdx);
            _intermediateLiterals.put(valuation, h);
        }
    }

    @Override
    public BFormula buildIntermediateFormula(AttributeValuation attVal,
            Collection<AttributeConstant> atts, ForceAttributeSchema fas,
            Map<SchemaVariable, Map<AttributeConstant, Integer>> domain) {
        int[] interp = new int[_variables.length];
        for (int i = 0; i < interp.length; i++) {
            interp[i] = attVal.getInt(_variables[i]);
        }
        return _intermediateLiterals.get(interp);
    }

    @Override
    protected boolean isDataIndependant() {
        return false;
    }
}

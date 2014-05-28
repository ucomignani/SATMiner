/* SatQLEngine/src/main/java/dag/satmining/problem/satql/ast/SQLDelegateAtom.java

   Copyright (C) 2014 Emmanuel Coquery.

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

package dag.satmining.problem.satql.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.problem.satql.ast.intermediate.AtomicHolder;
import dag.satmining.problem.satql.ast.intermediate.BFormula;
import dag.satmining.problem.satql.ast.sql.RAWSQLAtom;
import dag.satmining.problem.satql.ast.sql.SQLBooleanValue;
import dag.satmining.utils.BoundedIntPrefixTree;

public class SQLDelegateAtom extends MiningExpression {

    private final static Logger LOG = LoggerFactory
            .getLogger(SQLDelegateAtom.class);

    private RAWSQLAtom _delegate;
    private Set<AttributeVariable> _variables = new HashSet<AttributeVariable>();
    private AttributeVariable[] _variablesArray;
    private List<AtomPart> _parts;
    private BoundedIntPrefixTree<AtomicHolder> _intermediateFormulaHoles;

    public SQLDelegateAtom(RAWSQLAtom sql, ASTDictionnary dict) {
        this._delegate = sql;
        this._parts = buildDecomposition(sql.getExpression(), dict);
        this._variablesArray = _variables
                .toArray(new AttributeVariable[_variables.size()]);
        LOG.debug("Delegate atom parsed to {}", _parts);
    }

    private static class AtomPart {
        private String _val;
        private AttributeVariable _variable;

        public AtomPart(String value, AttributeVariable var) {
            _val = value;
            _variable = var;
        }

        public void render(Map<AttributeVariable, AttributeConstant> mapping,
                StringBuilder output) {
            if (_val == null) { // we have a variable
                output.append(mapping.get(_variable).getName());
            } else {
                output.append(_val.replace("$$", "$"));
            }
        }

        public String toString() {
            if (_val == null) {
                return _variable.toString();
            } else {
                return "RAW(" + _val + ")";
            }
        }
    }

    static final Pattern IDENTIFIER_PATTERN = Pattern
            .compile("\\$([A-Za-z_][0-9A-Za-z_]*)");
    static final Pattern OTHER_PATTERN = Pattern
            .compile("([^0-9A-Za-z_$]|\\$\\$)([^$]|\\$\\$|\\$$)*");

    private List<AtomPart> buildDecomposition(String rawStatement,
            ASTDictionnary dict) {
        List<AtomPart> res = new ArrayList<SQLDelegateAtom.AtomPart>();
        rawStatement = " " + rawStatement;
        Matcher mI = IDENTIFIER_PATTERN.matcher(rawStatement);
        Matcher mO = OTHER_PATTERN.matcher(rawStatement);
        int start = 0;
        while (start < rawStatement.length()) {
            if (mI.find(start) && mI.start() == start) {
                AttributeVariable var = dict.getAttributeVariable(mI.group(1));
                _variables.add(var);
                LOG.debug("found var @ {},{}: {}", mI.start(1), mI.end(1),
                        mI.group(1));
                res.add(new AtomPart(null, var));
                start = mI.end();
                LOG.debug("restart 1 at {}: {}", start,
                        rawStatement.substring(start));
            } else if (mO.find(start) && mO.start() == start) {
                res.add(new AtomPart(mO.group(), null));
                LOG.debug("found sql @ {},{}: {}", mO.start(), mO.end(),
                        mO.group());
                start = mO.end();
                LOG.debug("restart 2 at {}: {}", start,
                        rawStatement.substring(start));
            } else {
                throw new IllegalArgumentException(
                        "Could not parse raw statement '" + rawStatement
                                + "' starting from '"
                                + rawStatement.substring(start) + "'");
            }
        }
        return res;
    }

    @Override
    void registerSQLExpressions(Collection<AttributeConstant> atts,
            int maxAttId, SQLBinding binding, ASTDictionnary dict) {
        if (_intermediateFormulaHoles == null) {
            _intermediateFormulaHoles = new BoundedIntPrefixTree<AtomicHolder>(
                    0, maxAttId + 1);
        }
        registerSQLExpressions(atts, 0, binding, dict,
                new HashMap<AttributeVariable, AttributeConstant>(),
                new int[_variablesArray.length]);
    }

    private void registerSQLExpressions(Collection<AttributeConstant> atts,
            int curVar, SQLBinding binding, ASTDictionnary dict,
            Map<AttributeVariable, AttributeConstant> valuation,
            int[] intValuation) {
        if (curVar < _variablesArray.length) {
            for (AttributeConstant cst : atts) {
                valuation.put(_variablesArray[curVar], cst);
                intValuation[curVar] = cst.getId();
                registerSQLExpressions(atts, curVar + 1, binding, dict,
                        valuation, intValuation);
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (AtomPart part : _parts) {
                part.render(valuation, sb);
            }
            SQLBooleanValue expr = new RAWSQLAtom(sb.toString());
            int offset = binding.registerSQLStatement(expr);
            AtomicHolder ho = new AtomicHolder(offset);
            _intermediateFormulaHoles.put(intValuation, ho);
        }
    }

    @Override
    BFormula buildIntermediateFormula(AttributeValuation attVal,
            Collection<AttributeConstant> atts, ForceAttributeSchema fas,
            Map<SchemaVariable, Map<AttributeConstant, Integer>> domain) {
        int[] key = new int[_variablesArray.length];
        for (int i = 0; i < key.length; ++i) {
            key[i] = attVal.getInt(_variablesArray[i]);
        }
        return _intermediateFormulaHoles.get(key);
    }

    @Override
    protected MiningExpression pushDown(Quantifier q, AttributeVariable av,
            SchemaVariable s) {
        if (_variables.contains(av)) {
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
        return _variables;
    }

    @Override
    public String toString() {
        return "{" + _delegate.getExpression() + "}";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_delegate == null) ? 0 : _delegate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SQLDelegateAtom other = (SQLDelegateAtom) obj;
        if (_delegate == null) {
            if (other._delegate != null)
                return false;
        } else if (!_delegate.equals(other._delegate))
            return false;
        return true;
    }

    @Override
    public <E> E accept(Visitor<E> v) {
        return v.sqlAtom(this);
    }

    @Override
    public void acceptPrefix(VoidVisitor v) {
        v.sqlAtom(this, _delegate);
    }

    @Override
    protected boolean isDataIndependant() {
        return false;
    }

}

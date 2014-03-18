/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/ASTDictionnary.java

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author ecoquery
 */
public final class ASTDictionnary {

	private final Map<String, TupleVariable> _tupleVariables;
	private final Map<String, AttributeConstant> _attributes;
	private final Map<String, AttributeVariable> _attributeVariables;
	private final Map<String, SchemaVariable> _schemaVariables;
	private final List<SchemaVariable> _schemaVariablesByIndex;
	private final Map<MiningExpression, MiningExpression> _miningExpressions;

	public ASTDictionnary() {
		this._tupleVariables = new HashMap<String, TupleVariable>();
		this._attributes = new HashMap<String, AttributeConstant>();
		this._attributeVariables = new HashMap<String, AttributeVariable>();
		this._schemaVariables = new HashMap<String, SchemaVariable>();
		this._schemaVariablesByIndex = new ArrayList<SchemaVariable>();
		this._miningExpressions = new HashMap<MiningExpression, MiningExpression>();
	}

	public TupleVariable getTupleVariable(String name) {
		if (!_tupleVariables.containsKey(name)) {
			_tupleVariables.put(name,
					new TupleVariable(name, _tupleVariables.size()));
		}
		return _tupleVariables.get(name);
	}

	public AttributeConstant getAttributeConstant(String name) {
		if (!_attributes.containsKey(name)) {
			_attributes.put(name,
					new AttributeConstant(name, _attributes.size()));
		}
		return _attributes.get(name);
	}

	public AttributeVariable getAttributeVariable(String name) {
		if (!_attributeVariables.containsKey(name)) {
			_attributeVariables.put(name, new AttributeVariable(name,
					_attributeVariables.size()));
		}
		return _attributeVariables.get(name);
	}

	public SchemaVariable getSchemaVariable(String name) {
		addSchemaVariable(name);
		return _schemaVariables.get(name);
	}

	public List<SchemaVariable> getSchemaVariables() {
		return _schemaVariablesByIndex;
	}

	public MiningValue mkTupleAttributeValue(String tupleName, String attName) {
		if (_attributeVariables.containsKey(attName)) {
			return new TupleVariableAttribute(getTupleVariable(tupleName),
					getAttributeVariable(attName));
		}
		if (_attributes.containsKey(attName)) {
			return new TupleConstantAttribute(getTupleVariable(tupleName),
					getAttributeConstant(attName));
		}
		throw new IllegalArgumentException("Unknown attribute: " + attName);
	}

	public boolean hasSchemaVariable(String name) {
		return _schemaVariables.containsKey(name);
	}

	public boolean hasAttribute(String name) {
		return _attributes.containsKey(name);
	}

	public void addSchemaVariable(String name) {
		if (!_schemaVariables.containsKey(name)) {
			SchemaVariable sv = new SchemaVariable(name,
					_schemaVariablesByIndex.size());
			_schemaVariables.put(name, sv);
			_schemaVariablesByIndex.add(sv);
		}
	}

	public int getAttributeConstantCount() {
		return _attributes.size();
	}

	public int getAttributeVariableCount() {
		return _attributeVariables.size();
	}

	private MiningExpression getMiningExpression(MiningExpression e) {
		MiningExpression e2 = _miningExpressions.get(e);
		if (e2 == null) {
			_miningExpressions.put(e, e);
			e2 = e;
		}
		return e2;
	}

	public MiningExpression and(MiningExpression a, MiningExpression b) {
		return getMiningExpression(BinOpExpr.and(a, b));
	}

	public MiningExpression or(MiningExpression a, MiningExpression b) {
		return getMiningExpression(BinOpExpr.or(a, b));
	}

	public MiningExpression neg(MiningExpression a) {
		return getMiningExpression(new Neg(a));
	}

	public MiningExpression eq(MiningValue a, MiningValue b) {
		return getMiningExpression(BinOpAtom.eq(a, b));
	}
	
	public MiningExpression like(MiningValue a, MiningValue b) {
		return getMiningExpression(BinOpAtom.like(a, b));
	}
	
	public MiningExpression op(String op, MiningValue a, MiningValue b) {
	    if (op.equals("=")) {
	        return eq(a,b);
	    } else {
	        return like(a,b);
	    }
	}

	public MiningExpression tt() {
		return getMiningExpression(True.getInstance());
	}

	public MiningExpression forall(AttributeVariable v, SchemaVariable sv,
			MiningExpression expr) {
		return getMiningExpression(new AttributeQuantifier(Quantifier.ForAll,
				v, sv, expr));
	}

	public MiningExpression exists(AttributeVariable v, SchemaVariable sv,
			MiningExpression expr) {
		return getMiningExpression(new AttributeQuantifier(Quantifier.Exists,
				v, sv, expr));
	}

	public MiningExpression forall(String name, String schName,
			MiningExpression expr) {
		return forall(getAttributeVariable(name), getSchemaVariable(schName),
				expr);
	}

	public MiningExpression exists(String name, String schName,
			MiningExpression expr) {
		return exists(getAttributeVariable(name), getSchemaVariable(schName),
				expr);
	}

	public MiningExpression attCmp(String a, String b) {
		MiningExpression e;
		if (_attributeVariables.containsKey(a)) {
			if (_attributeVariables.containsKey(b)) {
				e = new AttributeVarComparison(getAttributeVariable(a), getAttributeVariable(b));
			} else {
				e=new AttributeVarConstantComparison(getAttributeVariable(a), getAttributeConstant(b));
			}
		} else {
			e = new AttributeVarConstantComparison(getAttributeVariable(b), getAttributeConstant(a));
		}
		return getMiningExpression(e);
	}
}

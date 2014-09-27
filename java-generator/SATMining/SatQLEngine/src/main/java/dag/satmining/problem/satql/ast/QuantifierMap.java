/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/QuantifierMap.java

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
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
import dag.satmining.constraints.Literal;
import dag.satmining.problem.satql.ast.constraints.TuplesQuantifiers;
import dag.satmining.problem.satql.ast.sql.QuantifierGeneralInformations;

/**
 * 
 * @author ucomignani
 */
public class QuantifierMap <L extends Literal<L>> {
	
	private boolean _isLastQuantifier;
	private int _quantifierLevel;
	private final HashMap<ArrayList<Integer>,QuantifierMap<L>> _quantifierMap = new HashMap<ArrayList<Integer>,QuantifierMap<L>>();
	private final HashMap<L,Integer> _mapWeightedLiteral = new HashMap<L,Integer>();
	private int _nTotal = 0;

	public void addLiteral(int quantifierLevel, LinkedList<ArrayList<Integer>> rowNumbersList, L insertedLiteral){
		ArrayList<Integer> key;
		_quantifierLevel = quantifierLevel;

		key = rowNumbersList.pollFirst();

		
		if(rowNumbersList.size() == 0){
			_isLastQuantifier = true;
			_nTotal++;

			if(key != null){
				if(_mapWeightedLiteral.containsKey(insertedLiteral)){
					int cmptLiteral = _mapWeightedLiteral.get(insertedLiteral) + 1;
					_mapWeightedLiteral.put(insertedLiteral, cmptLiteral);
				} else {
					_mapWeightedLiteral.put(insertedLiteral, 1);
				}
			}
		} else {
			_isLastQuantifier = false;

			if(_quantifierMap.containsKey(key)){
				_quantifierMap.get(key).addLiteral(_quantifierLevel+1, rowNumbersList, insertedLiteral);
			} else {
				_nTotal++;

				if(key != null) {
					_quantifierMap.put(key, new QuantifierMap<L>());
					_quantifierMap.get(key).addLiteral(_quantifierLevel+1, rowNumbersList, insertedLiteral);	
				}
			} 
		}
	}

	public void createFormula(ReifiedWeightedPBBuilder<L> handler, List<QuantifierGeneralInformations> quantifierInformationsList, L equivalentTo) throws NoSolutionException{
		QuantifierGeneralInformations quantifierInformations = quantifierInformationsList.get(_quantifierLevel-1);

		int nValue = quantifierInformations.getNValue();
		TuplesQuantifiers<L> tuplesQuantifiers = new TuplesQuantifiers<L>();
		
		if (quantifierInformations.isPercentQuantifier()){ //prise en compte du pourcentage
			nValue = (int) Math.ceil( ((double) nValue/100)*_nTotal);
		}


		if(_isLastQuantifier){
			
			if(quantifierInformations.isUniversalQuantifier())
				tuplesQuantifiers.addUniversalQuantifier(handler, _mapWeightedLiteral, _nTotal, equivalentTo);
			else
				tuplesQuantifiers.addAtLeastQuantifier(handler, _mapWeightedLiteral, nValue, equivalentTo);

		} else {
			for(QuantifierMap<L> quantifierMap: _quantifierMap.values()){
				L tmp = handler.newLiteral(true, false);
				quantifierMap.createFormula(handler,quantifierInformationsList, tmp);

				if(_mapWeightedLiteral.containsKey(tmp)){
					int cmptLiteral = _mapWeightedLiteral.get(tmp) + 1;
					_mapWeightedLiteral.put(tmp, cmptLiteral);
				} else {
					_mapWeightedLiteral.put(tmp, 1);
				}				
			}
			
			if(quantifierInformations.isUniversalQuantifier())
				tuplesQuantifiers.addUniversalQuantifier(handler, _mapWeightedLiteral, _nTotal, equivalentTo);
			else
				tuplesQuantifiers.addAtLeastQuantifier(handler, _mapWeightedLiteral, nValue, equivalentTo);
			
		}
	}


	public boolean getIsLastQuantifier(){
		return _isLastQuantifier;
	}

	public int getQuantifierLevel(){
		return _quantifierLevel;
	}

	public HashMap<ArrayList<Integer>,QuantifierMap<L>> getQuantifierMap(){
		return _quantifierMap;
	}

	public HashMap<L,Integer> getMapOfBitSet(){
		return _mapWeightedLiteral;
	}

}

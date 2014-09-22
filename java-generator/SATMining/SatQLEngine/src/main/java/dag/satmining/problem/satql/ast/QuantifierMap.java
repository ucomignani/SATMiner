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

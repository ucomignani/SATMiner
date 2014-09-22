package dag.satmining.problem.satql.ast.constraints;

import java.util.HashMap;

import dag.satmining.constraints.Literal;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Ineq;

public class TuplesQuantifiers  <L extends Literal<L>>{

    public void addUniversalQuantifier(ReifiedWeightedPBBuilder<L> handler, HashMap<L,Integer> literalMap, 
    		int nTotal, L equivalentTo) throws NoSolutionException{
    	L[] lits = literalMap.keySet().toArray(handler.lArray(literalMap.keySet().size()));

    	int[] coefs = new int[literalMap.size()];
    	int i = 0;
    	for(L lit: lits){
    		coefs[i] = literalMap.get(lit);
    		i++;
    	}
    	handler.addReifiedWPBInequality(lits, coefs, Ineq.GEQ, nTotal, equivalentTo);
    }
     
    public void addAtLeastQuantifier(ReifiedWeightedPBBuilder<L> handler, HashMap<L,Integer> literalMap, 
    		int nValue, L equivalentTo) throws NoSolutionException{
    	L[] lits = literalMap.keySet().toArray(handler.lArray(literalMap.keySet().size()));

		int[] coefs = new int[literalMap.size()];
		int i = 0;
		for(L lit: lits){
			coefs[i] = literalMap.get(lit);
			i++;
		}
		handler.addReifiedWPBInequality(lits, coefs, Ineq.GEQ, nValue, equivalentTo);
    }
}

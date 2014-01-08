/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.fim;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

/**
 * 
 * @author ecoquery
 */
public class IPClosed<L extends Literal<L>> implements Constraint<L> {

	private IPDomain<L> _domain;
	private IPSupport<L> _support;

	public IPClosed(IPDomain<L> domain, IPSupport<L> support) {
		this._domain = domain;
		this._support = support;
	}

	@Override
	public void addClauses(PBBuilder<L> h) throws NoSolutionException {
		@SuppressWarnings("unchecked")
		List<L>[] trNoMatches = new List[_domain.size()];
		for (int item = 0; item < _domain.size(); item++) {
			trNoMatches[item] = new ArrayList<L>();
		}
		for (int idxTr = 0; idxTr < _support.size(); idxTr++) {
			BitSet tr = _support.getTransaction(idxTr);
			L notThisTr = _support.getLiteral(idxTr).getOpposite();
			for (int item = 0; item < _domain.size(); item++) {
				if (!tr.get(item)) {
					trNoMatches[item].add(notThisTr);
				}
			}
		}
		for (int item = 0; item < _domain.size(); item++) {
			h.addReifiedConjunction(_domain.getItLit(item), trNoMatches[item]);
		}
	}
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.fim;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

/**
 * 
 * @author ecoquery
 */
public class IPMaximal<L extends Literal<L>> implements Constraint<L> {

	private static final Logger LOG = LoggerFactory.getLogger(IPMaximal.class);

	private int _freq;
	private IPDomain<L> _domain;
	private IPSupport<L> _support;

	public IPMaximal(IPDomain<L> domain, IPSupport<L> support, int freq) {
		this._domain = domain;
		this._support = support;
		this._freq = freq;
	}

	@Override
	public void addClauses(PBBuilder<L> h) throws NoSolutionException {
		for (int item = 0; item < _domain.size(); item++) {
			List<L> trToCount = new ArrayList<L>();
			for (int tr = 0; tr < _support.size(); tr++) {
				if (_support.isInTr(item, tr)) {
					trToCount.add(_support.getLiteral(tr));
				}
			}
			LOG.debug("freq = {}", _freq);
			h.addReifiedPBInequality(trToCount, Ineq.GEQ, _freq,
					_domain.getItLit(item));
		}
	}
}

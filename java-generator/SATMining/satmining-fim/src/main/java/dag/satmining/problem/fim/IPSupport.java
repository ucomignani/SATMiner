/* ./satmining-fim/src/main/java/dag/satmining/problem/fim/IPSupport.java

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
package dag.satmining.problem.fim;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
import dag.satmining.constraints.mining.AbstractSupport;

/**
 *
 * @author ecoquery
 */
public class IPSupport<L extends Literal<L>> extends AbstractSupport<L> {

    private List<BitSet> _data;
    private IPDomain<L> _domain;
    
    public IPSupport(IPDomain<L> domain, List<BitSet> data) {
        super(data.size());
        this._data = data;
        this._domain = domain;
    }
    
    @Override
    protected void addMatchAt(int patternPos, ReifiedWeightedPBBuilder<L> satHandler) throws NoSolutionException {
        List<L> lits = new ArrayList<L>();
        BitSet tr = _data.get(patternPos);
        for(int i = 0; i < _domain.size(); i++) {
            if (!tr.get(i)) {
                lits.add(_domain.getItNLit(i));
            }
        }
        satHandler.addReifiedConjunction(getLiteral(patternPos), lits);
    }
    
    public BitSet getTransaction(int idx) {
        return _data.get(idx);
    }
    
    public boolean isInTr(int item, int trId) {
        return _data.get(trId).get(item);
    }
}

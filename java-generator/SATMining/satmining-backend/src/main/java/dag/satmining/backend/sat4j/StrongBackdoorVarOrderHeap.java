/* ./satmining-backend/src/main/java/dag/satmining/backend/sat4j/StrongBackdoorVarOrderHeap.java

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
package dag.satmining.backend.sat4j;

import java.util.BitSet;

import org.sat4j.core.LiteralsUtils;
import org.sat4j.minisat.core.Heap;
import org.sat4j.minisat.orders.VarOrderHeap;

/**
 *
 * @author ecoquery
 */
public class StrongBackdoorVarOrderHeap extends VarOrderHeap {

	private static final long serialVersionUID = 1L;
	private Heap _strongHeap;
    private BitSet _backDoor;

    public StrongBackdoorVarOrderHeap(BitSet backDoor) {
        this._backDoor = backDoor;
    }
    
    @Override
    public void init() {
        // recodage avec double pile, très lié à l'implementation de la super classe
        super.init();
        int nlength = lits.nVars() + 1;
        set_strongHeap(new Heap(activity));
        get_strongHeap().setBounds(nlength);
        for (int i = 1; i < nlength; i++) {
            if (lits.belongsToPool(i) && _backDoor.get(i)) {
                get_strongHeap().insert(i);
            }
        }
    }

    @Override
    public int select() {
        // recodage avec double pile, très lié à l'implementation de la super classe
        while (!get_strongHeap().empty()) {
            int var = get_strongHeap().getmin();
            int next = phaseStrategy.select(var);
            if (lits.isUnassigned(next)) {
                return next;
            }
        }
        return super.select();
    }

    @Override
    public void undo(int x) {
        // recodage avec double pile, très lié à l'implementation de la super classe
        if (_backDoor.get(x)
                && !get_strongHeap().inHeap(x)) {
            get_strongHeap().insert(x);
        }
        super.undo(x);
    }

    @Override
    public void updateVar(int p) {
        // recodage avec double pile, très lié à l'implementation de la super classe
        super.updateVar(p);
        int var = LiteralsUtils.var(p);
        if (_backDoor.get(var)
                && get_strongHeap().inHeap(var)) {
            get_strongHeap().increase(var);
        }
    }

	public Heap get_strongHeap() {
		return _strongHeap;
	}

	public void set_strongHeap(Heap _strongHeap) {
		this._strongHeap = _strongHeap;
	}
}

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
        _strongHeap = new Heap(activity);
        _strongHeap.setBounds(nlength);
        for (int i = 1; i < nlength; i++) {
            if (lits.belongsToPool(i) && _backDoor.get(i)) {
                _strongHeap.insert(i);
            }
        }
    }

    @Override
    public int select() {
        // recodage avec double pile, très lié à l'implementation de la super classe
        while (!_strongHeap.empty()) {
            int var = _strongHeap.getmin();
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
                && !_strongHeap.inHeap(x)) {
            _strongHeap.insert(x);
        }
        super.undo(x);
    }

    @Override
    public void updateVar(int p) {
        // recodage avec double pile, très lié à l'implementation de la super classe
        super.updateVar(p);
        int var = LiteralsUtils.var(p);
        if (_backDoor.get(var)
                && _strongHeap.inHeap(var)) {
            _strongHeap.increase(var);
        }
    }
}

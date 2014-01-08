package dag.satmining.backend;

import java.util.BitSet;

import dag.satmining.constraints.Literal;

public class BitSetInterpretation implements Interpretation {

	private BitSet _bitset;
	
	public BitSetInterpretation(BitSet bitset) {
		this._bitset = bitset;
	}

	public boolean getValue(Literal<?> lit) {
		return  lit.isPositive() == _bitset.get(lit.getVariableId());
	}

}

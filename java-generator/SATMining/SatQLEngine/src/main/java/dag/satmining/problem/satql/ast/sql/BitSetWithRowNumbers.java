package dag.satmining.problem.satql.ast.sql;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;

public class BitSetWithRowNumbers {
	private final BitSet _bitSet = new BitSet();
	private final LinkedList<ArrayList<Integer>> _rowNumbersList = new LinkedList<ArrayList<Integer>>();
	
	public BitSet getBitSet(){
		return _bitSet;
	}
	
	public LinkedList<ArrayList<Integer>> getRowNumbersList(){
		return _rowNumbersList;
	}
	
}

package dag.satmining.utils;

import java.util.Arrays;

public class IntArrayContainer {

	private int [] _array;
	
	public IntArrayContainer(int [] array) {
		_array = array;
	}
	
	public String toString() {
		return Arrays.toString(_array);
	}
}

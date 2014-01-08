package dag.satmining.backend;

import java.util.BitSet;

/**
 * A bitset-based model reader.
 * 
 * @author ecoquery
 * 
 */
public abstract class AbstractModelReader implements ModelReader {

	private BitSet _model;

	/**
	 * @return the _model
	 */
	public BitSet getModel() {
		return _model;
	}

	/**
	 * @param model
	 *            the _model to set
	 */
	public void setModel(BitSet model) {
		this._model = model;
	}

	public boolean getNext() {
		parseModel();
		return _model != null;
	}

	/**
	 * Reads the model. This method must call {@link #setModel(BitSet)}.
	 */
	protected abstract void parseModel();

	public Interpretation getCurrentInterpretation() {
		if (_model == null) {
			return null;
		} else {
			return new BitSetInterpretation(_model);
		}
	}

}

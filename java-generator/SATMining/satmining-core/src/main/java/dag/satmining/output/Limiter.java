package dag.satmining.output;

public interface Limiter {
	/**
	 * A maximum number of models to enumerate
	 * 
	 * @return the maximum number of wanted models
	 */
	long getLimit();
}

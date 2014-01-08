package dag.satmining.utils;

public class UnreachableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnreachableException() {
		super();
	}

	public UnreachableException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public UnreachableException(String arg0) {
		super(arg0);
	}

	public UnreachableException(Throwable arg0) {
		super(arg0);
	}



}

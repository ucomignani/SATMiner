package dag.satmining;

public class NoSolutionException extends Exception {

	private static final long serialVersionUID = 1L;

	public NoSolutionException() {
	}

	public NoSolutionException(String arg0) {
		super(arg0);
	}

	public NoSolutionException(Throwable arg0) {
		super(arg0);
	}

	public NoSolutionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}

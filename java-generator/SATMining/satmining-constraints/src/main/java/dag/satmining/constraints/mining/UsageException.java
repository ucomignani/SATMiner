/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.constraints.mining;


/**
 *
 * @author ecoquery
 */
public class UsageException extends Exception {
    
	private static final long serialVersionUID = 1L;

	public UsageException(Throwable cause) {
        super(cause);
    }

    public UsageException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsageException(String message) {
        super(message);
    }

    public UsageException() {
    }

	@Override
	public String getLocalizedMessage() {
		if (getCause() != null) {
			return getCause().getLocalizedMessage();
		} else {
			return super.getLocalizedMessage();
		}
	}

	@Override
	public String getMessage() {
		if (getCause() != null) {
			return getCause().getMessage();
		} else {
			return super.getMessage();
		}
	}
    
}

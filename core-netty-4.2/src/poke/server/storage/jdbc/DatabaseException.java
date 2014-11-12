/**
 * 
 */
package poke.server.storage.jdbc;

/**
 * @author arun_malik
 *
 */
public class DatabaseException extends Exception {

	  /**
	 * 
	 */
	private static final long serialVersionUID = 7972154586218627900L;

	public DatabaseException(Throwable cause)
	    {
	        super(cause);
	    }

	    public DatabaseException(String message, Throwable cause)
	    {
	        super(message, cause);
	    }

	    public DatabaseException(String message)
	    {
	        super(message);
	    }

	    public DatabaseException()
	    {
	        super();
	    }
}

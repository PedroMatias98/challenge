package exceptions;

public class EmptyFileException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public EmptyFileException(String message) {
		super(message);
	}

}

package exceptions;

public class InvalidFileFormatException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public InvalidFileFormatException(String message) {
		super(message);
	}

}

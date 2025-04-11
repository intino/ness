package systems.intino.eventsourcing.datahubterminal.datamart;

public class SubjectNotAvailableException extends Exception {
	public SubjectNotAvailableException() {
	}

	public SubjectNotAvailableException(String message) {
		super(message);
	}

	public SubjectNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}

}

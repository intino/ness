package systems.intino.eventsourcing.datahubterminal.datamart;

public class DatahubRequestException extends Exception {
	public DatahubRequestException() {
	}

	public DatahubRequestException(String message) {
		super(message);
	}

	public DatahubRequestException(String message, Throwable cause) {
		super(message, cause);
	}
}

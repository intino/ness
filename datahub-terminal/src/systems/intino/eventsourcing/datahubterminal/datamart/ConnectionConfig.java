package systems.intino.eventsourcing.datahubterminal.datamart;

public class ConnectionConfig {
	public final long initialTimeoutAmount;
	public final java.util.concurrent.TimeUnit timeoutUnit;
	public final float timeoutMultiplier;
	public final int maxAttempts;

	public ConnectionConfig() {
		this(1, java.util.concurrent.TimeUnit.MINUTES, 2.0f, 5);
	}

	public ConnectionConfig(long initialTimeoutAmount, java.util.concurrent.TimeUnit timeoutUnit, float timeoutMultiplier, int maxAttempts) {
		this.initialTimeoutAmount = initialTimeoutAmount;
		this.timeoutUnit = timeoutUnit;
		this.timeoutMultiplier = timeoutMultiplier;
		this.maxAttempts = maxAttempts;
	}
}

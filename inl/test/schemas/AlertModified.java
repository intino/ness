package schemas;

public class AlertModified implements java.io.Serializable {

	private String alert = "";
	private Boolean active = true;
	private java.util.List<String> mailingList = new java.util.ArrayList<>();
	private Boolean applyToAllStations = true;

	public String alert() {
		return this.alert;
	}

	public Boolean active() {
		return this.active;
	}

	public java.util.List<String> mailingList() {
		return this.mailingList;
	}

	public Boolean applyToAllStations() {
		return this.applyToAllStations;
	}

	public AlertModified alert(String alert) {
		this.alert = alert;
		return this;
	}

	public AlertModified active(Boolean active) {
		this.active = active;
		return this;
	}

	public AlertModified mailingList(java.util.List<String> mailingList) {
		this.mailingList = mailingList;
		return this;
	}

	public AlertModified applyToAllStations(Boolean applyToAllStations) {
		this.applyToAllStations = applyToAllStations;
		return this;
	}
}
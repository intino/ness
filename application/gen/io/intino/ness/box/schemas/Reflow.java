package io.intino.ness.box.schemas;

import io.intino.ness.box.schemas.*;

public class Reflow implements java.io.Serializable {

	private Integer blockSize = 0;
	private java.time.Instant from;
	private java.util.List<String> tanks = new java.util.ArrayList<>();

	public Integer blockSize() {
		return this.blockSize;
	}

	public java.time.Instant from() {
		return this.from;
	}

	public java.util.List<String> tanks() {
		return this.tanks;
	}

	public Reflow blockSize(Integer blockSize) {
		this.blockSize = blockSize;
		return this;
	}

	public Reflow from(java.time.Instant from) {
		this.from = from;
		return this;
	}

	public Reflow tanks(java.util.List<String> tanks) {
		this.tanks = tanks;
		return this;
	}
}
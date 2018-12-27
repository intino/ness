package io.intino.ness.datalake.reflow;

public class ReflowConfiguration implements java.io.Serializable {

	private int blockSize = Integer.MAX_VALUE;
	private java.util.List<Tank> tankList = new java.util.ArrayList<>();

	public int blockSize() {
		return this.blockSize;
	}

	public ReflowConfiguration blockSize(int blockSize) {
		this.blockSize = blockSize;
		return this;
	}

	public java.util.List<Tank> tankList() {
		return this.tankList;
	}


	public ReflowConfiguration tankList(java.util.List<Tank> tankList) {
		this.tankList = tankList;
		return this;
	}

	public static class Tank implements java.io.Serializable {

		private String name = "";
		private java.time.Instant from;
		private java.time.Instant to;

		public String name() {
			return this.name;
		}

		public java.time.Instant from() {
			return this.from;
		}

		public java.time.Instant to() {
			return this.to;
		}

		public Tank name(String name) {
			this.name = name;
			return this;
		}

		public Tank from(java.time.Instant from) {
			this.from = from;
			return this;
		}

		public Tank to(java.time.Instant to) {
			this.to = to;
			return this;
		}

	}
}
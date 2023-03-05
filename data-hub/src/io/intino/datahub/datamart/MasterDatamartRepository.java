package io.intino.datahub.datamart;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MasterDatamartRepository {

	private final File datamartsRoot;
	private final Map<String, MasterDatamart<?>> datamarts;

	public MasterDatamartRepository(File datamartsRoot) {
		this.datamartsRoot = datamartsRoot;
		this.datamarts = new HashMap<>();
	}

	public File root() {
		return datamartsRoot;
	}

	public int size() {
		return datamarts.size();
	}

	public boolean contains(String name) {
		return datamarts.containsKey(name);
	}

	@SuppressWarnings("unchecked")
	public <T> MasterDatamart<T> get(String name) {
		return (MasterDatamart<T>) datamarts.get(name);
	}

	public void put(String name, MasterDatamart<?> datamart) {
		datamarts.put(name, datamart);
	}

	public void remove(String name) {
		datamarts.remove(name);
	}

	public void clear() {
		datamarts.clear();
	}

	public Collection<MasterDatamart<?>> datamarts() {
		return datamarts.values();
	}
}

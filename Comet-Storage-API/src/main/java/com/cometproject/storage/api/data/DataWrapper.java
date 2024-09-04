package com.cometproject.storage.api.data;

public class DataWrapper<T> {
	
	private T value;
	
	public DataWrapper() {
	
	}
	
	public void set(T value) {
		this.value = value;
	}
	
	public T get() {
		return value;
	}
	
	public boolean has() {
		return this.value != null;
	}
	
	public static <T> DataWrapper<T> createEmpty() {
		return new DataWrapper<>();
	}
	
}

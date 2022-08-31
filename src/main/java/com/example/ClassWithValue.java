package com.example;

public class ClassWithValue implements IClassWithValue {

	private Object value;
	
	public ClassWithValue(Object value) {
		this.value = value;
	}

	@Override
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ClassWithValue [value=" + value + "]";
	}
}

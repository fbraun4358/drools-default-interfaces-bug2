package com.example;

public interface IClassWithValue {
	
	Object getValue();
	
	default boolean getValueIsNull() {
		return this.getValue() == null;
	}
}

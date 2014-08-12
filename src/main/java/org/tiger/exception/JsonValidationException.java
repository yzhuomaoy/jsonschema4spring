package org.tiger.exception;

public class JsonValidationException extends RuntimeException {
	
	public JsonValidationException(String message) {
		super(message);
	}
	
	public JsonValidationException(String message, Throwable t) {
		super(message, t);
	}

}

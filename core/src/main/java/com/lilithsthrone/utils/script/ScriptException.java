package com.lilithsthrone.utils.script;

/**
 * Replaces {@code javax.script.ScriptException}, which does not exist on Android
 * (the whole {@code javax.script} package is absent from ART).
 */
public class ScriptException extends Exception {

	private static final long serialVersionUID = 1L;

	public ScriptException(String message) {
		super(message);
	}

	public ScriptException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScriptException(Throwable cause) {
		super(cause);
	}
}

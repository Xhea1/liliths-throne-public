package com.lilithsthrone.utils.script;

import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * The game's JavaScript engine, backed by Mozilla Rhino.
 *
 * Replaces Nashorn, which was removed from the JDK (JEP 372) and has never existed on
 * Android. Rhino is pure Java and runs on ART. Deliberately exposes a small
 * {@code put}/{@code get}/{@code compile} surface mirroring the {@code javax.script.ScriptEngine}
 * methods the dialogue parser already used, so the parser's ~185 binding registrations did not
 * need rewriting.
 *
 * Sandboxing notes: the scope is built with {@code initSafeStandardObjects} (no file/exit
 * primitives at all) and a {@link ClassShutter} denies scripts access to every Java class.
 * Together these are strictly stronger than the Nashorn setup they replace, which passed
 * {@code --no-java} and then manually deleted {@code exit}/{@code quit}/{@code load} bindings.
 *
 * Interpreted mode ({@code setOptimizationLevel(-1)}) is mandatory: Rhino's optimising modes
 * generate JVM bytecode at runtime, which does not work on Android's ART.
 */
public class LTScriptEngine {

	private final ContextFactory contextFactory;
	private final Scriptable scope;

	public LTScriptEngine() {
		contextFactory = new ContextFactory();
		Context cx = contextFactory.enterContext();
		try {
			cx.setOptimizationLevel(-1);
			cx.setLanguageVersion(Context.VERSION_ES6);
			cx.setClassShutter(new ClassShutter() {
				@Override
				public boolean visibleToScripts(String fullClassName) {
					return false;
				}
			});
			scope = cx.initSafeStandardObjects();
		} finally {
			Context.exit();
		}
	}

	/** Registers a Java object under the given name, visible to evaluated scripts. */
	public void put(String name, Object value) {
		Context cx = contextFactory.enterContext();
		try {
			cx.setOptimizationLevel(-1);
			ScriptableObject.putProperty(scope, name, value == null ? null : Context.javaToJS(value, scope));
		} finally {
			Context.exit();
		}
	}

	/** @return the previously {@link #put} value, unwrapped back to its Java form. */
	public Object get(String name) {
		Object value = ScriptableObject.getProperty(scope, name);
		if (value == Scriptable.NOT_FOUND || value == null) {
			return null;
		}
		return Context.jsToJava(value, Object.class);
	}

	/**
	 * Compiles a script so the caller can cache and re-evaluate it. Compilation is the
	 * expensive half of evaluation, which is why the parser memoises the result.
	 */
	public CompiledScript compile(String source) throws ScriptException {
		Context cx = contextFactory.enterContext();
		try {
			cx.setOptimizationLevel(-1);
			cx.setLanguageVersion(Context.VERSION_ES6);
			Script script = cx.compileString(source, "<dialogue>", 1, null);
			return new CompiledScript(script);
		} catch (RhinoException e) {
			throw new ScriptException(e.getMessage(), e);
		} finally {
			Context.exit();
		}
	}

	public Object eval(String source) throws ScriptException {
		return compile(source).eval();
	}

	/**
	 * JavaScript has only doubles, so Rhino hands every number back as a {@link Double} --
	 * meaning {@code String.valueOf(evaluate("1+2"))} would render "3.0" in dialogue text.
	 * Nashorn narrowed integral results before returning them, so the game's text is written
	 * expecting that; this restores it. Non-integral and non-numeric values pass through.
	 */
	private static Object narrowNumber(Object value) {
		if (value instanceof Double) {
			double d = (Double) value;
			if (d == Math.rint(d) && !Double.isInfinite(d) && !Double.isNaN(d)) {
				if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
					return (int) d;
				}
				return (long) d;
			}
		}
		return value;
	}

	/** A compiled script, re-evaluable against this engine's scope. */
	public class CompiledScript {

		private final Script script;

		private CompiledScript(Script script) {
			this.script = script;
		}

		public Object eval() throws ScriptException {
			Context cx = contextFactory.enterContext();
			try {
				cx.setOptimizationLevel(-1);
				cx.setLanguageVersion(Context.VERSION_ES6);
				Object result = script.exec(cx, scope);
				return result == null ? null : narrowNumber(Context.jsToJava(result, Object.class));
			} catch (RhinoException e) {
				throw new ScriptException(e.getMessage(), e);
			} finally {
				Context.exit();
			}
		}
	}
}

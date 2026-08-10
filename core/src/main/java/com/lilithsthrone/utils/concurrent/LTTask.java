package com.lilithsthrone.utils.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Platform-neutral replacement for {@code javafx.concurrent.Task}, so that {@code core}
 * (e.g. {@link com.lilithsthrone.world.Generation}) does not depend on JavaFX.
 *
 * Deliberately minimal: no thread-marshalling, no JavaFX Application-thread affinity for
 * listener callbacks (callers are responsible for hopping back to whatever thread they
 * need). It implements {@link Runnable} so existing call sites like {@code new Thread(gen).start()}
 * keep working unchanged; desktop and Android can otherwise drive it via any executor
 * without this contract changing.
 *
 * This type was introduced to let {@code core} compile standalone; behavioural testing of
 * world generation itself (the thing {@link com.lilithsthrone.world.Generation} actually
 * does) is out of scope for the milestone that introduced it.
 */
public abstract class LTTask<T> implements Runnable {

	private volatile boolean running = false;
	private volatile boolean cancelled = false;
	private volatile long progressCurrent;
	private volatile long progressTotal;

	private final List<Consumer<T>> succeededListeners = new ArrayList<>();
	private final List<Consumer<Throwable>> failedListeners = new ArrayList<>();
	private final List<Runnable> cancelledListeners = new ArrayList<>();

	public abstract T call() throws Exception;

	@Override
	public final void run() {
		running = true;
		try {
			T value = call();
			running = false;
			if (cancelled) {
				for (Runnable listener : cancelledListeners) {
					notifyListener(listener);
				}
			} else {
				for (Consumer<T> listener : succeededListeners) {
					notifyListener(() -> listener.accept(value));
				}
			}
		} catch (Exception e) {
			running = false;
			for (Consumer<Throwable> listener : failedListeners) {
				notifyListener(() -> listener.accept(e));
			}
		}
	}

	/**
	 * Completion handlers usually touch the UI, so they run wherever the front-end needs rather
	 * than on this worker thread. This mirrors {@code javafx.concurrent.Task}, whose handlers were
	 * dispatched to the JavaFX application thread -- without it, JavaFX rejects the callback with
	 * "Not on FX application thread".
	 */
	private void notifyListener(Runnable listener) {
		com.lilithsthrone.main.Platform platform = com.lilithsthrone.main.GameState.getPlatform();
		if (platform == null) {
			listener.run();
		} else {
			platform.runOnUiThread(listener);
		}
	}

	protected void updateProgress(long current, long total) {
		this.progressCurrent = current;
		this.progressTotal = total;
	}

	public double getProgress() {
		return progressTotal == 0 ? 0 : (double) progressCurrent / progressTotal;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Best-effort: sets a flag {@link #call()} implementations may check, but does not
	 * forcibly interrupt an in-progress {@link #call()}.
	 */
	public void cancel() {
		cancelled = true;
	}

	public void setOnSucceeded(Consumer<T> listener) {
		succeededListeners.add(listener);
	}

	public void setOnFailed(Consumer<Throwable> listener) {
		failedListeners.add(listener);
	}

	public void setOnCancelled(Runnable listener) {
		cancelledListeners.add(listener);
	}
}

package com.lilithsthrone.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.lilithsthrone.rendering.InformationTooltip;

/**
 * Registry of what the currently-rendered screen can do: which element ids are clickable, what
 * each does, and which carry a tooltip.
 *
 * This exists so that the "what does this id do" logic can live in {@code core} and be shared by
 * every front-end, instead of being expressed as JavaFX DOM listeners that only the desktop build
 * can use. Screen code calls {@link #bind} while its content is being prepared; the front-end is
 * then told via {@code RenderSink.bindingsChanged(Panel)} and applies the bindings however it
 * wants -- desktop attaches real DOM listeners, the HTTP front-end just serves the HTML and later
 * calls {@link #dispatch} when the client posts an id back.
 *
 * Bindings are per-panel and are replaced wholesale each time a panel re-renders, mirroring
 * desktop's existing unbind-then-rebind cycle.
 *
 * Not thread-safe: bindings are registered and dispatched from whichever single thread drives the
 * game loop (the JavaFX application thread on desktop, the engine service thread on Android).
 */
public class Ui {

	/** One element's registration: its action, its tooltip, or both. */
	public static final class Binding {

		private final String id;
		private ClickAction click;
		private boolean useCapture;
		private InformationTooltip tooltip;

		Binding(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public ClickAction getClick() {
			return click;
		}

		/**
		 * Whether desktop should attach its DOM listener in the capture phase. Preserved because
		 * the existing code passes {@code true} for some buttons and {@code false} for others, and
		 * that difference is load-bearing for event ordering.
		 */
		public boolean isUseCapture() {
			return useCapture;
		}

		public InformationTooltip getTooltip() {
			return tooltip;
		}
	}

	private static final Map<Panel, LinkedHashMap<String, Binding>> BINDINGS = new EnumMap<>(Panel.class);

	static {
		for (Panel panel : Panel.values()) {
			BINDINGS.put(panel, new LinkedHashMap<>());
		}
	}

	private Ui() {
	}

	/** Drops every binding for a panel. Call before re-registering that panel's screen. */
	public static void clear(Panel panel) {
		BINDINGS.get(panel).clear();
	}

	public static void bind(Panel panel, String id, ClickAction action) {
		bind(panel, id, action, false);
	}

	public static void bind(Panel panel, String id, ClickAction action, boolean useCapture) {
		Binding binding = binding(panel, id);
		binding.click = action;
		binding.useCapture = useCapture;
	}

	/** Registers hover/long-press information for an element, with or without a click action. */
	public static void bindTooltip(Panel panel, String id, InformationTooltip tooltip) {
		binding(panel, id).tooltip = tooltip;
	}

	private static Binding binding(Panel panel, String id) {
		return BINDINGS.get(panel).computeIfAbsent(id, Binding::new);
	}

	/** Registration order is preserved, so front-ends can attach listeners deterministically. */
	public static Collection<Binding> bindings(Panel panel) {
		return Collections.unmodifiableCollection(BINDINGS.get(panel).values());
	}

	public static Binding get(Panel panel, String id) {
		return BINDINGS.get(panel).get(id);
	}

	/**
	 * Runs the action bound to an id.
	 *
	 * @return true if something was bound and ran; false if the id is unknown to this panel, which
	 *         lets a caller fall through to its own handling or log the gap.
	 */
	public static boolean dispatch(Panel panel, String id) {
		Binding binding = BINDINGS.get(panel).get(id);
		if (binding == null || binding.click == null) {
			return false;
		}
		binding.click.run();
		return true;
	}

	public static InformationTooltip tooltip(Panel panel, String id) {
		Binding binding = BINDINGS.get(panel).get(id);
		return binding == null ? null : binding.tooltip;
	}
}

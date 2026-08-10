package com.lilithsthrone.rendering;

import com.lilithsthrone.game.character.npc.NPC;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.game.dialogue.DialogueNode;
import com.lilithsthrone.game.dialogue.utils.InventoryInteraction;
import com.lilithsthrone.game.settings.KeyboardAction;
import com.lilithsthrone.utils.colours.Colour;

/**
 * Everything {@code core} needs to push rendered HTML (and a handful of UI-state queries)
 * to whatever front-end is currently attached, without core depending on that front-end's
 * UI toolkit directly.
 *
 * Desktop's {@code MainController} implements this directly (it already had matching
 * methods before this type existed). Android's {@code HttpRenderSink} buffers the latest
 * HTML per panel for its HTTP routes to serve; methods not needed by the milestone that
 * introduced this interface (most of the panels beyond the main one, tooltip positioning,
 * inventory/phone/characters-present sub-screens) may throw
 * {@link UnsupportedOperationException} there rather than pretend to implement UI surfaces
 * Android doesn't have yet -- see the class-level note on HttpRenderSink for what's covered.
 */
public interface RenderSink {

	/**
	 * Number of response slots a single dialogue page exposes. A game-logic constant (response
	 * paging arithmetic in core depends on it) that also drives the UI's button layout, so it
	 * lives here and each front-end reads it rather than declaring its own copy.
	 */
	int RESPONSE_COUNT = 15;

	void setMainContent(String content);

	void setAttributePanelContent(String content);

	void setRightPanelContent(String content);

	void setButtonsLeftContent(String content);

	void setButtonsRightContent(String content);

	/** @return the calculated pixel height of the tooltip content, as the desktop UI needs it. */
	int setTooltipContent(String content);

	void hideTooltip();

	/** Replaces the response box's markup in place, without re-rendering the whole main panel. */
	void setResponseBoxContent(String content);

	/** Scroll offset of the main content, preserved across same-node re-renders. */
	int getScrollTop();

	void setScrollTop(int position);

	/**
	 * Shows a transient message over the main panel (the "Game saved!" style toast).
	 * Front-ends are free to present this however suits them.
	 */
	void showFlashMessage(Colour colour, String text);

	/**
	 * Current text of an {@code <input>}/{@code <textarea>} on the rendered screen.
	 *
	 * Prefer {@link com.lilithsthrone.controller.UiInput#get} at call sites. Returning null or an
	 * empty string is fine when the field isn't present.
	 */
	String getInputValue(String id);

	void openInventory();

	void openInventory(NPC npc, InventoryInteraction interaction);

	void openPhone();

	void openPhone(DialogueNode toDialogue);

	void openCharactersPresent();

	void openCharactersPresent(GameCharacter characterViewed);

	void openOptions();

	void updateUILeftPanel();

	/** Full UI refresh (both side panels and their buttons). */
	void updateUI();

	/** Re-attaches input handlers to the freshly-rendered response buttons. */
	void setResponseEventListeners();

	/** Stops any in-flight tooltip refresh work; replaces TooltipUpdateThread.cancelThreads. */
	void cancelTooltipThreads();

	void switchTheme();

	boolean isPrimaryBinding();

	boolean isPhoneDisabled();

	boolean isInventoryDisabled();

	KeyboardAction getActionToBind();

	void setFlashMessageColour(Colour flashMessageColour);

	void setFlashMessageText(String flashMessageText);

	/**
	 * Signals that {@link com.lilithsthrone.controller.Ui}'s bindings for a panel have been
	 * rebuilt, so the front-end should apply them however it needs to.
	 *
	 * Desktop attaches real DOM listeners for each binding; a front-end that dispatches ids on
	 * demand (e.g. over HTTP) has nothing to do and may no-op.
	 */
	void bindingsChanged(com.lilithsthrone.controller.Panel panel);

	/**
	 * Called once world generation finishes and a new game's initial state is ready.
	 * Desktop uses this to redo its FXML/Scene setup (mirroring the pre-split behaviour);
	 * an HTTP-backed sink has no equivalent "scene" to rebuild and may no-op.
	 */
	void onNewGameStarted();
}

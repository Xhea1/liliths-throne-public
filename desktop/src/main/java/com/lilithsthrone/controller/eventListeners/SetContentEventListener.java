package com.lilithsthrone.controller.eventListeners;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import com.lilithsthrone.controller.MainController;
import com.lilithsthrone.main.GameState;

/**
 * Sets the MainController's content.
 * 
 * @since 0.1.0
 * @version 0.1.7
 * @author Innoxia
 */
public class SetContentEventListener implements EventListener {
	private int index;
	private boolean nextPage = false, previousPage = false;

	@Override
	public void handleEvent(Event event) {
		if (nextPage) {
			if (GameState.game.isHasNextResponsePage()) {
				GameState.game.setResponsePage(GameState.game.getResponsePage() + 1);
				GameState.game.updateResponses();
//				GameState.game.setResponses(GameState.game.getCurrentDialogueNode());
			}
		} else if (previousPage) {
			if (GameState.game.getResponsePage() != 0) {
				GameState.game.setResponsePage(GameState.game.getResponsePage() - 1);
				GameState.game.updateResponses();
//				GameState.game.setResponses(GameState.game.getCurrentDialogueNode());
			}
		} else {
			if (GameState.game.getResponsePage() == 0)
				GameState.game.setContent(GameState.game.getResponsePage() * MainController.RESPONSE_COUNT + index);
			else {
				if (index != 0)
					GameState.game.setContent(GameState.game.getResponsePage() * MainController.RESPONSE_COUNT + index - 1);
				else
					GameState.game.setContent(GameState.game.getResponsePage() * MainController.RESPONSE_COUNT + (MainController.RESPONSE_COUNT-1));
			}

		}
	}

	public SetContentEventListener setIndex(int index) {
		this.index = index;

		nextPage = false;
		previousPage = false;
		return this;
	}

	public SetContentEventListener nextPage() {
		nextPage = true;
		previousPage = false;

		return this;
	}

	public SetContentEventListener previousPage() {
		nextPage = false;
		previousPage = true;

		return this;
	}
}
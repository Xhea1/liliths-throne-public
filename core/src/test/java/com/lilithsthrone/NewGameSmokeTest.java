package com.lilithsthrone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.lilithsthrone.controller.Panel;
import com.lilithsthrone.controller.ScreenBindings;
import com.lilithsthrone.controller.Ui;
import com.lilithsthrone.controller.UiInput;
import com.lilithsthrone.game.Game;
import com.lilithsthrone.game.combat.Combat;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.utils.OptionsDialogue;
import com.lilithsthrone.game.sex.Sex;
import com.lilithsthrone.main.GameState;
import com.lilithsthrone.main.Platform;
import com.lilithsthrone.rendering.RenderSink;
import com.lilithsthrone.utils.storage.DefaultGameStorage;
import com.lilithsthrone.utils.storage.Storage;

/**
 * Boots the engine headless the way the Android server does (no UI toolkit at all), so a change
 * that breaks core's front-end seams -- storage wiring, the Ui registry, input reads, the
 * thread hand-off -- fails here instead of on a phone.
 */
class NewGameSmokeTest {

	@TempDir
	Path data;

	/** Front-end stand-in: records the main panel and serves text-field values from a map. */
	private static final Map<String, String> INPUTS = new HashMap<>();
	private static final StringBuilder MAIN = new StringBuilder();

	private static Object stub(Class<?> type, java.lang.reflect.InvocationHandler special) {
		return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (proxy, method, args) -> {
			Object handled = special.invoke(proxy, method, args);
			if (handled != null) {
				return handled == Void.TYPE ? null : handled;
			}
			Class<?> r = method.getReturnType();
			if (r == boolean.class) {
				return false;
			}
			if (r == int.class) {
				return 0;
			}
			return null;
		});
	}

	@Test
	void bootsMenuAndRoutesInputsAndClicks() throws Exception {
		Storage.set(new DefaultGameStorage(data.toFile(), new File("res")));
		Storage.get().ensureDirectoriesExist();

		GameState.setPlatform((Platform) stub(Platform.class, (p, m, a) -> {
			if (m.getName().equals("runOnUiThread")) {
				((Runnable) a[0]).run();
				return Void.TYPE;
			}
			return null;
		}));
		GameState.setRenderSink((RenderSink) stub(RenderSink.class, (p, m, a) -> {
			switch (m.getName()) {
				case "setMainContent":
					MAIN.setLength(0);
					MAIN.append((String) a[0]);
					return Void.TYPE;
				case "getInputValue":
					return INPUTS.get((String) a[0]);
				default:
					return null;
			}
		}));

		GameState.initCredits();
		GameState.initProperties();
		GameState.game = new Game();
		GameState.sex = new Sex();
		GameState.combat = new Combat();
		GameState.game.setContent(new Response("", "", OptionsDialogue.MENU));

		assertFalse(MAIN.toString().isEmpty(), "main menu rendered nothing");

		// Settings persisted through the storage layer, not the working directory.
		GameState.getProperties().savePropertiesAsXML();
		assertTrue(new File(data.toFile(), "properties.xml").isFile(), "properties.xml not written via Storage");

		// A screen's registrar populates Ui, and a click on one of its ids runs.
		ScreenBindings.registerFor(OptionsDialogue.UNIT_PREFERENCE);
		assertNotNull(Ui.get(Panel.MAIN, "METRIC_SIZES_ON"), "unit screen registered no bindings");
		assertTrue(Ui.dispatch(Panel.MAIN, "METRIC_SIZES_ON"));
		assertFalse(Ui.dispatch(Panel.MAIN, "no_such_id"));

		// Text typed on the client reaches core, including awkward characters; missing -> "".
		INPUTS.put("nameField", "A}\\n'b");
		assertEquals("A}\\n'b", UiInput.get("nameField"));
		assertEquals("", UiInput.get("absent"));
	}
}

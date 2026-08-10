package com.lilithsthrone.android.service;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.lilithsthrone.android.render.HttpRenderSink;
import com.lilithsthrone.controller.Panel;
import com.lilithsthrone.controller.Ui;
import com.lilithsthrone.game.Game;
import com.lilithsthrone.game.combat.Combat;
import com.lilithsthrone.game.dialogue.utils.OptionsDialogue;
import com.lilithsthrone.game.sex.Sex;
import com.lilithsthrone.main.GameState;

import fi.iki.elonen.NanoHTTPD;

/**
 * Local-only HTTP front-end for the game engine.
 *
 * The in-app WebView loads {@code /} from here and posts user actions back to {@code /action}.
 * This replaces the desktop approach of attaching Java {@code EventListener}s to individual DOM
 * elements: a real WebView plus a delegated click handler makes that bookkeeping unnecessary.
 *
 * Bound to loopback only -- nothing is exposed off-device.
 */
public class GameHttpServer extends NanoHTTPD {

	private static final String TAG = "LTServer";
	public static final int PORT = 47653;

	private static final Pattern OPTION_ID = Pattern.compile("^option_(\\d+)$");
	private static final Pattern TAB_ID = Pattern.compile("^tab_(\\d+)$");

	private final HttpRenderSink sink;
	private boolean gameBooted = false;

	public GameHttpServer(HttpRenderSink sink) {
		super("127.0.0.1", PORT);
		this.sink = sink;
	}

	/** Builds the game singletons and renders the main menu. Mirrors desktop's Main.start(). */
	private synchronized void bootGameIfNeeded() {
		if (gameBooted) {
			return;
		}
		GameState.initCredits();
		GameState.initProperties();
		GameState.game = new Game();
		GameState.sex = new Sex();
		GameState.combat = new Combat();
		GameState.game.setContent(new com.lilithsthrone.game.dialogue.responses.Response("", "", OptionsDialogue.MENU));
		gameBooted = true;
		Log.i(TAG, "game booted; main panel " + sink.getMainContent().length() + " chars");
	}

	@Override
	public Response serve(IHTTPSession session) {
		String uri = session.getUri();
		try {
			switch (uri) {
				case "/":
				case "/index.html":
					bootGameIfNeeded();
					return html(Pages.shell(sink.getMainContent(), sink.getAttributePanelContent(),
							sink.getRightPanelContent(), sink.getButtonsLeftContent(),
							sink.getButtonsRightContent()));

				case "/panel/main":
					bootGameIfNeeded();
					return html(sink.getMainContent());

				case "/panel/attributes":
					bootGameIfNeeded();
					return html(sink.getAttributePanelContent());

				case "/panel/right":
					bootGameIfNeeded();
					return html(sink.getRightPanelContent());

				case "/panel/buttonsLeft":
					bootGameIfNeeded();
					return html(sink.getButtonsLeftContent());

				case "/panel/buttonsRight":
					bootGameIfNeeded();
					return html(sink.getButtonsRightContent());

				case "/css/webView_stylesheet.css":
					return css("/com/lilithsthrone/res/css/webView_stylesheet.css");

				case "/action":
					bootGameIfNeeded();
					return html(handleAction(session));

				default:
					return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found: " + uri);
			}
		} catch (Exception e) {
			Log.e(TAG, "Error serving " + uri, e);
			return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error: " + e);
		}
	}

	/**
	 * Dispatches a clicked element id to the equivalent game action, then returns the freshly
	 * rendered main panel. Mirrors desktop's {@code SetContentEventListener} id conventions.
	 */
	private String handleAction(IHTTPSession session) throws IOException, ResponseException {
		Map<String, String> body = new java.util.HashMap<>();
		session.parseBody(body);
		String json = body.get("postData");
		String id = session.getParameters().containsKey("id")
				? session.getParameters().get("id").get(0)
				: extractField(json, "id");
		Panel panel = parsePanel(extractField(json, "panel"));
		sink.setInputValues(extractInputs(json));

		if (id == null || id.isEmpty()) {
			Log.w(TAG, "action with no id");
			return sink.getMainContent();
		}
		Log.i(TAG, "action id=" + id);

		Matcher option = OPTION_ID.matcher(id);
		Matcher tab = TAB_ID.matcher(id);
		if (option.matches()) {
			GameState.game.setContent(Integer.parseInt(option.group(1)));
		} else if (tab.matches()) {
			GameState.game.setResponseTab(Integer.parseInt(tab.group(1)));
			GameState.game.setContent(new com.lilithsthrone.game.dialogue.responses.Response("", "", GameState.game.getCurrentDialogueNode()));
		} else if (!Ui.dispatch(panel, id)) {
			// Not every id is bound yet; log rather than fail so the gap is visible while the
			// remaining controllers are migrated into core.
			Log.i(TAG, "unhandled id: " + id + " (panel " + panel + ")");
		}
		return sink.getMainContent();
	}

	/** Which panel the click came from; ids are only unique within a panel. */
	private static Panel parsePanel(String panel) {
		if (panel == null) {
			return Panel.MAIN;
		}
		try {
			return Panel.valueOf(panel);
		} catch (IllegalArgumentException e) {
			return Panel.MAIN;
		}
	}

	/**
	 * Pulls the {@code "inputs":{...}} object out of the posted body: a flat map of element id to
	 * current text, which stands in for the live DOM the desktop front-end can interrogate.
	 */
	private static Map<String, String> extractInputs(String json) {
		Map<String, String> values = new java.util.HashMap<>();
		if (json == null) {
			return values;
		}
		Matcher object = Pattern.compile("\"inputs\"\\s*:\\s*\\{(.*?)\\}", Pattern.DOTALL).matcher(json);
		if (!object.find()) {
			return values;
		}
		Matcher entry = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(object.group(1));
		while (entry.find()) {
			values.put(entry.group(1), unescape(entry.group(2)));
		}
		return values;
	}

	/** Reverses the escaping JSON.stringify applies to the field values. */
	private static String unescape(String value) {
		return value.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t")
				.replace("\\\"", "\"").replace("\\\\", "\\");
	}

	/** Minimal string-field extraction, to avoid pulling in a JSON dependency for two fields. */
	private static String extractField(String json, String field) {
		if (json == null) {
			return null;
		}
		Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
		return m.find() ? m.group(1) : null;
	}

	/** Serves a stylesheet straight out of core's jar resources. */
	private Response css(String resourcePath) {
		java.io.InputStream in = GameHttpServer.class.getResourceAsStream(resourcePath);
		if (in == null) {
			Log.w(TAG, "Missing stylesheet resource: " + resourcePath);
			return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "no css");
		}
		Response r = newChunkedResponse(Response.Status.OK, "text/css; charset=utf-8", in);
		r.addHeader("Cache-Control", "no-store");
		return r;
	}

	private Response html(String body) {
		Response r = newFixedLengthResponse(Response.Status.OK, "text/html; charset=utf-8", body);
		r.addHeader("Cache-Control", "no-store");
		return r;
	}
}

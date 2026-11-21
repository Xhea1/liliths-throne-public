package com.lilithsthrone.android.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import com.lilithsthrone.android.LilithsThroneApplication;
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
		// The engine, Ui's registry and the shared Rhino scope are single-threaded, but NanoHTTPD
		// answers every request on its own thread (and the page fires several per tap).
		synchronized (LilithsThroneApplication.ENGINE_LOCK) {
			return serveLocked(session);
		}
	}

	private Response serveLocked(IHTTPSession session) {
		String uri = session.getUri();
		// Other apps and any browser tab can reach loopback; only our own WebView sends this Host.
		if (!("127.0.0.1:" + PORT).equals(session.getHeaders().get("host"))) {
			return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "Forbidden");
		}
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
					return handleAction(session);

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
	 * rendered main panel, or 204 if the id did nothing (so the page keeps its inputs and scroll).
	 * Mirrors desktop's {@code SetContentEventListener} id conventions.
	 */
	private Response handleAction(IHTTPSession session) throws IOException, ResponseException {
		// Without a JSON body type a cross-site form post could drive the game with no preflight.
		String contentType = session.getHeaders().get("content-type");
		if (contentType == null || !contentType.startsWith("application/json")) {
			return newFixedLengthResponse(Response.Status.FORBIDDEN, MIME_PLAINTEXT, "Forbidden");
		}
		Map<String, String> body = new java.util.HashMap<>();
		session.parseBody(body);

		String id;
		Panel panel;
		Map<String, String> inputs = new java.util.HashMap<>();
		try {
			JSONObject json = new JSONObject(body.get("postData"));
			id = json.optString("id");
			panel = parsePanel(json.optString("panel"));
			JSONObject fields = json.optJSONObject("inputs");
			if (fields != null) {
				for (java.util.Iterator<String> keys = fields.keys(); keys.hasNext();) {
					String key = keys.next();
					inputs.put(key, fields.optString(key));
				}
			}
		} catch (JSONException | NullPointerException e) {
			return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Bad request");
		}
		sink.setInputValues(inputs);

		if (id.isEmpty()) {
			return noContent();
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
			return noContent();
		}

		Response response = html(sink.getMainContent());
		// Lets the page tell "same screen re-rendered" (keep scroll) from "new screen" (scroll to top).
		response.addHeader("X-LT-Node", String.valueOf(System.identityHashCode(GameState.game.getCurrentDialogueNode())));
		String flash = sink.takeFlashMessageText();
		if (!flash.isEmpty()) {
			response.addHeader("X-LT-Flash", encode(flash));
			response.addHeader("X-LT-Flash-Colour", sink.getFlashMessageColour() == null
					? "#ffffff" : sink.getFlashMessageColour().toWebHexString());
		}
		return response;
	}

	private Response noContent() {
		return newFixedLengthResponse(Response.Status.NO_CONTENT, MIME_PLAINTEXT, "");
	}

	/** Header-safe text; URLEncoder writes spaces as '+', which decodeURIComponent would not undo. */
	private static String encode(String text) {
		try {
			return URLEncoder.encode(text, "UTF-8").replace("+", "%20");
		} catch (java.io.UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	/** Which panel the click came from; ids are only unique within a panel. */
	private static Panel parsePanel(String panel) {
		try {
			return Panel.valueOf(panel);
		} catch (IllegalArgumentException e) {
			return Panel.MAIN;
		}
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

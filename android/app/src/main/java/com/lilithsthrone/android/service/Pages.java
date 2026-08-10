package com.lilithsthrone.android.service;

/**
 * The HTML shell wrapped around core's generated panel markup, plus the click relay.
 *
 * The relay is a single delegated listener rather than per-element handlers: any clicked element
 * carrying an id POSTs it to {@code /action} and swaps in the returned HTML. Desktop needs
 * explicit bind/unbind bookkeeping because its WebEngine content swap drops listeners; replacing
 * {@code innerHTML} in a real WebView has no such problem.
 */
final class Pages {

	private Pages() {
	}

	static String shell(String mainContent, String attributes, String right,
			String buttonsLeft, String buttonsRight) {
		return "<!doctype html><html><head>"
				+ "<meta charset='utf-8'>"
				// The game's CSS sizes everything in px against a ~1280px desktop window. Declaring
				// that as the viewport width makes the browser scale the whole layout to fit the
				// screen, keeping desktop proportions instead of rendering desktop px at phone px
				// (which came out far too large). User scaling is left enabled deliberately.
				+ "<meta name='viewport' content='width=" + LAYOUT_WIDTH_PX + "'>"
				+ "<title>Lilith's Throne</title>"
				// The game's own web stylesheet, shared with the desktop front-end (core resource).
				+ "<link rel='stylesheet' href='/css/webView_stylesheet.css'>"
				+ "<style>" + MOBILE_OVERRIDES + "</style>"
				+ "</head><body>"
				+ "<div id='viewport'>"
				+ "<div id='panels'>"
				+ "<div id='panel-attributes' class='sidepanel' data-panel='ATTRIBUTES'>" + attributes + "</div>"
				+ "<div id='content' data-panel='MAIN'>" + mainContent + "</div>"
				+ "<div id='panel-right' class='sidepanel' data-panel='RIGHT'>" + right + "</div>"
				+ "</div>"
				+ "<div id='chrome'>"
				+ "<div id='panel-buttons-left' data-panel='BUTTONS_LEFT'>" + buttonsLeft + "</div>"
				+ "<div id='actionbar'></div>"
				+ "<div id='panel-buttons-right' data-panel='BUTTONS_RIGHT'>" + buttonsRight + "</div>"
				+ "</div>"
				+ "</div>"
				+ "<script>" + SCRIPT + "</script>"
				+ "</body></html>";
	}

	/** Logical width the game's stylesheet was authored against (see main.fxml's prefWidth). */
	private static final int LAYOUT_WIDTH_PX = 1280;

	private static final String MOBILE_OVERRIDES =
			"html,body{margin:0;padding:0;background:#1a1a1a;-webkit-text-size-adjust:100%}"
			+ "img,svg{max-width:100%;height:auto}"
			+ "*{-webkit-tap-highlight-color:rgba(255,255,255,.15)}"

			// Fixed-height column: the passage of text scrolls, the action bar stays put at the
			// bottom. #RESPONSE_BOX is relocated into #actionbar by script after every render --
			// see SCRIPT below for why that isn't done in CSS.
			+ "#viewport{display:flex;flex-direction:column;height:100vh;overflow:hidden}"
			// Three columns above, chrome strip below. The side panels are narrow because the
			// desktop layout gives them fixed pixel widths against a 1280px window.
			+ "#panels{flex:1 1 auto;display:flex;min-height:0}"
			+ "#content{flex:1 1 auto;overflow-y:auto;-webkit-overflow-scrolling:touch;min-width:0}"
			+ ".sidepanel{flex:0 0 22%;overflow-y:auto;-webkit-overflow-scrolling:touch;"
			+ "font-size:0.85em;background:#141414}"
			+ ".sidepanel:empty{display:none}"
			+ "#chrome{flex:0 0 auto;display:flex;align-items:stretch;"
			+ "background:#1a1a1a;border-top:1px solid #333}"
			+ "#actionbar{flex:1 1 auto;min-width:0}"
			+ "#panel-buttons-left,#panel-buttons-right{flex:0 0 auto;display:flex;align-items:center}"
			+ "#panel-buttons-left:empty,#panel-buttons-right:empty{display:none}"

			// --- Response box (the game's action bar) -------------------------------------
			// The engine always emits all 15 response slots plus both page-switcher arrows,
			// whether or not they're in use. That's a tidy 5x3 grid in a 1280px desktop panel,
			// but squeezes into unusable slivers on a phone, so:

			// 1. Hide slots that hold no action.
			+ "#RESPONSE_BOX .response-box.disabled:not(:has(.response-text)){display:none}"
			// 2. Hide the page arrows unless there's actually another page to reach.
			+ "#RESPONSE_BOX .response-switcher.disabled{display:none}"
			// 3. Lay the remaining actions out full-width, wrapping, with touch-sized targets.
			+ "#RESPONSE_BOX .response-full-container{display:flex;width:100%;max-width:none;"
			+ "box-sizing:border-box}"
			+ "#RESPONSE_BOX .response-container{display:flex;flex-wrap:wrap;flex:1 1 auto;"
			+ "width:auto;height:auto;gap:4px;padding:4px;white-space:normal}"
			// 5 per row, matching the desktop grid. Sizes are in CSS px against the 1280px
			// viewport declared above, so they are scaled up on screen -- 26px here lands at
			// roughly 48 physical px on a 2400px-wide display, i.e. a comfortable touch target.
			+ "#RESPONSE_BOX .response-box{position:static;flex:1 1 18%;min-width:18%;"
			+ "width:auto;height:auto;min-height:26px;margin:0;display:flex;"
			+ "align-items:center;justify-content:center}"
			+ "#RESPONSE_BOX .response-text{position:static;width:auto;padding:0 6px;margin:0}"
			// Keyboard shortcut badges ("DIGIT1", "SHIFT + DIGIT2", ...) mean nothing on touch.
			+ ".hotkey-icon{display:none !important}";

	/**
	 * Relocating the response box is done in script rather than CSS because the game's own
	 * stylesheet gives its ancestors their own flex/height rules ({@code #main-content} is
	 * {@code display:flex; height:100%}), which fight any attempt to anchor a descendant to the
	 * bottom of the screen. Moving the element into a dedicated footer sidesteps that entirely.
	 *
	 * It has to re-run after every content swap, since each response returns fresh markup that
	 * contains its own {@code #RESPONSE_BOX}.
	 */
	private static final String SCRIPT =
			"function relocateActionBar(){"
			+ "var bar=document.querySelector('#content #RESPONSE_BOX');"
			+ "var slot=document.getElementById('actionbar');"
			+ "if(bar&&slot){slot.innerHTML='';slot.appendChild(bar);}"
			+ "}"
			// Side panels are re-rendered by the engine every turn, so pull them after each action.
			+ "function refreshPanels(){"
			+ "[['/panel/attributes','panel-attributes'],"
			+ "['/panel/right','panel-right'],"
			+ "['/panel/buttonsLeft','panel-buttons-left'],"
			+ "['/panel/buttonsRight','panel-buttons-right']]"
			+ ".forEach(function(p){"
			+ "fetch(p[0]).then(function(r){return r.text()})"
			+ ".then(function(h){var el=document.getElementById(p[1]); if(el)el.innerHTML=h;});"
			+ "});"
			+ "}"
			// Element ids are only unique within a panel, so tell the server which one was clicked.
			+ "function panelOf(el){"
			+ "var p=el.closest('[data-panel]');"
			+ "return p?p.getAttribute('data-panel'):'MAIN';"
			+ "}"
			+ "relocateActionBar();"
			+ "document.addEventListener('click',function(e){"
			+ "var el=e.target.closest('[id]');"
			+ "if(!el||!el.id)return;"
			+ "fetch('/action',{method:'POST',headers:{'Content-Type':'application/json'},"
			+ "body:JSON.stringify({id:el.id,type:'click',panel:panelOf(el)})})"
			+ ".then(function(r){return r.text()})"
			+ ".then(function(html){"
			+ "document.getElementById('content').innerHTML=html;"
			+ "relocateActionBar();"
			+ "document.getElementById('content').scrollTop=0;"
			+ "refreshPanels();"
			+ "})"
			+ ".catch(function(err){console.error('action failed',err)});"
			+ "});";
}

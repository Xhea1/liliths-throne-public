package com.lilithsthrone.controller;

/**
 * The screen regions a front-end renders into.
 *
 * Desktop gives each one its own {@code WebView}/{@code Document}; the HTTP front-end serves each
 * as a separate route. Element ids are only unique within a panel, so {@link Ui} keys its bindings
 * on (panel, id) rather than on the id alone.
 */
public enum Panel {

	MAIN,
	ATTRIBUTES,
	RIGHT,
	BUTTONS_LEFT,
	BUTTONS_RIGHT;
}

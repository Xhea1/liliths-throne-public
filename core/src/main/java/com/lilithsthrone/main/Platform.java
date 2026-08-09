package com.lilithsthrone.main;

import java.io.File;

/**
 * The small set of OS/window-level hooks {@code core} needs but can't implement itself
 * without depending on a specific UI toolkit (java.awt.Toolkit, javafx.stage.Stage,
 * java.awt.Desktop are all unavailable or inappropriate on Android). Desktop and Android
 * each provide one implementation, set once at process start via {@link GameState#setPlatform(Platform)}.
 */
public interface Platform {

	/** Replaces the direct {@code java.awt.Toolkit.getDefaultToolkit().getScreenSize()} call. */
	int getScreenHeightPx();

	/**
	 * Whether to render hints that only make sense for the desktop distribution: which JVM to
	 * launch with, and how to maximise the window. False where the user neither chooses a JVM
	 * nor manages a window (e.g. Android), so that advice like "use the .exe build" doesn't
	 * appear on a phone.
	 */
	boolean showsDesktopLauncherHints();

	/** Width of the game's window/viewport, replacing {@code Stage#getWidth()}. */
	int getWindowWidthPx();

	/** Replaces the direct check for whether the game is running from an unpacked temp/zip directory. */
	boolean isRunningFromTempDir();

	/** Replaces {@code java.awt.Desktop#browse}/{@code Runtime.exec("xdg-open ...")}. */
	void openLink(String url);

	/** Replaces {@code Stage#close()} + {@code System.exit(0)}. */
	void exitApplication();

	/** Replaces {@code Stage#setTitle(...)}; a no-op is a valid implementation. */
	void refreshTitle();

	/** Replaces {@code Toolkit.getDefaultToolkit().getSystemClipboard()}. */
	void copyToClipboard(String text);

	/**
	 * Decodes an image file and downscales it to fit within the given bounds, returning it as
	 * a base64 PNG {@code data:} URI (the form the HTML UI embeds directly).
	 *
	 * Image decoding is the one genuinely platform-specific part of character-art rendering
	 * (desktop: ImageIO/Graphics2D, Android: BitmapFactory), so it sits here rather than in
	 * {@code core}. Implementations may return {@code null} if they don't support image
	 * loading yet -- callers must handle that.
	 *
	 * @return the loaded image, or {@code null} if decoding failed or is unsupported.
	 */
	LoadedImage loadImageAsDataUri(File file, int maxWidth, int maxHeight);

	/** Result of {@link #loadImageAsDataUri}: the encoded image plus its final dimensions. */
	class LoadedImage {
		public final String dataUri;
		public final int width;
		public final int height;
		/** Dimensions of the image before downscaling, used for aspect-ratio-based layout. */
		public final int originalWidth;
		public final int originalHeight;

		public LoadedImage(String dataUri, int width, int height, int originalWidth, int originalHeight) {
			this.dataUri = dataUri;
			this.width = width;
			this.height = height;
			this.originalWidth = originalWidth;
			this.originalHeight = originalHeight;
		}
	}
}

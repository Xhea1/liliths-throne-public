package com.lilithsthrone.rendering;

import java.io.File;

import com.lilithsthrone.main.GameState;
import com.lilithsthrone.main.Platform;

/**
 * @since 0.2.5.5
 * @version 0.3.0
 * @author Addi
 */
public class CachedImage {
	protected String imageString = "";
	protected int width = 200, height = 200, percentageWidth = 35;

	/**
	 * Load an image from the given file path into a reusable string.
	 * @param f A File containing the path to a .jpg or .png image
	 * @return True if the image was successfully loaded, false otherwise
	 */
	public boolean load(File f) {
		Platform platform = GameState.getPlatform();
		if (platform == null) {
			return false;
		}
		Platform.LoadedImage loaded = platform.loadImageAsDataUri(f, 600, 600);
		if (loaded == null) {
			return false;
		}
		updatePercentageWidth(loaded.originalWidth, loaded.originalHeight);
		width = loaded.width;
		height = loaded.height;
		imageString = loaded.dataUri;
		return true;
	}

	void updatePercentageWidth(int imageWidth, int imageHeight) {
		width = imageWidth;
		height = imageHeight;
		if(imageHeight == imageWidth) {
			percentageWidth = 45;
		} else if(imageHeight < imageWidth) {
			percentageWidth = 65;
		}
	}

	/**
	 * Retrieve the image in base 64 encoded string format. The string must start with 'data:image/png;base64'.
	 * @return The image as base64 string
	 */
	public String getImageString() {
		return imageString;
	}

	/**
	 * Retrieve the thumbnail of the image.
	 * @return The thumbnail image as base64 string
	 */
	public String getThumbnailString() {
		return imageString;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * Retrieve the width percentage that the image is supposed to take on the character information page. The value is
	 * calculated using the width to height ratio.
	 * @return 35 if the image is vertical, 45 if it's squared and 65 if it's horizontal.
	 */
	public int getPercentageWidth() {
		return percentageWidth;
	}

	/**
	 * Calculate and retrieve the size of the image after fitting it inside a box with the given dimensions.
	 * @param maxWidth Width of the box to fit the image into
	 * @param maxHeight Height of the box to fit the image into
	 * @return An integer array with the format [width, height] containing the fitted dimensions.
	 */
	public int[] getAdjustedSize(int maxWidth, int maxHeight) {
		// Calculate ratio between desired and original dimensions
		float widthRatio = (float) maxWidth / (float) width;
		float heightRatio = (float) maxHeight / (float) height;
		// Use the one that produces the smaller image as scaling factor
		float scale = Math.min(widthRatio, heightRatio);
		return new int[]{(int)(width * scale), (int)(height * scale)};
	}
}

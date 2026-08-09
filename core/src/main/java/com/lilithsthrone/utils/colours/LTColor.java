package com.lilithsthrone.utils.colours;

import java.util.Locale;

/**
 * Platform-neutral RGBA colour value, replacing both {@code javafx.scene.paint.Color}
 * (desktop UI colours) and {@code java.awt.Color} (world-map bitmap colour keys) so that
 * {@code core} does not depend on JavaFX or AWT, neither of which are available on Android.
 *
 * Channel values are stored in the 0.0-1.0 range, matching JavaFX's {@code Color} internal
 * representation, since almost all call sites were written against that convention.
 */
public final class LTColor {

	private final double red, green, blue, opacity;

	private LTColor(double red, double green, double blue, double opacity) {
		this.red = clamp(red);
		this.green = clamp(green);
		this.blue = clamp(blue);
		this.opacity = clamp(opacity);
	}

	private static double clamp(double value) {
		if (value < 0) return 0;
		if (value > 1) return 1;
		return value;
	}

	public static LTColor color(double red, double green, double blue) {
		return new LTColor(red, green, blue, 1.0);
	}

	public static LTColor color(double red, double green, double blue, double opacity) {
		return new LTColor(red, green, blue, opacity);
	}

	public static LTColor rgb(int red, int green, int blue) {
		return new LTColor(red / 255.0, green / 255.0, blue / 255.0, 1.0);
	}

	public static LTColor rgb(int red, int green, int blue, double opacity) {
		return new LTColor(red / 255.0, green / 255.0, blue / 255.0, opacity);
	}

	/**
	 * Parses a web colour string: {@code #RGB}, {@code #RRGGBB}, or {@code #RRGGBBAA}
	 * (with or without the leading {@code #}), mirroring {@code javafx.scene.paint.Color#web}.
	 */
	public static LTColor web(String hex) {
		String s = hex.startsWith("#") ? hex.substring(1) : hex;
		if (s.length() == 3) {
			int r = Integer.parseInt(s.substring(0, 1), 16) * 17;
			int g = Integer.parseInt(s.substring(1, 2), 16) * 17;
			int b = Integer.parseInt(s.substring(2, 3), 16) * 17;
			return rgb(r, g, b);
		}
		int r = Integer.parseInt(s.substring(0, 2), 16);
		int g = Integer.parseInt(s.substring(2, 4), 16);
		int b = Integer.parseInt(s.substring(4, 6), 16);
		double a = s.length() >= 8 ? Integer.parseInt(s.substring(6, 8), 16) / 255.0 : 1.0;
		return rgb(r, g, b, a);
	}

	/**
	 * Mirrors AWT's {@code new Color(int rgb)}: a packed 0xRRGGBB integer. Used for the
	 * world-map bitmap colour keys, which were written against that constructor.
	 */
	public static LTColor hex(int rgb) {
		return rgb((rgb & 0xFF0000) >> 16, (rgb & 0xFF00) >> 8, rgb & 0xFF);
	}

	/**
	 * Mirrors {@code java.awt.Color#decode}: accepts "#RRGGBB", "0xRRGGBB", or a plain
	 * decimal/octal string as understood by {@link Integer#decode(String)}.
	 */
	public static LTColor decode(String value) {
		int hex = Integer.decode(value);
		return rgb((hex & 0xFF0000) >> 16, (hex & 0xFF00) >> 8, hex & 0xFF);
	}

	/**
	 * Equivalent to {@code javafx.scene.paint.Color#hsb}: hue in degrees [0,360),
	 * saturation/brightness/alpha in [0,1].
	 */
	public static LTColor hsb(double hue, double saturation, double brightness, double alpha) {
		double h = hue;
		while (h < 0) h += 360;
		while (h >= 360) h -= 360;
		double c = brightness * saturation;
		double x = c * (1 - Math.abs((h / 60.0) % 2 - 1));
		double m = brightness - c;
		double r, g, b;
		if (h < 60)      { r = c; g = x; b = 0; }
		else if (h < 120) { r = x; g = c; b = 0; }
		else if (h < 180) { r = 0; g = c; b = x; }
		else if (h < 240) { r = 0; g = x; b = c; }
		else if (h < 300) { r = x; g = 0; b = c; }
		else              { r = c; g = 0; b = x; }
		return new LTColor(r + m, g + m, b + m, alpha);
	}

	public double getRed() {
		return red;
	}

	public double getGreen() {
		return green;
	}

	public double getBlue() {
		return blue;
	}

	public double getOpacity() {
		return opacity;
	}

	/**
	 * Equivalent to {@code javafx.scene.paint.Color#deriveColor}: shifts hue by
	 * {@code hueShift} degrees and scales saturation/brightness/opacity by the given factors.
	 */
	public LTColor deriveColor(double hueShift, double saturationFactor, double brightnessFactor, double opacityFactor) {
		double max = Math.max(red, Math.max(green, blue));
		double min = Math.min(red, Math.min(green, blue));
		double delta = max - min;

		double hue;
		if (delta == 0) {
			hue = 0;
		} else if (max == red) {
			hue = 60 * (((green - blue) / delta) % 6);
		} else if (max == green) {
			hue = 60 * (((blue - red) / delta) + 2);
		} else {
			hue = 60 * (((red - green) / delta) + 4);
		}
		if (hue < 0) hue += 360;

		double saturation = max == 0 ? 0 : delta / max;
		double brightness = max;

		return hsb(hue + hueShift, clamp(saturation * saturationFactor), clamp(brightness * brightnessFactor), clamp(opacity * opacityFactor));
	}

	/**
	 * "#RRGGBB" form, matching the codebase's existing {@code toWebHexString} convention.
	 */
	public String toWebHexString() {
		return String.format(Locale.ROOT, "#%02X%02X%02X", Math.round(red * 255), Math.round(green * 255), Math.round(blue * 255));
	}

	@Override
	public String toString() {
		return String.format(Locale.ROOT, "0x%02X%02X%02X%02X", Math.round(red * 255), Math.round(green * 255), Math.round(blue * 255), Math.round(opacity * 255));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LTColor)) return false;
		LTColor other = (LTColor) obj;
		return red == other.red && green == other.green && blue == other.blue && opacity == other.opacity;
	}

	@Override
	public int hashCode() {
		return Double.hashCode(red) ^ Double.hashCode(green) ^ Double.hashCode(blue) ^ Double.hashCode(opacity);
	}
}

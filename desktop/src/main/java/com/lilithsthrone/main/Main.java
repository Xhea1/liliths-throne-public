package com.lilithsthrone.main;

import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Base64;

import javax.imageio.ImageIO;

import com.lilithsthrone.controller.MainController;
import com.lilithsthrone.controller.TooltipUpdateThread;
import com.lilithsthrone.game.Game;
import com.lilithsthrone.game.PropertyValue;
import com.lilithsthrone.game.combat.Combat;
import com.lilithsthrone.game.dialogue.responses.Response;
import com.lilithsthrone.game.dialogue.utils.OptionsDialogue;
import com.lilithsthrone.game.sex.Sex;
import com.lilithsthrone.utils.storage.Storage;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * The desktop (JavaFX) entry point. Process-wide game state, settings and save/load
 * orchestration live in {@link GameState} so that they can be shared with non-JavaFX
 * front-ends; this class is now just the JavaFX bootstrapper plus the desktop
 * implementation of {@link Platform}.
 *
 * @since 0.1.0
 * @version 0.4.11
 * @author Innoxia
 */
public class Main extends Application implements Platform {

	public static MainController mainController;

	public static Scene mainScene;

	public static Stage primaryStage;

	public static Image WINDOW_IMAGE;

	@Override
	public void start(Stage primaryStage) throws Exception {

		CheckForDataDirectory();
		CheckForResFolder();

		GameState.initCredits();

		Main.primaryStage = primaryStage;

		Main.primaryStage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
				if(t) {
					TooltipUpdateThread.cancelThreads = true;
				}
			}
		});

		WINDOW_IMAGE = new Image("/com/lilithsthrone/res/images/windowIcon32.png");

		Main.primaryStage.getIcons().add(WINDOW_IMAGE);

		refreshTitle();

		loadFonts();

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lilithsthrone/res/fxml/main.fxml"));

		Pane pane = loader.load();

		mainScene = new Scene(pane);

		if (GameState.getProperties().hasValue(PropertyValue.lightTheme)) {
			mainScene.getStylesheets().add("/com/lilithsthrone/res/css/stylesheet_light.css");
		} else {
			mainScene.getStylesheets().add("/com/lilithsthrone/res/css/stylesheet.css");
		}

		mainController = loader.getController();
		GameState.setRenderSink(mainController);
		Main.primaryStage.setScene(mainScene);
		Main.primaryStage.show();
		GameState.game = new Game();
		GameState.sex = new Sex();
		GameState.combat = new Combat();

		GameState.game.setContent(new Response("", "", OptionsDialogue.MENU));
	}

	/**
	 * Rebuilds the FXML scene after world generation, mirroring the behaviour that used to
	 * live inline in {@code startNewGame}'s onSucceeded handler.
	 */
	public static void rebuildSceneForNewGame() {
		FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/lilithsthrone/res/fxml/main.fxml"));
		try {
			if (Main.mainScene == null) {
				Pane pane = loader.load();
				Main.mainController = loader.getController();
				GameState.setRenderSink(Main.mainController);

				Main.mainScene = new Scene(pane);
				if (GameState.getProperties().hasValue(PropertyValue.lightTheme))
					Main.mainScene.getStylesheets().add("/com/lilithsthrone/res/css/stylesheet_light.css");
				else
					Main.mainScene.getStylesheets().add("/com/lilithsthrone/res/css/stylesheet.css");
			}

			Main.primaryStage.setScene(Main.mainScene);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ------------------------------------------------------------------
	// Platform implementation (desktop)
	// ------------------------------------------------------------------

	@Override
	public int getScreenHeightPx() {
		return (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	}

	@Override
	public int getWindowWidthPx() {
		return primaryStage == null ? 0 : (int) primaryStage.getWidth();
	}

	@Override
	public boolean showsDesktopLauncherHints() {
		return true;
	}

	@Override
	public boolean isRunningFromTempDir() {
		String currentDir = new File("").getAbsolutePath();
		String tempDir = System.getProperty("java.io.tmpdir");
		return currentDir.contains(tempDir);
	}

	@Override
	public void openLink(String url) {
		Runtime runtime = Runtime.getRuntime();
		try {
			runtime.exec("xdg-open " + url);
		} catch (IOException e0) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI(url));
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
				e0.printStackTrace();
			}
		}
	}

	@Override
	public void exitApplication() {
		if (primaryStage != null) {
			primaryStage.close();
		}
		System.exit(0);
	}

	@Override
	public void refreshTitle() {
		if (primaryStage != null) {
			primaryStage.setTitle(GameState.getTitle());
		}
	}

	@Override
	public void runOnUiThread(Runnable task) {
		if (javafx.application.Platform.isFxApplicationThread()) {
			task.run();
		} else {
			javafx.application.Platform.runLater(task);
		}
	}

	@Override
	public void copyToClipboard(String text) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection data = new StringSelection(text);
		clipboard.setContents(data, data);
	}

	@Override
	public LoadedImage loadImageAsDataUri(File file, int maxWidth, int maxHeight) {
		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
			BufferedImage image = ImageIO.read(file);
			if (image == null) {
				return null;
			}
			int originalWidth = image.getWidth();
			int originalHeight = image.getHeight();

			// Fit within the requested bounds while preserving aspect ratio.
			float scale = Math.min((float) maxWidth / originalWidth, (float) maxHeight / originalHeight);
			int targetWidth = scale < 1 ? (int) (originalWidth * scale) : originalWidth;
			int targetHeight = scale < 1 ? (int) (originalHeight * scale) : originalHeight;

			image = scaleDown(image, targetWidth, targetHeight);

			ImageIO.setUseCache(false);
			ImageIO.write(image, "PNG", byteStream);
			String dataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(byteStream.toByteArray());
			return new LoadedImage(dataUri, image.getWidth(), image.getHeight(), originalWidth, originalHeight);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Scales down the given image using incremental, bilinear, anti-aliased sampling. Each step may only decrease the
	 * size by a factor of two, increasing the quality but taking additional time to complete. The parameters represent
	 * final values and do not respect the aspect ratio of the original image.
	 */
	private static BufferedImage scaleDown(BufferedImage original, int targetWidth, int targetHeight) {
		int width = original.getWidth();
		int height = original.getHeight();
		BufferedImage rv = original;

		while (width > targetWidth || height > targetHeight) {
			// Determine target dimensions while never decreasing size by more than half
			if (width > targetWidth)
				width = Math.max(width / 2, targetWidth);

			if (height > targetHeight)
				height = Math.max(height / 2, targetHeight);

			// Setup render target
			BufferedImage step = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D canvas = step.createGraphics();
			canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			canvas.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Render onto canvas
			canvas.drawImage(rv, 0, 0, width, height, null);
			canvas.dispose();

			rv = step;
		}

		return rv;
	}

	// ------------------------------------------------------------------
	// Startup checks / resources
	// ------------------------------------------------------------------

	protected static void CheckForDataDirectory() {
		File dir = Storage.get().dataDir();
		if(!dir.exists()) {
			Alert a = new Alert(AlertType.ERROR,
					"Unable to find the 'data' folder ("+dir.getAbsolutePath()+"). Saving and error logging is disabled."
							+ "\nMake sure that you've extracted the game from the zip file, and that the file has write permissions."
							+ "\n(Please read section 'MISSING FOLDERS' in the README.txt file.)"
							+ "\nContinue?",
					ButtonType.YES, ButtonType.NO);
			System.err.println("Unable to find the 'data' folder ("+dir.getAbsolutePath()+").");
			a.showAndWait().ifPresent(response -> {
			     if (response == ButtonType.NO) {
			         System.exit(1);
			     }
			 });
		}
	}

	protected static void CheckForResFolder() {
		File dir = Storage.get().resDir();
		if(!dir.exists()) {
			Alert a = new Alert(AlertType.WARNING,
					"Could not find the 'res' folder ("+dir.getAbsolutePath()+"). This WILL cause errors and present sections of missing text."
							+ "\nMake sure that you've extracted the game from the zip file, and that the file has write permissions."
							+ "\nIf you use the command line or a batch/script file to start the game, please try starting it in the game folder to prevent this error."
							+ "\n(Please read section 'MISSING FOLDERS' in the README.txt file.)"
							+ "\nContinue?",
					ButtonType.YES, ButtonType.NO);
			System.err.println("Unable to find the 'res' folder ("+dir.getAbsolutePath()+").");
			a.showAndWait().ifPresent(response -> {
				if(response == ButtonType.NO) {
					System.exit(1);
				}
			});
		}
	}

	/**
	 * Attempts to load fallback fonts to make sure that they are available later. The size doesn't actually matter as
	 * the WebEngine will reload other sizes as required. The files referenced must persist until application shutdown.
	 *
	 * Do not call Font.getFamilies() prior to this as additional fonts must be loaded before the list is cached.
	 */
	protected void loadFonts() {
		// Load fallback for Calibri
		if (Font.loadFont(toUri("res/fonts/Carlito/Carlito-Regular.ttf"), 11) != null) {
			// Load variants
			Font.loadFont(toUri("res/fonts/Carlito/Carlito-Bold.ttf"), 11);
			Font.loadFont(toUri("res/fonts/Carlito/Carlito-BoldItalic.ttf"), 11);
			Font.loadFont(toUri("res/fonts/Carlito/Carlito-Italic.ttf"), 11);
		} else {
			System.err.println("Carlito font could not be loaded.");
		}

		// Load fallback for Verdana
		if (Font.loadFont(toUri("res/fonts/DejaVu Sans/DejaVuSans.ttf"), 12) != null) {
			// Load variants
			Font.loadFont(toUri("res/fonts/DejaVu Sans/DejaVuSans-Bold.ttf"), 12);
			Font.loadFont(toUri("res/fonts/DejaVu Sans/DejaVuSans-BoldOblique.ttf"), 12);
			Font.loadFont(toUri("res/fonts/DejaVu Sans/DejaVuSans-ExtraLight.ttf"), 12);
			Font.loadFont(toUri("res/fonts/DejaVu Sans/DejaVuSans-Oblique.ttf"), 12);
		} else {
			System.err.println("DejaVu Sans font could not be loaded.");
		}
	}

	/**
	 * Creates a URI string with spaces. The given path can be absolute or relative to the current working directory.
	 * @param path The path to convert
	 * @return A string containing a URI
	 */
	public static String toUri(String path) {
		return Paths.get(path).toUri().toString().replaceAll("%20", " ");
	}

	public static void main(String[] args) {

		// Platform hooks must be in place before any core game code runs.
		GameState.setPlatform(new Main());

		// Create folders:
		Storage.get().ensureDirectoriesExist();

		// Open error log
		if(!GameState.DEBUG) {
			System.out.println("Printing to error.log");
			try {
				PrintStream stream = new PrintStream(Storage.get().errorLogFile());
				System.setErr(stream);
				System.err.println("Game Version: "+GameState.VERSION_NUMBER+" ("+System.getProperty("build.type", "jar")+")");
				System.err.println("Java: "+System.getProperty("java.version")+" ("+System.getProperty("java.vendor")+")");
				System.err.println("OS: "+System.getProperty("os.name")+" ("+System.getProperty("os.arch")+")");
				File modsDir = Storage.get().modsDir();
				if (modsDir.exists()) {
					System.err.print("Mod folders present: ");
					int i=0;
					for(File f : modsDir.listFiles()) {
						if(f.isDirectory()) {
							if(i>0) {
								System.err.print(", ");
							}
							System.err.print(f.getName());
						}
						i++;
					}
					System.err.println();
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		GameState.initProperties();

		launch(args);
	}
}

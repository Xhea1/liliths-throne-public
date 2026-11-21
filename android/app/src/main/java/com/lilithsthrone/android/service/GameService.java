package com.lilithsthrone.android.service;

import java.io.IOException;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.lilithsthrone.android.render.HttpRenderSink;
import com.lilithsthrone.main.GameState;

/**
 * Hosts the game engine and its local HTTP server for as long as the user is playing.
 *
 * A foreground service (rather than plain background work) because Android's background-execution
 * limits would otherwise be free to kill the process holding all in-memory game state. It is
 * stopped by the in-game Exit action ({@code AndroidPlatform.exitApplication}).
 */
public class GameService extends Service {

	private static final String TAG = "LTService";
	private static final String CHANNEL_ID = "lt_engine";
	private static final int NOTIFICATION_ID = 1;

	private GameHttpServer server;
	private HttpRenderSink sink;

	@Override
	public void onCreate() {
		super.onCreate();
		startForeground(NOTIFICATION_ID, buildNotification());

		sink = new HttpRenderSink();
		GameState.setRenderSink(sink);

		server = new GameHttpServer(sink);
		try {
			server.start(10_000, false);
			Log.i(TAG, "HTTP server listening on 127.0.0.1:" + GameHttpServer.PORT);
		} catch (IOException e) {
			Log.e(TAG, "Could not start HTTP server", e);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Not sticky: a restart would bring back an engine with no game and no activity.
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		if (server != null) {
			server.stop();
			Log.i(TAG, "HTTP server stopped");
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private Notification buildNotification() {
		NotificationManager manager = getSystemService(NotificationManager.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
			NotificationChannel channel = new NotificationChannel(
					CHANNEL_ID, "Game engine", NotificationManager.IMPORTANCE_LOW);
			channel.setShowBadge(false);
			manager.createNotificationChannel(channel);
		}
		return new Notification.Builder(this, CHANNEL_ID)
				.setContentTitle("Lilith's Throne")
				.setContentText("Game engine running")
				.setSmallIcon(android.R.drawable.ic_menu_edit)
				.setOngoing(true)
				.build();
	}
}

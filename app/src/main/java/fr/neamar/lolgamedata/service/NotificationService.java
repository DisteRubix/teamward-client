package fr.neamar.lolgamedata.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import fr.neamar.lolgamedata.GameActivity;
import fr.neamar.lolgamedata.R;
import fr.neamar.lolgamedata.Tracker;
import fr.neamar.lolgamedata.pojo.Account;

public class NotificationService extends GcmListenerService {

    private static final String TAG = "NotificationService";
    private static final int GAME_NOTIFICATION = 0;
    private static final int ANALYTICS_NOTIFICATION = 1;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (data.containsKey("gameId")) {
            long gameId = Long.parseLong(data.getString("gameId"));
            String gameMode = data.getString("gameMode");
            String summonerName = data.getString("summonerName");
            String region = data.getString("region");
            int mapId = Integer.parseInt(data.getString("mapId"));

            Account account = new Account(summonerName, region, "");

            Log.d(TAG, "From: " + from);
            Log.d(TAG, "Game mode: " + gameMode);

            displayNotification(account, gameId, mapId);
        } else if (data.containsKey("removeGameId")) {
            long gameId = Long.parseLong(data.getString("removeGameId"));

            Log.i(TAG, "End of game, hiding notification.");
            getNotificationManager().cancel(Long.toString(gameId).hashCode());
        } else if (data.containsKey("mp_message")) {
            String title = data.getString("mp_title", getString(R.string.app_name));
            String message = data.getString("mp_message");
            String url = data.getString("mp_url", "");
            displayCustomNotification(title, message, url);
        } else {
            Log.i(TAG, "Received unknown notification:" + data.toString());
        }
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     */
    private void displayNotification(Account account, long gameId, int mapId) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("source", "notification");

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_transparent_white)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_transparent))
                .setContentTitle(String.format(getString(R.string.welcome_to), getString(GameActivity.getMapName(mapId))))
                .setContentText(String.format(getString(R.string.player_is_in_game), account.summonerName))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getBoolean("notifications_new_game_vibrate", true)) {
            notificationBuilder.setVibrate(new long[]{1000, 1000});
        }

        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            Uri notificationUri = Uri.parse(prefs.getString("notifications_new_game_ringtone", Settings.System.DEFAULT_NOTIFICATION_URI.toString()));
            notificationBuilder.setSound(notificationUri);
        }

        NotificationManager notificationManager = getNotificationManager();
        boolean unableToDisplay = false;
        if (prefs.getBoolean("notifications_new_game", true)) {
            try {
                notificationManager.notify(Long.toString(gameId).hashCode(), notificationBuilder.build());

            } catch (RuntimeException e) {
                // Most likely, the ringtone doesn't exist anymore.
                // Used to happen only in Android 6.0, so the notification part is skipped on API M.
                // I'm keeping the try/catch just in case ;)
                unableToDisplay = true;
            }

            Tracker.trackNotificationDisplayed(this, account, mapId, getString(GameActivity.getMapName(mapId)), gameId, unableToDisplay);
        }
    }

    private void displayCustomNotification(String title, String message, String url) {
        Intent intent;

        if (url.isEmpty()) {
            intent = new Intent(this, GameActivity.class);
            intent.putExtra("source", "custom_notification");
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_transparent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_transparent))
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        getNotificationManager().notify(message.hashCode(), notificationBuilder.build());
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
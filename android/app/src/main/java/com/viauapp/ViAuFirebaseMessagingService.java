package com.viauapp;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.app.NotificationChannel;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.annotation.ColorRes;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ViAuFirebaseMessagingService extends FirebaseMessagingService {
	/**
	 * Called when message is received.
	 *
	 * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
	 */
	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("remoteMessage",remoteMessage.getData().toString());
        try {
        	sendNotification(remoteMessage);
        } catch (Exception e) {
            Log.e("onMessageReceived error", e.getMessage() + "\n" + e.toString());
        }
	}

	private Spannable getActionText(String title, @ColorRes int colorRes) {
		Spannable spannable = new SpannableString(title);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
			spannable.setSpan(
					new ForegroundColorSpan(this.getColor(colorRes)), 0, spannable.length(), 0);
		}
		return spannable;
	}

	/**
	 * Create and show a custom notification containing the received FCM message.
	 *
	 * @param remoteMessage FCM notification payload received.
	 */
	private void sendNotification(RemoteMessage remoteMessage) {
		int oneTimeID = (int) SystemClock.uptimeMillis();
		String channelId = "fcm_call_channel";
		String channelName = "Incoming Call";
		Uri uri= Uri.parse("viauapp://");

		Uri notification_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        String notification_title= remoteMessage.getData().get("title");

		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

		// notification action buttons start
		PendingIntent acptIntent = MainActivity.getActionIntent(oneTimeID,uri,this);
		PendingIntent rjctIntent = MainActivity.getActionIntent(oneTimeID,uri, this);

		NotificationCompat.Action rejectCall=new NotificationCompat.Action.Builder(R.drawable.rjt_btn,getActionText("Decline",android.R.color.holo_red_light),rjctIntent).build();
		NotificationCompat.Action acceptCall=new NotificationCompat.Action.Builder(R.drawable.acpt_btn,getActionText("Answer",android.R.color.holo_green_light),acptIntent).build();
		//end

	    //when device locked show fullscreen notification start
        Intent i = new Intent(getApplicationContext(), LockScreenActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        i.putExtra("APP_STATE",isAppRunning());
        i.putExtra("FALL_BACK",true);
        i.putExtra("NOTIFICATION_ID",oneTimeID);
        PendingIntent fullScreenIntent = PendingIntent.getActivity(this, 0 /* Request code */, i,
                PendingIntent.FLAG_ONE_SHOT);
        //end

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
				.setContentTitle(notification_title)
				.setContentText(channelName)
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setCategory(NotificationCompat.CATEGORY_CALL)
				.setAutoCancel(true)
				.setSound(notification_sound)
				.addAction(acceptCall)
				.addAction(rejectCall)
				.setContentIntent(pendingIntent)
				.setDefaults(Notification.DEFAULT_VIBRATE)
				.setFullScreenIntent(fullScreenIntent, true)
				.setSmallIcon(R.mipmap.ic_launcher);


		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int importance = NotificationManager.IMPORTANCE_MAX;

		//channel creation start
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			NotificationChannel mChannel = new NotificationChannel(
					channelId, channelName, importance);
			AudioAttributes attributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_NOTIFICATION)
					.build();
			mChannel.setSound(notification_sound,attributes);
			mChannel.setDescription(channelName);
			mChannel.enableLights(true);
			mChannel.enableVibration(true);
			notificationManager.createNotificationChannel(mChannel);
		}
		//end

		notificationManager.notify(oneTimeID, notificationBuilder.build());
	}

	private boolean isAppRunning() {
		ActivityManager m = (ActivityManager) this.getSystemService( ACTIVITY_SERVICE );
		List<ActivityManager.RunningTaskInfo> runningTaskInfoList =  m.getRunningTasks(10);
		Iterator<ActivityManager.RunningTaskInfo> itr = runningTaskInfoList.iterator();
		int n=0;
		while(itr.hasNext()){
			n++;
			itr.next();
		}
		if(n==1){ // App is killed
			return false;
		}
		return true; // App is in background or foreground
	}
}
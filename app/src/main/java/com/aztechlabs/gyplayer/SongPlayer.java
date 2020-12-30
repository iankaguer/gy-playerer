package com.aztechlabs.gyplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Size;


import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import io.realm.Realm;


//Service lecteur MP3
public class SongPlayer extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,

        AudioManager.OnAudioFocusChangeListener {

    MediaPlayer mediaPlayer;
    String mediaFile;
    private AudioManager audioManager;
    private int resumePosition;
    private boolean incomingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    private final IBinder iBinder = new LocalBinder();

    public static final String ACTION_PLAY = "com.aztechlabs.gyplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.aztechlabs.gyplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.aztechlabs.gyplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.aztechlabs.gyplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.aztechlabs.gyplayer.ACTION_STOP";

    List<SongModel> listSons;
    LecteurPrefModel lecteur;

    private MediaSessionManager mediaSessionManager;
    private MediaSession mediaSession;
    private MediaController.TransportControls transportControls;
    private static final int NOTIF_ID = 101;
    Realm realm;
    int positionLecture = 0;

    //lieur du service à l'activité
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    //initiation du lecteur
    private void initMediaPlayer() {
        
        //recuperation des données de lecture
        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();
        lecteur = realm.where(LecteurPrefModel.class).equalTo("id", 1).findFirst();

        listSons = realm.where(SongModel.class).findAll();
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }


        //Reinitialiser le lecteur au cas ou il pointe sur une autre source et lui attacher des listeners && attributs
        mediaPlayer.reset();

        mediaPlayer.setLooping(lecteur.isLoop());
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());

        try {
            mediaPlayer.setDataSource(mediaFile);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            //e.printStackTrace();
            stopSelf();
        }

    }

    //a la creation enregistrer les broadcast
    @Override
    public void onCreate() {
        super.onCreate();
        callStateListener();
        registerRemovingHeadphoneReceiver();
        register_playNewAudio();


    }

    //au lancement recuperer le path de la piste à jouer
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra("media")){
            try {
                //An audio file is passed to the service through putExtra();
                mediaFile = intent.getExtras().getString("media");
                if (intent.hasExtra("position")){
                    positionLecture = intent.getExtras().getInt("position");
                }

            } catch (NullPointerException e) {
                stopSelf();
                //Log.e("logerror2", e.getMessage());
            }
        }else {

            if (lecteur.getLastPlayedUri() != null){
                mediaFile=lecteur.getLastPlayedUri();
            }else {
                mediaFile=listSons.get(0).getUri();
            }
        }


        //Si le hautParleur est occupé abandonner
        if (requestAudioFocus() == false) {
            stopSelf();
        }
        //si un media est envoye l'assigner pour la lecture sinon lire le dernier element
        if (mediaFile != null && mediaFile != "") {
            
            initMediaPlayer();

        }else {
            SongModel nextSong = listSons.get(new Random().nextInt(listSons.size()));
            mediaFile = nextSong.getUri();
            initMediaPlayer();
        }
        //initier mediassessionmanager
        if (mediaSessionManager == null) {
            try {
                initMediaSession();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
        }
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
        //return  START_NOT_STICKY;
    }

        //Action a realiser en fonction de la disponibilite des hautparleurs
    @Override
    public void onAudioFocusChange(int i) {
        switch (i) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                /* Lost focus for a short time, but we have to stop
                 playback. We don't release the media player because playback
                 is likely to resume*/
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                /* Lost focus for a short time, but it's ok to keep playing
                 at an attenuated level*/
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    //action a realiser à la fin de la lecture
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (lecteur.isLoop()){
            playMedia();
        }else {
            if (lecteur.isShuffle()){
               SongModel nextSong = listSons.get(new Random().nextInt(listSons.size()));
               mediaFile = nextSong.getUri();
               playMedia();
            }else {
                mediaFile = listSons.get(prochainSon()).getUri();
                playMedia();
            }
        }
        //stopMedia();
        //stopSelf();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        //removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(removingHeadphone);
        unregisterReceiver(playNewAudio);
        unregisterReceiver(PlayAuChoix);

    }

    //si aucun son n'est envoyé via l'intent lire un au hasard, s'execute souvent au tout premier lancement de l'activité
    public  int prochainSon() {
        for(int i = 0 ; i < listSons.size(); i++){
            if (listSons.get(i).getUri() == mediaFile){
                int nextIndex = i+1;
                if (nextIndex > listSons.size()){
                    return 0;
                }else {
                    return i+1;
                }
            }
        }
        return 0;
    }

    //centre d'Ecoute des erreurs du service
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e("MediaPlayer Error", i+"MEDIA " + i1);

        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }


    //Lieur du service à l'activité'
    public class LocalBinder extends Binder {
        public SongPlayer getService() {
            return SongPlayer.this;
        }
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            if (positionLecture !=0){
                mediaPlayer.seekTo(positionLecture);
            }
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }


    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }
    //Broadcast en cas de debranchement des ecouteurs
    private BroadcastReceiver removingHeadphone = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio
            lecteur.setLastPlayedUri(mediaFile);
            lecteur.setLastPlayedPosition(mediaPlayer.getCurrentPosition());
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(lecteur);
            realm.commitTransaction();
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };
    
    //son Enregistrement
    private void registerRemovingHeadphoneReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(removingHeadphone, intentFilter);
    }

    //Ecoute des etats du telephone
    public void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            incomingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (incomingCall) {
                                incomingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    //Broadcast qui s'execute quand un son est deja joué et que le user essaie de jouer un autre
    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mediaFile = intent.getExtras().getString("media");

            //A PLAY_NEW_AUDIO action received eset mediaPlayer to play the new Audio
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            //updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(LecteurActivity._PLAY_NEW_SONG);
        registerReceiver(playNewAudio, filter);
    }
//quand on tente de jouer un son au hasard sans choisir lequel
    private BroadcastReceiver PlayAuChoix = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaFile = intent.getExtras().getString("media");
            initMediaPlayer();
            mediaPlayer.start();
            if (mediaSession != null){
                buildNotification(PlaybackStatus.PLAYING);
            }

        }
    };

   





    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSession(getApplicationContext(), "gyplayer");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);

        mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();

        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                transportControls.skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                transportControls.skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        Bitmap thumbnail = null;
        // Update the current
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                thumbnail = getApplicationContext().getContentResolver().loadThumbnail(
                                Uri.parse(mediaFile), new Size(640, 480), null);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource(mediaFile);


        mediaSession.setMetadata(new MediaMetadata.Builder()
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, thumbnail)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST))
                .putString(MediaMetadata.METADATA_KEY_ALBUM, metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM))
                .putString(MediaMetadata.METADATA_KEY_TITLE, metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE))
                .build());


    }

   /* public MediaSession getCurrentMediaSession(){
        return mediaSession;
    }*/



    private void buildNotification(PlaybackStatus playbackStatus) {
        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;
        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.boreal); //replace with your own image

        MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
        metaRetriver.setDataSource(mediaFile);


        // Create a new Notification
        Notification.Builder notificationBuilder = (Notification.Builder) new Notification.Builder(this)
                .setShowWhen(false)
                .setStyle(new Notification.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setColor(getResources().getColor(R.color.color_primary))
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setContentText( "mediaSession.getController().getQueueTitle()")
                .setContentTitle(mediaSession.getController().getQueueTitle())
                .setContentInfo(mediaSession.getController().getQueueTitle())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIF_ID, notificationBuilder.build());
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIF_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, SongPlayer.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }
}



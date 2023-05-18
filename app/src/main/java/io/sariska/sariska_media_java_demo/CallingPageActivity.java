package io.sariska.sariska_media_java_demo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.react.bridge.ReactContext;
import com.oney.WebRTCModule.GetUserMediaImpl;
import com.oney.WebRTCModule.WebRTCView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.sariska.sdk.Conference;
import io.sariska.sdk.Connection;
import io.sariska.sdk.JitsiLocalTrack;
import io.sariska.sdk.JitsiRemoteTrack;
import io.sariska.sdk.SariskaMediaTransport;

public class CallingPageActivity extends AppCompatActivity {

    private Connection connection;
    private Conference conference;
    JitsiLocalTrack desktopTrack;
    private ImageView endCallView;
    private ImageView muteAudioView;
    private ImageView muteVideoView;
    private ImageView shareScreenView;
    private ImageView switchCameraView;
    private boolean audioState;
    private boolean videoState;

    private Bundle optionsBundle;

    private static Intent dataPermissionIntent;

    private RelativeLayout mLocalContainer;

    private List<JitsiLocalTrack> localTracks;

    private WebRTCView localView;

    private ReactContext reactContext;

    @BindView(R.id.remoteRecycleView)
    RecyclerView rvOtherMembers;
    ArrayList<JitsiRemoteTrack> remoteTrackArrayList;
    RemoteAdapter sariskaRemoteAdapter;
    AlertDialog alert;

    // Need activity to pass it down to Get User Media Impl
    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calling_page_layout);
        mLocalContainer = findViewById(R.id.local_video_view_container);
        endCallView = findViewById(R.id.endcall);
        muteAudioView = findViewById(R.id.muteAudio);
        muteVideoView = findViewById(R.id.muteVideo);
        shareScreenView = findViewById(R.id.sharescreen);
        switchCameraView = findViewById(R.id.switchcamera);
        alert = getBuilder().create();
        ButterKnife.bind(this);
        optionsBundle = getIntent().getExtras();
        mActivity = this;

        String roomName = optionsBundle.getString("Room Name");
        String userName = optionsBundle.getString("User Name");
        audioState = optionsBundle.getBoolean("audio");
        videoState = optionsBundle.getBoolean("video");

        SariskaMediaTransport.initializeSdk(getApplication());

        this.setupLocalStream(optionsBundle.getBoolean("audio"), optionsBundle.getBoolean("video"));

        Thread tokenThread = new Thread(() -> {
            try {
                String token = GetToken.generateToken(userName);
                connection = SariskaMediaTransport.JitsiConnection(token, roomName, false);
                connection.addEventListener("CONNECTION_ESTABLISHED", this::createConference);
                connection.addEventListener("CONNECTION_FAILED", () -> {
                });
                connection.addEventListener("CONNECTION_DISCONNECTED", () -> {
                });
                connection.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        tokenThread.start();
        remoteTrackArrayList = new ArrayList<>();
        sariskaRemoteAdapter = new RemoteAdapter();
        rvOtherMembers.setAdapter(sariskaRemoteAdapter);
        addRequiredListener(alert);
    }

    private void addRequiredListener(AlertDialog alert) {

        endCallView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.show();
            }
        });

        muteVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (JitsiLocalTrack track : localTracks) {
                    if (track.getType().equals("video")) {
                        if (videoState) {
                            track.mute();
                            videoState = false;
                            muteVideoView.setImageResource(R.drawable.iconsvideocallon);
                        } else {
                            track.unmute();
                            videoState = true;
                            muteVideoView.setImageResource(R.drawable.iconsvideocalloff);
                        }
                    }
                }
            }
        });

        muteAudioView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(JitsiLocalTrack track : localTracks){
                    if(track.getType().equals("audio")){
                        if(audioState){
                            track.mute();
                            audioState = false;
                            muteAudioView.setImageResource(R.drawable.iconsmicon);
                        }else{
                            track.unmute();
                            audioState = true;
                            muteAudioView.setImageResource(R.drawable.iconsmicoff);
                        }
                    }
                }
            }
        });

        shareScreenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(JitsiLocalTrack track:localTracks){
                    if(track.getType().equals("video")){
                        System.out.println("inside video track");
                        conference.removeTrack(track);
                        localTracks.remove(track);
                        System.out.println("Local Tracks Size: " + localTracks.size());
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLocalContainer.removeAllViews();
                    }
                });

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(mActivity, SariskaScreenCaptureService.class));
                }
                // Get the projection manager
                new Handler().postDelayed(() -> {
                    // code to be executed after 1 seconds
                    reactContext = SariskaMediaTransport.getReactContext();
                    reactContext.onHostResume(mActivity);
                    setupDesktopTrack();
                }, 1000);
            }
        });
    }

    private void setupDesktopTrack() {
        Bundle options = new Bundle();
        options.putBoolean("desktop", true);
        SariskaMediaTransport.createLocalTracks(options, tracks ->{
            System.out.println("Desktop Created");
            desktopTrack = tracks.get(0);
            localTracks.add(desktopTrack);
            conference.addTrack(desktopTrack);
        });
    }
    // Invoked from startActivityResult inside React-Native-Webrtc
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dataPermissionIntent = data;
        System.out.println("sdadadasd");
        GetUserMediaImpl.setMediaData(data);
    }

    // Important for Screen Sharing
    public static Intent getMediaProjectionPermissionDetails(){
        return dataPermissionIntent;
    }

    private void createConference() {
        conference = connection.initJitsiConference();
        conference.addEventListener("CONFERENCE_JOINED", () -> {
            for (JitsiLocalTrack track : localTracks) {
                conference.addTrack(track);
            }
        });

        conference.addEventListener("DOMINANT_SPEAKER_CHANGED", p -> {
            String id = (String) p;
            conference.selectParticipant(id);
        });


        conference.addEventListener("CONFERENCE_LEFT", () -> {
        });

        conference.addEventListener("TRACK_ADDED", p -> {
            JitsiRemoteTrack track = (JitsiRemoteTrack) p;
            for(JitsiLocalTrack track1: localTracks){
                if(track1.getType().equals("video")){
                    if (track.getStreamURL().equals(track1.getStreamURL())) {
                        //So as to not add local track in remote container
                        return;
                    }
                }
            }
            runOnUiThread(() -> {
                if (track.getType().equals("video")) {
                    System.out.println("Adding to userList");
                    remoteTrackArrayList.add(0,track);
                    sariskaRemoteAdapter.notifyDataSetChanged();
                }
            });
        });

        conference.addEventListener("TRACK_REMOVED", p -> {
            JitsiRemoteTrack track = (JitsiRemoteTrack) p;
            runOnUiThread(() -> {
                for(int i=0;i<remoteTrackArrayList.size();i++){
                    if(remoteTrackArrayList.get(i) == track) remoteTrackArrayList.remove(i);
                }
                sariskaRemoteAdapter.notifyDataSetChanged();
            });
        });

        conference.join();

        System.out.println("We are past createConference");
    }

    private void setupLocalStream(boolean audio, boolean video){
        Bundle options = new Bundle();
        options.putBoolean("audio", audio);
        options.putBoolean("video", true);
        options.putInt("resolution", 360);
        SariskaMediaTransport.createLocalTracks(options, tracks -> {
            runOnUiThread(() -> {
                localTracks = tracks;
                for (JitsiLocalTrack track : tracks) {
                    if (track.getType().equals("video")) {
                        WebRTCView view = track.render();
                        view.setMirror(true);
                        view.setObjectFit("cover");
                        mLocalContainer.addView(view);
                    }
                }
            });
        });
    }


    public Builder getBuilder(){
        Builder builder = new Builder(CallingPageActivity.this);
        builder.setMessage("Are you sure you want to leave?");
        builder.setCancelable(true);
        builder.setPositiveButton(
                "Leave",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(conference != null){
                            conference.leave();
                        }
                        connection.disconnect();
                        finish();
                    }
                }).setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder;
    }

    public class RemoteAdapter extends RecyclerView.Adapter<RemoteAdapter.ItemViewHolder> {

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_remote_views, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            WebRTCView view = remoteTrackArrayList.get(position).render();
            view.setMirror(true);
            view.setObjectFit("cover");
            holder.remote_video_view_container.addView(view);
        }

        @Override
        public int getItemCount() {
            return remoteTrackArrayList.size();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.remote_video_view_container)
            RelativeLayout remote_video_view_container;

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

}
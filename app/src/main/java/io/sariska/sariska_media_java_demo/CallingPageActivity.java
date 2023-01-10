package io.sariska.sariska_media_java_demo;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

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

    private ImageView endCallView;
    private ImageView muteAudioView;
    private ImageView muteVideoView;
    private ImageView startStreamingView;
    private boolean audioState;
    private boolean videoState;
    String roomName = "";

    private Bundle optionsBundle;


    private RelativeLayout mLocalContainer;

    private List<JitsiLocalTrack> localTracks;

    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
    };

    int PERMISSION_ALL = 1;

    private WebRTCView localView;

    @BindView(R.id.remoteRecycleView)
    RecyclerView rvOtherMembers;
    ArrayList<JitsiRemoteTrack> remoteTrackArrayList;
    RemoteAdapter sariskaRemoteAdapter;
    AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calling_page_layout);
        mLocalContainer = findViewById(R.id.local_video_view_container);
        endCallView = findViewById(R.id.endcall);
        muteAudioView = findViewById(R.id.muteAudio);
        muteVideoView = findViewById(R.id.muteVideo);
        startStreamingView = findViewById(R.id.startRecording);

        alert = getBuilder().create();
        ButterKnife.bind(this);

        optionsBundle = getIntent().getExtras();
        this.roomName = optionsBundle.getString("Room Name");
        String userName = optionsBundle.getString("User Name");
        audioState = optionsBundle.getBoolean("audio");
        videoState = optionsBundle.getBoolean("video");

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

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

        startStreamingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStreaming(roomName);
            }
        });
    }

    private void startStreaming(String roomName){
        System.out.println("Room NAme: "+ roomName);
        Thread streamingThread = new Thread(() -> {
            try {
                String streamingToken = GetToken.generateToken("streamer");
                String rtmpURL = StartStreamingCaller.startStreaming(streamingToken, roomName);
                System.out.println("rtmpURL: " + rtmpURL);
            }catch (IOException e){
                System.out.println(e);
            }
        });
        streamingThread.start();
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
            if (track.getStreamURL().equals(localTracks.get(1).getStreamURL())) {
                //So as to not add local track in remote container
                return;
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
        options.putBoolean("video", video);
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

    private boolean hasPermissions(CallingPageActivity context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
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
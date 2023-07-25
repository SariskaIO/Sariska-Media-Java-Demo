package io.sariska.sariska_media_java_demo;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.oney.WebRTCModule.WebRTCModule;
import com.oney.WebRTCModule.WebRTCView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class CallingPageFragment extends Fragment {
    private Connection connection;
    private Conference conference;
    private View endCallView;
    private View muteAudioView;
    private Bundle roomDetails;
    private View muteVideoView;
    private View switCameraView;
    WebRTCModule webRTCModule;
    private boolean audioState;
    private boolean videoState;
    private RelativeLayout mLocalContainer;
    private List<JitsiLocalTrack> localTracks;
    @BindView(R.id.remoteViewRecycle)
    RecyclerView rvOtherMemberss;
    ArrayList<JitsiRemoteTrack> remoteTrackArrayList;
    RemoteAdapter sariskaRemoteAdapter;
    AlertDialog alert;
    private String roomName;
    private View switchCameraFab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This callback will only be called when CallingPageFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                alert.show();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);



    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.calling_page_layout, container, false);
        Toolbar toolbar = view.findViewById(R.id.bottomAppBar);
        switchCameraFab = view.findViewById(R.id.switchCamera);

        mLocalContainer = view.findViewById(R.id.local_video_view_container);
        endCallView = toolbar.findViewById(R.id.end_call_button);
        muteAudioView = toolbar.findViewById(R.id.mute_button);
        muteVideoView = toolbar.findViewById(R.id.mute_video_button);
        //switCameraView = view.findViewById(R.id.sw);

        alert = getBuilder().create();
        ButterKnife.bind(this, view);
        roomDetails = getArguments();
        roomName = roomDetails.getString("roomName");
        String roomName = roomDetails.getString("Room Name");
        String userName = roomDetails.getString("User Name");
        audioState = roomDetails.getBoolean("audio");
        videoState = roomDetails.getBoolean("video");
        SariskaMediaTransport.initializeSdk(getActivity().getApplication());
        this.setupLocalStream(true, true);
        try {
            GetToken.generateToken(userName, new GetToken.HttpRequestCallback() {
                @Override
                public void onResponse(String response) throws JSONException {
                    String responseString = response;
                    responseString = "[" + responseString + "]";
                    JSONArray array = new JSONArray(responseString);
                    String finalResponse = null;
                    for(int i=0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        finalResponse = object.getString("token");
                    }
                    connection = SariskaMediaTransport.JitsiConnection(finalResponse, roomName, false);
                    connection.addEventListener("CONNECTION_ESTABLISHED", this::createConference);
                    connection.addEventListener("CONNECTION_FAILED", () -> {
                    });
                    connection.addEventListener("CONNECTION_DISCONNECTED", () -> {
                    });
                    connection.connect();
                }

                private void createConference() {

                    conference = connection.initJitsiConference();

                    conference.addEventListener("CONFERENCE_JOINED", () -> {
                        for (JitsiLocalTrack track : localTracks) {
                            conference.addTrack(track);
                        }
                    });

                    conference.addEventListener("CONFERENCE_FAILED", () -> {
                        System.out.println("conference failed");
                        conference.joinLobby(conference.getUserName(), "random_email");
                    });

                    conference.addEventListener("DOMINANT_SPEAKER_CHANGED", p -> {
                        String id = (String) p;
                        conference.selectParticipant(id);
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

                    conference.addEventListener("USER_ROLE_CHANGED", (id, role)-> {
                        if (conference.getUserId()== id ) {
                            System.out.println("Your user role changed"+role);
                        }
                        if(role.equals("moderator")){
                            System.out.println("Moderator");
                            conference.enableLobby();
                        }
                    });

                    conference.addEventListener("LOBBY_USER_JOINED", (id, name)-> {
                        System.out.println("Lobby user joined");
                        runOnUiThread(() -> {
                            showLobbyAlert(view, conference, id.toString());
                        });
                    });

                    conference.join();
                }

                @Override
                public void onFailure(Throwable throwable) {

                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        remoteTrackArrayList = new ArrayList<>();
        sariskaRemoteAdapter = new RemoteAdapter();
        rvOtherMemberss.setAdapter(sariskaRemoteAdapter);
        addRequiredListener(alert);

        return view;
    }

    public void showLobbyAlert(View view, Conference conference, String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Approve user?");
        builder.setMessage("Do you want to approve this user?");
        builder.setPositiveButton("Approve", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Approve" button
                Toast.makeText(getActivity(), "Action Approved!", Toast.LENGTH_SHORT).show();
                // Add your approval logic here
                conference.lobbyApproveAccess(id);
            }
        });

        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Deny" button
                Toast.makeText(getActivity(), "Action Denied!", Toast.LENGTH_SHORT).show();
                // Add your denial logic here
                conference.lobbyDenyAccess(id);
            }
        });

        // Create and show the alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setupLocalStream(boolean audio, boolean video) {

        Bundle options = new Bundle();
        options.putBoolean("audio", audio);
        options.putBoolean("video", video);
        options.putInt("resolution", 720);

        SariskaMediaTransport.createLocalTracks(options, tracks -> {
            runOnUiThread(() -> {
                localTracks = tracks;
                for (JitsiLocalTrack track : tracks) {
                    if (track.getType().equals("video")) {
                        WebRTCView view = track.render();
                        view.setMirror(true);
                        view.setObjectFit("cover");
                        mLocalContainer.addView(view);
                        System.out.println("Create local tracks");
                    }
                }
                System.out.println("Length of local tracks:" + localTracks.size());
            });
        });
    }

    private void addRequiredListener(AlertDialog alert) {

        endCallView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.show();
            }
        });


        switchCameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (JitsiLocalTrack track : localTracks) {
                    if (track.getType().equals("video")) {
                        track.switchCamera();
                    }
                }
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
                            //muteVideoView.setImageResource(R.drawable.iconsvideocallon);
                        } else {
                            track.unmute();
                            videoState = true;
                            //muteVideoView.setImageResource(R.drawable.iconsvideocalloff);
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
                            //muteAudioView.setImageResource(R.drawable.iconsmicon);
                        }else{
                            track.unmute();
                            audioState = true;
                            //muteAudioView.setImageResource(R.drawable.iconsmicoff);
                        }
                    }
                }
            }
        });
    }



    public AlertDialog.Builder getBuilder(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you want to leave?");
        builder.setCancelable(true);
        builder.setPositiveButton(
                "Leave",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for (JitsiLocalTrack track : localTracks) {
                            track.dispose();
                        }
                        remoteTrackArrayList.clear();
                        conference.leave();
                        connection.disconnect();
                        System.out.println("Conference left");
                        getActivity().finish();
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

    public static CallingPageFragment newInstance() {
        return new CallingPageFragment();
    }

    public class RemoteAdapter extends RecyclerView.Adapter<CallingPageFragment.RemoteAdapter.ItemViewHolder> {
        @NonNull
        @Override
        public RemoteAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_remote_views, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RemoteAdapter.ItemViewHolder holder, int position) {
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

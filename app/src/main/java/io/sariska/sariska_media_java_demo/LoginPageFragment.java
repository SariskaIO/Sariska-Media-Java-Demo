package io.sariska.sariska_media_java_demo;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.button.MaterialButton;

public class LoginPageFragment extends Fragment {
    Boolean isMuted = false;
    Boolean isVideoMuted = false;
    Boolean isSpeakerOn = false;
    Bundle bundle = new Bundle();
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_page_layout, container, false);

        TextView roomName = (TextView) view.findViewById(R.id.roomName);
        TextView username = (TextView) view.findViewById(R.id.username);

        bundle.putBoolean("audio", true);
        bundle.putBoolean("video", true);

        MaterialButton loginButton = (MaterialButton) view.findViewById(R.id.loginButton);
        if (!hasPermissions(getActivity(), PERMISSIONS)) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_ALL);
        }

        ImageView muteAudioImage = (ImageView) view.findViewById(R.id.landingMute);
        ImageView muteVideoImage = (ImageView) view.findViewById(R.id.landingVideoMute);
        ImageView speakerOnOff = (ImageView) view.findViewById(R.id.speakerOnOff);

        setOnClickListenersForOptions(muteAudioImage, muteVideoImage, speakerOnOff);

        loginButton.setOnClickListener(v ->{
            if(roomName.getText().toString().isEmpty()) {
                System.out.println("Enter a roomName");
            }else {
                System.out.println("Calling fragment");
                CallingPageFragment fragment = CallingPageFragment.newInstance();
                bundle.putString("Room Name", roomName.getText().toString());
                bundle.putString("User Name", username.getText().toString());
                fragment.setArguments(bundle);
                navigateToFragment(fragment);
            }
        });

        return view;
    }


    private void navigateToFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(null); // Add to back stack to enable back navigation
        fragmentTransaction.commit();
    }



    private void setOnClickListenersForOptions(ImageView muteAudioImage, ImageView muteVideoImage, ImageView speakerOnOff) {
        muteAudioImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMuted){
                    isMuted = false;
                    muteAudioImage.setImageResource(R.drawable.ic_baseline_mic_24);
                    bundle.putBoolean("audio", true);
                }else{
                    isMuted = true;
                    muteAudioImage.setImageResource(R.drawable.ic_baseline_mic_off_24);
                    bundle.putBoolean("audio", false);
                }
            }
        });

        muteVideoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isVideoMuted){
                    isVideoMuted = false;
                    muteVideoImage.setImageResource(R.drawable.ic_baseline_videocam_24);
                    bundle.putBoolean("video", true);
                }else{
                    isVideoMuted = true;
                    muteVideoImage.setImageResource(R.drawable.ic_baseline_videocam_off_24);
                    bundle.putBoolean("video", false);
                }
            }
        });

        speakerOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSpeakerOn){
                    isSpeakerOn = false;
                    speakerOnOff.setImageResource(R.drawable.ic_baseline_volume_mute_24);
                }else{
                    isSpeakerOn = true;
                    speakerOnOff.setImageResource(R.drawable.ic_baseline_volume_up_24);
                }
            }
        });
    }
    private boolean hasPermissions(Activity context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}

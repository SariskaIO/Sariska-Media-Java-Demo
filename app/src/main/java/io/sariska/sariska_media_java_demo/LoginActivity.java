package io.sariska.sariska_media_java_demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {
    Boolean isMuted = false;
    Boolean isVideoMuted = false;
    Boolean isSpeakerOn = false;
    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page_layout);

        TextView roomName = (TextView) findViewById(R.id.roomName);
        TextView username = (TextView) findViewById(R.id.username);

        bundle.putBoolean("audio", true);
        bundle.putBoolean("video", true);

        MaterialButton loginButton = (MaterialButton) findViewById(R.id.loginButton);

        ImageView muteAudioImage = (ImageView) findViewById(R.id.landingMute);
        ImageView muteVideoImage = (ImageView) findViewById(R.id.landingVideoMute);
        ImageView speakerOnOff = (ImageView) findViewById(R.id.speakerOnOff);

        setOnClickListenersForOptions(muteAudioImage, muteVideoImage, speakerOnOff);

        loginButton.setOnClickListener(v -> {
            if(roomName.getText().toString().isEmpty()){
                System.out.println("Enter a roomName");
            }else{
                Intent i = new Intent(LoginActivity.this, CallingPageActivity.class);
                bundle.putString("Room Name", roomName.getText().toString());
                bundle.putString("User Name", username.getText().toString());
                i.putExtras(bundle);
                startActivity(i);
            }
        });
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
}
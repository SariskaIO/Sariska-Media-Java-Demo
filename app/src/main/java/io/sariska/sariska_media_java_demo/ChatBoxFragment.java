package io.sariska.sariska_media_java_demo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ChatBoxFragment extends Fragment {

    private LinearLayout chatMessagesLayout;
    private EditText messageInput;
    private ScrollView chatScrollView;

    public ChatBoxFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_box, container, false);

        // Find references to UI elements
        chatMessagesLayout = rootView.findViewById(R.id.chatMessagesLayout);
        messageInput = rootView.findViewById(R.id.messageInput);
        chatScrollView = rootView.findViewById(R.id.chatScrollView);
        Button sendButton = rootView.findViewById(R.id.sendButton);

        // Set click listener for the send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        return rootView;
    }

    // Method to send a new message
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
            // Create a new TextView to display the message
            TextView messageView = new TextView(requireContext());
            messageView.setText(messageText);

            // Add the message view to the chatMessagesLayout
            chatMessagesLayout.addView(messageView);

            // Scroll to the bottom of the chat
            chatScrollView.post(new Runnable() {
                @Override
                public void run() {
                    chatScrollView.fullScroll(View.FOCUS_DOWN);
                }
            });

            // Clear the message input
            messageInput.setText("");
        }
    }
}

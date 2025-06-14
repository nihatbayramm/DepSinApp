// Generated by view binder compiler. Do not edit!
package com.example.depsinn.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.depsinn.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityChatBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final EditText messageInput;

  @NonNull
  public final RecyclerView messageRecyclerView;

  @NonNull
  public final Button sendButton;

  private ActivityChatBinding(@NonNull LinearLayout rootView, @NonNull EditText messageInput,
      @NonNull RecyclerView messageRecyclerView, @NonNull Button sendButton) {
    this.rootView = rootView;
    this.messageInput = messageInput;
    this.messageRecyclerView = messageRecyclerView;
    this.sendButton = sendButton;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityChatBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityChatBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_chat, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityChatBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.messageInput;
      EditText messageInput = ViewBindings.findChildViewById(rootView, id);
      if (messageInput == null) {
        break missingId;
      }

      id = R.id.messageRecyclerView;
      RecyclerView messageRecyclerView = ViewBindings.findChildViewById(rootView, id);
      if (messageRecyclerView == null) {
        break missingId;
      }

      id = R.id.sendButton;
      Button sendButton = ViewBindings.findChildViewById(rootView, id);
      if (sendButton == null) {
        break missingId;
      }

      return new ActivityChatBinding((LinearLayout) rootView, messageInput, messageRecyclerView,
          sendButton);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}

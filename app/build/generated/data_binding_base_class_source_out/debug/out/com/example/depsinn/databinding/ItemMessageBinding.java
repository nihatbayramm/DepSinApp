// Generated by view binder compiler. Do not edit!
package com.example.depsinn.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.depsinn.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemMessageBinding implements ViewBinding {
  @NonNull
  private final FrameLayout rootView;

  @NonNull
  public final LinearLayout messageContainer;

  @NonNull
  public final TextView messageText;

  private ItemMessageBinding(@NonNull FrameLayout rootView, @NonNull LinearLayout messageContainer,
      @NonNull TextView messageText) {
    this.rootView = rootView;
    this.messageContainer = messageContainer;
    this.messageText = messageText;
  }

  @Override
  @NonNull
  public FrameLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemMessageBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemMessageBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_message, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemMessageBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.messageContainer;
      LinearLayout messageContainer = ViewBindings.findChildViewById(rootView, id);
      if (messageContainer == null) {
        break missingId;
      }

      id = R.id.messageText;
      TextView messageText = ViewBindings.findChildViewById(rootView, id);
      if (messageText == null) {
        break missingId;
      }

      return new ItemMessageBinding((FrameLayout) rootView, messageContainer, messageText);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}

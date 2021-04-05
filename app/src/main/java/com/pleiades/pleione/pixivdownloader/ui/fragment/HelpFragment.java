package com.pleiades.pleione.pixivdownloader.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.pleiades.pleione.pixivdownloader.Converter;
import com.pleiades.pleione.pixivdownloader.R;

import static com.pleiades.pleione.pixivdownloader.Variable.helpImageResIdList;

public class HelpFragment extends Fragment {
    private Context context;
    private int downloadType;
    private int position;

    public static HelpFragment newInstance(int downloadType, int position) {
        HelpFragment fragment = new HelpFragment();

        Bundle arguments = new Bundle();
        arguments.putInt("download_type", downloadType);
        arguments.putInt("position", position);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments == null)
            return;

        downloadType = arguments.getInt("download_type");
        position = arguments.getInt("position");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_help, container, false);

        // initialize photo view
        ImageView imageView = rootView.findViewById(R.id.help_image);

        // set photo view
        Glide.with(rootView)
                .load(helpImageResIdList.get(position))
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);

        // initialize message
        TextView messageTextView = rootView.findViewById(R.id.help_message);
        messageTextView.setText(Converter.getHelpMessage(context, downloadType, position));

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}

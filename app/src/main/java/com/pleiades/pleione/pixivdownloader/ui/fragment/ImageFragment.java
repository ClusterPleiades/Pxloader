package com.pleiades.pleione.pixivdownloader.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.pleiades.pleione.pixivdownloader.R;

import static com.pleiades.pleione.pixivdownloader.Variable.artList;

public class ImageFragment extends Fragment {
    private int position;

    public static ImageFragment newInstance(int position) {
        ImageFragment fragment = new ImageFragment();

        Bundle arguments = new Bundle();
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

        position = arguments.getInt("position");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_image, container, false);

        // initialize photo view
        PhotoView photoView = rootView.findViewById(R.id.image);

        // set photo view
        Glide.with(rootView)
                .load(artList.get(position).uri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(photoView);

        return rootView;
    }
}

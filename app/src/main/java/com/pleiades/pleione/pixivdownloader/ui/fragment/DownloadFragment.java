package com.pleiades.pleione.pixivdownloader.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.pleiades.pleione.pixivdownloader.Converter;
import com.pleiades.pleione.pixivdownloader.DeviceController;
import com.pleiades.pleione.pixivdownloader.R;
import com.pleiades.pleione.pixivdownloader.ui.activity.download.CollectionActivity;
import com.pleiades.pleione.pixivdownloader.ui.activity.download.FollowingActivity;
import com.pleiades.pleione.pixivdownloader.ui.activity.download.RankingsActivity;
import com.pleiades.pleione.pixivdownloader.ui.activity.download.SearchActivity;
import com.pleiades.pleione.pixivdownloader.ui.activity.download.UserActivity;
import com.pleiades.pleione.pixivdownloader.ui.activity.download.WorkActivity;

import java.util.ArrayList;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static android.widget.Toast.LENGTH_SHORT;
import static com.pleiades.pleione.pixivdownloader.Config.COUNT_DOWNLOAD_TYPE;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_COLLECTION;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_FOLLOWING;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_RANKINGS;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_RATING;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_SEARCH;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_USER;
import static com.pleiades.pleione.pixivdownloader.Config.DOWNLOAD_TYPE_WORK;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_COMPLETE_COUNT;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_IS_RATED;
import static com.pleiades.pleione.pixivdownloader.Config.PREFS;
import static com.pleiades.pleione.pixivdownloader.Config.SPAN_COUNT;
import static com.pleiades.pleione.pixivdownloader.Config.URI_PXLOADER;
import static com.pleiades.pleione.pixivdownloader.Variable.artList;
import static com.pleiades.pleione.pixivdownloader.Variable.isGuest;

public class DownloadFragment extends Fragment {
    private Context context;
    private ArrayList<Thumbnail> thumbnailList;
    private ThumbnailRecyclerAdapter downloadAdapter;

    public static DownloadFragment newInstance() {
        return new DownloadFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_download, container, false);
        SharedPreferences prefs = context.getSharedPreferences(PREFS, MODE_PRIVATE);

        // set has options menu
        setHasOptionsMenu(true);

        // initialize search image view
        ImageView searchThumbnailImageView = rootView.findViewById(R.id.download_banner);
        Glide.with(context)
                .load(R.drawable.drawable_thumbnail_long)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(DeviceController.getWidth(context), DeviceController.getWidth(context) / 2)
                .into(searchThumbnailImageView);
        searchThumbnailImageView.setColorFilter(ContextCompat.getColor(context, R.color.color_signature_filter_darker));

        // initialize search text view
        TextView searchTitleTextView = rootView.findViewById(R.id.download_banner_title);
        searchTitleTextView.setText(Converter.getDownloadLabelResId(DOWNLOAD_TYPE_SEARCH));
        searchThumbnailImageView.setOnClickListener(v -> {
            // TODO update
            Toast.makeText(context, R.string.toast_error_temporarily_unavailable, LENGTH_SHORT).show();
//            Intent intent = new Intent(context, SearchActivity.class);
//            startActivity(intent);
        });

        // initialize thumbnail list
        thumbnailList = new ArrayList<>();
        thumbnailList.add(new Thumbnail(DOWNLOAD_TYPE_RANKINGS));
        thumbnailList.add(new Thumbnail(DOWNLOAD_TYPE_FOLLOWING));
        thumbnailList.add(new Thumbnail(DOWNLOAD_TYPE_COLLECTION));
        thumbnailList.add(new Thumbnail(DOWNLOAD_TYPE_USER));
        thumbnailList.add(new Thumbnail(DOWNLOAD_TYPE_WORK));
        if (!prefs.getBoolean(KEY_IS_RATED, false) && prefs.getInt(KEY_COMPLETE_COUNT, 0) >= 3)
            thumbnailList.add(new Thumbnail(DOWNLOAD_TYPE_RATING));

        // initialize thumbnail recycler view
        RecyclerView thumbnailRecyclerView = rootView.findViewById(R.id.download_recycler);
        thumbnailRecyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, SPAN_COUNT);
        thumbnailRecyclerView.setLayoutManager(gridLayoutManager);

        // initialize thumbnail recycler adapter
        downloadAdapter = new ThumbnailRecyclerAdapter();
        thumbnailRecyclerView.setAdapter(downloadAdapter);

        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            // initialize art list
            if (artList == null) {
                DocumentFile directoryDocumentFile = Converter.getDirectoryDocumentFile(context);
                DeviceController.readArtList(artList = new ArrayList<>(), directoryDocumentFile);
            }

            // on complete
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {

                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@io.reactivex.rxjava3.annotations.NonNull String s) {

                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        // initialize thumbnail art position
                        for (Thumbnail thumbnail : thumbnailList)
                            thumbnail.initializeArtPosition();
                        Thumbnail searchThumbnail = new Thumbnail(DOWNLOAD_TYPE_SEARCH);
                        searchThumbnail.initializeArtPosition();

                        downloadAdapter.notifyItemRangeChanged(0, COUNT_DOWNLOAD_TYPE - 1);
                        Glide.with(context)
                                .load((searchThumbnail.artPosition == -1) ? R.drawable.drawable_thumbnail_long : artList.get(searchThumbnail.artPosition).uri)
                                .centerCrop()
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .override(DeviceController.getWidth(context), DeviceController.getWidth(context) / 2)
                                .into(searchThumbnailImageView);
                    }
                });

        return rootView;
    }

    private void showRatingDialog() {
        // initialize builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogBuilderTheme);

        // initialize rating bar view
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_message_rating, null);

        // set attributes
        builder.setView(view);
        builder.setCancelable(true);
        builder.setMessage(R.string.message_rating);
        builder.setPositiveButton(R.string.confirm,
                (dialog, which) -> {
                    RatingBar ratingBar = view.findViewById(R.id.dialog_rating);
                    float rating = ratingBar.getRating();

                    // check rating
                    if (rating >= 5) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(URI_PXLOADER));
                        startActivity(intent);
                    }

                    SharedPreferences prefs = context.getSharedPreferences(PREFS, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(KEY_IS_RATED, true);
                    editor.apply();

                    thumbnailList.remove(DOWNLOAD_TYPE_RATING);
                    downloadAdapter.notifyItemRemoved(DOWNLOAD_TYPE_RATING);
                });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
        });

        // show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // set dialog message attributes
        TextView messageTextView = dialog.findViewById(android.R.id.message);
        if (messageTextView != null) {
            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text_size_default));
            messageTextView.setLineSpacing(0f, 1.2f);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
    }

    private class Thumbnail {
        int artPosition;
        int downloadType;

        public Thumbnail(int downloadType) {
            this.downloadType = downloadType;
            this.artPosition = -1;
        }

        public void initializeArtPosition() {
            if (artList == null)
                return;
            if (artList.size() < COUNT_DOWNLOAD_TYPE)
                return;
            if (downloadType == DOWNLOAD_TYPE_RATING)
                return;

            ArrayList<Integer> existArtPositionList = new ArrayList<>();
            for (Thumbnail thumbnail : thumbnailList)
                existArtPositionList.add(thumbnail.artPosition);
            Random random = new Random();
            do {
                this.artPosition = random.nextInt(artList.size());
            } while (existArtPositionList.contains(this.artPosition));
        }
    }

    private class ThumbnailRecyclerAdapter extends RecyclerView.Adapter<ThumbnailRecyclerAdapter.ArtViewHolder> {
        class ArtViewHolder extends RecyclerView.ViewHolder {
            ImageView thumbnailImageView;
            LottieAnimationView thumbnailLottieAnimationView;
            TextView titleTextView;

            ArtViewHolder(View itemView) {
                super(itemView);
                thumbnailImageView = itemView.findViewById(R.id.download_image);
                thumbnailLottieAnimationView = itemView.findViewById(R.id.download_lottie_animation);
                titleTextView = itemView.findViewById(R.id.download_title);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION)
                        return;

                    Intent intent = null;
                    // TODO update
                    switch (thumbnailList.get(position).downloadType) {
                        case DOWNLOAD_TYPE_RANKINGS:
                            // TODO update
                            Toast.makeText(context, R.string.toast_error_temporarily_unavailable, LENGTH_SHORT).show();
//                            intent = new Intent(context, RankingsActivity.class);
                            break;
                        case DOWNLOAD_TYPE_FOLLOWING:
                            if (isGuest)
                                Toast.makeText(context, R.string.toast_error_guest, LENGTH_SHORT).show();
                            else
                                intent = new Intent(context, FollowingActivity.class);
                            break;
                        case DOWNLOAD_TYPE_COLLECTION:
                            if (isGuest)
                                Toast.makeText(context, R.string.toast_error_guest, LENGTH_SHORT).show();
                            else
                                intent = new Intent(context, CollectionActivity.class);
                            break;
                        case DOWNLOAD_TYPE_USER:
                            intent = new Intent(context, UserActivity.class);
                            break;
                        case DOWNLOAD_TYPE_WORK:
                            intent = new Intent(context, WorkActivity.class);
                            break;
                        case DOWNLOAD_TYPE_RATING:
                            showRatingDialog();
                            break;
                        default:
                            intent = null;
                    }
                    if (intent != null)
                        startActivity(intent);
                });
            }
        }

        @NonNull
        @Override
        public ThumbnailRecyclerAdapter.ArtViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_download, viewGroup, false);
            return new ThumbnailRecyclerAdapter.ArtViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ThumbnailRecyclerAdapter.ArtViewHolder holder, int position) {
            Thumbnail thumbnail = thumbnailList.get(position);


            if (thumbnail.downloadType == DOWNLOAD_TYPE_RATING) return;

            holder.thumbnailLottieAnimationView.setVisibility(View.GONE);

            Glide.with(context)
                    .load((thumbnail.artPosition == -1) ? R.drawable.drawable_thumbnail : artList.get(thumbnail.artPosition).uri)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .override(DeviceController.getWidth(context) / SPAN_COUNT)
                    .into(holder.thumbnailImageView);

            // TODO update
            if(thumbnail.downloadType == DOWNLOAD_TYPE_SEARCH || thumbnail.downloadType == DOWNLOAD_TYPE_RANKINGS)
                holder.thumbnailImageView.setColorFilter(ContextCompat.getColor(context, R.color.color_signature_filter_darker));
            else
                holder.thumbnailImageView.setColorFilter(ContextCompat.getColor(context, R.color.color_signature_filter));

            holder.titleTextView.setText(Converter.getDownloadLabelResId(position));
        }

        @Override
        public int getItemCount() {
            return thumbnailList.size();
        }
    }
}

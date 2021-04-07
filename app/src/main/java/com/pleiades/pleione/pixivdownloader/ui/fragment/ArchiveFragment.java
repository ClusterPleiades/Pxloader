package com.pleiades.pleione.pixivdownloader.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.michaelflisar.dragselectrecyclerview.DragSelectTouchListener;
import com.pleiades.pleione.pixivdownloader.Converter;
import com.pleiades.pleione.pixivdownloader.DeviceController;
import com.pleiades.pleione.pixivdownloader.R;
import com.pleiades.pleione.pixivdownloader.ui.activity.ImageActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.pleiades.pleione.pixivdownloader.Config.EXTRA_NAME_POSITION;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_CUSTOM_DIRECTORY_URI;
import static com.pleiades.pleione.pixivdownloader.Config.PARCELABLE_KEY_SCROLL;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_PREFS;
import static com.pleiades.pleione.pixivdownloader.Config.SPAN_COUNT;
import static com.pleiades.pleione.pixivdownloader.Variable.artList;

public class ArchiveFragment extends Fragment {
    private Context context;
    private Bundle bundle;
    private Parcelable parcelable;
    private RecyclerView artRecyclerView;
    private DragSelectTouchListener dragSelectTouchListener;
    private ArtRecyclerAdapter artAdapter;

    public static ArchiveFragment newInstance() {
        return new ArchiveFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_archive, container, false);

        // set has options menu
        setHasOptionsMenu(true);

        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            // initialize art list
            DocumentFile directoryDocumentFile = Converter.getDirectoryDocumentFile(context);
            DeviceController.readArtList(artList = new ArrayList<>(), directoryDocumentFile);
            Collections.sort(artList);

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
                        rootView.findViewById(R.id.archive_message).setVisibility(View.GONE);

                        // initialize art recycler view
                        artRecyclerView = rootView.findViewById(R.id.archive_recycler);
                        artRecyclerView.setHasFixedSize(true);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, SPAN_COUNT);
                        artRecyclerView.setLayoutManager(gridLayoutManager);
                        SimpleItemAnimator simpleItemAnimator = (SimpleItemAnimator) artRecyclerView.getItemAnimator();
                        if (simpleItemAnimator != null)
                            simpleItemAnimator.setSupportsChangeAnimations(false);

                        // initialize art adapter
                        artAdapter = new ArtRecyclerAdapter();
                        artAdapter.setHasStableIds(true);
                        artRecyclerView.setAdapter(artAdapter);

                        // initialize on drag selection listener
                        DragSelectTouchListener.OnDragSelectListener onDragSelectionListener = (start, end, isSelected) -> artAdapter.setSelectedRange(start, end, isSelected);

                        // initialize drag select touch listener
                        dragSelectTouchListener = new DragSelectTouchListener()
                                // set drag selection listener
                                .withSelectListener(onDragSelectionListener)
                                // set options
                                .withMaxScrollDistance(24);    // default: 16; 	defines the speed of the auto scrolling
                        //.withTopOffset(toolbarHeight)       // default: 0; 		set an offset for the touch region on top of the RecyclerView
                        //.withBottomOffset(toolbarHeight)    // default: 0; 		set an offset for the touch region on bottom of the RecyclerView
                        //.withScrollAboveTopRegion(enabled)  // default: true; 	enable auto scrolling, even if the finger is moved above the top region
                        //.withScrollBelowTopRegion(enabled)  // default: true; 	enable auto scrolling, even if the finger is moved below the top region
                        //.withDebug(enabled);                // default: false;
                        artRecyclerView.addOnItemTouchListener(dragSelectTouchListener);
                    }
                });

        return rootView;
    }

    public boolean onBackPressed() {
        if (artAdapter != null)
            if (artAdapter.selectionMode) {
                artAdapter.unsetSelectedAll();
                artAdapter.selectionMode = false;
                ((FragmentActivity) context).invalidateOptionsMenu();

                return true;
            }
        return false;
    }

    @Override
    public void onResume() {
        // restore recycler view position
        if (artRecyclerView != null)
            if (bundle != null) {
                parcelable = bundle.getParcelable(PARCELABLE_KEY_SCROLL);
                RecyclerView.LayoutManager layoutManager = artRecyclerView.getLayoutManager();
                if (layoutManager != null)
                    layoutManager.onRestoreInstanceState(parcelable);
            }

        super.onResume();
    }

    @Override
    public void onPause() {
        // initialize recycler state bundle
        bundle = new Bundle();

        // store recycler view position
        if (artRecyclerView != null) {
            RecyclerView.LayoutManager layoutManager = artRecyclerView.getLayoutManager();
            if (layoutManager != null) {
                parcelable = artRecyclerView.getLayoutManager().onSaveInstanceState();
                bundle.putParcelable(PARCELABLE_KEY_SCROLL, parcelable);
            }
        }

        super.onPause();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();

        if (artAdapter != null)
            if (artAdapter.getSelectionMode()) {
                inflater.inflate(R.menu.menu_archive_selected, menu);
                return;
            }
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (artList == null || artAdapter == null) return false;
        SharedPreferences settingPrefs = context.getSharedPreferences(SETTING_PREFS, Context.MODE_PRIVATE);
        String customDirectoryUri = settingPrefs.getString(KEY_CUSTOM_DIRECTORY_URI, null);
        int id = item.getItemId();

        // initialize selected list
        ArrayList<Integer> selectedList = new ArrayList<>(artAdapter.getSelected());
        Collections.sort(selectedList);

        if (id == R.id.action_delete) {
            Collections.reverse(selectedList);
            for (int position : selectedList) {
                boolean result = false;
                if (artList.size() > position) {
                    if (customDirectoryUri == null) {
                        String pathName = artList.get(position).uri.getPath();
                        if (pathName != null) {
                            File file = new File(pathName);
                            result = file.delete();
                        }
                    } else {
                        DocumentFile documentFile = DocumentFile.fromSingleUri(context, artList.get(position).uri);
                        if (documentFile != null)
                            result = documentFile.delete();
                    }

                    if (result) {
                        artList.remove(position);
                        artAdapter.notifyItemRemoved(position);
                    }
                }
            }

            // cancel selection
            artAdapter.unsetSelectedAll();
            artAdapter.setSelectionMode(false);

            // refresh menu
            ((FragmentActivity) context).invalidateOptionsMenu();
        } else if (id == R.id.action_share) {
            ArrayList<Uri> uris = new ArrayList<>();

            for (Integer i : selectedList) {
                // case default uri
                if (customDirectoryUri == null) {
                    String pathName = artList.get(i).uri.getPath();
                    if (pathName != null) {
                        File file = new File(pathName);
                        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
                        uris.add(uri);
                    }
                }
                // case custom uri
                else
                    uris.add(artList.get(i).uri);
            }

            // initialize intent
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("image/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

            // start activity
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private class ArtRecyclerAdapter extends RecyclerView.Adapter<ArtRecyclerAdapter.ArtViewHolder> {
        private Context context;

        private final HashSet<Integer> selected;
        private boolean selectionMode;

        // constructor
        ArtRecyclerAdapter() {
            selected = new HashSet<>();
            selectionMode = false;
        }

        // attach view with holder
        class ArtViewHolder extends RecyclerView.ViewHolder {
            ImageView artImage, artSelected;

            ArtViewHolder(View itemView) {
                super(itemView);
                artImage = itemView.findViewById(R.id.art_image);
                artSelected = itemView.findViewById(R.id.art_check);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION)
                        return;

                    if (selectionMode) {
                        toggleSelected(position);

                        if (selected.size() == 0) {
                            selectionMode = false;

                            // refresh action bar menu
                            ((FragmentActivity) context).invalidateOptionsMenu();
                        }
                    } else {
                        Intent intent = new Intent(context, ImageActivity.class);
                        intent.putExtra(EXTRA_NAME_POSITION, position);
                        context.startActivity(intent);
                    }
                });

                itemView.setOnLongClickListener(view -> {
                    // initialize position
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION)
                        return false;

                    // haptic feedback
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    // change selection mode
                    selectionMode = true;

                    // set item selected
                    setSelected(position, true);

                    // refresh action bar menu
                    ((FragmentActivity) context).invalidateOptionsMenu();

                    // start drag
                    dragSelectTouchListener.startDragSelection(position);

                    return true;
                });
            }
        }

        @NonNull
        @Override
        public ArtViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_art, viewGroup, false);
            return new ArtViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ArtViewHolder holder, int position) {
            // set profile image
            Glide.with(context)
                    .load(artList.get(position).uri)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .override(DeviceController.getWidth(context) / SPAN_COUNT)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.artImage);


            // selection visibility
            if (selectionMode) {
                if (selected.contains(position))
                    holder.artSelected.setVisibility(View.VISIBLE);
                else
                    holder.artSelected.setVisibility(View.INVISIBLE);
            } else
                holder.artSelected.setVisibility(View.INVISIBLE);
        }

        // selected
        public void setSelected(int position, boolean isSelected) {
            if (isSelected)
                selected.add(position);
            else
                selected.remove(position);
            notifyItemChanged(position);
        }

        public void setSelectedRange(int startPosition, int endPosition, boolean isSelected) {
            if (isSelected)
                for (int position = startPosition; position <= endPosition; position++) {
                    selected.add(position);
                    notifyItemChanged(position);
                }
            else
                for (int position = startPosition; position <= endPosition; position++) {
                    selected.remove(position);
                    notifyItemChanged(position);
                }
        }

        public void toggleSelected(int position) {
            if (selected.contains(position))
                selected.remove(position);
            else
                selected.add(position);
            notifyItemChanged(position);
        }

        public void unsetSelectedAll() {
            selected.clear();
            notifyDataSetChanged();
        }

        public HashSet<Integer> getSelected() {
            return selected;
        }

        // selection mode
        public void setSelectionMode(boolean selectionMode) {
            this.selectionMode = selectionMode;
        }

        public boolean getSelectionMode() {
            return selectionMode;
        }

        @Override
        public int getItemCount() {
            return artList.size();
        }

        @Override
        public long getItemId(int position) {
            return artList.get(position).name.hashCode();
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            context = recyclerView.getContext();
        }
    }
}
package com.pleiades.pleione.pixivdownloader.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.pleiades.pleione.pixivdownloader.DeviceController;
import com.pleiades.pleione.pixivdownloader.R;

import java.util.ArrayList;

import static com.pleiades.pleione.pixivdownloader.Config.KEY_TAGS_TO_EXCLUDE;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_PREFS;

public class TagsActivity extends AppCompatActivity {
    private ArrayList<String> tagList;
    private RecyclerView tagRecyclerView;
    private TagRecyclerAdapter tagAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);

        // initialize context
        Context context = TagsActivity.this;

        // initialize app bar
        View appbar = findViewById(R.id.tags_appbar);
        Toolbar toolbar = appbar.findViewById(R.id.include_toolbar);
        setSupportActionBar(toolbar);

        // initialize actionbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // initialize tag list
        tagList = DeviceController.getTagListPrefs(context);

        // initialize tag recycler view
        tagRecyclerView = findViewById(R.id.tags_recycler);
        tagRecyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        tagRecyclerView.setLayoutManager(linearLayoutManager);

        // initialize tag adapter
        tagAdapter = new TagRecyclerAdapter();
        tagRecyclerView.setAdapter(tagAdapter);

        // initialize edit tag to exclude
        EditText addEditText = findViewById(R.id.tags);
        addEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                // initialize title
                String tag = v.getText().toString();
                if (tag.equals(""))
                    return false;

                // add tag
                tagList.add(tag);

                // apply tag list
                SharedPreferences settingPrefs = getSharedPreferences(SETTING_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor settingEditor = settingPrefs.edit();
                String json = new Gson().toJson(tagList);
                settingEditor.putString(KEY_TAGS_TO_EXCLUDE, json);
                settingEditor.apply();

                // refresh recycler view
                tagAdapter.notifyItemInserted(tagList.size() - 1);
                tagRecyclerView.scrollToPosition(tagList.size() - 1);

                addEditText.setText("");
                addEditText.requestFocus();
                return true;
            }
            return false;
        });
        addEditText.requestFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }


    private class TagRecyclerAdapter extends RecyclerView.Adapter<TagRecyclerAdapter.TagViewHolder> {

        class TagViewHolder extends RecyclerView.ViewHolder {
            TextView tagTextView;
            ImageButton removeImageButton;

            public TagViewHolder(@NonNull View itemView) {
                super(itemView);

                // initialize view
                tagTextView = itemView.findViewById(R.id.tag_title);
                removeImageButton = itemView.findViewById(R.id.tag_remove);
                removeImageButton.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION)
                        return;

                    // remove tag
                    tagList.remove(position);

                    // set setting prefs
                    SharedPreferences settingPrefs = getSharedPreferences(SETTING_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor settingEditor = settingPrefs.edit();
                    String json = new Gson().toJson(tagList);
                    settingEditor.putString(KEY_TAGS_TO_EXCLUDE, json);
                    settingEditor.apply();

                    // refresh recycler view
                    notifyItemRemoved(position);
                });
            }
        }

        @NonNull
        @Override
        public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_tag, parent, false);
            return new TagViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
            // set title
            holder.tagTextView.setText(tagList.get(position));
        }

        @Override
        public int getItemCount() {
            if (tagList == null)
                return 0;
            return tagList.size();
        }
    }
}

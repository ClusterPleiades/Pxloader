package com.pleiades.pleione.pixivdownloader.ui.activity;

import static android.provider.DocumentsContract.EXTRA_INITIAL_URI;
import static android.widget.Toast.LENGTH_SHORT;
import static com.pleiades.pleione.pixivdownloader.Config.FORMAT_TYPE_WORK_ID;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_CREATE_SUB_DIRECTORY;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_CUSTOM_DIRECTORY_URI;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_IS_CROWN;
import static com.pleiades.pleione.pixivdownloader.Config.KEY_SELECTED_FORMATS;
import static com.pleiades.pleione.pixivdownloader.Config.PREFS;
import static com.pleiades.pleione.pixivdownloader.Config.REQUEST_CODE_DIRECTORY;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_PREFS;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_TYPE_ABOUT;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_TYPE_CREATE_SUB_DIRECTORY;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_TYPE_DIRECTORY_PATH;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_TYPE_DISCLAIMER;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_TYPE_FILE_NAME_FORMAT;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_TYPE_REMOVE_ADS;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_TYPE_SHARE;
import static com.pleiades.pleione.pixivdownloader.Config.SETTING_TYPE_TAGS_TO_EXCLUDE;
import static com.pleiades.pleione.pixivdownloader.Config.URI_PXLOADER;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.pleiades.pleione.pixivdownloader.DeviceController;
import com.pleiades.pleione.pixivdownloader.R;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity{
    private Activity activity;
    private ArrayList<Setting> settingList;
    private SettingRecyclerAdapter settingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // initialize context
        activity = SettingsActivity.this;

        // initialize app bar
        View appbar = findViewById(R.id.settings_appbar);
        Toolbar toolbar = appbar.findViewById(R.id.include_toolbar);
        setSupportActionBar(toolbar);

        // initialize actionbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // initialize setting list
        settingList = new ArrayList<>();
        settingList.add(new Setting(SETTING_TYPE_CREATE_SUB_DIRECTORY));
        settingList.add(new Setting(SETTING_TYPE_DIRECTORY_PATH));
        settingList.add(new Setting(SETTING_TYPE_FILE_NAME_FORMAT));
        settingList.add(new Setting(SETTING_TYPE_TAGS_TO_EXCLUDE));
        settingList.add(new Setting(SETTING_TYPE_ABOUT));
        settingList.add(new Setting(SETTING_TYPE_DISCLAIMER));
        if (!prefs.getBoolean(KEY_IS_CROWN, false)) settingList.add(new Setting(SETTING_TYPE_REMOVE_ADS));
        settingList.add(new Setting(SETTING_TYPE_SHARE));

        // initialize setting recycler view
        RecyclerView settingRecyclerView = findViewById(R.id.settings_recycler);
        settingRecyclerView.addItemDecoration(new DividerItemDecoration(activity, DividerItemDecoration.VERTICAL));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        settingRecyclerView.setLayoutManager(linearLayoutManager);

        // initialize setting adapter
        settingAdapter = new SettingRecyclerAdapter();
        settingRecyclerView.setAdapter(settingAdapter);
    }

    private void showDisclaimerDialog() {
        // initialize builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogBuilderTheme);
        builder.setMessage(R.string.message_disclaimer);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
        });

        // show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // set dialog message attributes
        TextView messageTextView = dialog.findViewById(android.R.id.message);
        if (messageTextView != null) {
            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_default));
            messageTextView.setLineSpacing(0f, 1.2f);
        }
    }

    private void showFileNameFormatDialog() {
        SharedPreferences settingPrefs = getSharedPreferences(SETTING_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor settingEditor = settingPrefs.edit();
        String[] formatContents = getResources().getStringArray(R.array.contents_formats);
        ArrayList<Integer> formatCodeList = new ArrayList<>();

        // initialize builder
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogBuilderTheme);
        builder.setMultiChoiceItems(formatContents, null, (dialog, which, isChecked) -> {
            if (isChecked)
                formatCodeList.add(which);
            else
                formatCodeList.remove((Integer) which);
        });
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            if (formatCodeList.size() > 0) {
                String json = new Gson().toJson(formatCodeList);
                settingEditor.putString(KEY_SELECTED_FORMATS, json);
                settingEditor.apply();
                settingAdapter.notifyItemRangeChanged(0, settingAdapter.getItemCount());
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
        });

        // show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.action_reset) {
            SharedPreferences settingPrefs = getSharedPreferences(SETTING_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor settingEditor = settingPrefs.edit();
            settingEditor.clear();
            settingEditor.apply();

            settingAdapter.notifyItemRangeChanged(0, settingAdapter.getItemCount());
            // notify data set~ : change data only, cut ripple effect
            // notify item ~    : change item(including data) all, persist ripple effect
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // case directory path
        if (requestCode == REQUEST_CODE_DIRECTORY && resultCode == Activity.RESULT_OK) {
            if (data == null)
                return;
            if (data.getData() == null)
                return;

            // initialize custom uri
            Uri customUri = data.getData();
            String customUriString = customUri.toString();

            if (customUriString.split("%3A|%2F").length == 1)
                Toast.makeText(activity, R.string.toast_error_path, LENGTH_SHORT).show();
            else {
                // set setting prefs
                SharedPreferences settingPrefs = getSharedPreferences(SETTING_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor settingEditor = settingPrefs.edit();
                settingEditor.putString(KEY_CUSTOM_DIRECTORY_URI, customUri.toString());
                settingEditor.apply();

                // set permission persist
                int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(customUri, flags);
            }
        }

        settingAdapter.notifyItemRangeChanged(0, settingAdapter.getItemCount());
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static class Setting {
        int settingCode;

        Setting(int settingCode) {
            this.settingCode = settingCode;
        }
    }

    private class SettingRecyclerAdapter extends RecyclerView.Adapter<SettingRecyclerAdapter.SettingViewHolder> {
        private boolean isCheckLocked;

        class SettingViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView, contentsTextView;
            SwitchCompat switchCompat;

            // constructor
            SettingViewHolder(View view) {
                super(view);

                // initialize view
                titleTextView = view.findViewById(R.id.setting_title);
                contentsTextView = view.findViewById(R.id.setting_contents);
                switchCompat = view.findViewById(R.id.setting_switch);

                // set view click listener
                view.setOnClickListener(v -> {
                    // initialize position
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION)
                        return;

                    Intent intent;
                    switch (settingList.get(position).settingCode) {
                        case SETTING_TYPE_DIRECTORY_PATH:
                            // initialize intent
                            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                            // initialize extra uri
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                SharedPreferences settingPrefs = getSharedPreferences(SETTING_PREFS, Context.MODE_PRIVATE);
                                String customUriString = settingPrefs.getString(KEY_CUSTOM_DIRECTORY_URI, null);
                                if (customUriString != null) {
                                    Uri customUri = Uri.parse(customUriString);
                                    DocumentFile directoryFile = DocumentFile.fromTreeUri(activity, customUri);
                                    if (directoryFile != null)
                                        intent.putExtra(EXTRA_INITIAL_URI, directoryFile.getUri());
                                }
                            }

                            // start activity
                            activity.startActivityForResult(intent, REQUEST_CODE_DIRECTORY);
                            break;
                        case SETTING_TYPE_FILE_NAME_FORMAT:
                            showFileNameFormatDialog();
                            break;
                        case SETTING_TYPE_TAGS_TO_EXCLUDE:
                            activity.startActivity(new Intent(activity, TagsActivity.class));
                            break;
                        case SETTING_TYPE_DISCLAIMER:
                            showDisclaimerDialog();
                            break;
                        case SETTING_TYPE_REMOVE_ADS:
                            break;
                        case SETTING_TYPE_SHARE:
                            intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, URI_PXLOADER);
                            activity.startActivity(intent);
                            break;
                    }
                });
            }
        }

        @NonNull
        @Override
        public SettingViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_setting, viewGroup, false);
            return new SettingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SettingViewHolder holder, final int position) {
            SharedPreferences settingPrefs = getSharedPreferences(SETTING_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor settingEditor = settingPrefs.edit();
            int settingCode = settingList.get(position).settingCode;

            // set title, contents
            String contents;
            switch (settingCode) {
                case SETTING_TYPE_CREATE_SUB_DIRECTORY:
                    holder.titleTextView.setText(R.string.setting_create_sub_directory);
                    break;
                case SETTING_TYPE_DIRECTORY_PATH:
                    holder.titleTextView.setText(R.string.setting_directory_path);
                    String customDirectoryUri = settingPrefs.getString(KEY_CUSTOM_DIRECTORY_URI, null);
                    contents = (customDirectoryUri == null) ? (Environment.DIRECTORY_PICTURES + "/Pxloader/" + "~") : Uri.parse(customDirectoryUri).getPath() + "~";
                    holder.contentsTextView.setText(contents);
                    break;
                case SETTING_TYPE_FILE_NAME_FORMAT:
                    holder.titleTextView.setText(R.string.setting_file_name_format);
                    ArrayList<Integer> formatTypeList = DeviceController.getFormatListPrefs(activity);
                    String[] formatContents = getResources().getStringArray(R.array.contents_formats);
                    StringBuilder contentsStringBuilder;
                    if (formatTypeList == null)
                        contentsStringBuilder = new StringBuilder(formatContents[FORMAT_TYPE_WORK_ID]);
                    else {
                        contentsStringBuilder = new StringBuilder(formatContents[formatTypeList.remove(0)]);
                        for (int format : formatTypeList)
                            contentsStringBuilder.append("_").append(formatContents[format]);
                    }
                    contents = contentsStringBuilder.toString();
                    holder.contentsTextView.setText(contents);
                    break;
                case SETTING_TYPE_TAGS_TO_EXCLUDE:
                    holder.titleTextView.setText(R.string.setting_tags_to_exclude);
                    break;
                case SETTING_TYPE_ABOUT:
                    holder.titleTextView.setText(null);
                    holder.contentsTextView.setText(R.string.setting_about);
                    break;
                case SETTING_TYPE_DISCLAIMER:
                    holder.titleTextView.setText(R.string.setting_disclaimer);
                    break;
                case SETTING_TYPE_REMOVE_ADS:
                    holder.titleTextView.setText(R.string.setting_remove_ads);
                    break;
                case SETTING_TYPE_SHARE:
                    holder.titleTextView.setText(R.string.setting_share);
                    break;
            }

            // set contents visibility
            switch (settingCode) {
                case SETTING_TYPE_DIRECTORY_PATH:
                case SETTING_TYPE_FILE_NAME_FORMAT:
                case SETTING_TYPE_ABOUT:
                    holder.contentsTextView.setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.contentsTextView.setVisibility(View.GONE);
                    break;
            }

            // set contents color
            if (position == SETTING_TYPE_ABOUT)
                holder.contentsTextView.setTextColor(ContextCompat.getColor(activity, R.color.color_signature_light_blue));
            else
                holder.contentsTextView.setTextColor(ContextCompat.getColor(activity, R.color.color_signature_gray));

            // set switch visibility
            if (settingCode == SETTING_TYPE_CREATE_SUB_DIRECTORY)
                holder.switchCompat.setVisibility(View.VISIBLE);
            else
                holder.switchCompat.setVisibility(View.INVISIBLE);

            // initialize on check change listener
            holder.switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isCheckLocked)
                    return;

                if (position == SETTING_TYPE_CREATE_SUB_DIRECTORY)
                    settingEditor.putBoolean(KEY_CREATE_SUB_DIRECTORY, isChecked);
                settingEditor.apply();
            });

            // set checked
            isCheckLocked = true;
            if (position == SETTING_TYPE_CREATE_SUB_DIRECTORY)
                holder.switchCompat.setChecked(settingPrefs.getBoolean(KEY_CREATE_SUB_DIRECTORY, true));
            isCheckLocked = false;
        }

        @Override
        public int getItemCount() {
            return settingList.size();
        }
    }
}
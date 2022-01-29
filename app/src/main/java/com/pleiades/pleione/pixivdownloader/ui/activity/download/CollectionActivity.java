package com.pleiades.pleione.pixivdownloader.ui.activity.download;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.pleiades.pleione.pixivdownloader.Converter;
import com.pleiades.pleione.pixivdownloader.DeviceController;
import com.pleiades.pleione.pixivdownloader.LaunchController;
import com.pleiades.pleione.pixivdownloader.R;
import com.pleiades.pleione.pixivdownloader.client.Client;
import com.pleiades.pleione.pixivdownloader.pixiv.DownloadCallback;
import com.pleiades.pleione.pixivdownloader.pixiv.ParserParam;
import com.pleiades.pleione.pixivdownloader.pixiv.Work;

import java.util.List;

import static com.pleiades.pleione.pixivdownloader.Config.PAGE_NO_NEXT;
import static com.pleiades.pleione.pixivdownloader.Config.RELATIVE_PATH_COLLECTION;
import static com.pleiades.pleione.pixivdownloader.Variable.backupBytes;
import static com.pleiades.pleione.pixivdownloader.Variable.interstitialAd;
import static com.pleiades.pleione.pixivdownloader.Variable.message1;
import static com.pleiades.pleione.pixivdownloader.Variable.message2;

public class CollectionActivity extends AppCompatActivity {
    private Activity activity;
    private String publicity;
    private CollectionLauncher collectionLauncher;
    private boolean isInBackground;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // initialize context
        activity = CollectionActivity.this;

        // initialize app bar
        View appbar = findViewById(R.id.collection_appbar);
        Toolbar toolbar = appbar.findViewById(R.id.include_toolbar);
        setSupportActionBar(toolbar);

        // initialize actionbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // initialize publicity auto complete text view
        AutoCompleteTextView publicityAutoCompleteTextView = findViewById(R.id.collection_publicity);
        publicityAutoCompleteTextView.setOnTouchListener((v, event) -> {
            String[] publicities = getResources().getStringArray(R.array.input_publicities);
            publicityAutoCompleteTextView.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, publicities));
            publicityAutoCompleteTextView.showDropDown();
            return false;
        });

        // initialize download button
        TextView downloadTextView = findViewById(R.id.collection_download);
        downloadTextView.setOnClickListener(v -> {
            if (((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(activity.getPackageName())) {
                // initialize input attributes
                String inputPublicity = publicityAutoCompleteTextView.getText().toString();

                // initialize attributes
                publicity = Converter.getPublicity(activity, inputPublicity);

                if (publicity != null){
                    collectionLauncher = new CollectionLauncher();
                    collectionLauncher.execute();
                }
            } else
                DeviceController.showOptimizationDialog(activity);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        isInBackground = false;
        if (collectionLauncher != null) {
            collectionLauncher.restoreProgressDialog();

            if (collectionLauncher.client != null)
                collectionLauncher.client.setPoolSize(Runtime.getRuntime().availableProcessors());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        isInBackground = true;
        if (collectionLauncher != null)
            if (collectionLauncher.client != null)
                collectionLauncher.client.setPoolSize(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (collectionLauncher != null) {
            if (collectionLauncher.client != null)
                collectionLauncher.client.shutDownProcess();
            collectionLauncher.cancel(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private class CollectionLauncher extends AsyncTask<Object, Object, Object> {
        private Client client;
        private ParserParam parserParam;
        private ProgressDialog progressDialog;
        private ImageView dialogTitleImageView;
        private ProgressBar dialogTitleProgressBar;

        @Override
        protected void onPreExecute() {
            // initialize async dialog
            progressDialog = new ProgressDialog(activity, R.style.AlertDialogBuilderTheme);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.stop), (dialog, which) -> {
                // cancel client process
                client.shutDownProcess();

                // complete launcher
                finish();
            });

            // initialize progress dialog title
            @SuppressLint("InflateParams")
            View titleView = activity.getLayoutInflater().inflate(R.layout.dialog_title_progress, null);
            dialogTitleImageView = titleView.findViewById(R.id.dialog_image);
            dialogTitleProgressBar = titleView.findViewById(R.id.dialog_progress);
            progressDialog.setCustomTitle(titleView);

            // show progress dialog
            progressDialog.show();

            // initialize client
            client = new Client();

            // initialize progress dialog message
            TextView messageTextView = progressDialog.findViewById(android.R.id.message);
            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_default));
            messageTextView.setLineSpacing(0f, 1.2f);
            progressDialog.setMessage(getString(R.string.message_index) + " (" + getString(R.string.page) + " " + client.currentPage + ")");

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Object[] objects) {
            // initialize parser param
            parserParam = new ParserParam()
                    .withFilter(work -> {
                        if (LaunchController.filterWorkDelete(work))
                            return LaunchController.filterWorkTags(activity, work);
                        return false;
                    })
                    .withCallback(work -> {
                        if (!client.submitWork(work, new DownloadCallback() {
                            @Override
                            public void onIllustrationDownloaded(Work work, byte[] bytes) {
                                if (client.isShutDown()) return;

                                String fileName = Converter.getFileName(activity, work, -1);
                                String[] relativePaths = {RELATIVE_PATH_COLLECTION, Converter.getPublicityRelativePath(activity, publicity)};
                                DeviceController.saveBytes(activity, work, bytes, fileName, relativePaths);
                                updateDownload(bytes, work.getId());
                            }

                            @Override
                            public void onUgoiraDownloaded(Work work, byte[] bytes) {
                                if (client.isShutDown()) return;

                                byte[] zipBytes = client.searchFrameZipBytes(work);
                                String fileName = Converter.getFileName(activity, work, -1);
                                String[] relativePaths = {RELATIVE_PATH_COLLECTION, Converter.getPublicityRelativePath(activity, publicity)};
                                DeviceController.saveBytes(activity, work, zipBytes, fileName, relativePaths);
                                updateDownload(bytes, work.getId());
                            }

                            @Override
                            public void onMangaDownloaded(Work work, List<byte[]> bytesList) {
                                if (client.isShutDown()) return;

                                for (int i = 0; i < bytesList.size(); i++) {
                                    byte[] bytes = bytesList.get(i);
                                    String fileName = Converter.getFileName(activity, work, i + 1);
                                    String[] relativePaths = {RELATIVE_PATH_COLLECTION, Converter.getPublicityRelativePath(activity, publicity)};
                                    DeviceController.saveBytes(activity, work, bytes, fileName, relativePaths);
                                    updateDownload(work.getId(), i + 1);
                                }
                                updateDownload(bytesList.get(0), work.getId());
                            }
                        })) {
                            // case no works with filter found in current page
                            launchNextPage();
                        }
                    });

            // search collection
            client.searchCollection(publicity, parserParam);

            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            if (client.isShutDown())
                return;

            // set downloaded image view from values[0] (byte[])
            if (values[0] != null) {
                byte[] byteFile = (byte[]) values[0];
                Glide.with(activity)
                        .load(byteFile)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(dialogTitleImageView);
            }

            // set progress bar
            dialogTitleProgressBar.setMax(client.currentPageSize);
            dialogTitleProgressBar.setProgress(client.currentPageDownloadCount);

            // set messages from values[1], values[2] (String)
            String message = values[1].toString();
            String messageAppend = values[2].toString();

            // set dialog message
            progressDialog.setMessage(message);

            // append dialog message
            if (message.equals(getString(R.string.message_index)))
                // case indexing
                progressDialog.setMessage(message + " (" + messageAppend + ")");
            else {
                // case download
                progressDialog.setMessage(message + "\n" + messageAppend);
            }

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
        }

        public void restoreProgressDialog() {
            if (backupBytes != null)
                publishProgress(backupBytes, message1, message2);
        }

        private synchronized void updateDownload(byte[] bytes, int workId) {
            // count download
            client.countDownload(workId);

            // initialize messages
            String message = getString(R.string.last_downloaded) + " " + client.currentWorkId;
            String messageAppend = getString(R.string.count) + " " + client.totalDownloadCount;

            // backup or update dialog
            if (isInBackground) {
                backupBytes = bytes;
                message1 = message;
                message2 = messageAppend;
            } else
                publishProgress(bytes, message, messageAppend);

            // push notification
            DeviceController.pushNotification(activity, message, messageAppend, NotificationCompat.PRIORITY_LOW);

            // index next page
            if (client.isPageFinished())
                launchNextPage();
        }

        private synchronized void updateDownload(int workId, int iterator) {
            // initialize messages
            String message = getString(R.string.last_downloaded) + " " + workId + "_" + iterator;
            String messageAppend = getString(R.string.count) + " " + client.totalDownloadCount;

            // backup or update dialog
            if (isInBackground) {
                message1 = message;
                message2 = messageAppend;
            } else
                publishProgress(null, message, messageAppend);

            // push notification
            DeviceController.pushNotification(activity, message, messageAppend, NotificationCompat.PRIORITY_LOW);
        }

        private void launchNextPage() {
            if (client.isShutDown())
                return;

            if (client.indexNextPageUri() == PAGE_NO_NEXT) {
                finish();
            } else {
                // update progress dialog
                publishProgress(null, getString(R.string.message_index), getString(R.string.page) + " " + client.currentPage);

                // search next page
                client.searchCollection(publicity, parserParam);
            }
        }

        private void finish() {
            // dismiss progress dialog
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

            // complete process
            LaunchController.completeClient(activity, client);

            // cancel launcher
            cancel(true);
        }
    }
}
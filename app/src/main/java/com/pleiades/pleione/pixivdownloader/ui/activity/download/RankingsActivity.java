package com.pleiades.pleione.pixivdownloader.ui.activity.download;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Toast;

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
import com.pleiades.pleione.pixivdownloader.pixiv.Rank;
import com.pleiades.pleione.pixivdownloader.pixiv.Work;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.widget.Toast.LENGTH_SHORT;
import static com.pleiades.pleione.pixivdownloader.Config.RELATIVE_PATH_RANKINGS;
import static com.pleiades.pleione.pixivdownloader.Variable.backupBytes;
import static com.pleiades.pleione.pixivdownloader.Variable.interstitialAd;
import static com.pleiades.pleione.pixivdownloader.Variable.isGuest;
import static com.pleiades.pleione.pixivdownloader.Variable.message1;
import static com.pleiades.pleione.pixivdownloader.Variable.message2;

public class RankingsActivity extends AppCompatActivity {
    private Activity activity;
    private String date;
    private String aggregationMode;
    private RankingsLauncher rankingsLauncher;
    private boolean isInBackground;

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rankings);

        // keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // initialize context
        activity = RankingsActivity.this;

        // initialize app bar
        View appbar = findViewById(R.id.rankings_appbar);
        Toolbar toolbar = appbar.findViewById(R.id.include_toolbar);
        setSupportActionBar(toolbar);

        // initialize actionbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // initialize date text view
        TextView dateTextView = findViewById(R.id.rankings_date);
        dateTextView.setOnClickListener(view -> {
            // initialize calendar
            Calendar calendar = Calendar.getInstance();

            // initialize dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(activity, R.style.DatePickerDialogTheme, (datePicker, year, month, date) ->
                    dateTextView.setText(String.format(Locale.getDefault(), "%4d-%02d-%02d", year, month + 1, date)), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

            // set dialog max date
            Calendar maxCalendar = Calendar.getInstance();
            maxCalendar.add(Calendar.DATE, -1);
            Date maxDate = maxCalendar.getTime();
            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTime());

            // show date picker dialog
            datePickerDialog.show();
        });

        // initialize aggregation mode text view
        AutoCompleteTextView aggregationModeAutoCompleteTextView = findViewById(R.id.rankings_aggregation_mode);
        aggregationModeAutoCompleteTextView.setOnTouchListener((v, event) -> {
            String[] aggregationModes = getResources().getStringArray(isGuest ? R.array.input_aggregation_modes_guest : R.array.input_aggregation_modes);
            aggregationModeAutoCompleteTextView.setAdapter(new ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, aggregationModes));
            aggregationModeAutoCompleteTextView.showDropDown();
            return false;
        });

        // initialize button
        TextView downloadTextView = findViewById(R.id.rankings_download);
        downloadTextView.setOnClickListener(v -> {
            if (((PowerManager) getSystemService(POWER_SERVICE)).isIgnoringBatteryOptimizations(activity.getPackageName())) {
                // initialize input attributes
                String inputDate = dateTextView.getText().toString();
                String inputAggregationMode = aggregationModeAutoCompleteTextView.getText().toString();

                // initialize attributes
                date = Converter.getDate(inputDate);
                aggregationMode = Converter.getAggregationMode(activity, inputAggregationMode);

                if (date != null && aggregationMode != null) {
                    rankingsLauncher = new RankingsLauncher();
                    rankingsLauncher.execute();
                }
            } else
                DeviceController.showOptimizationDialog(activity);
        });

        // show ad
        if (interstitialAd != null) {
            DeviceController.setInterstitialAdCallback(activity);
            interstitialAd.show(activity);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        isInBackground = false;
        if (rankingsLauncher != null) {
            rankingsLauncher.restoreProgressDialog();

            if (rankingsLauncher.client != null)
                rankingsLauncher.client.setPoolSize(Runtime.getRuntime().availableProcessors());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        isInBackground = true;
        if (rankingsLauncher != null)
            if (rankingsLauncher.client != null)
                rankingsLauncher.client.setPoolSize(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (rankingsLauncher != null) {
            if (rankingsLauncher.client != null)
                rankingsLauncher.client.shutDownProcess();
            rankingsLauncher.cancel(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private class RankingsLauncher extends AsyncTask<Object, Object, Object> {
        private Client client;
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
            progressDialog.setMessage(getString(R.string.message_index) + " (" + getString(R.string.page_all) + ")");
            publishProgress(null, getString(R.string.message_index), getString(R.string.page_all));

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Object[] objects) {
            // initialize parser param
            ParserParam param = new ParserParam()
                    .withCallbackRank((work, ranking) -> {
                        if (!client.submitWork(work, new DownloadCallback() {
                            @Override
                            public void onIllustrationDownloaded(Work work, byte[] bytes) {
                                if (client.isShutDown()) return;

                                String fileName = ranking + "_" + Converter.getFileName(activity, work, -1);
                                String[] relativePaths = {RELATIVE_PATH_RANKINGS, date, Converter.getAggregationModeRelativePath(activity, aggregationMode)};
                                DeviceController.saveBytes(activity, work, bytes, fileName, relativePaths);
                                updateDownload(bytes, work.getId());
                            }

                            @Override
                            public void onUgoiraDownloaded(Work work, byte[] bytes) {
                                if (client.isShutDown()) return;

                                byte[] zipBytes = client.searchFrameZipBytes(work);
                                String fileName = ranking + "_" + Converter.getFileName(activity, work, -1);
                                String[] relativePaths = {RELATIVE_PATH_RANKINGS, date, Converter.getAggregationModeRelativePath(activity, aggregationMode)};
                                DeviceController.saveBytes(activity, work, zipBytes, fileName, relativePaths);
                                updateDownload(bytes, work.getId());
                            }

                            @Override
                            public void onMangaDownloaded(Work work, List<byte[]> bytesList) {
                                if (client.isShutDown()) return;

                                for (int i = 0; i < bytesList.size(); i++) {
                                    byte[] bytes = bytesList.get(i);
                                    String fileName = ranking + "_" + Converter.getFileName(activity, work, i + 1);
                                    String[] relativePaths = {RELATIVE_PATH_RANKINGS, date, Converter.getAggregationModeRelativePath(activity, aggregationMode)};
                                    DeviceController.saveBytes(activity, work, bytes, fileName, relativePaths);
                                    updateDownload(work.getId(), i + 1);
                                }
                                updateDownload(bytesList.get(0), work.getId());
                            }
                        })) {
                            // case no works with filter found in current page
                            finish();
                        }
                    });

            // search rankings
            Rank rank = client.searchRank(date, aggregationMode);
            if (rank == null) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> Toast.makeText(activity, R.string.toast_error_aggregation, LENGTH_SHORT).show());

                // complete launcher
                finish();
            } else
                client.searchRankings(rank, param);

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

            // finish
            if (client.isPageFinished())
                finish();
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
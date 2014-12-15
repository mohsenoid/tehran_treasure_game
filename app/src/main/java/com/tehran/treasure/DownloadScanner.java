package com.tehran.treasure;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadScanner extends AsyncTask<String, Integer, Boolean> {
    ProgressDialog downloadDialog;
    private Context context;

    public DownloadScanner(final Context context) {
        this.context = context;
        downloadDialog = new ProgressDialog(context);
        downloadDialog.setMessage(context.getResources().getString(
                R.string.downloading));
        downloadDialog.setIndeterminate(false);
        downloadDialog.setMax(100);
        downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downloadDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
                Activity a = (Activity) context;
                a.finish();
            }
        });

        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... downloadURL) {
        try {
            URL url = new URL(downloadURL[0]);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();

            int fileLength = c.getContentLength();

            String PATH = Environment.getExternalStorageDirectory().getPath()
                    + "/Download/";
            File file = new File(PATH);
            file.mkdirs();
            File outputFile = new File(file, "BarcodeScanner.apk");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = is.read(data)) != -1) {
                total += count;
                // publishing the progress....
                publishProgress((int) (total * 100 / fileLength));
                fos.write(data, 0, count);
            }

            fos.close();
            is.close();

            return true;
        } catch (Exception e) {
            Log.e("UpdateAPP", "Update error! " + e.getMessage());
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean isDone) {
        if (isDone) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(
                            Environment.getExternalStorageDirectory().getPath()
                                    + "/Download/BarcodeScanner.apk")),
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag
            // android returned
            // a intent error!
            context.startActivity(intent);

            exit();
        } else {
            exit();
            Toast.makeText(
                    context,
                    context.getResources().getString(
                            R.string.update_download_error), Toast.LENGTH_LONG)
                    .show();

        }
        super.onPostExecute(isDone);
    }

    private void exit() {
        Activity a = (Activity) context;
        a.finish();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        downloadDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        downloadDialog.setProgress(progress[0]);
    }

}
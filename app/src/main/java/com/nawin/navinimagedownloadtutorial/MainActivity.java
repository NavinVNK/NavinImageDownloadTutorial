package com.nawin.navinimagedownloadtutorial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    // button to show progress dialog
    Button downLoad;

    // Progress Dialog
    private ProgressDialog progressDialog;
    ImageView my_image;
    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;
    public static final boolean toggle = true;

    // File url to download
    private static String file_url = "http://1.bp.blogspot.com/-gdtM0DcoSys/UzD-y3a9k9I/AAAAAAAABNo/ewzR8IKKPAQ/s1600/wid4.png";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // show progress bar button
        downLoad = (Button) findViewById(R.id.btnProgressBar);
        // Image view to show image after downloading
        my_image = (ImageView) findViewById(R.id.my_image);
        //requestPermission();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("per","Permission is granted1");

            } else {

                requestPermission();
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("per","Permission is granted1");

        }
        downLoad.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(checkPermission())
                {
                    new DownloadFileFromURL().execute(file_url);
                }
                else {
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Showing Dialog
     * */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Downloading file. Please wait...");
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(100);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(true);
                progressDialog.show();
                return progressDialog;
            default:
                return null;
        }
    }

    /**
     * Background Async Task to download file
     * */
    class DownloadFileFromURL extends AsyncTask<String, Integer, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... fileurl) {
            String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();




            try
            {
                URL url = new URL(fileurl[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();
                //set the path where we want to save the file in this case, going to save it on the root directory of the sd card.
                File SDCardRoot = Environment.getExternalStorageDirectory();
                //create a new file, specifying the path, and the filename which we want to save the file as.
                File file = new File(SDCardRoot,"image.jpg");
                //this will be used to write the downloaded data into the file we created
                FileOutputStream fileOutput = new FileOutputStream(file);
                //this will be used in reading the data from the internet
                InputStream inputStream = urlConnection.getInputStream();
                //this is the total size of the file
                int totalSize = urlConnection.getContentLength();
                //variable to store total downloaded bytes
                int downloadedSize = 0;
                byte[] buffer = new byte[1024];
                int bufferLength = 0; //used to store a temporary size of the buffer
                //now, read through the input buffer and write the contents to the file
                while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                    fileOutput.write(buffer, 0, bufferLength);
                    //add up the size so we know how much is downloaded
                    downloadedSize += bufferLength;
                    //this is where you would do something to report the prgress, like this maybe
                    //updateProgress(downloadedSize, totalSize);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(Integer... downloadprogress){//String... downloadprogress) {
            // setting progress percentage
            progressDialog.setProgress(downloadprogress[0]);
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);

            // Displaying downloaded image into image view
            // Reading image path from sdcard
            String imagePath = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/image.jpg";
            // String imagePath = "/sdcard/downloadedfile.jpg";
            // setting downloaded into image view /mnt/sdcard
            //  my_image.setImageDrawable(Drawable.createFromPath(imagePath));
            File imgFile = new  File(imagePath);//"/mnt/sdcard/downloadedfile.jpg.jpg");

            if(imgFile.exists()){

                Log.d("path", imgFile.toString());
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inPreferredConfig= Bitmap.Config.ARGB_8888;
                Bitmap bitmap=BitmapFactory.decodeFile(String.valueOf(imgFile), options);
                my_image.setImageBitmap(bitmap);
                // Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());


                //my_image.setImageURI(Uri.fromFile(imgFile));
                //my_image.setImageBitmap(myBitmap);

            }
        }

    }
    public boolean checkPermission() {

        int CallPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);


        return CallPermissionResult == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE

                }, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSION_REQUEST_CODE:


                if (grantResults.length > 0) {

                    boolean WriteStoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;


                    if (WriteStoragePermission ) {

                        Toast.makeText(MainActivity.this,
                                "Permission accepted", Toast.LENGTH_LONG).show();
                        downLoad.setEnabled(true);
//If permission is denied...//

                    } else {
                        Toast.makeText(MainActivity.this,
                                "Permission denied", Toast.LENGTH_LONG).show();

//....disable the Call and Contacts buttons//

                        downLoad.setEnabled(false);

                    }
                    break;
                }
        }
    }
}

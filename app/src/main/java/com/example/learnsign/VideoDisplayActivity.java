package com.example.learnsign;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class VideoDisplayActivity extends AppCompatActivity {

    public static final Map<String, String>  URLMap = new HashMap<>();
    static {
        URLMap.put("buy", "https://www.signingsavvy.com/media/mp4-ld/6/6442.mp4");
        URLMap.put("house", "https://www.signingsavvy.com/media/mp4-ld/23/23234.mp4");
        URLMap.put("fun", "https://www.signingsavvy.com/media/mp4-ld/22/22976.mp4");
        URLMap.put("hope", "https://www.signingsavvy.com/media/mp4-ld/22/22197.mp4");
        URLMap.put("arrive", "https://www.signingsavvy.com/media/mp4-ld/14/14210.mp4");
        URLMap.put("really", "https://www.signingsavvy.com/media/mp4-ld/24/24977.mp4");
        URLMap.put("read", "https://www.signingsavvy.com/media/mp4-ld/7/7042.mp4");
        URLMap.put("lip", "https://www.signingsavvy.com/media/mp4-ld/26/26085.mp4");
        URLMap.put("mouth", "https://www.signingsavvy.com/media/mp4-ld/22/22188.mp4");
        URLMap.put("some", "https://www.signingsavvy.com/media/mp4-ld/23/23931.mp4");
        URLMap.put("communicate", "https://www.signingsavvy.com/media/mp4-ld/22/22897.mp4");
        URLMap.put("write", "https://www.signingsavvy.com/media/mp4-ld/8/8441.mp4");
        URLMap.put("create", "https://www.signingsavvy.com/media/mp4-ld/22/22337.mp4");
        URLMap.put("pretend", "https://www.signingsavvy.com/media/mp4-ld/25/25901.mp4");
        URLMap.put("sister", "https://www.signingsavvy.com/media/mp4-ld/21/21587.mp4");
        URLMap.put("man", "https://www.signingsavvy.com/media/mp4-ld/21/21568.mp4");
        URLMap.put("one", "https://www.signingsavvy.com/media/mp4-ld/11/11001.mp4");
        URLMap.put("drive", "https://www.signingsavvy.com/media/mp4-ld/23/23918.mp4");
        URLMap.put("perfect", "https://www.signingsavvy.com/media/mp4-ld/24/24791.mp4");
        URLMap.put("mother", "https://www.signingsavvy.com/media/mp4-ld/21/21571.mp4");
    }
    String selectedGestureName = "";
    String selectedGestureURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_display);

        Bundle bundle = getIntent().getExtras();
        selectedGestureName = bundle.getString("gestureToBeRecorded");
        selectedGestureURL = URLMap.get(selectedGestureName);

        // Start Downloading the video immediately after this activity loads //
        DownloadTask dw1 = new DownloadTask();
//        Toast.makeText(getApplicationContext(),"Downloading gesture video...", Toast.LENGTH_LONG).show();
        dw1.execute();

        Button btnNext = (Button) findViewById(R.id.button6);
        btnNext.setEnabled(true);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText usernameField = (EditText) findViewById(R.id.inputUsername);
                MainActivity.userName = usernameField.getText().toString();
                Bundle bundle = new Bundle();
                bundle.putString("gestureToBeRecorded", selectedGestureName);

//                bundle.putString("username", username);

                Intent intent = new Intent(VideoDisplayActivity.this, RecordingActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }


    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }


    public class DownloadTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            //Toast.makeText(getApplicationContext(), "Starting to execute Background Task", Toast.LENGTH_LONG).show();
            return;
        }

        @Override
        protected String doInBackground(String... text) {

            File directory = null;
            try {
                File SDCardRoot = Environment.getExternalStorageDirectory(); // location where you want to store
                Log.d("DEBUG", "SD Card Root folder: " + SDCardRoot.getAbsolutePath());
                directory = new File(SDCardRoot, MainActivity.BASE_SD_CARD_DIR_NAME + "/downloaded"); //create directory to keep your downloaded file
                if (!directory.exists()) {
                    directory.mkdirs();
                }
            } catch (Exception e) {
                Log.e("Error", "Error: Problem finding/creating directory." + e);
            }

            String fileName = selectedGestureName + ".mp4"; //Video name that will be stored in device
            try {
                InputStream input = null;
                try {

                    URL url = new URL(selectedGestureURL); // link of the song which you want to download like (http://...)
                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setReadTimeout(95 * 1000);
                    urlConnection.setConnectTimeout(95 * 1000);
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestProperty("X-Environment", "android");


                    urlConnection.setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            /** if it necessarry get url verfication */
                            //return HttpsURLConnection.getDefaultHostnameVerifier().verify("your_domain.com", session);
                            return true;
                        }
                    });
                    urlConnection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());


                    urlConnection.connect();
                    input = urlConnection.getInputStream();

                    if( !VideoDisplayActivity.isExternalStorageAvailable() ) {
                        Log.e("Error", "External Storage not available!");
                    }

                    if( VideoDisplayActivity.isExternalStorageReadOnly() ) {
                        Log.e("Error", "External Storage is READ ONLY!");
                    }

                    File outputFile = new File(directory, fileName);
                    if(!outputFile.getParentFile().exists()) {
                        Log.e("Error", "Folder does not exist." );
                    }
                    outputFile.createNewFile();
                    OutputStream output = new FileOutputStream(outputFile);

                    try {
                        byte[] buffer = new byte[1024];
                        int bytesRead = 0;
                        while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0)
                        {
                            output.write(buffer, 0, bytesRead);

                        }
                        output.close();
                    } catch (Exception exception) {

//                        Toast.makeText(getApplicationContext(),"Error while downloading the video. "+ exception + "", Toast.LENGTH_LONG).show();
                        Log.e("Error", "Error while downloading the video ... " + String.valueOf(exception));
                        publishProgress(String.valueOf(exception));
                        output.close();

                    }
                }
                catch (Exception exception) {
                    Log.e("Error", "Error while downloading the video ... " + String.valueOf(exception));
                    publishProgress(String.valueOf(exception));

                }
                finally {
                    input.close();
                }
            }
            catch (Exception exception)
            {
                publishProgress(String.valueOf(exception));
            }

            return "true";
        }



        @Override
        protected void onProgressUpdate(String... text) {
//            Toast.makeText(getApplicationContext(), "In Background Task" + text[0], Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(String text) {

            File f = null;
            File SDCardRoot = null;
            try {
                SDCardRoot = Environment.getExternalStorageDirectory();
                f = new File(SDCardRoot, MainActivity.BASE_SD_CARD_DIR_NAME + "/downloaded/" + selectedGestureName + ".mp4"); // Directory where the file will be downloaded.
                if (!f.exists()) {
                    Log.e("Error: ", "Video file not available.");
                    return;
                }
            } catch (Exception e) {
                Log.e("Error: ", "Problem finding/creating directory." + e);
            }

            VideoView vv = (VideoView) findViewById(R.id.videoView2);

            /** Keep the video looping **/
            vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                }
            });

            vv.setVideoPath(SDCardRoot+ "/" + MainActivity.BASE_SD_CARD_DIR_NAME + "/downloaded/" + selectedGestureName + ".mp4");
            vv.start();
        }
    }
}

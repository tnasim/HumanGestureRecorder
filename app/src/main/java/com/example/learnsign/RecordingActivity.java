package com.example.learnsign;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RecordingActivity extends AppCompatActivity {

    private static final int VIDEO_CAPTURE = 101;

    String gestureToBeRecorded = "";
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        Bundle bundle = getIntent().getExtras();
        gestureToBeRecorded = bundle.getString("gestureToBeRecorded");

        Button bt1 = (Button) findViewById(R.id.button);

        if(!hasCamera()){
            bt1.setEnabled(false);
            Log.w("DEBUG", "Camera not available.");
            Toast.makeText(getApplicationContext(),"Camera Not Available",Toast.LENGTH_LONG).show();
        }

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording();
            }
        });

        Button bt5 = (Button)findViewById(R.id.button4);
        bt5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadTask up1 = new UploadTask();
                Toast.makeText(getApplicationContext(),"Stating to Upload",Toast.LENGTH_LONG).show();
                up1.execute();
            }
        });
    }

    public class UploadTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String boundary = "*****";
            File SDCardRoot = Environment.getExternalStorageDirectory(); // location where you want to store
            File directory = new File(SDCardRoot, "/my_folder/"); //create directory to keep your downloaded file
            final String fileName = "Action1" + ".mp4";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            try
            {
                OutputStream output = null;
                try{

                    URL url = new URL("http://10.218.107.121/cse535/upload_video.php"); // link of the song which you want to download like (http://...)
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    //urlConnection.setRequestMethod("POST");
                    //urlConnection.setReadTimeout(95 * 1000);
                    //urlConnection.setConnectTimeout(95 * 1000);
                    //urlConnection.setDoInput(true);
                    //urlConnection.setRequestProperty("Accept", "application/json");
                    //urlConnection.setRequestProperty("X-Environment", "android");


                    //urlConnection.setHostnameVerifier(new HostnameVerifier() {
                    //    @Override
                    //    public boolean verify(String hostname, SSLSession session) {
                    /** if it necessarry get url verfication */
                    //return HttpsURLConnection.getDefaultHostnameVerifier().verify("your_domain.com", session);
                    //        return true;
                    //   }
                    //});
                    //urlConnection.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());

                    urlConnection.setDoInput(true); // Allow Inputs
                    urlConnection.setDoOutput(true); // Allow Outputs
                    urlConnection.setUseCaches(false); // Don't use a Cached Copy
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Connection", "Keep-Alive");
                    urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    urlConnection.setRequestProperty("uploaded_file", fileName);

                    //urlConnection.connect();

                    DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());

                    //dos.writeBytes(twoHyphens + boundary + lineEnd);
                    //dos.writeBytes("Content-Disposition: form-data; name="uploaded_file";filename=""
                    //               + fileName + """ + lineEnd);

                    //      dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    //FileInputStream fileInputStream = new FileInputStream(directory+fileName);
                    FileInputStream input = new FileInputStream(new File(directory, fileName));
                    bytesAvailable = input.available();
                    publishProgress(String.valueOf(bytesAvailable));
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = input.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = input.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = input.read(buffer, 0, bufferSize);

                    }

                    dos.flush();
                    dos.close();
                    input.close();
                    //output = urlConnection.getOutputStream();
                    //input = url.openStream();

                    int serverResponseCode = urlConnection.getResponseCode();
                    String serverResponseMessage = urlConnection.getResponseMessage();

                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);

                    if(serverResponseCode == 200){

                        runOnUiThread(new Runnable() {
                            public void run() {

                                String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                        +" http://www.androidexample.com/media/uploads/"
                                        +fileName;

                                //messageText.setText(msg);
                                Toast.makeText(getApplicationContext(), "File Upload Complete.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
                catch (Exception exception)
                {

                    //Toast.makeText(getApplicationContext(), "input exception in catch....."+ exception + "", Toast.LENGTH_LONG).show();
                    publishProgress(String.valueOf(exception));

                }
                finally
                {
                    //output.close();
                }
            }
            catch (Exception exception)
            {
                publishProgress(String.valueOf(exception));
            }

            return null;
        }


        @Override
        protected void onProgressUpdate(String... text) {
            Toast.makeText(getApplicationContext(), "In Background Task " + text[0], Toast.LENGTH_LONG).show();
        }

    }


    public void startRecording() {
        try {
            File SDCardRoot = Environment.getExternalStorageDirectory();

            File directory = new File(SDCardRoot, MainActivity.BASE_SD_CARD_DIR_NAME + "/recorded"); //create directory to keep your downloaded file
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // TODO: increment practice number for each new record.
            int practice_number = 1;
            File mediaFile = new
                    File(directory
                    + "/" + gestureToBeRecorded + "_PRACTICE_"+ MainActivity.practiceCount + "_" + MainActivity.userName + ".mp4");
            MainActivity.practiceCount = MainActivity.practiceCount + 1;

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
            fileUri = Uri.fromFile(mediaFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, VIDEO_CAPTURE);
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(), "Problem in recording: " + e.getMessage() , Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasCamera() {
        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY)){
            return true;
        } else {
            return false;
        }
    }

    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video has been saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}

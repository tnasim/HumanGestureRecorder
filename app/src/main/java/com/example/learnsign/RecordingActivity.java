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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

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

        Button bt5 = (Button)findViewById(R.id.btnUpload);
        bt5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadTask uploadTask = new UploadTask();
                Toast.makeText(getApplicationContext(),"Stating to Upload",Toast.LENGTH_LONG).show();
                uploadTask.execute();
            }
        });
    }

    public class UploadTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String boundary = "*****";
            File SDCardRoot = Environment.getExternalStorageDirectory(); // location where you want to store
            File directory = new File(SDCardRoot, MainActivity.BASE_SD_CARD_DIR_NAME + "/recorded/"); //create directory to keep your downloaded file
            final String fileName = gestureToBeRecorded + "_PRACTICE_"+ (MainActivity.practiceCount-1) + "_" + MainActivity.userName + ".mp4";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            try
            {
                OutputStream output = null;
                try{

                    URL url = new URL("http://10.218.107.121/cse535/upload_video.php"); // link of the upload site (provided by instructor)
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setDoInput(true); // Allow Inputs
                    urlConnection.setDoOutput(true); // Allow Outputs
                    urlConnection.setUseCaches(false); // Don't use a Cached Copy
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Connection", "Keep-Alive");
                    urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    urlConnection.setRequestProperty("uploaded_file", fileName);
//                    urlConnection.setRequestProperty("group_id", "3"); // hard coded group ID
//                    urlConnection.setRequestProperty("id", "1212886322"); // Hard coded ASU ID
//                    urlConnection.setRequestProperty("accept", "1");


                    HashMap<String, String> parameters = new HashMap<>();
                    parameters.put("group_id", "3");
                    parameters.put("id", "1212886322");
                    parameters.put("accept", "1");

                    StringBuilder stringBuilder = new StringBuilder();
                    int i = 0;
                    for (String key : parameters.keySet()) {
                        try {
                            if (i != 0){
                                stringBuilder.append("&");
                            }
                            stringBuilder.append(key).append("=")
                                    .append(URLEncoder.encode(parameters.get(key), "UTF-8"));

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        i++;
                    }
                    String paramsString = stringBuilder.toString();

                    DataOutputStream dos = new DataOutputStream(urlConnection.getOutputStream());
                    dos.writeBytes(paramsString);

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

                    int serverResponseCode = urlConnection.getResponseCode();
                    String serverResponseMessage = urlConnection.getResponseMessage();

                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);

                    if(serverResponseCode == 200){

                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(getApplicationContext(), "File Upload Complete.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    if( serverResponseMessage.equals("success") ){

                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(getApplicationContext(), "File Upload Successful.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {

                                Toast.makeText(getApplicationContext(), "File Upload failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }


                }
                catch (Exception exception)
                {
                    Log.e("Error", "Problem while uploading: " + exception.getMessage());
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
                Log.e("Error", "Problem uploading: " + exception.getMessage());
                publishProgress(String.valueOf(exception));
            }

            return null;
        }


        @Override
        protected void onProgressUpdate(String... text) {
            Log.d("DEBUG", "Upload status: " + text[0]);
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

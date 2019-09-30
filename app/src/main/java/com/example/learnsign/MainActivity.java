package com.example.learnsign;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {



    /**
     * Name of the gesture that will be set here and passed to next screen.
     **/
    private String selectedGestureName = "buy";
    private final String DEFAULT_DROPDOWN_SELECTION = "-- Select a gesture --";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinner = (Spinner) findViewById(R.id.gestures_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gestures_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set default selection
        int nonePosition = adapter.getPosition(DEFAULT_DROPDOWN_SELECTION);
        spinner.setSelection(nonePosition);

        spinner.setOnItemSelectedListener(this);

        Button btnNext = (Button) findViewById(R.id.button5);
        btnNext.setEnabled(false);
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO: We 'might' need to change screen on button click
                //  (rather than on selecting an item from the dropdown list).

            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        selectedGestureName = (String) parent.getItemAtPosition(position);

        if( selectedGestureName.equals(DEFAULT_DROPDOWN_SELECTION) ) {
            // Do nothing
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString("selectedGestureName", selectedGestureName);

        Intent intent = new Intent(MainActivity.this, VideoDisplayActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        return;
    }
}

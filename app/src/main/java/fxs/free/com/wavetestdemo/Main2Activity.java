package fxs.free.com.wavetestdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SurfaceViewL(this));
//        setContentView(new WaveView(this));
    }
}

package fxs.free.com.wavetestdemo;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;

/**
 * @author ll
 */
public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //SurfaceView
        setContentView(new WaveView(this));
        // CustomView
//        setContentView(new WaveView(this));

        HashMap<String,String> map = new HashMap<>(10);
        ContentValues contentValues = new ContentValues(map.keySet().size());

        for (String s : map.keySet()) {
            contentValues.put(s,map.get(s));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.custom_view:
//                break;
//            case R.id.surface_view:
//                startActivity(new Intent(this, MainActivity.class));
//                this.finish();
//                break;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}

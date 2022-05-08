package com.urmobo.filemanager;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout1);

        final Button b1 = findViewById(R.id.b1);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                b1.setText("pressed");
            }
        });

        final ListView listView = findViewById(R.id.listView);
        final TextAdapter textAdapter1 = new TextAdapter();
        listView.setAdapter(textAdapter1);
        List<String> example = new ArrayList<>();

        for (int i = 0; i < 100; i++){
            example.add(String.valueOf(i));
        }
        System.out.println(example);
        textAdapter1.setData(example);

    }

    class TextAdapter extends BaseAdapter{

        private List<String> data = new ArrayList<>();
        public void setData(List<String> data){
            if (data != null){
                this.data.clear();
                if (data.size() > 0){
                    this.data.addAll(data);
                }
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.textItem)));
            }
            ViewHolder holder  = (ViewHolder) convertView.getTag();
            final String item = getItem(position);
            holder.info.setText(item);
            return convertView;
        }

        class ViewHolder{
            TextView info;
            ViewHolder(TextView info){
                this.info = info;
            }
        }
    }

    private static final int REQUEST_PERMISSIONS = 1234;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private final static int PERMISSIONS_COUNT = 2;

    private boolean arePermissionsDenied(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int p = 0;
            while (p < PERMISSIONS_COUNT){
               if (checkSelfPermission(PERMISSIONS[p]) != PackageManager.PERMISSION_GRANTED){
                    return true;
                }
               p++;
            }
        }
        return false;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (arePermissionsDenied()){
            //requestPermissions
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           final String[] permissions, final int[] grantResults){

    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_PERMISSIONS && grantResults.length>0){
        if (arePermissionsDenied()){
            //Need review
            ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
            recreate();
        }
    }

    }
}

package cs.whu.com.downloadexample;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    private DownloadService.DownloadBinder downloadBinder;
    private Button startDownloadBtn;
    private Button pauseDownloadBtn;
    private Button cancelDownloadBtn;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initClickEvents();
        Intent intent = new Intent(MainActivity.this,DownloadService.class);
        startService(intent);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },1);
        }

    }

    public void initView(){
        startDownloadBtn = (Button) findViewById(R.id.start_download_button);
        pauseDownloadBtn = (Button) findViewById(R.id.pause_download_button);
        cancelDownloadBtn = (Button) findViewById(R.id.cancel_download_button);

    }
    public void initClickEvents(){
        startDownloadBtn.setOnClickListener(this);
        pauseDownloadBtn.setOnClickListener(this);
        cancelDownloadBtn.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(MainActivity.this, "拒绝权限无法使用", Toast.LENGTH_SHORT).show();

                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    @Override
    public void onClick(View v) {
        if(downloadBinder == null)
            return;
        switch(v.getId())
        {
            case R.id.start_download_button:
                String url = "https://img.ofo.so/apk/ofo-local-12593.apk";
                downloadBinder.startDownload(url);
                break;
            case R.id.pause_download_button:
                downloadBinder.pauseDownload();
                break;
            case R.id.cancel_download_button:
                downloadBinder.cancelDownload();
                break;
            default:
                break;
        }
    }
}

package cs.whu.com.downloadexample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;

    private DownloadListener downloadListener= new DownloadListener(){

        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1,getNotification("Downloading......",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download success",-1));
            Toast.makeText(DownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();
            openFile();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("Download Failed",-1));
            Toast.makeText(DownloadService.this, "Download Failed", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(DownloadService.this, "Download Paause", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "Download Cancel", Toast.LENGTH_SHORT).show();
        }
    };
    private DownloadBinder mbinder = new DownloadBinder();

    public DownloadService() {
    }

    /**
     * 自动安装apk文件
     */
    private void openFile() {
        // TODO Auto-generated method stub
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        File file = new File(directory+fileName);
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

    public NotificationManager getNotificationManager(){
        return (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * 带有进度条的通知
     * @param title
     * @param progress
     * @return
     */
    public Notification getNotification(String title,int progress)
    {
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        builder.setContentIntent(pendingIntent);
        builder.setContentTitle(title);
        if(progress >= 0)
        {
            builder.setContentText(progress+"%");
            builder.setProgress(100,progress,false);
        }

        return builder.build();

    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return  mbinder;
    }

    class DownloadBinder extends Binder{
        public void startDownload(String url)
        {
            if(downloadTask == null)
            {
                downloadUrl = url;
                downloadTask = new DownloadTask(downloadListener);
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("Downloading.....",0));
                Toast.makeText(DownloadService.this, "正在下载", Toast.LENGTH_SHORT).show();
            }
        }
        public void pauseDownload(){
            if(downloadTask != null )
            {
                downloadTask.pauseDownload();
            }
        }
        public void cancelDownload(){
            if(downloadTask != null)
            {
                downloadTask.cancelDownload();
            }
            //如果当前无下载任务，则直接删除下载好的文件
            else
            {
                 if(downloadUrl != null)
                {
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory+fileName);
                    if (file.exists())
                    {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "取消下载", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}

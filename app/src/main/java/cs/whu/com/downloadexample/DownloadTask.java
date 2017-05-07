package cs.whu.com.downloadexample;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by cxq on 2017/5/7.
 */
public class DownloadTask extends AsyncTask<String,Integer,Integer> {
    private  DownloadListener downloadListener;

    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    public static final int TYPE_PAUSED = 2;
    public static final int TYPE_CANCELED = 3;

    private  boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;

    public DownloadTask(DownloadListener downloadListener){
        this.downloadListener = downloadListener;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if(progress > lastProgress)
        {
            downloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try{
            long downloadLength = 0;
            String downloadUrl = params[0];
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory+fileName);
            //如果文件存在，则读取文件已下载的文件大小
            if(file.exists())
            {
                downloadLength = file.length();
            }
            //获取将要下载文件的总大小
            long contentLength = getContentLength(downloadUrl);
            if(contentLength == 0)
            {
                return TYPE_FAILED;
            }else if(contentLength == downloadLength)
            {
                return TYPE_SUCCESS;
            }
            //进行断点续传
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().addHeader("RANGE","bytes="+downloadLength+"-")
                    .url(downloadUrl).build();
            Response response = okHttpClient.newCall(request).execute();
            if(response != null)
            {
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file,"rw");
                savedFile.seek(downloadLength);
                byte b[] = new byte[1024];
                int total = 0;
                int len = 0;
                while((len = is.read(b)) != -1){
                    if(isCanceled)
                    {
                        return TYPE_CANCELED;
                    }else if (isPaused)
                    {
                        return  TYPE_PAUSED;
                    }else
                    {
                        total += len;
                        savedFile.write(b,0,len);
                        int progress = (int)((downloadLength + total)*100/contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                if(is != null)
                {
                    is.close();
                }
                if(savedFile != null)
                {
                    savedFile.close();
                }
                if(isCanceled && file != null)
                {
                    file.delete();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    /**
     * 获取将要下载文件的总的大小
     * @param downloadUrl
     * @return
     * @throws IOException
     */
    public long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        Response response = okHttpClient.newCall(request).execute();
        if(response != null && response.isSuccessful())
        {
            long contentLength = response.body().contentLength();
            response.body().close();
            return contentLength;
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case TYPE_CANCELED:
                downloadListener.onCanceled();
                break;
            case TYPE_SUCCESS:
                downloadListener.onSuccess();
                break;
            case TYPE_PAUSED:
                downloadListener.onPaused();
                break;
            case TYPE_FAILED:
                downloadListener.onFailed();
                break;
            default:
                break;
        }
    }
    public void pauseDownload()
    {
        isPaused = true;
    }
    public void cancelDownload()
    {
        isCanceled= true;
    }
}

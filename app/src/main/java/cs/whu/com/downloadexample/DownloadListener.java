package cs.whu.com.downloadexample;

/**
 * Created by cxq on 2017/5/7.
 */
public interface DownloadListener {
    void onProgress(int progress);
    void onSuccess();
    void onFailed();
    void onPaused();
    void onCanceled();
}

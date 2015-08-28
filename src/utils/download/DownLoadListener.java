package utils.download;

public interface DownLoadListener {

    void onCancel();

    void onDone(boolean canceled, int error);

    void onPercentUpdate(int percent);
}

# Download
Android Download utils

<u><li>该下载工具类是在<a href="https://github.com/etao-open-source/cube-sdk">etao-open-source/cube-sdk</a>基础上改写。<li>
<li>原工具类中不能下载链接含中文的文件，该工具类做了修改。</li></u>

使用方法（filename, e.g. XXX.doc）：
new Handler().post(new DownloadTask(new DownLoadListener() {
            @Override
            public void onCancel() {
            }

            @Override
            public void onDone(boolean canceled, int error) {
                mHandler.sendEmptyMessage(PROGRESS_HIDE);
                if (error == DownloadTask.RESULT_DOWNLOAD_ERROR) {
                    SuperToastManager.makeText(getActivity(), "下载失败", Toast.LENGTH_LONG).show();
                } else if (error == DownloadTask.RESULT_URL_ERROR) {
                    SuperToastManager.makeText(getActivity(), "下载链接错误", Toast.LENGTH_LONG).show();
                } else if (error == DownloadTask.RESULT_NO_ENOUGH_SPACE) {
                    SuperToastManager.makeText(getActivity(), "存储空间不足", Toast.LENGTH_LONG).show();
                } else {
                    open(mFilepath);
                }
            }

            @Override
            public void onPercentUpdate(final int percent) {
                CLog.i(TAG, "下载进度" + percent);
                mHandler.sendMessage(mHandler.obtainMessage(PROGRESS_UPDATE_PROGRESS,percent,0));
            }
        }, url, filename));

或者：
new Thread(new DownloadTask(new DownLoadListener() {
            @Override
            public void onCancel() {
            }

            @Override
            public void onDone(boolean canceled, int error) {
                mHandler.sendEmptyMessage(PROGRESS_HIDE);
                if (error == DownloadTask.RESULT_DOWNLOAD_ERROR) {
                    SuperToastManager.makeText(getActivity(), "下载失败", Toast.LENGTH_LONG).show();
                } else if (error == DownloadTask.RESULT_URL_ERROR) {
                    SuperToastManager.makeText(getActivity(), "下载链接错误", Toast.LENGTH_LONG).show();
                } else if (error == DownloadTask.RESULT_NO_ENOUGH_SPACE) {
                    SuperToastManager.makeText(getActivity(), "存储空间不足", Toast.LENGTH_LONG).show();
                } else {
                    open(mFilepath);
                }
            }

            @Override
            public void onPercentUpdate(final int percent) {
                CLog.i(TAG, "下载进度" + percent);
                mHandler.sendMessage(mHandler.obtainMessage(PROGRESS_UPDATE_PROGRESS, percent, 0));
            }
        }, url, filename)).start();

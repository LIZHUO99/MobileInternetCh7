# Mobile Internet (android part) Chapter7 Assignment
完成Pro版

## 1. 基本功能

基本功能如下图所示：

1. 点击play按钮播放/pause按钮停止。
2. 拖动进度条可调整视频进度。
3. 将屏幕横置后，视频全屏。

<img src=".\snapshots\fundamental_function.gif" width="300" />

- （new feature）全屏时加入了一个control bar

  <img src=".\snapshots\control_bar.jpg" width="500" />

- （new feature）双击视频可暂停/播放

```java
videoView.setOnClickListener(new View.OnClickListener() {
    private static final long DOUBLE_TIME = 500;
    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastClickTime < DOUBLE_TIME) {
            if (videoView.isPlaying()){
                videoView.pause();
            }
            else{
                videoView.start();
                changeSeekBar = new ChangeSeekBar();
                changeSeekBar.execute();
            }
        }
        lastClickTime = currentTimeMillis;
    }
});
```

- 小优化（把竖屏视频显示在中间）：

<img src=".\snapshots\portrait_fullscreen.jpg" width="500" />

## 2. Pro功能

- 打开系统视频：

<img src=".\snapshots\open.gif" width="300" />

- 点击open按钮打开视频：

<img src=".\snapshots\openButton.gif" width="300" />





## 3. 部分代码说明

- 定义一个AsyncTask来更新seekBar

```java
private class ChangeSeekBar extends AsyncTask<Void, Integer, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
        while(videoView.isPlaying()){
            publishProgress(videoView.getCurrentPosition());
            SystemClock.sleep(100);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        seekBar.setProgress(values[0]);
        curProgress = values[0];
        textCurTime.setText(SIMPLE_DATE_FORMAT.format(new Date(values[0] - TimeZone.getDefault().getRawOffset())));
    }
}
```

- 横屏时设置为全屏

```java
if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
    supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
}
```

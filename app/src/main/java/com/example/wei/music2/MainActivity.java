package com.example.wei.music2;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wei.music2.adapter.SongAdapter;
import com.example.wei.music2.entity.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity:";
    private static final int MY_PERMISSIONS_REQUEST = 1;

    private List<Song> songList = new ArrayList<>();
    private MediaPlayer mediaPlayer;
    private SongAdapter adapter;
    //歌曲列表索引
    private int index = 0;

    private SeekBar seekBar;
    private TextView textView;
    private ImageButton playButton;
    private ImageButton nextButton;

    /*滑动菜单*/
    private DrawerLayout drawerLayout;
    //滑动菜单页面
    private NavigationView navView;
    /*卡片式布局*/
    private RecyclerView recyclerView;

    /*下拉刷新*/
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //标题栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_36dp);
        }

        // 检查是否拥有READ_EXTERNAL_STORAGE权限
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        /*
        *如果应用具有此权限，方法将返回 PackageManager.PERMISSION_GRANTED，
        *如果应用不具有此权限，方法将返回 PackageManager.PERMISSION_DENIED，
        * */
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            //不拥有权限，明确向用户要求权限
            // shouldShowRequestPermissionRationale
            // 方法来判断申请的权限是否需要自己定义请求权限的说明窗口
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
//            } else {
//
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
        } else {
            //拥有权限
            runPlayer();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //活动结束时释放资源
        if ((mediaPlayer != null) && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //对用户授权结果进行处理
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED) {
                    // 权限被批准
                    runPlayer();
                } else {
                    // 未获得权限
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void runPlayer() {
        //绑定控件
        initUI();
        //设置监听器
        initListener();
        //设置songList
        initSongList();
        //初始化mediaPlayer
        initMediaPlayer();
        //UI更新
        updateTime();
    }

    private void initUI() {

        //滑动页面
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //滑动菜单页面
        navView = (NavigationView) findViewById(R.id.nav_view);

        /*滑动布局*/
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        /*下拉刷新*/
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textView = (TextView) findViewById(R.id.song_text);
        playButton = (ImageButton) findViewById(R.id.button_play);
        nextButton = (ImageButton) findViewById(R.id.button_next);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    }

    private void initListener() {
        //滑动菜单页面，点击菜单项时的处理
        navView.setNavigationItemSelectedListener(new NavigationView
                .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                return true;
            }
        });

        //设置刷新提示颜色和刷新监听器
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshSongs();
            }
        });

        //seekBar，设置播放进度
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playButton.setImageResource(R.drawable.ic_play_circle_outline_black_36dp);
                } else {
                    mediaPlayer.start();
                    playButton.setImageResource(R.drawable.ic_pause_black_36dp);
                }
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index++;
                if (index == songList.size()) {
                    index = 0;
                }
                playButton.setImageResource(R.drawable.ic_pause_black_36dp);
                setPlaySong(mediaPlayer, songList.get(index).getData());
            }
        });
        //加载文件完成
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekBar.setMax(mp.getDuration());
                seekBar.setProgress(0);
                textView.setText(songList.get(index).getPlayText());
            }
        });
        //播放完毕
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                index++;
                if (index == songList.size()) {
                    index = 0;
                }
                setPlaySong(mediaPlayer, songList.get(index).getData());
            }
        });
        //加载文件失败

    }

    private void initMediaPlayer() {
        try {
            mediaPlayer.setDataSource(songList.get(index).getData());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSongList() {
        //强制系统更新媒体数据库
        MediaScannerConnection.scanFile(this, new String[]{Environment
                .getExternalStorageDirectory().getAbsolutePath()}, null, null);

        songList = findSongs();
        adapter = new SongAdapter(songList);
        //把歌曲信息填充到滑动列表
        recyclerView.setAdapter(adapter);

        //列表点击处理，需要自己实现
        adapter.setOnItemClickListener(new SongAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                index = position;
                setPlaySong(mediaPlayer, songList.get(index).getData());
                playButton.setImageResource(R.drawable.ic_pause_black_36dp);
                Log.d(TAG, "OnItemClick: " + position);
            }
        });
    }

    private void setPlaySong(MediaPlayer mPlayer, String songPath) {
        try {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer.setDataSource(getBaseContext(), Uri.parse(songPath));
            mPlayer.prepareAsync();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 查询数据库中的所有歌曲信息
    private List<Song> findSongs() {

        List<Song> songList = new ArrayList<>();

        //需要查询的列
        String[] projection = {
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
        };

        //查询数据库
        ContentResolver resolver = getApplication().getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        //遍历游标
        if (cursor != null) {
            while (cursor.moveToNext()) {
                //把查询到的数据封装到实体类，并添加到List集合
                Song song = new Song();
                song.setData(cursor.getString(0));
                song.setDisplay_name(cursor.getString(1));
                song.setSize(cursor.getInt(2));
                song.setTitle(cursor.getString(3));
                song.setArtist(cursor.getString(4));
                song.setDuration(cursor.getInt(5));
                songList.add(song);
            }
            cursor.close();
        }
        return songList;
    }

    //刷新事件处理
    private void refreshSongs() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //重新读取数据库数据，并填充到滑动列表
                        songList = findSongs();
                        adapter.notifyDataSetChanged();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    //菜单栏点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //向上菜单点击处理
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    //隔1秒钟更新播放进度
    private void updateTime() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    //双击结束程序，对点击返回键进行处理
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                    return true;
                } else {
                    System.exit(0);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}

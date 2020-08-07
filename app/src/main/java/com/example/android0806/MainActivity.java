package com.example.android0806;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button btnPlay, btnStop, btnPrev, btnNext;
    SeekBar progress;
    TextView filename;

    //노래 제목들을 저장할 List
    //m의 의미는 인스턴스 변수를 의미
    ArrayList<String> mSongList;
    //현재 재생 중인 노래의 인덱스
    int mIdx;
    //재생 여부를 판달할 변수
    boolean isPlaying;

    //음악 재생기 변수
    MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPlay = findViewById(R.id.btnPlay);
        btnStop = findViewById(R.id.btnStop);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        progress = findViewById(R.id.progress);
        filename = findViewById(R.id.filename);

        mSongList = new ArrayList<>();
        //스레드를 생성해서 노래 목록을 다운로드 받기
        new Thread(){
            public void run(){
                try{
                    String addr = "http://192.168.0.117:8080/song/";
                    //노래 목록 파일 주소 생성
                    URL url = new URL(addr + "list.txt");
                    //연결
                    HttpURLConnection con =
                            (HttpURLConnection)url.openConnection();
                    //문자열 받기 위한 스트림 생성
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    while(true){
                        String line = br.readLine();
                        if(line == null){
                            break;
                        }
                        sb.append(line + "\n");
                    }
                    String data = sb.toString();
                    //문자열을 콤마로 분해
                    String [] songList = data.split(",");
                    for(String song : songList){
                        mSongList.add(addr + song + ".mp3");
                    }

                    //음원 재생기 생성
                    mMediaPlayer = new MediaPlayer();
                    mIdx = 0;
                    mMediaPlayer.setOnCompletionListener(mOnComplete);
                    mMediaPlayer.setOnErrorListener(mOnError);
                    mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleter);
                    progress.setOnSeekBarChangeListener(mOnSeek);
                    //핸들러 호출
                    mProgressHandler.sendEmptyMessageDelayed(
                            0, 1000);

                    //버튼의 이벤트 핸들러
                    btnPlay.setOnClickListener(new Button.OnClickListener(){
                                public void onClick(View view){
                                    if(mMediaPlayer.isPlaying() == false){
                                        mMediaPlayer.start();
                                        btnPlay.setText("pause");
                                    }else{
                                        mMediaPlayer.pause();
                                        btnPlay.setText("play");
                                    }
                                }
                            });
                    btnStop.setOnClickListener(new Button.OnClickListener(){
                                public void onClick(View view){
                                    mMediaPlayer.stop();
                                    btnPlay.setText("Play");
                                    progress.setProgress(0);
                                }
                            });
                    btnPrev.setOnClickListener(
                            new Button.OnClickListener(){
                                public void onClick(View view){
                                    //재생 중인지 여부를 저장
                                    boolean isPlaying =
                                            mMediaPlayer.isPlaying();
                                    //이전으로 이동
                                    mIdx = (mIdx == 0 ? mSongList.size()-1
                                            :mIdx-1);
                                    //플레이어 초기화
                                    mMediaPlayer.reset();
                                    //노래 재생 준비
                                    loadMedia(mIdx);
                                    //이전에 재생 중이면 바로 재생
                                    if(isPlaying){
                                        mMediaPlayer.start();;
                                        btnPlay.setText("Pause");
                                    }
                                }
                            });
                    btnNext.setOnClickListener(
                            new Button.OnClickListener(){
                                public void onClick(View view){
                                    //재생 중인지 여부를 저장
                                    boolean isPlaying =
                                            mMediaPlayer.isPlaying();
                                    //이전으로 이동
                                    mIdx = (mIdx == mSongList.size()-1 ?
                                            0
                                            :mIdx+1);
                                    //플레이어 초기화
                                    mMediaPlayer.reset();
                                    //노래 재생 준비
                                    loadMedia(mIdx);
                                    //이전에 재생 중이면 바로 재생
                                    if(isPlaying){
                                        mMediaPlayer.start();;
                                        btnPlay.setText("Pause");
                                    }
                                }
                            });



                }catch(Exception e){
                    Log.e("다운로드 예외", e.getMessage());
                }
            }
        }.start();
    }

    //내가 만든 클래스가 아닌 클래스의 메소드를 오버라이딩 할 때는
    //상위 클래스의 메소드를 호출하는 것이 좋습니다.
    //안드로이드에서는 추상메소드가 아닌 경우 상위 클래스의 메소드를
    //호출하지 않으면 에러
    //메소드 호출 순서는 리턴이 있거나 종료하는 메소드의 경우는
    //마지막에 호출해야 하고 그렇지 않으면 먼저 호출해야 합니다.
    @Override
    public void onDestroy(){
        if(mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    //인덱스를 받아서 재생 가능한 노래인지 판단하는 메소드
    private void loadMedia(int idx){
        //핸들러에게 전송할 메시지
        Message message = new Message();
        //메시지를 구분할 번호를 저장
        message.what = idx;
        try{
            mMediaPlayer.setDataSource(
                    this, Uri.parse(
                            mSongList.get(idx)));
        }catch(Exception e){
            Log.e("노래 준비 실패", e.getMessage());
            message.obj = false;
        }
        //노래를 바로 재생할 수 있도록 재생 준비
        try{
            mMediaPlayer.prepare();
            message.obj = true;
        }catch(Exception e){
            Log.e("노래 준비 실패", e.getMessage());
            message.obj = false;
        }

        //핸들러 호출
        mMessageHandler.sendMessage(message);

    }

    //MediaPlayer의 이벤트를 처리할 리스너 생성
    //음원 재생이 끝났을 때 호출되는 리스너
    MediaPlayer.OnCompletionListener mOnComplete =
            new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(
                        MediaPlayer mediaPlayer) {
                    //현재 노래 재생이 끝나면 다음 노래 재생
                    mIdx =
                            (mIdx == mSongList.size()-1
                                    ?0:mIdx+1);
                    mMediaPlayer.reset();
                    loadMedia(mIdx);
                    mediaPlayer.start();

                }
            };

    //노래 재생에 실패했을 때 호출되는 리스너
    MediaPlayer.OnErrorListener mOnError =
            new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(
                        MediaPlayer mediaPlayer,
                        int i, int i1) {
                    Toast.makeText(MainActivity.this,
                            "재생 중 에러 발생",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
            };
    //노래 재생 준비가 완료되었을 때 호출되는 리스너
    MediaPlayer.OnSeekCompleteListener mOnSeekCompleter =
            new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mediaPlayer) {
                    if(isPlaying){
                        mMediaPlayer.start();
                    }
                }
            };

    //시크바의 위치가 변경되었을 때 호출되는 리스너
    SeekBar.OnSeekBarChangeListener mOnSeek =
            new SeekBar.OnSeekBarChangeListener() {
                //썸을 눌러서 이동하고 값이 변경된 후에 호출되는 메소드
                @Override
                public void onProgressChanged(
                        SeekBar seekBar, int i, boolean b) {
                    //boolean b 가 사람에 의해서 변경이 된것인지
                    //다른 이유로 변경되었는지 알려주는 변수
                    if(b){
                        mMediaPlayer.seekTo(i);
                    }

                }
                //썸을 처음 눌렀을 때 호출되는 메소드
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if(mMediaPlayer.isPlaying()){
                        isPlaying = mMediaPlayer.isPlaying();
                        mMediaPlayer.pause();
                    }
                }
                //썸에서 손을 뗐을 때 호출되는 메소드
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            };

    //화면 갱신을 위한 핸들러
    Handler mMessageHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message message){
            //넘어온 결과 찾아오기
            boolean result = (Boolean)message.obj;
            String resultMsg = null;
            if(result == true){
                resultMsg = "재생 준비 완료";
                filename.setText(mSongList.get(
                        message.what));
                //재생할 노래의 길이로 seekbar의 길이 설정
                progress.setMax(mMediaPlayer.getDuration());
            }else{
                resultMsg = "재생 준비 실패";
            }
            Toast.makeText(MainActivity.this, resultMsg,
                    Toast.LENGTH_LONG).show();
        }
    };

    //0.2초마다 시크바의 값을 업데이트하는 핸들러
    Handler mProgressHandler = new Handler(
            Looper.getMainLooper()){
        @Override
        public void handleMessage(Message message){
            if(mMediaPlayer == null){
                return;
            }else if(mMediaPlayer.isPlaying()){
                progress.setProgress(
                        mMediaPlayer.getCurrentPosition());
            }
            //0.2초 다시 자기 자신을 호출
            mProgressHandler.sendEmptyMessageDelayed(
                    0,200);
        }
    };

}

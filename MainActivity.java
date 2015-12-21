Enter file contents herepackage com.example.sunku.mp3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private static boolean isPlaying = false;			//재생 여부
    private static boolean isShuffle = false;			//셔플 재생 여부
    private static int playIndex = 0;					//플레이리스트 내에서 재생중인 곡의 순서
    private static MediaPlayer mMediaPlayer = null;		//곡을 재생하기 위한 MediaPlayer 객체
    private static final int NO_REPEAT = 0;				//반복없음
    private static final int REPEAT_ONE = 1;			//한곡반복
    private static final int REPEAT_ALL = 2;			//전체반복
    private static int CURRENT_REPEAT_MODE = -1;		//현재 반복 모드
    private static final String PREFS_NAME = "MusicPlayerSetting";

    private Handler mHandler;
    private Runnable r;
    private SharedPreferences settings;

    ArrayList<SongInfo> arrSongInfo;		//모든 노래 정보를 저장
    ArrayList<SongInfo> arrPlayListInfo;	//플레이리스트 정보를 저장

    MusicAdapter allSongs;
    MusicAdapter playList;

    TabHost mTabHost;
    ListView listView02;	//PLAYLIST 탭메뉴
    ListView listView03;	//ALLSONG 탭메뉴
    ImageView mImg01;
    Button mBtnRepeat;			//반복버튼
    Button mBtnShuffle;			//Shuffle버튼
    Button mBtnBack;			//이전곡
    Button mBtnPlayOrPause;		//재생|정지
    Button mBtnNext;			//다음곡
    Button mBtnVolUp;			//볼륨증가
    Button mBtnVolDown;			//볼륨감소
    TextView mTxtTitle;			//재생중인 곡의 타이틀
    TextView mTxtArtist;		//재생중인 곡의 아티스트명
    TextView mTxt01;			//현재 재생 중인 곡의 위치
    TextView mTxt02;			//재생중인 곡의 총 재생 시간
    TextView mTxtVol;			//볼륨의 크기를 표시
    SeekBar mSeek01;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Util.init(getBaseContext());

        arrSongInfo = new ArrayList<SongInfo>();		//플레이리스트 정보를 저장할 ArrayList
        arrPlayListInfo = new ArrayList<SongInfo>();	//SD Card 내의 모든 노래 정보를 저장할 ArrayList

        mImg01 = (ImageView) findViewById(R.id.imgAlbumArt);
        mBtnRepeat = (Button) findViewById(R.id.btnRepeat);
        mBtnShuffle = (Button) findViewById(R.id.btnShuffle);
        mBtnBack = (Button) findViewById(R.id.btnBack);
        mBtnPlayOrPause = (Button) findViewById(R.id.btnPlayOrPlause);
        mBtnNext = (Button) findViewById(R.id.btnNext);
        mBtnVolUp = (Button) findViewById(R.id.btnVolUp);
        mBtnVolDown = (Button) findViewById(R.id.btnVolDown);
        mTxtTitle = (TextView) findViewById(R.id.txtTitle);
        mTxtArtist = (TextView) findViewById(R.id.txtArtist);
        mTxt01 = (TextView) findViewById(R.id.txtTime01);
        mTxt02 = (TextView) findViewById(R.id.txtTime02);
        mTxtVol = (TextView) findViewById(R.id.txtVol);
        mSeek01 = (SeekBar) findViewById(R.id.seek01);
        listView02 = (ListView) findViewById(R.id.lv02);
        listView03 = (ListView) findViewById(R.id.lv03);

        allSongs = new MusicAdapter(getBaseContext(), arrSongInfo);
        playList = new MusicAdapter(getBaseContext(), 0, arrPlayListInfo);

        listView02.setAdapter(playList);
        listView03.setAdapter(allSongs);
        listView02.setOnItemClickListener(mItemClickListener);
        listView02.setOnItemLongClickListener(mOnItemLongClickListener);
        listView03.setOnItemClickListener(mItemClickListener);

        mBtnRepeat.setOnClickListener(mClickListener);
        mBtnShuffle.setOnClickListener(mClickListener);
        mBtnBack.setOnClickListener(mClickListener);
        mBtnPlayOrPause.setOnClickListener(mClickListener);
        mBtnNext.setOnClickListener(mClickListener);
        mBtnVolUp.setOnClickListener(mClickListener);
        mBtnVolDown.setOnClickListener(mClickListener);

        mTabHost = (TabHost) findViewById(R.id.tabHost);
        mTabHost.setup();

        //탭메뉴 추가
        mTabHost.addTab(mTabHost.newTabSpec("tab01").setIndicator("재생")
                .setContent(R.id.tab01));
        mTabHost.addTab(mTabHost.newTabSpec("tab02").setIndicator("재생 목록")
                .setContent(R.id.tab02));
        mTabHost.addTab(mTabHost.newTabSpec("tab03").setIndicator("전체 목록")
                .setContent(R.id.tab03));

        //PLAY탭 클릭 시
        mTabHost.getTabWidget().getChildAt(0)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mTabHost.setCurrentTab(0);
                    }
                });

        //PLAYLIST탭 클릭 시
        mTabHost.getTabWidget().getChildAt(1)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mTabHost.setCurrentTab(1);
                    }
                });

        //ALLSONG탭 클릭 시
        mTabHost.getTabWidget().getChildAt(2)
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mTabHost.setCurrentTab(2);
                    }
                });

        //SharedPreferences로부터 반복모드, 랜덤재생 여부를 가져온다.
        settings = getSharedPreferences(PREFS_NAME, 0);
        isShuffle = settings.getBoolean("IsShuffle", false);
        CURRENT_REPEAT_MODE = settings.getInt("repeatMode", NO_REPEAT);

        mSeek01.setOnSeekBarChangeListener(mOnSeek);
        onHandler();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //셔플버튼은 셔플모드이면 흰색, 셔플모드가 아니면 검은색
        mBtnShuffle.setTextColor(isShuffle ? Color.WHITE : Color.BLACK);

        //반복모드에 따라 버튼의 텍스트를 변경
        switch (CURRENT_REPEAT_MODE) {
            case NO_REPEAT:
                mBtnRepeat.setText("반복없음");
                break;
            case REPEAT_ONE:
                mBtnRepeat.setText("한곡반복");
                break;
            case REPEAT_ALL:
                mBtnRepeat.setText("전체반복");
                break;
            default:
                break;
        }

        //현재 볼륨을 출력
        mTxtVol.setText(String.valueOf(Util.getVol()));

        if(arrPlayListInfo.size()>0)
            setMusicInfo();	//앨범아트 이미지, 노래 제목, 아티스트 명 출력

        if (mMediaPlayer != null) {

            //현재 재생 상태에 따라 버튼의 택스트를 변경
            mBtnPlayOrPause.setText(mMediaPlayer.isPlaying() ? "일시정지" : "재생");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //반복모드와, 셔플재생 여부를 SharedPreferences에 저장한다.
        settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("IsShuffle", isShuffle);
        editor.putInt("repeatMode", CURRENT_REPEAT_MODE);
        editor.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(r);	//Handler 종료
        Util.destroy();					//어플에서 음소거상태일 경우 해제
        stopMusic();					//음악을 정지시킨다.
    }

    //음악 재생 메소드
    public void playMusic(Uri uri) {
        try {
            stopMusic();
            mBtnPlayOrPause.setText("재생");
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            if (CURRENT_REPEAT_MODE == REPEAT_ONE) {
                setLooping(true);	//반복 재생
            }
            isPlaying = true;
            mMediaPlayer.setOnCompletionListener(mOnComplete);
            setMusicInfo();
            mBtnPlayOrPause.setText("일시정지");

        } catch (IOException e) {
        } catch (IllegalStateException e) {
        }
    }

    //음악 정보 출력
    public void setMusicInfo() {
        Bitmap albumArt;
        try {
            albumArt = Util.getArtworkQuick(getBaseContext(),
                    Integer.parseInt((arrPlayListInfo.get(playIndex).albumID)),
                    250, 250);
            mImg01.setImageBitmap(albumArt); // 앨범아트
            mTxtTitle.setText(arrPlayListInfo.get(playIndex).musicTitle); // 타이틀
            mTxtArtist.setText(arrPlayListInfo.get(playIndex).artist); // 아티스트명
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            playIndex = 0;
        } catch (IndexOutOfBoundsException e) {
            playIndex = 0;
        }
    }

    //음악 정지
    public static void stopMusic() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            isPlaying = false;
        }

    }

    //다음 음악 재생
    public void nextMusic() {

        // 랜덤 재생이면
        if (isShuffle) {

            // 랜덤 재생
            playShuffle();
            return;
        }

        playIndex++;

        // 다음곡이 있으면
        if (playIndex < arrPlayListInfo.size()) {

            // 곡 재생
            playMusic(arrPlayListInfo.get(playIndex).uri);
            return;
        }

        // 다음곡이 없으면
        playIndex--;

        // 반복 설정이 전체 반복이면
        if (CURRENT_REPEAT_MODE == REPEAT_ALL) {
            playIndex = 0;

            playMusic(arrPlayListInfo.get(playIndex).uri);
            return;
        }

        // 플레이리스트 내 모든 곡 재생 완료시 다음곡이 없다는 메시지
        viewMsg("다음곡이 없습니다.");

        if (mMediaPlayer != null && mMediaPlayer.isPlaying() == false) {
            mBtnPlayOrPause.setText("재생");
            isPlaying = false;
            mMediaPlayer.seekTo(0);

        }
    }

    //이전 곡 재생
    public void prevMusic() {

        // 램덤 재생이면
        if (isShuffle) {
            playShuffle();
            return;
        }

        playIndex--;

        if (arrPlayListInfo.size() > 0 && playIndex >= 0) {
            playMusic(arrPlayListInfo.get(playIndex).uri);
            return;
        }

        // 이전곡이 있으면
        playIndex++;
        viewMsg("이전곡이 없습니다.");
    }

    public static void setLooping(boolean isLooping) {

        // MediaPlayer의 객체에 Looping 설정을 한다.
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(isLooping); // 반복을 활성화 또는 비활성화 합니다.
        }
    }

    //랜덤 재생
    public void playShuffle() {
        int num = (int) (Math.random() * arrPlayListInfo.size());
        playIndex = num;
        playMusic(arrPlayListInfo.get(playIndex).uri);
    }

    public int playPause() {

        // MediaPlayer의 객체에 Play, Pause 설정을 한다.
        if (mMediaPlayer != null) {

            // 재생 중이 아니라면
            if (!mMediaPlayer.isPlaying()) {

                // 파일을 재생합니다.
                mMediaPlayer.start();

                viewMsg("재생");
                mBtnPlayOrPause.setText("일시정지");
                return 0;

                // 재생 중이라면
            } else {

                // 일시정지합니다.
                mMediaPlayer.pause();

                // 일시정지 메시지
                viewMsg("일시정지");
                mBtnPlayOrPause.setText("재생");
                return 1;
            }
        } else {
            if (arrPlayListInfo.size() > 0)
                playMusic(arrPlayListInfo.get(playIndex).uri);
            else
                viewMsg("재생할 곡이 없습니다.");
            return -1;
        }
    }

    //음악 재생 위치에 맞춰 Seek바를 이동시킨다.
    private void onHandler() {
        mHandler = new Handler();
        r = new Runnable() {

            @Override
            public void run() {
                mHandler.postDelayed(r, 500);
                if (mMediaPlayer == null) {
                    return;
                }

                if (mMediaPlayer.isPlaying()) {
                    mSeek01.setMax(mMediaPlayer.getDuration());
                    mSeek01.setProgress(mMediaPlayer.getCurrentPosition());
                }

                mTxt01.setText(Util.milliSecToTime(String.valueOf(mMediaPlayer
                        .getCurrentPosition())));

                try {
                    if (arrPlayListInfo != null && arrPlayListInfo.size() > 0)
                        mTxt02.setText("/"
                                + arrPlayListInfo.get(playIndex).playTime);
                } catch (ArrayIndexOutOfBoundsException e) {
                } catch (IndexOutOfBoundsException e){
                }

            }
        };

        mHandler.postDelayed(r, 500);
    }

    SeekBar.OnSeekBarChangeListener mOnSeek = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (isPlaying)
                mMediaPlayer.start();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

            if (mMediaPlayer != null) {
                if (isPlaying)
                    mMediaPlayer.pause();
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {

            // SeekBar의 위치에 맞게 MediaPlayer의 재생 위치를 변경시킨다.
            if (mMediaPlayer != null && fromUser)
                mMediaPlayer.seekTo(progress);
        }
    };

    // 재생 완료 리스너
    MediaPlayer.OnCompletionListener mOnComplete = new MediaPlayer.OnCompletionListener() {

        public void onCompletion(MediaPlayer arg0) {

            // 다음곡 재생
            nextMusic();
        }
    };

    // 버튼 클릭 리스너
    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btnRepeat: // 반복 재생 버튼 클릭 시
                    CURRENT_REPEAT_MODE++;
                    if (CURRENT_REPEAT_MODE > REPEAT_ALL)
                        CURRENT_REPEAT_MODE = 0;

                    switch (CURRENT_REPEAT_MODE) {
                        case NO_REPEAT:
                            mBtnRepeat.setText("반복없음");
                            break;
                        case REPEAT_ONE:
                            mBtnRepeat.setText("한곡반복");
                            setLooping(true);
                            break;
                        case REPEAT_ALL:
                            mBtnRepeat.setText("전체반복");
                            setLooping(false);
                            break;
                        default:
                            break;
                    }
                    break;

                case R.id.btnShuffle:
                    isShuffle = !isShuffle;
                    mBtnShuffle.setTextColor(isShuffle ? Color.WHITE : Color.BLACK);
                    break;
                case R.id.btnBack: // 이전 곡 재생 버튼 클릭 시
                    prevMusic();
                    break;
                case R.id.btnPlayOrPlause: // 재생|일시정지 버튼
                    playPause();
                    break;
                case R.id.btnNext: // 다음 곡 재생 버튼 클릭 시
                    nextMusic();
                    break;
                case R.id.btnVolDown:
                    mTxtVol.setText(String.valueOf(Util.setVolDown()));	//볼륨 감소
                    break;
                case R.id.btnVolUp:
                    mTxtVol.setText(String.valueOf(Util.setVolUp()));	//볼륨 증가
                    break;
            }
        }
    };

    //리스트아이템 클릭시
    AdapterView.OnItemClickListener mItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            switch (mTabHost.getCurrentTab()) {

                case 0:
                    break;
                case 1:
                    playIndex = arg2;
                    playMusic(arrPlayListInfo.get(playIndex).uri);
                    mTabHost.setCurrentTab(0);
                    break;
                case 2:
                    viewMsg(playListAdd(arg2) ? "곡이 추가 됐습니다." : "");
                    break;
                default:
                    break;
            }
        }
    };

    //리스트 아이템 롱클릭 시
    OnItemLongClickListener mOnItemLongClickListener = new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                       final int arg2, long arg3) {

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("삭제할 범위를 지정하세요")
                    .setPositiveButton("선택 삭제",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    playListDel(arg2);
                                    viewMsg("곡이 삭제됐습니다.");
                                }
                            })
                    .setNegativeButton("전체 삭제",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    playListDelAll();
                                    viewMsg("곡이 삭제됐습니다.");
                                }
                            }).show();
            return true;
        }
    };

    //플레이리스트 내의 i번째 곡 정보 삭제
    public boolean playListAdd(int i) {

        ContentResolver resolver = getContentResolver();

        // 플레이리스트에 추가
        Util.addToPlayList(resolver,
                Integer.parseInt(arrSongInfo.get(i).musicID),
                Util.MY_PLAYLIST_ID);

        Util.getPlayListInfos(getBaseContext(), arrPlayListInfo);
        playList.notifyDataSetChanged();

        return true;

    }

    //플레이리스트 내 모든 곡 삭제
    public boolean playListDel(int i) {
        ContentResolver resolver = getContentResolver();

        Util.removeFromPlaylist(resolver,
                Integer.parseInt(arrPlayListInfo.get(i).musicID),
                Util.MY_PLAYLIST_ID);

        Util.getPlayListInfos(getBaseContext(), arrPlayListInfo);
        playList.notifyDataSetChanged();

        return true;
    }

    public boolean playListDelAll() {
        ContentResolver resolver = getContentResolver();

        for (int j = 0; j < arrPlayListInfo.size(); j++) {
            Util.removeFromPlaylist(resolver,
                    Integer.parseInt(arrPlayListInfo.get(j).musicID),
                    Util.MY_PLAYLIST_ID);
        }

        Util.getPlayListInfos(getBaseContext(), arrPlayListInfo);
        playList.notifyDataSetChanged();

        return true;
    }

    public void viewMsg(String str) {
        Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();
    }

}

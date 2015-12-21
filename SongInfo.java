Enter file contents herepackage com.example.sunku.mp3;

/**
 * Created by sunku on 2015-12-05.
 */

import android.net.Uri;
import android.provider.MediaStore;

public class SongInfo {

    Uri uri;			//해당 곡의 uri
    String musicID;		//음악 ID
    String albumID;		//앨범아트 ID
    String musicTitle;	//타이틀
    String artist;		//아티스트명
    String playTime;	//총 재생시간
    String audioID;

    public SongInfo(String musicID, String albumID, String musicTitle,
                    String artist, String playTime) {

        this.musicID = musicID;
        this.albumID = albumID;
        this.musicTitle = musicTitle;
        this.artist = artist;
        this.playTime = playTime;

    }

    public SongInfo(String musicID, String albumID, String musicTitle,
                    String artist, String playTime, String audioID) {

        this.musicID = musicID;
        this.albumID = albumID;
        this.musicTitle = musicTitle;
        this.artist = artist;
        this.playTime = playTime;
        this.audioID = audioID;
        uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                "" + audioID);
    }

}

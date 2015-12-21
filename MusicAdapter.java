Enter file contents herepackage com.example.sunku.mp3;

/**
 * Created by sunku on 2015-12-05.
 */
import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//플레이리스트 탭과 모든노래 탭에 적용하기 위한 Adapter 클래스
public class MusicAdapter extends BaseAdapter {

    private ArrayList<SongInfo> arrList;
    LayoutInflater mInflater;

    static final String[] STAR = { "*" };

    private Context mContext;

    MusicAdapter(Context c, ArrayList<SongInfo> arr) {

        mContext = c;

        arrList = arr;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 모든노래 정보를 불러와 arrList에 저장한다.
        getAllSongsInfo();

    }

    MusicAdapter(Context c, int a, ArrayList<SongInfo> arr) {

        mContext = c;
        arrList = arr;
        mInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //플레이르스트 정보를 불러와 arrList에 저장한다.
        Util.getPlayListInfos(mContext, arrList);

    }

    public int getCount() {
        return arrList.size();
    }

    public Object getItem(int position) {
        return arrList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getMusicID(int position) {
        return Integer.parseInt(arrList.get(position).musicID);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.song_item, null);
            holder.text1 = (TextView) convertView.findViewById(R.id.singer);
            holder.text2 = (TextView) convertView.findViewById(R.id.title);
            holder.text3 = (TextView) convertView.findViewById(R.id.playTime);
            holder.image1 = (ImageView) convertView.findViewById(R.id.albumart);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Bitmap albumArt = Util.getArtworkQuick(mContext,
                Integer.parseInt((arrList.get(position).albumID)), 50, 50);

        holder.image1.setImageBitmap(albumArt);
        holder.text1.setText(arrList.get(position).artist);
        holder.text2.setText(arrList.get(position).musicTitle);
        holder.text3.setText(arrList.get(position).playTime);

        return convertView;

    }

    private void getAllSongsInfo() {

        String[] proj = { MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.MIME_TYPE, };

        // mime_type을 audio/mpeg으로 가져온다. null이면 wma파일까지 모두 가져온다.
        Cursor musicCursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj,
                "mime_type = '" + "audio/mpeg'", null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {

            arrList.clear();

            String musicID;
            String albumID;
            String musicTitle;
            String artist;
            String playTime;

            int musicIDCol = musicCursor
                    .getColumnIndex(MediaStore.Audio.Media._ID);
            int albumIDCol = musicCursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int musicTitleCol = musicCursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE);
            int singerCol = musicCursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int playTimeCol = musicCursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                musicID = musicCursor.getString(musicIDCol);
                albumID = musicCursor.getString(albumIDCol);
                musicTitle = musicCursor.getString(musicTitleCol);
                artist = musicCursor.getString(singerCol);
                playTime = musicCursor.getString(playTimeCol);

                SongInfo songInfo = new SongInfo(musicID, albumID, musicTitle,
                        artist, Util.milliSecToTime(playTime));

                arrList.add(songInfo);

            } while (musicCursor.moveToNext());

        }
        musicCursor.close();
        return;
    }

    class ViewHolder {
        TextView text1;
        TextView text2;
        TextView text3;
        ImageView image1;
    }

}

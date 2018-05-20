package com.example.wei.music2.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wei.music2.R;
import com.example.wei.music2.entity.Song;

import java.util.List;

/**
 * Created by WEI on 2018/5/15.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> implements View.OnClickListener{

    private Context context;

    private List<Song> songList;

    private OnItemClickListener mOnItemClickListener = null;

    public static interface OnItemClickListener{
        void OnItemClick(View view ,int position);
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imageView;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            imageView = (ImageView) itemView.findViewById(R.id.song_image);
            textView = (TextView) itemView.findViewById(R.id.song_name);
        }
    }

    public SongAdapter(List<Song> songList) {
        this.songList = songList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.song_item, parent, false);
        //将创建的View注册点击事件
        view.setOnClickListener(this);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //将position保存在itemView的Tag中，以便点击时进行获取
        holder.itemView.setTag(position);

        Song song = songList.get(position);
        holder.textView.setText(song.getTitle());
        Glide.with(context).load(R.drawable.ic_music_note_black_36dp).into(holder.imageView);
    }

    @Override
    public void onClick(View v) {
        if(mOnItemClickListener!=null){
            //注意这里使用getTag方法获取position
            mOnItemClickListener.OnItemClick(v,(int)v.getTag());
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }
    //获取数据的数量
    @Override
    public int getItemCount() {
        return songList.size();
    }

}

package com.kimiwakirei.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class SongItemAdapter extends RecyclerView.Adapter<SongItemAdapter.SongItemViewHolder> {
    public ArrayList<Song> mSongList;
    private Context mContext;
    private onItemClickListener mListener;

    public interface onItemClickListener{
        void onItemClick(int position);
        void onDeleteClick(int position);

    }

    public void onItemClickListener(onItemClickListener listener){
        mListener = listener;
    }

    public static class SongItemViewHolder extends RecyclerView.ViewHolder{

        public ImageView mAlbumArt, mDeleteSong, mFavoriteSong;
        public TextView mSongName, mArtistName;


        public SongItemViewHolder(@NonNull View itemView, final onItemClickListener listener) {
            super(itemView);
            mAlbumArt = itemView.findViewById(R.id.albumArtRV);
            mSongName = itemView.findViewById(R.id.songNameRV);
            mArtistName = itemView.findViewById(R.id.artistNameRV);
            mDeleteSong = itemView.findViewById(R.id.deleteSongRV);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
            mDeleteSong.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });


        }
    }

    public SongItemAdapter(ArrayList<Song> songItemList, Context mContext){
        mSongList = songItemList;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public SongItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.template_item, parent, false);
        SongItemViewHolder songItemViewHolder = new SongItemViewHolder(v, mListener);
        return songItemViewHolder;
    }

    @Override
    public void onBindViewHolder(SongItemViewHolder holder, int position) {
        Song currentSong = mSongList.get(position);

        holder.mSongName.setText(currentSong.getTitle());
        holder.mArtistName.setText(currentSong.getArtist());

        Glide.with(mContext).asBitmap().
                load(currentSong.getCoverArt()).placeholder(R.drawable.spinner).into(holder.mAlbumArt);

    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }
}

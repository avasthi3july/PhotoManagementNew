package com.photo.management.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.photo.management.R;
import com.photo.management.dao.Photo;
import com.photo.management.util.Util;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kavasthi on 12/7/2016.
 */

public class MyPictureAdapter extends RecyclerView.Adapter<MyPictureAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<Photo> photoList;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_row, parent, false);

        return new ViewHolder(itemView);
    }

    public MyPictureAdapter(Context mContext, ArrayList<Photo> photoList1) {
        this.mContext = mContext;
        this.photoList = photoList1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Photo mPhoto = photoList.get(position);
        /*Typeface face = Typeface.createFromAsset(mContext.getAssets(),
                "fonts/HelveticaNeue Light.ttf");
        holder.tagName.setTypeface(face);*/
        holder.tagName.setText(mPhoto.getTagName());
        holder.time.setText(mPhoto.getDate());
        if (!mPhoto.isDownload()) {
            File imgFile = new File(mPhoto.getPicPath());
            try {
                Glide.with(mContext).load(imgFile).into(holder.tagImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else Glide.with(mContext).load(mPhoto.getPicPath()).into(holder.tagImage);

    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public void setFilter(ArrayList<Photo> photoModels) {
        photoList = new ArrayList<>();
        photoList.addAll(photoModels);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tagName, time;
        private ImageView tagImage;

        public ViewHolder(View itemView) {
            super(itemView);
            tagName = (TextView) itemView.findViewById(R.id.tagName);
            time = (TextView) itemView.findViewById(R.id.time);
            tagImage = (ImageView) itemView.findViewById(R.id.imageV);
        }
    }

    public void setFilter(List<Photo> countryModels) {
        photoList = new ArrayList<>();
        photoList.addAll(countryModels);
        notifyDataSetChanged();
    }
}

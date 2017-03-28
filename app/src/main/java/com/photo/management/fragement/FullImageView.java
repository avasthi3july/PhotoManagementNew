package com.photo.management.fragement;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.photo.management.R;
import com.photo.management.util.Util;

/**
 * Created by kavasthi on 1/23/2017.
 */

public class FullImageView extends Fragment {
    private ImageView imageView;
    private String picPath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.full_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        imageView = (ImageView) view.findViewById(R.id.image_full_view);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            picPath = bundle.getString("picPath");
        }
        try {
            Glide.with(getActivity()).load(picPath)
                    .into(imageView);
            // selectedView.setImageBitmap(com.photo.management.util.Util.getBitMap(imgFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

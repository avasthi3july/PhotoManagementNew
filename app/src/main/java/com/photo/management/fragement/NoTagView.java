package com.photo.management.fragement;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.photo.management.R;
import com.photo.management.activity.MainActivity;

/**
 * Created by kavasthi on 12/6/2016.
 */

public class NoTagView extends Fragment implements View.OnClickListener {
    private Button importPic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.no_tag_view, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initLayout(view);

    }

    private void initLayout(View view) {
        ((MainActivity) getActivity()).setHeaderName("My Pictures");
        importPic = (Button) view.findViewById(R.id.importPic);
        importPic.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        ((MainActivity) getActivity()).importGalleryImage();
    }
}

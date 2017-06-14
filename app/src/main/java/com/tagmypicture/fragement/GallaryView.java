package com.tagmypicture.fragement;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.tagmypicture.activity.MainActivity;
import com.tagmypicture.adapter.GalleryAdapter;
import com.tagmypicture.R;

import java.util.ArrayList;

/**
 * Created by kavasthi on 12/6/2016.
 */

public class GallaryView extends Fragment implements View.OnClickListener {
    private ArrayList<String> images;
    private TextView unTagPic;
    private GridView gallery;

    @Override
    public void onClick(View v) {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gallery_activity, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initLayout(view);

    }

    private void initLayout(View view) {
        ((MainActivity) getActivity()).setHeaderName("My Pictures");
        gallery = (GridView) view.findViewById(R.id.galleryGridView);
        images = new ArrayList<>();
        if (getAllShownImagesPath().size() > 0) {
            images.addAll(getAllShownImagesPath());
        } else {
            images.add("http://tagmypicture.com/appdefaultimage/icon.png");
        }
        unTagPic = (TextView) view.findViewById(R.id.unTagPic);
        unTagPic.setText("Untagged Pictures in Device" + "   (" + images.size() + ")");
        gallery.setAdapter(new GalleryAdapter(getActivity(), images));

        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                if (null != images && !images.isEmpty()) {
                    TagPictureView mTagPictureView = new TagPictureView();
                    Bundle mBundle = new Bundle();
                    mBundle.putString("picPath", images.get(position));
                    mTagPictureView.setArguments(mBundle);
                    ((MainActivity) getActivity()).addFragementView(mTagPictureView);
                }


            }
        });
    }

    private ArrayList<String> getAllShownImagesPath() {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = getActivity().getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

}

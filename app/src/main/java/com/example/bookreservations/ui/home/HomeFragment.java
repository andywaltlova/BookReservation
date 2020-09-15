package com.example.bookreservations.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.bookreservations.R;
import com.example.bookreservations.utils.JsonAPIparser;
import com.example.bookreservations.utils.Timer;


public class HomeFragment extends Fragment {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        ImageView my_image = root.findViewById(R.id.FirstImage);
        my_image.setImageResource(R.drawable.logo_knihovna);


        // Should also set view and buttons that timer uses
        RequestQueue mQueue = Volley.newRequestQueue(requireActivity());

        final JsonAPIparser parser = new JsonAPIparser(getActivity(), root, mQueue, "password");
        new Timer(root, parser);

        if (savedInstanceState == null) {
            parser.jsonParse();
        }

        Button refresh_button = root.findViewById(R.id.refresh_button);
        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parser.jsonParse();
            }
        });

        //TODO: Save fragment state

        return root;
    }


}
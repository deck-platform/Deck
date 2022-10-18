package com.bupt.kdapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bupt.kdapp.db.AppDatabase;
import com.bupt.kdapp.db.StartInfo;
import com.bupt.deck.data.GlobalData;
import com.bupt.deck.websocket.WebSocketConn;


public class FirstFragment extends Fragment {
    String TAG = "FirstFragment-Deck";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);

                Log.i(TAG, "onClick: Disconnect WebSocket connection");
                WebSocketConn.getInstance(getContext()).disconnect();

                // Add startInfo into start_info_db
                GlobalData.executorService.submit(() -> {
                    AppDatabase.getInstance(getContext()).startInfoDao().insertAll(new StartInfo());
                });
            }
        });
    }
}
package androidkejar.app.mymovielist.view.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidkejar.app.mymovielist.R;

/**
 * Created by alodokter-it on 10/05/17 -- NowPlayingFragment.
 */

public class NowPlayingFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_now_playing, container, false);
    }
}
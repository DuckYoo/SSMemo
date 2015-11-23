package app.ssm.duck.duckapp.ui.fragments;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import app.ssm.duck.duckapp.Adapter.PhotoAdapter;
import app.ssm.duck.duckapp.R;
import app.ssm.duck.duckapp.api.SampleClient;
import app.ssm.duck.duckapp.api.model.Photo;
import app.ssm.duck.duckapp.api.model.SearchResult;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link app.ssm.duck.duckapp.ui.fragments.HorizontalRecyclerViewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link app.ssm.duck.duckapp.ui.fragments.HorizontalRecyclerViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HorizontalRecyclerViewFragment extends CardViewFragment {

    public static final String TAG = "HorizontalRecyclerViewFragment";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RecyclerViewFragment.
     */
    public static HorizontalRecyclerViewFragment newInstance() {
        HorizontalRecyclerViewFragment fragment = new HorizontalRecyclerViewFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        ButterKnife.inject(this, rootView);


        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        mList.setLayoutManager(mLinearLayoutManager);
        mList.setItemAnimator(new DefaultItemAnimator());


        mPhotos = new PhotoAdapter(new ArrayList<Photo>(), R.layout.row_photo_card_horizontal, getActivity(), this);

        mList.setAdapter(mPhotos);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SampleClient.getFlickrApiClient(getActivity()).getSearchResults("black", new Callback<SearchResult>() {
            @Override
            public void success(SearchResult searchResult, Response response) {
                mPhotos.getPhotos().addAll(searchResult.getPhotos().getPhoto());
                mPhotos.notifyDataSetChanged();
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
}

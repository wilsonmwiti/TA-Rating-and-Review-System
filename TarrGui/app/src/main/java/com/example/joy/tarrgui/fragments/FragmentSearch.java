package com.example.joy.tarrgui.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joy.tarr.User;
import com.example.joy.tarr.UserClientApi;
import com.example.joy.tarrgui.adapters.AdapterMovies;
import com.example.joy.tarrgui.adapters.AdapterUser;
import com.example.joy.tarrgui.callbacks.SearchUserListner;
import com.example.joy.tarrgui.extras.MovieSorter;
import com.example.joy.tarrgui.extras.SortListener;
import com.example.joy.tarrgui.logging.L;
import com.example.joy.tarrgui.R;
import com.example.joy.tarrgui.pojo.Movie;

import java.util.ArrayList;
import java.util.Collection;

import retrofit.RestAdapter;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link FragmentSearch#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSearch extends Fragment implements SortListener, SearchUserListner
        ,SwipeRefreshLayout.OnRefreshListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final String URL = "http://192.168.1.16:8080";
    String str_search;
    Collection<User> searchName, searchSkills, searchCourse;
    private UserClientApi userServiceSearch = new RestAdapter.Builder()
            .setEndpoint(URL).setLogLevel(RestAdapter.LogLevel.FULL).build()
            .create(UserClientApi.class);

    private ImageButton mSearch;
    private EditText txtSearch;
    private TextView txtSearchResult;
    private RelativeLayout userTap;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    //////////////////////////

    private static final String STATE_MOVIES = "state_movies";
    //the arraylist containing our list of box office his
    private ArrayList<User> mListUser = new ArrayList<User>();
    //the adapter responsible for displaying our movies within a RecyclerView
    private AdapterUser mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    //the recyclerview containing showing all our movies
    private RecyclerView mRecyclerMovies;
    //the TextView containing error messages generated by Volley
    private TextView mTextError;
    //the sorter responsible for sorting our movie results based on choice made by the user in the FAB
    private MovieSorter mSorter = new MovieSorter();




    /////////////////////////

    public FragmentSearch() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentSearch.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentSearch newInstance(String param1, String param2) {
        FragmentSearch fragment = new FragmentSearch();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }



    }
    public void onSortByName(){
        L.t(getActivity(), "sort name search");
    }

    @Override
    public void onSortByDate() {

    }

    @Override
    public void onSortByRating() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View view = inflater.inflate(R.layout.fragment_search, container, false);
        mTextError = (TextView) view.findViewById(R.id.textVolleyError);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeUser);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerMovies = (RecyclerView) view.findViewById(R.id.listUser);
        //set the layout manager before trying to display data
        mRecyclerMovies.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new AdapterUser(getActivity());
        mRecyclerMovies.setAdapter(mAdapter);


        mSearch = (ImageButton) view.findViewById(R.id.imageButton);
        txtSearch = (EditText) view.findViewById(R.id.editText);
        //txtSearchResult = (TextView) view.findViewById(R.id.textView3);

        if(!txtSearch.getText().toString().isEmpty())
            new SearchTask(getActivity(),mRecyclerMovies).execute("");


        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListUser.clear();
                onBoxOfficeMoviesLoaded(mListUser);
                System.out.println("JMD click"+txtSearch.getText().toString());
                /*InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);*/
                //txtSearchResult.setText(txtSearch.getText().toString());

                if(!txtSearch.getText().toString().isEmpty()) {
                    new SearchTask(getActivity(), mRecyclerMovies).execute("");
                    mAdapter.setMovies(mListUser);
                    userTap = (RelativeLayout) view.findViewById(R.id.usersearch);
                }
            }
        });
        return view;
        //return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onStart(){
        super.onStart();
        if(!txtSearch.getText().toString().isEmpty())
            new SearchTask(getActivity(),mRecyclerMovies).execute("");

    }

    /*@Override
    public void onViewStateRestored(Bundle b){
        super.onViewStateRestored(b);
        new SearchTask(getActivity(),mRecyclerMovies).execute("");

    }*/

    @Override
    public void onRefresh() {

        if(!txtSearch.getText().toString().isEmpty())
        new SearchTask(getActivity(),mRecyclerMovies).execute("");
    }

    @Override
    public void onBoxOfficeMoviesLoaded(ArrayList<User> listMovies) {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mAdapter.setMovies(listMovies);
    }

    class SearchTask extends AsyncTask<String,String,Boolean>{
    RecyclerView tv;
    Activity mContext;

    public SearchTask(Activity cont, RecyclerView temp){
        this.tv= temp;
        this.mContext=cont;
    }
    @Override
    protected Boolean doInBackground(String... params) {
        searchSkills = userServiceSearch.findBySkillsContainingIgnoreCase(txtSearch.getText().toString());
        searchName = userServiceSearch.findByNameContainingIgnoreCase(txtSearch.getText().toString());
        searchCourse = userServiceSearch.findByCourseContainingIgnoreCase(txtSearch.getText().toString());

        if (searchSkills.isEmpty()&& searchName.isEmpty() && searchCourse.isEmpty()) {
            return false;
        } else {

            return true;
        }
    }

    protected void onPostExecute(Boolean b)
    {

        if(b){
            if(!searchSkills.isEmpty()){
                if(!mListUser.containsAll(searchSkills))
                mListUser.addAll(searchSkills);

                /*for(User iter:searchSkills){
                    iter.getEmailId();
                    txtSearchResult.setText(txtSearchResult.getText()+" "+iter.getEmailId()+" "+iter.getName());
                }*/
            }
            if(!searchName.isEmpty()){
                //if(!mListUser.contains(new ArrayList<User>().addAll(searchName)))
                if(!mListUser.containsAll(searchName))
                    mListUser.addAll(searchName);
                /*for(User iter:searchName){
                    iter.getEmailId();
                    txtSearchResult.setText(txtSearchResult.getText()+" "+iter.getEmailId()+" "+iter.getName());
                }*/
            }

            if(!searchCourse.isEmpty()){

                //if(!mListUser.contains(new ArrayList<User>().addAll(searchCourse)))
                if(!mListUser.containsAll(searchCourse))
                    mListUser.addAll(searchCourse);

                /*for(User iter:searchCourse){
                    iter.getEmailId();
                    txtSearchResult.setText(txtSearchResult.getText()+" "+iter.getEmailId()+" "+iter.getName());
                }*/
            }


            Toast.makeText(getActivity(), "Jai mata Di Done", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(getActivity(), "No records found!", Toast.LENGTH_LONG).show();


        onBoxOfficeMoviesLoaded(mListUser);

        System.out.print("Search Profile");
    }
}

}

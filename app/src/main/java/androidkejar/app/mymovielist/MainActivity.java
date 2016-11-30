package androidkejar.app.mymovielist;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidkejar.app.mymovielist.controller.MoviesConnecting;
import androidkejar.app.mymovielist.controller.MoviesResult;
import androidkejar.app.mymovielist.controller.MoviesURL;
import androidkejar.app.mymovielist.controller.adapter.MoviesAdapter;
import androidkejar.app.mymovielist.pojo.ItemObject;

public class MainActivity extends AppCompatActivity implements MoviesResult, View.OnClickListener {
    private RecyclerView mainMovieList;
    private LinearLayout mainMovieLayout;
    private ImageView mainMoviePic;
    private TextView mainMovieTitle;
    private TextView mainMovieBigTitle;
    private RelativeLayout mainMovieLoading;
    private SwipeRefreshLayout mainMovieRefresh;
    private RelativeLayout mainMovieError;
    private TextView mainMovieErrorContent;
    private FloatingActionButton mainMovieScrollTop;
    private ProgressBar mainMovieListLoading;

    private List<ItemObject.ListOfMovie.MovieDetail> movieList;
    private Handler changeHeaderHandler;
    private Runnable changeHeaderRunnable;
    private int randomList = -1;
    private String urlList;
    private int page = 1;
    private int maxPage = 1;
    private String[] sortByList = new String[]{"Now Playing", "Popular", "Top Rated", "Coming Soon"};
    private int sortPosition = 0;
    private boolean isSearching = false;
    private String querySearch;
    private MoviesAdapter moviesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainMovieLayout = (LinearLayout) findViewById(R.id.main_movie_layout);
        mainMovieList = (RecyclerView) findViewById(R.id.main_movie_list);
        mainMovieListLoading = (ProgressBar) findViewById(R.id.main_movie_list_loading);
        mainMoviePic = (ImageView) findViewById(R.id.main_movie_pic);
        mainMovieTitle = (TextView) findViewById(R.id.main_movie_title);
        mainMovieBigTitle = (TextView) findViewById(R.id.main_movie_bigtitle);
        mainMovieLoading = (RelativeLayout) findViewById(R.id.main_movie_loading);
        mainMovieRefresh = (SwipeRefreshLayout) findViewById(R.id.main_movie_refresh);
        mainMovieScrollTop = (FloatingActionButton) findViewById(R.id.main_movie_scrolltop);
        mainMovieError = (RelativeLayout) findViewById(R.id.main_movie_error);
        mainMovieErrorContent = (TextView) findViewById(R.id.main_movie_error_content);

        mainMovieScrollTop.setOnClickListener(this);
        mainMovieScrollTop.hide();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        mainMovieList.setLayoutManager(gridLayoutManager);
        mainMovieList.setHasFixedSize(true);
        mainMovieList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recyclerView.getAdapter().getItemCount() != 0) {
                    int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                    if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1) {
                        Log.d("onScrolled", "isBottom");
                        if (movieList.size() % 20 == 0 && lastVisibleItemPosition != 0) {
                            getMoviesfromBottom();
                        }
                    }
                }
                mainMovieScrollTop.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int verticalOffset = recyclerView.computeVerticalScrollOffset();
                    if (verticalOffset > 550) mainMovieScrollTop.show();
                }
            }
        });

        moviesAdapter = new MoviesAdapter(this);
        mainMovieList.setAdapter(moviesAdapter);

        movieList = new ArrayList<>();

        mainMovieRefresh.setColorSchemeColors(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE);
        mainMovieRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                launchGetMovies();
            }
        });

        changeHeaderHandler = new Handler();

        changeHeaderRunnable = new Runnable() {

            @Override
            public void run() {
                setRandomHeader();
                changeHeaderHandler.postDelayed(changeHeaderRunnable, 5000);
            }
        };

        launchGetMovies();
    }

    private void getMoviesfromBottom() {
        mainMovieListLoading.setVisibility(View.VISIBLE);
        page += 1;
        if (page != maxPage) {
            mainMovieListLoading.setVisibility(View.VISIBLE);
            changeHeaderHandler.removeCallbacks(changeHeaderRunnable);
            setURLMovies();
            getMovies(urlList);
            mainMovieListLoading.setVisibility(View.GONE);
        }
    }

    private void launchGetMovies() {
        page = 1;
        maxPage = 1;
        movieList.clear();
        moviesAdapter.resetData();
        mainMovieRefresh.setRefreshing(false);
        mainMovieLayout.setVisibility(View.GONE);
        mainMovieError.setVisibility(View.GONE);
        mainMovieLoading.setVisibility(View.VISIBLE);
        changeHeaderHandler.removeCallbacks(changeHeaderRunnable);
        mainMovieList.removeAllViews();
        setURLMovies();
        getMovies(urlList);
    }

    private void setURLMovies() {
        if (isSearching) {
            urlList = MoviesURL.getListMovieBasedOnWord(querySearch, page);
            mainMovieBigTitle.setText(querySearch.toUpperCase(Locale.getDefault()));
        } else {
            mainMovieBigTitle.setText(sortByList[sortPosition].toUpperCase(Locale.getDefault()));
            switch (sortPosition) {
                case 0:
                    urlList = MoviesURL.getListMovieNowPlaying(page);
                    break;
                case 1:
                    urlList = MoviesURL.getListMoviePopular(page);
                    break;
                case 2:
                    urlList = MoviesURL.getListMovieTopRated(page);
                    break;
                case 3:
                    urlList = MoviesURL.getListMovieUpcoming(page);
                    break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void getMovies(String url) {
        MoviesConnecting connecting = new MoviesConnecting();

        Log.d("getMovies", "url = " + url);

        connecting.getData(getApplicationContext(), url, this);
    }

    private void convertToMovies(String response) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        ItemObject.ListOfMovie myMovie = gson.fromJson(response, ItemObject.ListOfMovie.class);

        maxPage = myMovie.getTotalPages();

        movieList.addAll(myMovie.getResults());
        moviesAdapter.addAll(myMovie.getResults());

        if (movieList.size() > 0) {
            mainMovieLayout.setVisibility(View.VISIBLE);
            setHeaderLayout();
        } else {
            mainMovieError.setVisibility(View.VISIBLE);
            mainMovieErrorContent.setText("No Movies Available.");
        }

        mainMovieLoading.setVisibility(View.GONE);
    }

    private void setHeaderLayout() {
        setRandomHeader();
        changeHeaderHandler.postDelayed(changeHeaderRunnable, 5000);
    }

    private void setRandomHeader() {
        int tempRandomList;
        do {
            tempRandomList = (int) (Math.random() * movieList.size());
        } while (tempRandomList == randomList);

        randomList = tempRandomList;

        mainMovieTitle.setText(movieList.get(randomList).getTitle());

        if (movieList.get(randomList).getBackdrop() != null) {
            Glide.with(getApplicationContext())
                    .load(MoviesURL.getUrlImage(movieList.get(randomList).getBackdrop()))
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop()
                    .placeholder(R.drawable.ic_genre)
                    .into(mainMoviePic);
        } else {
            Glide.with(getApplicationContext())
                    .load(MoviesURL.getUrlImage(movieList.get(randomList).getPoster()))
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop()
                    .placeholder(R.drawable.ic_genre)
                    .into(mainMoviePic);
        }


        mainMoviePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeHeaderHandler.removeCallbacks(changeHeaderRunnable);
                Intent i = new Intent(getApplicationContext(), DetailActivity.class);
                i.putExtra("id", movieList.get(randomList).getId());
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final SearchView mainMovieSearch = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mainMovieSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                isSearching = true;
                querySearch = query;
                launchGetMovies();
                mainMovieSearch.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sortby:
                sortListBy();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortListBy() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setItems(sortByList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sortListMovieBy(i);
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

    private void sortListMovieBy(int i) {
        isSearching = false;
        sortPosition = i;
        launchGetMovies();
    }

    @Override
    public void resultData(String response) {
        Log.d("resultData", response);
        convertToMovies(response);
    }

    @Override
    public void errorResultData(String errorResponse) {
        Log.e("errorResultData", errorResponse);
        mainMovieLoading.setVisibility(View.GONE);
        mainMovieError.setVisibility(View.VISIBLE);
        mainMovieErrorContent.setText("Connection Problem. Please try again.");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_movie_scrolltop:
                mainMovieList.smoothScrollToPosition(0);
                break;
            default:
                break;
        }
    }
}
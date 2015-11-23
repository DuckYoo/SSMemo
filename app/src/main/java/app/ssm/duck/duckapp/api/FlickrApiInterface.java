package app.ssm.duck.duckapp.api;

import app.ssm.duck.duckapp.api.model.SearchResult;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;


/**
 * Noodlesoup
 * <p/>
 * Created by arueggeberg on 23.10.14.
 */
public interface FlickrApiInterface {

    @GET("/services/rest/?method=flickr.photos.search&extras=url_l&format=json&nojsoncallback=1")
    void getSearchResults(@Query("text") String searchQuery, Callback<SearchResult> results);

}

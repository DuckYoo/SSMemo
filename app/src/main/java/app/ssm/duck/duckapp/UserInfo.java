package app.ssm.duck.duckapp;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by broDuck on 15. 12. 25.
 */
public class UserInfo implements Serializable {
    private String id;
    private String email;
    private String displayName;
    private Uri photoUri;

    public UserInfo(String id, String email, String displayName, Uri photoUri) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.photoUri = photoUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }
}

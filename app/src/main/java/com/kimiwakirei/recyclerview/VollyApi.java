package com.kimiwakirei.recyclerview;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.database.annotations.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VollyApi {

    MySingleton mySingleton;
    public String name, preview_url, album_art_url, internal_id, artist;
    public int popularity, songDuration;
    public boolean responseListener = false;



    public void getJsonInformation(String JsonUrl, final Context context){

        String url = "http://kimiwakirei.azurewebsites.net/Test2.php?UrlLink=" + JsonUrl;
        JsonObjectRequest jsonObjectRequest = new
                JsonObjectRequest(Request.Method.GET, url  , null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.d("JsonResponse", "" + response.toString());
                responseListener = true;
                try {
                    //gets artist
                    JSONObject tempArtist;
                    JSONArray artist = (JSONArray) response.get("artists");
                    Log.d("MyApp","ArtistList" + artist);
                    tempArtist = (JSONObject) artist.get(0);

                    //Gets Album Art Link and SongLink
                    JSONObject tempAlbumArt = (JSONObject) response.get("album");
                    JSONArray array = (JSONArray) tempAlbumArt.get("images");
                    tempAlbumArt = (JSONObject) array.get(0);

                    setName(response.get("name").toString());
                    setAlbum_art_url(tempAlbumArt.get("url").toString());
                    setInternal_id(response.get("id").toString());
                    setPopularity(Integer.parseInt(response.get("popularity").toString()));
                    setPreview_url(response.get("preview_url").toString());
                    setArtist(tempArtist.get("name").toString());
                    setSongDuration(Integer.parseInt(response.get("duration_ms").toString()));


                    if(!getPreview_url().equals("null")){
                        ((MainActivity) context).addSong(getName(),getArtist(),getPreview_url(),getSongDuration(),getAlbum_art_url(),false
                        ,getInternal_id(), getPopularity());
                        ((MainActivity) context).changeSearchStatus(true);
                        ((MainActivity) context).saveSongs();
                        Toast.makeText( ((MainActivity)context).getApplicationContext(), "Song '" + response.get("name") +"' has been added!", Toast.LENGTH_SHORT).show();

                    }
                    else {
                        Toast.makeText( ((MainActivity) context).getApplicationContext() , "Song '" + response.get("name") + "' has no preview link :(", Toast.LENGTH_LONG ).show();
                        ((MainActivity) context).changeSearchStatus(true);
                    }

                    Log.d("MyApp", "AlbumArt: "+ tempAlbumArt.get("url"));
                    Log.d("MyApp", "SongLink: "+ response.get("preview_url"));
                    Log.d("MyApp", "SongName: "+ response.get("name"));
                    Log.d("MyApp", "Popularity: "+ response.get("popularity"));
                    Log.d("MyApp", "Artist: "+ tempArtist.get("name"));
                    Log.d("MyApp", "Internal ID: "+ response.get("id"));
                    Log.d("MyApp","Song Length (ms): " + response.get("duration_ms"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText( ((MainActivity) context).getApplicationContext() , "Error finding song. Invalid Id?", Toast.LENGTH_LONG ).show();
                    ((MainActivity) context).changeSearchStatus(true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("JsonResponse","" + error);
                Toast.makeText( ((MainActivity) context).getApplicationContext() , "Error finding song. Internet connection issues?" , Toast.LENGTH_LONG ).show();
                ((MainActivity) context).changeSearchStatus(true);

            }
        });

        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }

    public String getAlbum_art_url() {
        return album_art_url;
    }

    public void setAlbum_art_url(String album_art_url) {
        this.album_art_url = album_art_url;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public String getInternal_id() {
        return internal_id;
    }

    public void setInternal_id(String internal_id) {
        this.internal_id = internal_id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(int songDuration) {
        this.songDuration = songDuration;
    }
}

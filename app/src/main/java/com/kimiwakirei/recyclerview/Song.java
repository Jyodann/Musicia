package com.kimiwakirei.recyclerview;

import java.io.Serializable;
import java.util.ArrayList;

class Song implements Serializable {

    private String title;
    private String artist;
    private String fileLink;
    private String coverArt;
    private int popularity, songLength;
    private boolean liked;
    private String internalID;

    Song(String title, String artist, String fileLink, int songLength, String coverArt, boolean liked, String internalID, int popularity) {
        this.title = title;
        this.artist = artist;
        this.fileLink = fileLink;
        this.songLength = songLength;
        this.coverArt = coverArt;
        this.liked = liked;
        this.internalID = internalID;
        this.popularity = popularity;
    }

    String getTitle() {
        return title;
    }

    String getArtist() {
        return artist;
    }

    String getFileLink() {
        return fileLink;
    }

    int getSongLength() {
        return songLength;
    }

    String  getCoverArt() {
        return coverArt;
    }

    String getInternalID() {
        return internalID;
    }

    int getPopularity() {
        return popularity;
    }
}

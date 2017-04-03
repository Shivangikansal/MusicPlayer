package com.first.shivangi.musicplayer;

public class Song {
    private long id;
    private String title;
    private String artist;
    private int index;

    public Song(long songId, String songTitle, String songArtist, int index){
        id=songId;
        title=songTitle;
        artist=songArtist;
    }
    public long getId(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public int getIndex(){return index;}
}

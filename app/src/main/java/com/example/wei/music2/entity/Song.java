package com.example.wei.music2.entity;

/**
 * Created by WEI on 2018/5/2.
 */

public class Song {

    //文件绝对路径
    private String data;
    //文件名
    private String display_name;
    //文件大小
    private Integer size;
    //歌曲名
    private String title;
    //歌手名
    private String artist;
    //歌曲时长
    private Integer duration;


    public Song() {
        super();
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Song{" +
                "data='" + data + '\'' +
                ", display_name='" + display_name + '\'' +
                ", sizr=" + size +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration=" + duration +
                '}';
    }

    public String getPlayText() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(title);
        stringBuilder.append("--");
        stringBuilder.append(artist);

        return stringBuilder.toString();
    }

}

package com.example.cyan.model;

import javax.persistence.*;

/**
 * File: RssFeedModel.java
 * A Model class that store and retrieves analysed feeds topic from the database.
 */
@Entity
public class RssFeedModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Lob
    private String hotRssFeedTopics;

    @Lob
    private String topRssFeeds;


    public RssFeedModel() { }

    public RssFeedModel(String hotRssFeedTopics, String topRssFeeds) {
        this.hotRssFeedTopics = hotRssFeedTopics;
        this.topRssFeeds = topRssFeeds;
    }

    public RssFeedModel(int id, String hotRssFeedTopics, String topRssFeeds) {
        this.id = id;
        this.hotRssFeedTopics = hotRssFeedTopics;
        this.topRssFeeds = topRssFeeds;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHotRssFeedTopics() {
        return hotRssFeedTopics;
    }

    public void setHotRssFeedTopics(String hotRssFeedTopics) {
        this.hotRssFeedTopics = hotRssFeedTopics;
    }

    public String getTopRssFeeds() {
        return topRssFeeds;
    }

    public void setTopRssFeeds(String topRssFeeds) {
        this.topRssFeeds = topRssFeeds;
    }

    @Override
    public String toString() {
        return "RssFeedModel{" +
                "id=" + id +
                ", hotRssFeedTopics='" + hotRssFeedTopics + '\'' +
                ", topRssFeeds='" + topRssFeeds + '\'' +
                '}';
    }
}

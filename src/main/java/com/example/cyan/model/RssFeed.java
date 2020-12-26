package com.example.cyan.model;


public class RssFeed {

  private String title;
  private String link;
  private String pubDate;

  public RssFeed() { }

  public RssFeed(String title, String link, String pubDate)
  {
    this.title = title;
    this.link = link;
    this.pubDate = pubDate;
  }

  @Override
  public String toString() {
    return "RssFeed{" +
        "title='" + title + '\'' +
        ", link='" + link + '\'' +
        ", pubDate='" + pubDate + '\'' +
        '}';
  }

  public String prepareJson() {
      StringBuilder sb = new StringBuilder();
      sb.append("{ title : ");
      sb.append(title);
      sb.append(",link : ");
      sb.append(link);
      sb.append(" }");
      return sb.toString();
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getPubDate() {
    return pubDate;
  }

  public void setPubDate(String pubDate) {
    this.pubDate = pubDate;
  }
}

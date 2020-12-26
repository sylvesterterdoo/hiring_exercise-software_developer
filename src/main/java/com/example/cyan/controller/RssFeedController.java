
package com.example.cyan.controller;

import com.example.cyan.services.RssFeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * File: RssFeedController.java
 * An API class that request and analysis rss feeds
 */
@RestController
public class RssFeedController {

    @Autowired
    private RssFeedService rssFeedService;

  /**
   * Returns a unique identifier which reference analysed stored data,
   * given at least two rss urls as parameters
   * @param urls
   */
  @GetMapping(value="/analyse/new", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity getRss( @RequestParam Map<String, String> urls) {

      if (urls.values().toArray().length < 2) {
      return new ResponseEntity("You must provide at least two urls", HttpStatus.NOT_ACCEPTABLE);
      }

      Map<String, Object>  response = rssFeedService.analyseRssFeeds(urls);
      return  new ResponseEntity(response, HttpStatus.OK);
  }


  /**
   * Returns the three elements with the most matches,
   * with the orignal news header and the link to the whole news text.
   * @param id
   */
  @GetMapping(value="/frequency/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity getRssFeedById(@PathVariable(required=true) int id) {
      Map<String, Object>  response = rssFeedService.getFeedById(id);

      if (response != null) {
          return new ResponseEntity(response, HttpStatus.OK);
      } else {
          return new ResponseEntity(response, HttpStatus.NOT_FOUND);
      }
  }

}
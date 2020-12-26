package com.example.cyan.services;

import com.example.cyan.dao.RssFeedRepository;
import com.example.cyan.model.RssFeed;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.example.cyan.model.RssFeedModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * File: RssFeedService.java
 * A Service class that communicate between the controller and database repository.
 */
@Service
public class RssFeedService {

    @Autowired
    private RssFeedRepository rssFeedRepository;

    private List<List<RssFeed>> rssFeedsList= new ArrayList<>();
    private Map<String, List<String>> tokensFeedsList = new HashMap<>();
    private Map<String, Integer> topRssFeeds= new LinkedHashMap<>();

    private List<String> commonWords = Arrays.asList(
            "news", "here", "what", "post", "record", "after", "press", "says", "week", "with",
            "picks", "schedule", "from", "you", "year", "and", "a"
    );


    public Map<String, Integer> getTopRssFeeds() {
        return topRssFeeds;
    }

    /** Returns the analysed results from given feeds */
    public Map<String, Object>  analyseRssFeeds(Map<String, String> rssFeedsUrl) {
        return analyseRssFeeds(getRssUrls(rssFeedsUrl));
    }

    /** AnalysedRssFeeds helper method. */
    public Map<String, Object>  analyseRssFeeds(List<String> rssFeedsUrls) {

        for (String url : rssFeedsUrls) {
            this.rssFeedsList.add(this.parseRssFeedsUrls(url));
        }

        for (List<RssFeed> feeds : this.rssFeedsList) {

            for (RssFeed rssFeed : feeds) {

                String[] tokens = rssFeed.getTitle().toLowerCase().split("[^a-zA-Z]");

                for (String word : tokens) {
                    if ((word.length() <= 3) || (commonWords.contains(word))) {  // TODO: exclude propositions and pronouns like what
                        continue;
                    }

                    tokensFeedsList.computeIfAbsent(word, v -> {
                        List<String> feedsList = new ArrayList<>();
                        feedsList.add(rssFeed.prepareJson());
                        return feedsList;
                    });

                    tokensFeedsList.computeIfPresent(word, (k, v) -> {
                        List<String> feedsList = tokensFeedsList.get(k);
                        if (!feedsList.contains(rssFeed.prepareJson())) {
                            feedsList.add(rssFeed.prepareJson());
                        }
                        return feedsList;
                    });
                }
            }
        }

        computeTopRssFeeds();

        Map<String, Object> response = new HashMap<String, Object>();
        try {
            RssFeedModel newFeedModel = new RssFeedModel(getRssFeedsAsString(), convertToJson(this.topRssFeeds));
            newFeedModel = rssFeedRepository.save(newFeedModel);

            response.put("Related feeds", newFeedModel.getTopRssFeeds());
            response.put("To view these feeds use the endpoint /frequency/{id}", " where id is " + newFeedModel.getId());
            return response;
        } catch (NullPointerException ex) {
            response.put("Message", "Something went wrong, try again");
            return response;
        }

    }

    /** Return a list of feed headers as command seperated string */
    private  String getRssFeedsAsString() {
        StringBuilder sb = new StringBuilder();
        for (String feeds : topRssFeeds.keySet()) {
            sb.append(String.join(", ", tokensFeedsList.get(feeds)));
        }
        return sb.toString();
    }

    /** Returns a list of proper urls headers from the map params provided */
    private static List<String> getRssUrls(Map<String, String> feedsUrls) {
        List<String> rssUrls = new ArrayList<>();
        for (String url : feedsUrls.values()) {
            if (url.matches("^(http?|ftp)://.*$")) {
                rssUrls.add(url);
            }
        }
        return rssUrls;
    }

    /** Returns a Java object as Json */
    public static String convertToJson(Map<String, Integer> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Compute the top RssFeeds */
    public void computeTopRssFeeds() {
        Map<String, Integer> feedsTokenFrequency = new HashMap<>();

        // compute the frequency of tokens in rssFeeds
        for (Map.Entry<String, List<String>> feedToken : this.tokensFeedsList.entrySet()) {
            feedsTokenFrequency.put(feedToken.getKey(), feedToken.getValue().size());
        }

        // Sorted token in descending order
        Map<String, Integer> sortedTokenFrequency = feedsTokenFrequency.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

        // store the top rssFeeds
        int i = 0;
        Iterator iterator = sortedTokenFrequency.entrySet().iterator();
        while (iterator.hasNext() && i < 3) {
            Map.Entry mapElement = (Map.Entry) iterator.next();
            this.topRssFeeds.put((String) mapElement.getKey(), (int) mapElement.getValue());
            i += 1;
        }

    }

    /** Returns a list of feed objects, from xml data given an xml feed url */
    public List<RssFeed> parseRssFeedsUrls(String url) {

        List<RssFeed> rssFeeds = new ArrayList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(url);
            doc.getDocumentElement().normalize();

            NodeList nodes = doc.getElementsByTagName("item");

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;
                    RssFeed rssFeed = new RssFeed();

                    rssFeed.setTitle(element.getElementsByTagName("title")
                            .item(0).getTextContent());
                    rssFeed.setLink(element.getElementsByTagName("link")
                            .item(0).getTextContent());
                    rssFeed.setPubDate(element.getElementsByTagName("pubDate")
                            .item(0).getTextContent());

                    rssFeeds.add(rssFeed);
                }
            }

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return rssFeeds;

    }

    /** Returns analysed feed data given the id of feed */
    public Map<String, Object>  getFeedById(int id) {
        Map<String, Object> response = new HashMap<>();

        Optional<RssFeedModel> rssFeedModel =  rssFeedRepository.findById(id);

        if (rssFeedModel.isPresent()) {
            response.put("Result", rssFeedModel.toString());
        } else {
            response.put("message", "id does not exist.");
        }

        return response;
    }
}

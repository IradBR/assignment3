package bgu.spl.net.impl.newsfeed;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NewsFeed {

    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> channels = new ConcurrentHashMap<String, ConcurrentLinkedQueue<String>>();

    public ArrayList<String> fetch(String channel) {
        ConcurrentLinkedQueue<String> queue = channels.get(channel);
        if (queue == null) {
            return new ArrayList<String>(0); //empty
        } else {
            return new ArrayList<String>(queue); //copy of the queue, arraylist is serializable
        }
    }

    public void publish(String channel, String news) {
        ConcurrentLinkedQueue<String> queue = channels.computeIfAbsent(channel, k -> new ConcurrentLinkedQueue<String>());
        queue.add(news);
    }

    public void clear() {
        channels.clear();
    }
}

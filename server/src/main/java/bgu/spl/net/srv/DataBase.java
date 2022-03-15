package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class DataBase {

    private ConcurrentHashMap<String,User > name_user;
    private Connections connections;
    private ConcurrentHashMap<String,String> filterWords;

    public DataBase(){
        this.name_user=new ConcurrentHashMap<String,User>();
        this.connections=null;
        this.filterWords=new ConcurrentHashMap<String,String>();
        filterWords.putIfAbsent("spl", "<filtered>");//add filter words as you wish
        filterWords.putIfAbsent("hw3", "<filtered>");
        filterWords.putIfAbsent("client", "<filtered>");
        filterWords.putIfAbsent("server", "<filtered>");
    }

    private static class DataBaseHolder {
        private static DataBase instance = new DataBase();
    }

    public static DataBase getInstance() {
        return DataBaseHolder.instance;
    }

    public void setConnections(Connections connections){
        this.connections=connections;
    }

    public ConcurrentHashMap<String, User> getName_user() {
        return name_user;
    }

    public void setName_user(ConcurrentHashMap<String, User> name_user) {
        this.name_user = name_user;
    }

    public Connections getConnections() {
        return connections;
    }

    public User getUser(String name){
        return name_user.get(name);
    }


    public ConcurrentHashMap<String, String> getFilteredWords(){
        return filterWords;
    }
}

package bgu.spl.net.srv;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;


public class User {
    private int id;
    private String username;
    private String password;
    private String birthday;
    private ConcurrentLinkedQueue<String> followers;
    private ConcurrentLinkedQueue<String> following;
    private boolean connect;
    private int numOfPosts;
    private int age;
    private ConcurrentLinkedQueue<Message> notifications;
    private  ConcurrentLinkedQueue<String> postsAndPM;
    private ConcurrentLinkedQueue<String> blocked;


    public User(int id, String username, String password, String birthday){
        this.id=id;
        this. username=username;
        this.password=password;
        this.birthday=birthday;
        this.followers=new ConcurrentLinkedQueue<String>();
        this.following=new ConcurrentLinkedQueue<String>();
        this.connect=false;
        this.numOfPosts=0;
        this.age= calculateAge(birthday);
        this.notifications=new ConcurrentLinkedQueue<Message>();
        this.postsAndPM=new ConcurrentLinkedQueue<String>();
        this.blocked=new ConcurrentLinkedQueue<String>();


    }


    public int getId() {
        return id;
    }


    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }


    public String getBirthday() {
        return birthday;
    }


    public ConcurrentLinkedQueue<String> getFollowing() {
        return following;
    }

    public ConcurrentLinkedQueue<String> getFollowers() {
        return followers;
    }

    public void addFolower(String name){
        followers.add(name);
    }

    public void addFolowing(String name){
        following.add(name);
    }

    public boolean isConnect() {
        return connect;
    }

    public int calculateAge(String birthday){
        String year= birthday.substring(6,10);
        String month = birthday.substring(3,5);
        String day = birthday.substring(0,2);
        LocalDate birthDay = LocalDate.parse(year + "-" + month + "-" + day);
        return Period.between(birthDay, LocalDate.now()).getYears();
    }

    public int getAge(){
        return age;
    }

    public int getNumOfPosts(){
        return numOfPosts;
    }

    public void setNumOfPosts(int num){
        numOfPosts=num;
    }

    public void addNotification(Message notification){
        notifications.add(notification);
    }

    public void addPostOrPM(String post){
        postsAndPM.add(post);
    }


    public void Connect(){
        connect=true;
    }
    public void disConnect(){
        connect=false;

    }

    public ConcurrentLinkedQueue<Message> getNotifications(){
        return notifications;
    }
    public int getNumberOfFollowers (){
        return followers.size();
    }
    public int getNumberOfFollowing (){
        return following.size();
    }

    public void block(String toBlock){
        blocked.add(toBlock);
    }

    public ConcurrentLinkedQueue<String> getBlocked(){
        return blocked;
    }
    public void increaseNomOfPost() {
        this.numOfPosts=this.numOfPosts+1;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setFollowers(ConcurrentLinkedQueue<String> followers) {
        this.followers = followers;
    }

    public void setFollowing(ConcurrentLinkedQueue<String> following) {
        this.following = following;
    }

    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setNotifications(ConcurrentLinkedQueue<Message> notifications) {
        this.notifications = notifications;
    }

    public ConcurrentLinkedQueue<String> getPostsAndPM() {
        return postsAndPM;
    }

    public void setPostsAndPM(ConcurrentLinkedQueue<String> postsAndPM) {
        this.postsAndPM = postsAndPM;
    }

    public void setBlocked(ConcurrentLinkedQueue<String> blocked) {
        this.blocked = blocked;
    }

    public boolean isBlock(String name) {
        return blocked.contains(name);
    }
}

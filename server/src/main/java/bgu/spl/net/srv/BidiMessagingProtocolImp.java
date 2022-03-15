package bgu.spl.net.srv;
import bgu.spl.net.api.BidiMessagingProtocol;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class BidiMessagingProtocolImp implements BidiMessagingProtocol<Message> {
    private boolean shouldTerminate;
    private int Id;
    String name;
    private Connections connections;
    private DataBase dataBase;


    public BidiMessagingProtocolImp(){
        this.shouldTerminate=false;
        this.Id=0;
        this.connections=null;
        this.dataBase=DataBase.getInstance();
        this.name="";

    }

    @Override
    public void process(Message message) {
        short opCode= message.getOpCode();
        String [] msg= message.getMessage();
        switch (opCode) {
            case 1:
               register(msg);
                break;
            case 2:
                Login(msg);
                break;
            case 3:
                LogOut();
                break;
            case 4:
                FollowUnFollow(msg);
                break;
            case 5:
               post(msg);
                break;
            case 6:
                PM(msg);
                break;
            case 7:
                LogStat();
                break;
            case 8:
                STAT(msg);
                break;
            case 12:
                Block(msg);
                break;
        }

    }

    @Override
    public void start(int connectionId, Connections<Message> connections) {
        this.Id=connectionId;
        this.connections=connections;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public void register(String[] msg){
        Message answer;
        if(isRegister(msg[0])) {
            answer = new Error((short) 11, (short) 1); //ERROR
        }
        else {

            this.name=msg[0];
            User newUser = new User(this.Id, msg[0],msg[1],msg[2]);
            dataBase.getName_user().putIfAbsent(msg[0], newUser);
            answer = new ACK((short) 10, (short)1,"");  //ACK
        }
        connections.send(Id,answer);

    }

    public void Login(String[] msg){
        Message answer;
        if(dataBase.getUser(msg[0])!=null){
            dataBase.getUser(msg[0]).setId(Id);
            name=msg[0];
        }

        if(!(isRegister(msg[0]) && !isConect(msg[0]) &&
                dataBase.getUser(msg[0]).getPassword().equals(msg[1]) && msg[2].equals("1"))){
            answer = new Error((short) 11, (short)2); //ERROR
            connections.send(Id,answer);
        }
        else{
            User user= dataBase.getUser(msg[0]);
            user.setConnect(true);
            answer = new ACK((short) 10, (short)2,"");  //ACK
            connections.send(Id,answer);
            ConcurrentLinkedQueue<Message> messages=user.getNotifications();
            while(!messages.isEmpty()){
                Message m=messages.remove();
                connections.send(Id,m);
            }
        }

    }

    public void LogOut(){
        Message answer;
        if(!isConect(name)){
            answer = new Error((short) 11, (short)3); //ERROR
            connections.send(Id,answer);
        }
        else{
            User user= dataBase.getUser(name);
            user.disConnect();
            answer=new ACK((short) 10, (short)3,"");  //ACK
            connections.send(Id,answer);
            connections.disconnect(Id);
            shouldTerminate=true;
        }

    }

    public void FollowUnFollow(String[] msg){
        Message answer;
        String s=msg[0];
        char operation= s.charAt(0);
        String FollowName=s.substring(1);

        if(operation=='1') { //unfollow
            if (!(isRegister(name) && isConect(name) && isRegister(FollowName) && dataBase.getUser(name).getFollowing().contains(FollowName))) {
                answer = new Error((short) 11, (short) 4); //ERROR
            }
            else{
                dataBase.getUser(name).getFollowing().remove(FollowName);
                dataBase.getUser(FollowName).getFollowers().remove(name);
                answer=new ACK((short) 10, (short)4,"1 "+FollowName);  //ACK
            }

        }
        else{ //follow
            if (!(isRegister(name) && isConect(name) && isRegister(FollowName)&& !dataBase.getUser(name).getFollowing().contains(FollowName)&&
                    !dataBase.getUser(name).getBlocked().contains(FollowName) && !dataBase.getUser(FollowName).getBlocked().contains(name)) ){
                answer = new Error((short) 11, (short) 4); //ERROR
            }
            else{
                dataBase.getUser(name).getFollowing().add(FollowName);
                dataBase.getUser(FollowName).getFollowers().add(name);
                answer=new ACK((short) 10, (short)4,"0 "+FollowName);  //ACK //todo  check the forum
            }

        }
        connections.send(Id,answer);
    }

    public void post(String[] msg){
        if (isConect(getName())) {
            User thisUser = dataBase.getName_user().get(getName());
            ConcurrentLinkedQueue<String> followers = thisUser.getFollowers();
            thisUser.addPostOrPM(msg[0]);
            thisUser.increaseNomOfPost();
            for (String follower : followers) {
                User user = dataBase.getName_user().get(follower);
                if (user.isConnect()) {
                    connections.send(user.getId(), new Notification((short) 9, '1', getName()+" ", msg[0]));
                }
                else {
                    user.getNotifications().add(new Notification((short) 9, '1', getName()+" ", msg[0]));
                }

            }
            String[] post = msg[0].split(" ");
            for (String word : post) { //to check if someone tagged in the post
                if (word.charAt(0) == '@') { //if after @ is userName
                    String userName = word.substring(1);
                    //If it's a username that register, but not a user who follows this because then he will get 2 notification
                    if (dataBase.getName_user().containsKey(userName) && !dataBase.getName_user().get(getName()).getFollowers().contains(userName)
                            && !dataBase.getName_user().get(getName()).getBlocked().contains(userName)) {
                        if( !dataBase.getUser(userName).getBlocked().contains(name)){
                            User user = dataBase.getName_user().get(userName);
                            if (user.isConnect())
                                connections.send(user.getId(), new Notification((short) 9, '1', getName()+" ", msg[0]));
                            else
                                user.getNotifications().add(new Notification((short) 9, '1', getName()+" ", msg[0]));
                        }
                    }

                }

            }

            connections.send(getId(), new ACK((short)10, (short)5, ""));

        }
        else
            connections.send(getId(), new Error((short)11, (short)5));
    }

    void PM(String[] msg){
        if (isConect(getName()) && dataBase.getUser(msg[0])!=null) {
            User thisUser = dataBase.getUser(name);
            boolean isFollow = thisUser.getFollowing().contains(msg[0]);
            String PM_message=msg[1];
            if (isFollow) {
                //filter the message
                Set<String> filterWords = dataBase.getFilteredWords().keySet();
                String[] PM = PM_message.split(" ");
                for (int i = 0; i < PM.length; i++) {
                    Iterator<String> iter = filterWords.iterator();
                    while (iter.hasNext()) {
                        String word = iter.next();
                        if (PM[i].equals(word)||PM[i].equals(word+",")||PM[i].equals(word+".")||PM[i].equals(word+"!")||PM[i].equals(word+"?"))
                            PM[i] = "<filtered>";
                    }
                }
                PM_message = "";
                for (int i = 0; i < PM.length; i++)
                    PM_message = PM_message + " " + PM[i];
                PM_message = PM_message.substring(1);

                User toSendUser = dataBase.getName_user().get(msg[0]);
                thisUser.addPostOrPM(PM_message);
                if (toSendUser.isConnect()) {
                    connections.send(toSendUser.getId(), new Notification((short) 9, '0', getName(), PM_message + " " + msg[2]));
                }
                else {
                    toSendUser.getNotifications().add(new Notification((short) 9, '0', getName(), PM_message + " " + msg[2]));

                }
                connections.send(Id, new ACK((short)10,(short)6 ,""));
            }
            else
                connections.send(getId(), new Error((short)11, (short)6));
        }
        else
            connections.send(getId(), new Error((short)11, (short)6));
    }

    public void LogStat(){
        if(isConect(getName())) {
            User thisuser = dataBase.getUser(name);
            for (User user : dataBase.getName_user().values())
                if (user.isConnect() && !thisuser.isBlock(user.getUsername())) {
                    String userDetails = user.getAge() + " " + user.getNumOfPosts() + " " + user.getNumberOfFollowers() + " " + user.getNumberOfFollowing();
                    connections.send(getId(), new ACK((short) 10, (short) 7, userDetails));
                }
        }
        else
            connections.send(getId(), new Error((short)11, (short)7));
    }

    public void STAT(String[] msg){
        boolean userNotFound=false;
        if(isConect(getName())) {
            User thisuser = dataBase.getUser(name);
            String [] listOfUsername = msg[0].split("\\|");
            String userDetails="";
            for (int i = 0; i < listOfUsername.length &!userNotFound; i++) {
                if (isRegister(listOfUsername[i])&& !thisuser.isBlock(listOfUsername[i])) {
                    User user = dataBase.getName_user().get(listOfUsername[i]);
                    userDetails =userDetails + user.getAge() + " " + user.getNumOfPosts() + " " + user.getNumberOfFollowers() + " " + user.getNumberOfFollowing()+";";
                }
                else {
                    connections.send(getId(), new Error((short) 11, (short) 8));
                    userNotFound=true;
                }
            }
            if(!userNotFound) connections.send(getId(), new ACK((short) 10, (short) 8, userDetails));
        }
        else
            connections.send(getId(), new Error((short)11, (short)8));
    }


    public void Block(String[] msg){
        Message answer;
        if (!(isRegister(name) && isRegister(msg[0]) && isConect(name) )){
            answer = new Error((short) 11, (short) 12); //ERROR
        }
        else{
            dataBase.getUser(name).getFollowing().remove(msg[0]);
            dataBase.getUser(msg[0]).getFollowing().remove(name);
            dataBase.getUser(msg[0]).getFollowers().remove(name);
            dataBase.getUser(name).getFollowers().remove(msg[0]);
            dataBase.getUser(name).block(msg[0]);
            dataBase.getUser(msg[0]).block(name);
            answer=new ACK((short) 10, (short)12,"");  //ACK
        }
        connections.send(Id,answer);
    }

    public boolean isRegister(String name){
        return dataBase.getUser(name)!=null;
    }

    public boolean isConect(String name){
        if(isRegister(name)){
            return dataBase.getUser(name).isConnect();
        }
        return false;
    }

    public boolean isShouldTerminate() {
        return shouldTerminate;
    }

    public void setShouldTerminate(boolean shouldTerminate) {
        this.shouldTerminate = shouldTerminate;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public Connections getConnections() {
        return connections;
    }

    public void setConnections(Connections connections) {
        this.connections = connections;
    }

    public DataBase getDataBase() {
        return dataBase;
    }

    public void setDataBase(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }













}

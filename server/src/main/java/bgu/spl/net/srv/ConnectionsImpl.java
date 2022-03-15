package bgu.spl.net.srv;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T>{

    private ConcurrentHashMap<Integer, ConnectionHandler> Id_handler;

    public ConnectionsImpl(){
        this.Id_handler=new ConcurrentHashMap<Integer, ConnectionHandler>();
    }


    @Override
    public boolean send(int connectionId,T msg) {
        if(Id_handler.get(connectionId)==null) {
            return false;
        }
        else{
            try {
                Id_handler.get(connectionId).send(msg);
                return true;
            }catch (IOException e){
                return false;
            }
        }
    }


    @Override
    public void broadcast(T msg)  {
        try {
            for (Integer id : Id_handler.keySet()) {
                Id_handler.get(id).send(msg);
            }
        }catch (IOException e){};
    }

    @Override
    public void disconnect(int connectionId) {
        if(Id_handler.get(connectionId)!=null) Id_handler.remove(connectionId);
    }

    public void addConectionHandler(int id, ConnectionHandler handler){
        Id_handler.putIfAbsent(id,handler);
    }
}

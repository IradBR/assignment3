package bgu.spl.net.srv;

import java.io.IOException;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void broadcast(T msg) throws IOException;

    void disconnect(int connectionId);

    void addConectionHandler(int id, ConnectionHandler handler);
}

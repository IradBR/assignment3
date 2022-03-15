package bgu.spl.net.srv;

public class Message {

    private short opCode;
    private String[] message;

    public Message(short opCode,String message){

        this.opCode=opCode;
        this.message= message.split("\0");
    }

    public short getOpCode(){
        return opCode;
    }
    public String [] getMessage(){
        return message;
    }

}

package bgu.spl.net.srv;

public class Notification extends Message {
    private char type;
    private String postingUser;
    private String content;

    public Notification(short opcode, char type, String postingUser, String content) {
        super(opcode, "");
        this.type=type;
        this.postingUser=postingUser;
        this.content=content;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    public String getPostingUser() {
        return postingUser;
    }

    public void setPostingUser(String postingUser) {
        this.postingUser = postingUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
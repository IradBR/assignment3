package bgu.spl.net.srv;

public class Error extends Message{
    private short opcode2;

    public Error(short ErrorOpcode, short opcode2){
        super(ErrorOpcode,"");
        this.opcode2=opcode2;
    }

    public short getOpcode2() {
        return opcode2;
    }

    public void setOpcode2(short opcode2) {
        this.opcode2 = opcode2;
    }
}

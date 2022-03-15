package bgu.spl.net.srv;

public class ACK extends Message {
    private short opcode2;

    public ACK(short ACKOpcode, short opcode2, String optional) {
        super(ACKOpcode, optional);
        this.opcode2 = opcode2;
    }

    public short getOpcode2() {
        return opcode2;
    }

    public void setOpcode2(short opcode2) {
        this.opcode2 = opcode2;
    }

}
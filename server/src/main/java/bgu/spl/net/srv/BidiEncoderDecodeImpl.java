package bgu.spl.net.srv;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BidiEncoderDecodeImpl implements MessageEncoderDecoder<Message> { //TODO CHANGE T
    private short opCode;
    private byte[] OpBytes;
    private int level;
    private byte[] bytes;
    private int len;
    private boolean startReadMessage;

    public BidiEncoderDecodeImpl(){
        this.opCode = 0;
        this.OpBytes = new byte[2];
        this.level = 1;
        this.bytes = new byte[1<<10];
        this.len = 0;
        this.startReadMessage=false;

    }

    @Override
    public Message decodeNextByte(byte nextByte) {
        if(level==1) {
            OpBytes[0]=nextByte;
            level++;
        }

        else if(level==2){
            OpBytes[1]=nextByte;
            opCode=bytesToShort(OpBytes);
            level++;
        }
        else{
            if(nextByte==';'){
                if(startReadMessage) {
                    String message = popString();
                    Message m = new Message(opCode, message);
                    clean();
                    return m;
                }
                else{
                    startReadMessage=true;
                }
            }
            else {
                pushBytes(nextByte);
            }
        }
        return null;
    }

    @Override
    public byte[] encode(Message message) {
        short opCode= message.getOpCode();
        byte[] ans=null;
        if(opCode==(short) 10){
            ans=encodeACK(((ACK)message).getOpcode2(),((ACK)message).getMessage() );
        }
        if(opCode==(short) 11){
            ans=encodeERROR(((Error)message).getOpcode2());
        }
        if(opCode==(short) 9){
            ans=encodeNotification(((Notification)message).getType(), ((Notification)message).getPostingUser(), ((Notification)message).getContent());
        }
        return ans;
    }

    public byte[] encodeACK(short opCode,String[] optional){
        byte []op= shortToBytes((short)10);
        byte [] op2=shortToBytes(opCode);

        if(opCode==1|opCode==2|opCode==3|opCode==5|opCode==6|| opCode==12){
            byte[] byteArray=new byte[5];
            ByteBuffer buff=ByteBuffer.wrap(byteArray);
            buff.put(op);
            buff.put(op2);
            buff.put((byte) ';');
            return buff.array();
        }
        if(opCode==4){
            byte[] Optional= optional[0].getBytes();
            byte[] byteArray=new byte[5+Optional.length];
            ByteBuffer buff=ByteBuffer.wrap(byteArray);
            buff.put(op);
            buff.put(op2);
            buff.put(Optional);
            buff.put((byte) ';');
            return buff.array();
        }
        else{
            String usersData = optional[0];//to check if this optional in length 1
            String [] usersDataArr = usersData.split(";");
            int length = 13 * usersDataArr.length; //12 bytes every message - 2 ACK opcode, 2 Logstat opcode, 2 age, 2 numPosts, 2 numFollowers, 2 numFollowing, 1 ";"
            byte [] ans = new byte [length];
            ByteBuffer buff=ByteBuffer.wrap(ans);
            for (int i=0; i< usersDataArr.length; i++) {
                String [] singleUserData = usersDataArr[i].split(" ");
                short ACKOpcode = (short) 10;
                byte [] ACKOpcodeByte = shortToBytes(ACKOpcode);
                short opcode = opCode;
                byte [] opcodeByte = shortToBytes(opcode);
                short age = (short) Integer.parseInt(singleUserData[0]);
                byte [] ageByte = shortToBytes(age);
                short numPosts = (short) Integer.parseInt(singleUserData[1]);
                byte [] numPostsByte = shortToBytes(numPosts);
                short numFollowers = (short) Integer.parseInt(singleUserData[2]);
                byte [] numFollowersByte = shortToBytes(numFollowers);
                short numFollowing = (short) Integer.parseInt(singleUserData[3]);
                byte [] numFollowingByte = shortToBytes(numFollowing);
                byte endByte = ';';
                buff.put(ACKOpcodeByte);
                buff.put(opcodeByte);
                buff.put(ageByte);
                buff.put(numPostsByte);
                buff.put(numFollowersByte);
                buff.put(numFollowingByte);
                buff.put(endByte);
            }
            return buff.array();
        }
    }


    public byte[] encodeERROR(short opCode){
        short ErrorOpcode = (short) 11;
        byte [] ErrorOpcodeByte = shortToBytes(ErrorOpcode);
        short opcode = opCode;
        byte [] opcodeByte = shortToBytes(opcode);
        byte [] ans = new byte [5];
        byte endByte = ';';
        ByteBuffer buff=ByteBuffer.wrap(ans);
        buff.put(ErrorOpcodeByte);
        buff.put(opcodeByte);
        buff.put(endByte);
        return buff.array();

    }
    public byte[] encodeNotification(char type,String postingUser, String content){
        short opcode = (short) 9;
        byte [] opcodeByte = shortToBytes(opcode);
        content = postingUser + content + ";";
        byte [] contentByte = content.getBytes();
        int length = 3 + contentByte.length; // 3= 2 bytes for opcode + 1 for type
        byte [] ans = new byte [length];
        ByteBuffer buff=ByteBuffer.wrap(ans);
        buff.put(opcodeByte);
        buff.put((byte) type);
        buff.put(contentByte);
        return buff.array();
    }


    public short bytesToShort(byte[] byteArr) {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    private String popString(){
        String result=new String(bytes,0,len, StandardCharsets.UTF_8);
        len=0;
        return result;
    }

    private void pushBytes(byte nextByte){
        if(len>=bytes.length){
            bytes= Arrays.copyOf(bytes,len*2);
        }
        bytes[len]=nextByte;
        len++;
    }

    private void clean(){
        this.opCode = 0;
        this.OpBytes = new byte[2];
        this.level = 1;
        this.bytes = new byte[1<<10];
        this.len = 0;
        this.startReadMessage=false;

    }

}
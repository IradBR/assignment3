package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.BidiEncoderDecodeImpl;
import bgu.spl.net.srv.BidiMessagingProtocolImp;
import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args) {
        if (args[0] != null) {
            Server.threadPerClient(Integer.parseInt(args[0]), () -> new BidiMessagingProtocolImp(), () -> new BidiEncoderDecodeImpl()).serve();
        } else {
            System.out.println("No arguments found");
        }
    }
}

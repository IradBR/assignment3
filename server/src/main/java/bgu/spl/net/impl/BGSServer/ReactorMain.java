package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.BidiEncoderDecodeImpl;
import bgu.spl.net.srv.BidiMessagingProtocolImp;
import bgu.spl.net.srv.Reactor;

public class ReactorMain {
    public static void main(String[] args) {
        if (args != null) {
            int port = Integer.parseInt(args[0]);
            int numOfThreads = Integer.parseInt(args[1]);
            Reactor server = new Reactor(numOfThreads, port, () -> new BidiMessagingProtocolImp(), () -> new BidiEncoderDecodeImpl());
            server.serve();
        } else {
            System.out.println("No arguments found");
        }
    }
}

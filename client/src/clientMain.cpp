#include <stdlib.h>
#include <connectionHandler.h>
#include "ReadFromKeyboard.h"
#include <thread>
#include "ReadFromSocket.h"

bool shouldTerminate=false;
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    ReadFromSocket ReadfromSocket(connectionHandler);
    ReadFromKeyborad ReadfromKeyboard(connectionHandler);

    thread th1(&ReadFromSocket::run, &ReadfromSocket);
    while (!shouldTerminate){
        ReadfromKeyboard.run();
        sleep(1);
    }

    th1.join();
    return 0;
}

#include "../include/ReadFromSocket.h"
using namespace std;
extern bool shouldTerminate;
ReadFromSocket::ReadFromSocket(ConnectionHandler &handler1): handler(handler1){}


void ReadFromSocket::run() {

    while (1) {
        string answer;
        if (!handler.getMessage(answer)) {
            cout << "Disconnected. Exiting...;" << endl;
            break;
        }


        int len = answer.length();
        answer=answer.substr(0,len-1);
        cout <<answer  << endl;
        if (answer == "ACK 3 ") {
            shouldTerminate= true;
            break;
        }

    }





}
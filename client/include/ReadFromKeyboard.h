#include <mutex>
#include "../include/connectionHandler.h"
#ifndef READFROMKEYBORAD_H_
#define READFROMKEYBORAD_H_
using namespace std;

class ReadFromKeyborad {
private:
    ConnectionHandler& handler;
    bool close;

public:
    ReadFromKeyborad(ConnectionHandler &handler1);
    void run();
    short convertCommend(string operation);
    void shortToBytes(short num, char* bytesArr);
    string timeAndDate();
    void SplitString(string line, vector<string> &words);

};

#endif 


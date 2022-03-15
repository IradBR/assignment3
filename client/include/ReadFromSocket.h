#include "connectionHandler.h"
#include <mutex>
#ifndef READFROMSOCKET_H_
#define READFROMSOCKET_H_
class ReadFromSocket{

private:
    ConnectionHandler& handler;

public:
    ReadFromSocket(ConnectionHandler& handler1);
    void run();

};

#endif 



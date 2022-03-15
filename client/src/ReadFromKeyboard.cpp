#include "../include/connectionHandler.h"
#include "ReadFromKeyboard.h"
#include <vector>
#include <ctime>

using namespace std;

ReadFromKeyborad::ReadFromKeyborad(ConnectionHandler &handler1): handler(handler1), close(false) {
}

void ReadFromKeyborad::run() {
    while (1) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        short opcode;
        vector<string>words;
        if (line.substr(0,8)=="REGISTER") {
            opcode = (short)1;
            line = line.substr(9);
            SplitString(line,words);
        }
        else if (line.substr(0,5)=="LOGIN") {
            opcode = (short)2;
            line = line.substr(6);
            SplitString(line,words);
        }
        else if (line.substr(0,6)=="LOGOUT") {
            opcode = (short)3;
            close=true;
        }
        else if (line.substr(0,6)=="FOLLOW") {
            opcode = (short)4;
            line = line.substr(7);
            SplitString(line,words);
        }

        else if (line.substr(0,4)=="POST") {
            opcode = (short)5;
            line = line.substr(5);
        }
        else if (line.substr(0,2)=="PM") {
            opcode = (short)6;
            line = line.substr(3);
        }
        else if (line.substr(0,7)=="LOGSTAT") {
            opcode = (short)7;
        }
        else if (line.substr(0,4)=="STAT") {
            opcode = (short)8;
            line = line.substr(5);
        }
        else if (line.substr(0,5)=="BLOCK") {
            opcode = (short)12;
            line = line.substr(6);

        }

        char byteArr[2];
        shortToBytes(opcode,byteArr);
        string opcodSendToserver;
        opcodSendToserver = byteArr[0];
        opcodSendToserver = opcodSendToserver + byteArr[1];
        if(!handler.sendLine(opcodSendToserver)){
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        string sendToserver="";
        //for opcode 3 and 7 I didnt need to do anything else
        if ((opcode==1)||(opcode==2)) {
            for (std::size_t j = 0; j < words.size()-1; j++) {
                sendToserver.append(words[j] + '\0');
            }
            sendToserver.append(words[words.size()-1]);
         }

        else if (opcode==4)
            sendToserver.append(words[0] + words[1]);
        else if ((opcode==5)||(opcode==8)||(opcode==12))
            sendToserver.append(line+'\0');
        else if (opcode==6) {
            int i = 0;
            string userName = "";
            while (line[i] != ' ') {
                userName=userName+line[i];
                i++;
            }
            sendToserver.append(userName+'\0');
            line= line.substr(userName.length());
            sendToserver.append(line+'\0');
            sendToserver.append(timeAndDate()+'\0');

        }

        if(!handler.sendLine(sendToserver)){
            std::cout << "rrDisconnected. Exiting...\n" << std::endl;
            break;
        }
        if(close){
            //sleep(1);
           // while(!mutex.try_lock()){
            break;
        }

    }
}


string ReadFromKeyborad::timeAndDate(){
    time_t t = time(0);   // get time now
    tm* now = std::localtime(&t);
    string day = to_string(now->tm_mday);
    string month = to_string(now->tm_mon+1);
    string hour = to_string(now->tm_hour);
    string min = to_string(now->tm_min);
    if (day.size()==1)
        day="0" + day;
    if (month.size()==1)
        month ="0"+ month;
    if(min.size()==1)
        min = "0" + min;
    if(hour.size()==1)
        min = "0" + hour;
    string date = day + '-' + month + '-' +  to_string(now->tm_year + 1900);
    string time = hour + ':' + min;
    return date + " " + time;
}


void ReadFromKeyborad::shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}


void ReadFromKeyborad:: SplitString(string line, vector<string> &words){
    int i=0;
    int len=line.length();
    int d=0;
    while (i < len) {
        if (line.at(i) == ' ') {
            string word=line.substr(d, i - d);
            words.push_back(word);
            d = i + 1;
        }
        if (i == len - 1) {
            string word=line.substr(d);
            words.push_back(word);
        }
        i++;
    }
}



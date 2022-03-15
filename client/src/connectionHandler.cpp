#include <connectionHandler.h>

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_){}

ConnectionHandler::~ConnectionHandler() {
    close();
}

bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
            tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getLine(std::string& line) {
    return getFrameAscii(line, ';');
}

bool ConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, ';');
}

bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    // Stop when we encounter the null character.
    // Notice that the null character is not appended to the frame string.
    try {
        do{
            getBytes(&ch, 1);
            frame.append(1, ch);
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
    bool result=sendBytes(frame.c_str(),frame.length());
    if(!result) return false;
    return sendBytes(&delimiter,1);
}

// Close down the connection properly.
void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}


short ConnectionHandler::bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

bool ConnectionHandler::getMessage(string& frame){
    char ch;
    char opCode[2];
    short op;

    if (!getBytes(opCode, 2)) {
        return false;
    }
    op=bytesToShort(opCode);
    if (op == 10 || op == 11) {//function reads error/ack + opcode
        if (!getBytes(opCode, 2)) {
            return false;
        }
        short op2 = bytesToShort(opCode);
        std::stringstream ss;
        ss << op2;
        string s = ss.str();

        if (op==10 &&(op2 == 7 || op2 == 8) ){
            char ageByte[2];
            if (!getBytes(ageByte, 2)) {
                return false;
            }
            short age = bytesToShort(ageByte);
            std::stringstream streamAge;
            streamAge << age;
            string ageString = streamAge.str();

            char numPostsByte[2];
            if (!getBytes(numPostsByte, 2)) {
                return false;
            }
            short numPosts = bytesToShort(numPostsByte);
            std::stringstream streamNumPosts;
            streamNumPosts << numPosts;
            string numPostsString = streamNumPosts.str();


            char numFollowersByte[2];
            if (!getBytes(numFollowersByte, 2)) {
                return false;
            }
            short numFollowers = bytesToShort(numFollowersByte);
            std::stringstream streamNumFollowers;
            streamNumFollowers << numFollowers;
            string NumFollowersString = streamNumFollowers.str();


            char numFollowingsByte[2];
            if (!getBytes(numFollowingsByte, 2)) {
                return false;
            }
            short numFollowing = bytesToShort(numFollowingsByte);
            std::stringstream streamNumFollowing;
            streamNumFollowing << numFollowing;
            string FollowingString = streamNumFollowing.str();

            char endByte[1]; // ;
            if (!getBytes(endByte, 1)) {
                return false;
            }

            frame.append(("ACK " + s + " " + ageString + " " + numPostsString + " " + NumFollowersString + " " +
                          FollowingString+ ";")); //finished

        }
        else {
            string content;
            if (!getLine(content)) {
                return false;
            }
            if (op == 10) frame.append(("ACK " + s + " " + content)); //finished
            else frame.append("ERROR " + s + " " + content); //finished
        }
    }

    else if (op == 9) { //finished
        if(!getBytes(&ch, 1)){  //read public\pm
            return false;
        }

        frame.append("NOTIFICATION ");
        if(ch=='1') frame.append("public ");
        else frame.append("PM ");
        string content;
        if(!getLine(content)){
            return false;
        }
        frame.append(content);
    }
    return true;

}
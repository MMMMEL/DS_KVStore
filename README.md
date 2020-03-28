# KVStore
Distributed System

## How to build server and client codes

get into the src folder

javac utility/*.java client/*.java server/*.java

## How to run server and client programs

Run server:
java server/Server2PC \<port> \<peer> \<peer> \<peer> \<peer>

e.g. java server/Server2PC 1111 2222 3333 4444 5555

Run client:
java client.Client2PC localhost \<port>

e.g. java client.Client2PC localhost 1111
  
\<port> can be 1111, 2222, 3333, 4444 or 5555
  
## How to run jar file

Run server:
java -jar Server2PC.jar \<port> \<peer> \<peer> \<peer> \<peer>

e.g. java -jar Server2PC.jar 1111 2222 3333 4444 5555

Run client:
java -jar Client2PC.jar localhost \<port>

e.g. java -jar Server2PC.jar localhost 1111
  
\<port> can be 1111, 2222, 3333, 4444 or 5555

## Executive summary
### Assignment overview
The main purpose of this assignment is to replicate servers each of which has its own data, i.e. Key-Value Store. Different clients can connect to different servers and get information or change data. When a client change data through its connection to a server, the other servers should also get the proposal and when all active servers agree to commit the update, the proposal will be committed on all servers.

### Technical impression
The implementation is based on the previous projects. In this assignment, the functions include:
•	When a client wants to GET a value of a key, the server will get the value from its Key-Value Store.
•	When a client wants to update (PUT or DELETE), the server will propagate this request to it peer servers and waiting for their responses. Any updates successfully committed by client initialized the request, clients connected to other servers also get the update.
•	If any response is ABORTED, the request will be aborted. 

These functions are presented in the demo video.

The program started by starting 5 servers and giving them 5 port numbers, first of which is the server’s port number and the other four are peer servers’ port numbers.

Start servers:
 
 
 
 
 


Then start the clients to connect with servers by offering server host name and port number. Clients can send request with PUT, GET, DELETE commands.

Client 1 connected to server 1111 send requests:
 
 

Client 2 connected to server 2222 can get the value of k6:
 
 

When a server is down and clients connected to other servers send request, the request will be aborted.

Make server 2222 down and Client 1 tries to delete k6:
 

Log from other serves:
 

Key k6 is not deleted.
 

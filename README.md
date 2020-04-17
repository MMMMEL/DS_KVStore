# KVStore
Distributed System

## How to build server and client codes

get into the src folder

javac utility/*.java client/*.java server/*.java

## How to run server and client programs

Run server:
java utility.Coordinator

Run client:
java client.Client2PC localhost \<port>

\<port> can be 1111, 2222, 3333, 4444 or 5555

## How to run jar file

Run server:
java -jar Server2PC.jar

Run client:
java -jar Client2PC.jar localhost \<port>

\<port> can be 1111, 2222, 3333, 4444 or 5555

## Executive summary
### Assignment overview
The main purpose of this assignment is to replicate servers each of which has its own data, i.e. Key-Value Store. Different clients can connect to different servers and get information or change data. When a client change data through its connection to a server, the other servers should also get the proposal and when all active servers agree to commit the update, the proposal will be committed on all servers.

### Technical impression
The implementation is based on the previous projects. In this assignment, the functions include:
•	When a client wants to GET a value of a key, the server will get the value from its Key-Value Store.
•	When a client wants to update (PUT or DELETE), the server will propagate this request to it peer servers and waiting for their responses. If any response is ABORTED, the request will be aborted. Otherwise, the server will tell all peers to commit the update.
•	If a server is down, its opinion won’t will be received and won’t be counted when the proposer server collects the responses.
•	When a down server comes back, it can still get the store that its peers have and continue to process client requests.

These functions are presented in the demo video.

To implement these functions, the program has a coordinator which takes responsibility to start or restart servers and tell them their peers’ ports. Also, the coordinator keeps a Key-Value Store and when an update is committed, coordinator’s store also gets updated. When a server is started or restarted, the server will get the updated store data from coordinator.

In the video, the coordinator first starts all servers and then three clients connect to three different servers. When client 1 update the Key-Value Store, client 2 and client 3 can also get the update on the servers they connect to.

In the code, there is a pre-defined event which will happen when all servers started for 45 seconds. The event is to make a server down and come back after 20 seconds. During the down time, clients connected to other servers make update. When the down server comes back, it can also get the update happened during its down time.

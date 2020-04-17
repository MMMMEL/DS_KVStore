# KVStore
Distributed System

## How to build server and client codes

get into the src folder

javac client/*.java server/*.java utility/*.java

## How to run server and client programs

Run server:
java server.ServerImplPaxos 1111 2222 3333 4444 5555

java server.ServerImplPaxos 2222 1111 3333 4444 5555

java server.ServerImplPaxos 3333 1111 2222 4444 5555

java server.ServerImplPaxos 4444 1111 2222 3333 4444

java server.ServerImplPaxos 5555 1111 2222 3333 5555


Run client:
java client.ClientPaxos localhost \<port>

\<port> can be 1111, 2222, 3333, 4444 or 5555

## How to run jar file

Run server:
java -jar ServerPaxos.jar 1111 2222 3333 4444 5555

java -jar ServerPaxos.jar 2222 1111 3333 4444 5555

java -jar ServerPaxos.jar 3333 1111 2222 4444 5555

java -jar ServerPaxos.jar 4444 1111 2222 3333 5555

java -jar ServerPaxos.jar 5555 1111 2222 3333 5555


Run client:
java -jar ClientPaxos.jar localhost \<port>

\<port> can be 1111, 2222, 3333, 4444 or 5555

## Executive summary
### Assignment overview
The main purpose of this assignment is to implement PAXOS protocol to reach consensus among all servers. In PAXOS protocol, a server can be a proposer, an acceptor or a learner of a suggestion. In this assignment, the update (PUT or DELETE) of the Key-Value Store should use such protocol to be processed. When a client sends a request to update a record in KVStore, the server that this client is connecting becomes the proposer. The proposer should build a proposal with the client request and multicast this proposal to its peer servers (acceptors). A peer server which now is an acceptor should decide to make a promise to proposer or not. Such decision is made based on the suggestionID of the proposal. If over half of the acceptors promise, the proposal is accepted. The proposer will then multicast the decision with the promised value. The value may be different from the original proposal because some acceptor may already commit to another value which will be the new value.

### Technical impression
In this assignment, the main functions with PAXOS protocol include:
•	When a client wants to GET a value of a key, the server he/she connects will get the value from its Key-Value Store.
•	When a client wants to update (PUT or DELETE), the server will propagate this request to it peer servers (acceptors) and waiting for their responses. The acceptors will make their promise based on the PAXOS rules. Only if majority of the acceptors promise their commitment, will the proposal be accepted. 
•	After the proposer server goes through the responses and found more than half of the responses are with status “PROMISED”, the proposer server will continue to ask all acceptors commit. If the final decision is ABORTED, the request will be aborted. 
•	The server randomly (50%) fails to accept a proposal as an acceptor. In this situation, the server won’t be counted in the acceptors’ valid responses.
•	If a server is totally down and then restarted, it will request information from other active peer servers for the latest suggestionID and the KVStore.

These functions are presented in the demo video.

The implement of these functions can be considered as an extension of 2PC. In this implement, a Message class is built to generalize the message transmitted between client and server as well as between server and server. ClientMessage is as basic as a Message with properties of KEY and VALUE. ServerMessage is extended to have properties including suggestionID, last promise and STATUS which represents the status in PAXOS. A ServerMessage also has a property of source server it is from. 

The decision process is implemented in ServerImplPaxos class. If a server receives a request from client, it will propagate the request to peers and get responses as ServerMessages. If a server receives a Message from server with a status of PREPARE, it will response its decision after it checked the suggestionID. If the server can accept the proposal, it will set the STATUS as PROMISED and set the proper value. The proposal server will evaluate the response and continue to send commitment message to acceptors if the proposal is accepted. If a peer server receives a ServerMessage from server with a status of ACCEPT, it will process the request on its KVStore.

The program started by starting 5 servers and giving them 5 port numbers, first of which is the server’s port number and the other four are peer servers’ port numbers.

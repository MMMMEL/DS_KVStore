# KVStore
Distributed System

## How to build server and client codes (including any external libraries necessary) 

Build server:
javac ServerTCP.java
javac ServerUDP.java

Build client:
javac ClientTCP.java
javac ClientUDP.java

## How to run server and client programs

Run server:
java ServerTCP <port>
java ServerUDP <port>

Run client:
java ClientTCP <hostname> <port>
java ClientUDP <hostname> <port>

## Executive summary
### Assignment overview
This project is built based on the previous version. In the previous version, servers adopting TCP and UDP were developed, in which only one client’s request can be processed at one time. In this project, the server is extended to use Remote Procedure Calls and take requests from different clients at the same time.

### Technical impression
For RPC implementation, Java RMI is adopted. At server side, it registers the method processing clients’ request with a name. At client side, it will look up the method with the name and invoke the method remotely.

For multithread server implementation, Java ThreadPool is adopted. In this implementation, server has a thread pool of ten threads. Since the service provided by this project contains a critical resource, Key-Value Store, which is actually a Map, synchronization on the service is needed to avoid conflict results while processing requests from different clients.

In the demo, the key-value store will be initialized with 5 key-value pairs:
store = new HashMap<>();

store.put("k1", "v1");
store.put("k2", "v2");
store.put("k3", "v3");
store.put("k4", "v4");
store.put("k5", "v5");

Each client will send a set of requests:
"PUT k1 v1",
"PUT k2 v2",
"PUT k3 v3",
"GET k1",
"GET k2",
"DELETE k1",
"GET k1",       (non-existed key)
"GET k2",
"GET k3",
"PUT k4 v4",
"PUT k5 v5",
"GET k4",
"GET k5",
"GET k1 k2",    (invalid GET request)
"GET",          (invalid GET request)
"DELETE k1 k2", (invalid DELETE request)
"PUT k6",       (invalid PUT request)
"GET k6",       (non-existed key because of unsuccessful previous request)
"DELETE k2",
"DELETE k3",
"DELETE k4",
"DELETE k5",
If two clients are sending the requests at the same time, the response can be different. For example, the second client who sends the delete request of “k1”, “k2”, “k3”, “k4”, “k5” won’t successfully delete them because those keys are already deleted by the first client who sent the same request.

For a clear vision, when clients send their request, their hash codes are sent together. When server get a request, it can get from which client that request is sent.
Feb 18, 2020 5:25:21 PM utility.ServiceLog infoLog
INFO: Get request from client 1654589030: GET k3
Feb 18, 2020 5:25:21 PM utility.ServiceLog infoLog
INFO: Processing request: GET k3
Feb 18, 2020 5:25:21 PM utility.ServiceLog infoLog
INFO: Finish request, result: Successfully GET value v3 for key k3
Feb 18, 2020 5:25:22 PM utility.ServiceLog infoLog
INFO: Get request from client 33524623: DELETE k1
Feb 18, 2020 5:25:22 PM utility.ServiceLog infoLog
INFO: Processing request: DELETE k1
Feb 18, 2020 5:25:22 PM utility.ServiceLog infoLog
INFO: Finish request, 

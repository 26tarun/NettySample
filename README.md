NettySample
===========

This is an Actor System (an independent distributed event driven server). The job of such system is to broker available infrastructure resources and user request with minimal supervision or human intervention. The following features are expected to be delivered from this system : 

1) Able to recognize the scale of infrastructure. Also scale up (with addition of node) without requirement of system taken offline or scale down (manage heartbeats and if a node goes down, remove it from task distribution list) .
2) Manage it's own (Leader election) network and keep separation of concern from external network to internal decision making and communication 
3) Handle request , scale them accordingly on the underlying infrastructure with optimum use of resources (for eg: parallelize wherever possible).
4) Load balance and take care of delivery semantics and fault tolerance. 


The following are the salient technical features of this project 
1) Uses Netty api (based on Java's New IO) , use of Channel  to cast raw handshake objects between nodes and send/receive messages. 
2) Uses protocol buffers for object serialization. 
3) Uses Mongo DB and java drivers for Mongo DB (specifically for this project as requirement was to insert HD images without system facing a fault or crash) 
4) Used Flood Max algorithm for leader election , Ant algorithm for load balancing, created a mesh network for better propogation of messages .


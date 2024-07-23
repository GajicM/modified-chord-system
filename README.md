[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/_usaz5p7)

# Documentation

## Overview
- System functions as a chord dht with mutex implementation of Suzuki-Kasami algorithm for mutual exclusivity, and ping-pong messages for fault detection.
- Chord Size as of now is 128, and hashing algorithm is SHA-1 hashing address:port for nodes and fileName for files. and modding it with 128.
- Files are stored in Chord system, where key is hashed file name. Backups are stored in its successor and predecessor. When node that had the values exits, the nodes next successor will take over. 


## Supported commands are:
- dht_put _[file_name]_ _[public/private]_ //puts file_name in chord system 
- dht_get _[file_name]_ //gets a file from a chord system synonym for -  add_file _[file_name]_ _[public/private]_ //adds a file 
- view_files _[address:port]_ //views the files for address:port, can see private only if friends with that address:port
- add_friend _[address:port]_ //adds a address:port as a  friend for current node 
- remove_file _[file_name]_ //removes the file from chord system
- pause _[time in ms]_ //pauses the node for that amount of ms
- stop    //stops the node


## Messages 
- AskGet/TellGet works for dht_get where a node that writes dht_get sends ask_gets through the chord system until it reaches a node that has said file, and then returns tell get with file with TellGet message through the chord system
- Put - travels through the chord system until it reaches the node that has a key for that file (fileName is chordHashed) and then puts it in its map
- Backup - is sent to successor and predecessor when a node puts file in system, with file that has been put. when someone receives backup its saved in backupMap and is used when original node that sent backup fails
- TellView/AskView - travels through chord until it reaches destination. When it does, the destined node sends TellView message with contents of it valueMap
- AskDelete/TellDelete - similar to put, but instead of putting value, it deletes it, also it deletes the file from backups whenever node receives ask/tell since that`s where backups will be.
- TokenRequest/Reply will be explained in **#Mutex**
- Ping/Pong+Sus/SusResponse will be explained in **#FaultDetection**
- Welcome/Update/Sorry unchanged from original chord implementation


## Mutex
- Modified Suzuki-Kasami implementation, whereas in Suzuki-Kasami implementation the node sends Request to all nodes at once, here it sends it to its successors, and successors propagate request until they reach every node. TokenReply is also sent through the chord system and isn't direct.
- Base Algorithm explained: https://www.geeksforgeeks.org/suzuki-kasami-algorithm-for-mutual-exclusion-in-distributed-system/?ref=lbp
- Upon requestingCriticalSection, it will send TokenRequests to its successors, and will be propagated through the system
- Waiting until node gets the token, then waking up from wait and entering critical section
- once critical section is done, we releaseCriticalSection and send the token to the next one in queue or the first request we get if queue is empty


## Fault tolerance and backup
- Standard ping-pong, where node sends pings to its first successor and receives pings from its predecessor. When it receives ping it has 4sec delay before the sender decides the receiver is acting sus
- If node thinks the successor is acting sus, it`ll send another ping message, and a Sus message to successor of its successor
- if a node doesn't receive pong from its successor or a sus response from successor of its successor within 10 seconds it will mark the successor for deletion and delete it
- when node deletes its successor it adjusts its successor list and sends RemovedNode message to the new successor, which will propagate node deletion through the system.
- if both successor and successor of its successor are stopped, it will need 28+ seconds but eventually successor lists will be in order
- Also, when deletion of a node happens, it will trigger writing backups in a value map (it is still written in the chord, so only a node that now has a key will write the file in), which will in turn trigger sending backups

package servent;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.ReplyTokenMessage;
import servent.message.RequestTokenMessage;
import servent.message.util.MessageUtil;

import java.util.*;

import static servent.message.util.MessageUtil.sendMessage;

public class SKTokenMutex {

    public static int sequenceNumber=0;
    public static int[] requestNumber = new int[ChordState.CHORD_SIZE];
    public static boolean hasToken = (AppConfig.myServentInfo.getChordId() == 35);
    public static List<Integer> tokenQueue = new ArrayList<>();
    public static int[] lastRequest = new int[ChordState.CHORD_SIZE];
    public static final Object objectLock=new Object();



    public  void requestCriticalSection() {
        synchronized (objectLock) {
            AppConfig.timestampedStandardPrint("zahteva kriticnu sekciju");
            sequenceNumber++;
            requestNumber[(AppConfig.myServentInfo.getChordId())] = sequenceNumber;
            if(!hasToken){
               sendRequestsToAllSuccessors();
            }
            while (!hasToken &&  (!tokenQueue.isEmpty() && tokenQueue.get(0) != AppConfig.myServentInfo.getChordId())) {
                try {
                    objectLock.wait();
                    AppConfig.timestampedStandardPrint("I WOKE UP ");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            tokenQueue.remove((Integer) (AppConfig.myServentInfo.getChordId()));
            AppConfig.timestampedStandardPrint("izasao iz zahteva kriticnu sekciju");
        }
    }
    public  void releaseCriticalSection() {
        synchronized (objectLock) {
            AppConfig.timestampedStandardPrint("usao u release");
            lastRequest[AppConfig.myServentInfo.getChordId()] = requestNumber[AppConfig.myServentInfo.getChordId()];
            for (int i = 0; i < ChordState.CHORD_SIZE; i++) {
                if (i != AppConfig.myServentInfo.getChordId() && requestNumber[i] > lastRequest[i]) {
                    tokenQueue.add(i);
                }
            }

            if (!tokenQueue.isEmpty() && hasToken) {
                int nextNode = tokenQueue.remove(0);
                sendToken(nextNode);
                hasToken = false;
            }
            AppConfig.timestampedStandardPrint("hastoken= "+hasToken);
            AppConfig.timestampedStandardPrint("zavrsio release THE TOKEN FROM CRITICAL SECTION");
        }
    }



    public static void sendToken(int nextNode) {
       ServentInfo nextNodeInfo= AppConfig.chordState.getNextNodeForKey(String.valueOf(nextNode));
        AppConfig.timestampedStandardPrint("servent info in sendToken: "+ nextNodeInfo);
        sendMessage(new ReplyTokenMessage((AppConfig.myServentInfo.getListenerPort()),
                nextNodeInfo.getChordId()==AppConfig.myServentInfo.getChordId()? AppConfig.chordState.getNextNodePort():nextNodeInfo.getListenerPort(),
                "Token",
                new ArrayList<>(tokenQueue),
                lastRequest.clone(),
                nextNode));
    }



    public static void sendRequestsToAllSuccessors(){
        Set<ServentInfo> sentTo=new HashSet<>();
        Set<ServentInfo> helper=new HashSet<>(Arrays.stream(AppConfig.chordState.getSuccessorTable()).toList());
        helper.add(AppConfig.chordState.getPredecessor());
        for(ServentInfo successor:helper){
           if(sentTo.contains(successor)){
               continue;
           }
            sentTo.add(successor);
            RequestTokenMessage requestTokenMessage=new RequestTokenMessage(AppConfig.myServentInfo.getListenerPort(),successor.getListenerPort(), sequenceNumber,AppConfig.myServentInfo.getChordId(),helper);
            MessageUtil.sendMessage(requestTokenMessage);
        }
        sentTo.clear();
    }
    public static void propagateRequestToAllSuccessors(int requesterChordId,Set<ServentInfo> alreadySentTo){
        Set<ServentInfo> sentTo=new HashSet<>();
        Set<ServentInfo> helper=new HashSet<>(Arrays.stream(AppConfig.chordState.getSuccessorTable()).toList());
        helper.add(AppConfig.chordState.getPredecessor());
        alreadySentTo.addAll(helper);
        for(ServentInfo successor:helper){
            if(sentTo.contains(successor)){
                continue;
            }
            if(successor.getChordId()==requesterChordId)
                continue;
            if(alreadySentTo.contains(successor))
                continue;
            sentTo.add(successor);
            RequestTokenMessage requestTokenMessage=new RequestTokenMessage(AppConfig.myServentInfo.getListenerPort(),successor.getListenerPort(), sequenceNumber,requesterChordId,alreadySentTo);
            MessageUtil.sendMessage(requestTokenMessage);
        }
        sentTo.clear();

    }







}

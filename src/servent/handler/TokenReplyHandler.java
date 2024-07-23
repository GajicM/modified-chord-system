package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.SKTokenMutex;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReplyTokenMessage;
import servent.message.util.MessageUtil;

public class TokenReplyHandler implements MessageHandler{

    private final Message message;

    public TokenReplyHandler(Message m){
        this.message=m;
    }



    @Override
    public void run() {
        if(message.getMessageType()== MessageType.TOKEN_REPLY){
            synchronized (SKTokenMutex.objectLock) {
                ReplyTokenMessage rtm = (ReplyTokenMessage) message;
                AppConfig.timestampedStandardPrint("my chord id is "+ AppConfig.myServentInfo.getChordId() + " and message requester is "+ rtm.getRequesterId());
                if(rtm.getRequesterId()!=AppConfig.myServentInfo.getChordId()){
                  ServentInfo nextNode=  AppConfig.chordState.getNextNodeForKey(String.valueOf(rtm.getRequesterId()));
                  ReplyTokenMessage toSend=new ReplyTokenMessage(AppConfig.myServentInfo.getListenerPort(),nextNode.getListenerPort()==AppConfig.myServentInfo.getListenerPort()?AppConfig.chordState.getNextNodePort() : nextNode.getListenerPort(), "PROPAGATE TOKEN",rtm.getTokenQueue(),rtm.getLastRequest(),rtm.getRequesterId());
                  MessageUtil.sendMessage(toSend);
                   AppConfig.timestampedStandardPrint("Propagating token to "+nextNode.getChordId() + "  " + nextNode.getListenerPort());
                    return;
                }
                AppConfig.timestampedStandardPrint("Using token");
                SKTokenMutex.tokenQueue = rtm.getTokenQueue();
                SKTokenMutex.lastRequest = rtm.getLastRequest();
                SKTokenMutex.hasToken = true;
                SKTokenMutex.objectLock.notifyAll();
            }


        }



    }


}

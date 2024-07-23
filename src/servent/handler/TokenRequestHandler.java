package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.SKTokenMutex;
import servent.message.Message;
import servent.message.RequestTokenMessage;
import servent.message.util.MessageUtil;

import static servent.SKTokenMutex.*;
import static servent.message.MessageType.TOKEN_REQUEST;

public class TokenRequestHandler implements MessageHandler{
    private Message message;

    public TokenRequestHandler(Message message){
        this.message=message;
    }

    @Override
    public void run() {
        if(message.getMessageType()==TOKEN_REQUEST){
            RequestTokenMessage requestTokenMessage= (RequestTokenMessage) message;
            int senderId = requestTokenMessage.getRequestorChordId();
            int seqNum = requestTokenMessage.getSequenceNumber();


            synchronized (objectLock){
                requestNumber[requestTokenMessage.getRequestorChordId()] = Math.max(requestNumber[requestTokenMessage.getRequestorChordId()], seqNum);
                AppConfig.timestampedErrorPrint("senderid="+senderId +"\nRNsi"+requestNumber[senderId]+"\nlastReqeust"+lastRequest[senderId]);
              //  AppConfig.timestampedErrorPrint("tokenQue"+requestTokenMessage.get);
                if (hasToken && tokenQueue.isEmpty() && (requestNumber[senderId] > lastRequest[senderId] || (requestNumber[senderId] == lastRequest[senderId] && senderId < AppConfig.myServentInfo.getChordId()))) {
                    hasToken = false;

                    sendToken(requestTokenMessage.getRequestorChordId());
                }else if(!hasToken){
                    SKTokenMutex.propagateRequestToAllSuccessors(requestTokenMessage.getRequestorChordId(),requestTokenMessage.getAlreadySentTo());
                }
            }



        }
    }
}

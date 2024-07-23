package servent.handler.faultDetection;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PongMessage;
import servent.message.util.MessageUtil;

public class PingHandler implements MessageHandler {
    private final Message clientMessage;
    public PingHandler(Message message){
        this.clientMessage=message;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType()== MessageType.PING){
            AppConfig.gotPingFromPredecessor=true;
            PongMessage pongMessage=new PongMessage(AppConfig.myServentInfo.getListenerPort(), clientMessage.getSenderPort());
            MessageUtil.sendMessage(pongMessage);
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            AppConfig.gotPingFromPredecessor=false;

        }else AppConfig.timestampedStandardPrint("PING HANDLER GOT NON PING MESSAGE");
    }
}

package servent.handler.faultDetection;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;


public class PongHandler implements MessageHandler {
    private final Message clientMessage;
    public PongHandler(Message message){
        this.clientMessage=message;
    }

    @Override
    public void run() {
        if(clientMessage.getMessageType()== MessageType.PONG){
            AppConfig.gotPongFromSuccessor=true;

        }else AppConfig.timestampedStandardPrint("PONG HANDLER GOT NON PING MESSAGE");
    }
}

package servent.handler.faultDetection;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.RemovedNodeMessage;

public class RemovedNodeHandler implements MessageHandler {
    RemovedNodeMessage removedNodeMessage;
    public RemovedNodeHandler(Message message){
        removedNodeMessage= (RemovedNodeMessage) message;
    }

    @Override
    public void run() {
        AppConfig.timestampedStandardPrint("handler: deleting node"+removedNodeMessage.getRemovedServentInfo());
        AppConfig.chordState.removeNode(removedNodeMessage.getRemovedServentInfo());


    }
}

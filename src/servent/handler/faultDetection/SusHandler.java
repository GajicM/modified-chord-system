package servent.handler.faultDetection;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.SusMessage;
import servent.message.SusResponseMessage;
import servent.message.util.MessageUtil;

public class SusHandler implements MessageHandler {
    SusMessage message;
    public SusHandler(SusMessage m){
        this.message=m;
    }

    @Override
    public void run() {
        SusResponseMessage susResponseMessage=new SusResponseMessage(AppConfig.myServentInfo.getListenerPort(), message.getSenderPort(), AppConfig.gotPingFromPredecessor);
        MessageUtil.sendMessage(susResponseMessage);
        AppConfig.timestampedStandardPrint("GOT SUS MESSAGE FOR MY PREDECESSOR :"+ AppConfig.chordState.getPredecessor());

    }
}

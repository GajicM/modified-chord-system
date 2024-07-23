package servent.handler.faultDetection;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.SusResponseMessage;

public class SusResponseHandler implements MessageHandler {
    private SusResponseMessage susResponseMessage;
    public SusResponseHandler(SusResponseMessage susResponseMessage){
        this.susResponseMessage=susResponseMessage;
    }
    @Override
    public void run() {
        if(susResponseMessage.isNotSus()) {
            AppConfig.gotPongFromSuccessor = true;
        }

    }
}

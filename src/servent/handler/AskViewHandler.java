package servent.handler;

import app.AppConfig;
import app.file.SavedFile;
import servent.message.AskViewMessage;
import servent.message.TellGetMessage;
import servent.message.TellViewMessage;
import servent.message.util.MessageUtil;

import java.util.List;

public class AskViewHandler implements MessageHandler{
    private AskViewMessage message;
    public AskViewHandler(AskViewMessage askViewMessage){
        this.message=askViewMessage;
    }

    @Override
    public void run() {
        try {
         List<SavedFile> result=  AppConfig.chordState.getValues(message.getAddressPort(),message.getOriginalSender());
         AppConfig.timestampedStandardPrint(result + "asda");//Integer.valueOf()message.getAddressPort().split(":")[1]
         TellViewMessage tellViewMessage=new TellViewMessage(AppConfig.myServentInfo.getListenerPort(),AppConfig.chordState.getNextNodeForKey(String.valueOf(message.getOriginalSender())).getListenerPort(), result,message.getOriginalSender());
         MessageUtil.sendMessage(tellViewMessage);
        } catch (Exception e) {
            //kljuc nije moj saljem dalje
        }
    }
}

package servent.handler;

import app.AppConfig;
import servent.message.TellViewMessage;
import servent.message.util.MessageUtil;

public class TellViewHandler implements MessageHandler {
    private final TellViewMessage message;

    public TellViewHandler(TellViewMessage message) {
        this.message = message;
    }



    @Override
    public void run() {
        if(message.getOriginalAsker()== AppConfig.myServentInfo.getChordId()){
            //ja sam trazio
            AppConfig.timestampedStandardPrint("TELL VIEW FILES FOR " +message.getOriginalAsker() + "  : "+message.getFileList());
        }else{
            if(AppConfig.chordState.getNextNodeForKey(String.valueOf(message.getOriginalAsker()))!=AppConfig.myServentInfo) {
                TellViewMessage tellViewMessage = new TellViewMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodeForKey(String.valueOf(message.getOriginalAsker())).getListenerPort(), message.getFileList(), message.getOriginalAsker());
                MessageUtil.sendMessage(tellViewMessage);
            }else{
                TellViewMessage tellViewMessage = new TellViewMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), message.getFileList(), message.getOriginalAsker());
                MessageUtil.sendMessage(tellViewMessage);
            }

        }
    }
}

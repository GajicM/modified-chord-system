package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.AskDeleteMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellDeleteMessage;
import servent.message.util.MessageUtil;

public class DeleteHandler implements MessageHandler {
    private Message message;
    public DeleteHandler(Message message){
        this.message=message;
    }

    @Override
    public void run() {
        if(message.getMessageType()== MessageType.ASK_DELETE){
            AskDeleteMessage deleteMessage=(AskDeleteMessage)message;
            try{
                if(AppConfig.chordState.removeValue(deleteMessage.getFileName(),deleteMessage.getOriginalAskerChordId())){
                 ServentInfo nextNode= AppConfig.chordState.getNextNodeForKey(String.valueOf(deleteMessage.getOriginalAskerChordId()));
                        AppConfig.chordState.removeValueFromBackup(deleteMessage.getFileName());
                    TellDeleteMessage tellDeleteMessage=new TellDeleteMessage(AppConfig.myServentInfo.getListenerPort(),
                            nextNode==AppConfig.myServentInfo?AppConfig.chordState.getNextNodePort() : nextNode.getListenerPort()
                            , "Deleted successfully :"+ deleteMessage.getFileName(),deleteMessage.getOriginalAskerChordId());
                    MessageUtil.sendMessage(tellDeleteMessage);
                }
            }catch (Exception e){
                ServentInfo nextNode= AppConfig.chordState.getNextNodeForKey(String.valueOf(deleteMessage.getOriginalAskerChordId()));
                AppConfig.chordState.removeValueFromBackup(deleteMessage.getFileName());
                TellDeleteMessage tellDeleteMessage=new TellDeleteMessage(AppConfig.myServentInfo.getListenerPort(),
                        nextNode==AppConfig.myServentInfo?AppConfig.chordState.getNextNodePort() : nextNode.getListenerPort()
                        , deleteMessage.getFileName()+ "  Doesnt exist", deleteMessage.getOriginalAskerChordId());
                MessageUtil.sendMessage(tellDeleteMessage);
            }


        }else if(message.getMessageType()==MessageType.TELL_DELETE){
            TellDeleteMessage tellDeleteMessage=(TellDeleteMessage) message;
            if(tellDeleteMessage.getOriginalAsker()==AppConfig.myServentInfo.getChordId()){
                AppConfig.chordState.removeValueFromBackup(tellDeleteMessage.getRetVal().split(":")[1]);
                AppConfig.timestampedStandardPrint(((TellDeleteMessage) message).getRetVal());
            }else {
                AppConfig.chordState.removeValueFromBackup(tellDeleteMessage.getRetVal().split(":")[1]);
                ServentInfo nextNode= AppConfig.chordState.getNextNodeForKey(String.valueOf(tellDeleteMessage.getOriginalAsker()));
                TellDeleteMessage tellDeleteMessage1=new TellDeleteMessage(AppConfig.myServentInfo.getListenerPort(),
                        nextNode==AppConfig.myServentInfo?AppConfig.chordState.getNextNodePort() : nextNode.getListenerPort()
                        , tellDeleteMessage.getRetVal(), tellDeleteMessage.getOriginalAsker());
                MessageUtil.sendMessage(tellDeleteMessage1);

            }



        }

    }
}

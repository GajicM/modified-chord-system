package servent.handler;

import app.AppConfig;
import app.file.SavedFile;
import servent.message.Message;
import servent.message.MessageType;

import java.io.File;

public class BackupHandler  implements MessageHandler {

    private Message clientMessage;

    public BackupHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.BACKUP) {
            String[] splitText = clientMessage.getMessageText().split(":");
            if (splitText.length == 2) {
                AppConfig.chordState.putBackupValue(splitText[0], new SavedFile(new File(AppConfig.WORKSPACE_DIR+splitText[0])));
            } else {
                AppConfig.timestampedErrorPrint("Got backup message with bad text: " + clientMessage.getMessageText());
            }
        } else {
            AppConfig.timestampedErrorPrint("Backup handler got a message that is not Backup");
        }

    }
}

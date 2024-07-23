package servent.message;

import app.file.SavedFile;

public class BackupMessage extends BasicMessage{
    private static final long serialVersionUID = 5163039209888734276L;

    public BackupMessage(int senderPort, int receiverPort, String key, SavedFile value) {
        super(MessageType.BACKUP, senderPort, receiverPort, key + ":" + value);
    }
}

package servent.message;

import app.AppConfig;
import app.ChordState;
import app.file.SavedFile;

public class PutMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;

	public PutMessage(int senderPort, int receiverPort, String key, SavedFile value) {
		super(MessageType.PUT, senderPort, receiverPort, key + ":" + value + "chordID = "+ ChordState.chordHash(key));
	}
}

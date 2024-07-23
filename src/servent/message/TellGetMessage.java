package servent.message;

import app.file.SavedFile;

public class TellGetMessage extends BasicMessage {

	private static final long serialVersionUID = -6213394344524749872L;

	public TellGetMessage(int senderPort, int receiverPort, String key, SavedFile value) {
		super(MessageType.TELL_GET, senderPort, receiverPort, key + ":" + value);
	}
}

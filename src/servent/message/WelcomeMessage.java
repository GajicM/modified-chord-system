package servent.message;

import app.file.SavedFile;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private Map<String, SavedFile> values;
	
	public WelcomeMessage(int senderPort, int receiverPort,Map<String, SavedFile> values) {
		super(MessageType.WELCOME, senderPort, receiverPort);
		
		this.values = values;
	}
	
	public Map<String,SavedFile> getValues() {
		return values;
	}
}

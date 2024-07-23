package servent.message;

public class AskDeleteMessage extends BasicMessage{
    private String fileName;
    private  int originalAskerChordId;
    public AskDeleteMessage(int senderPort, int receiverPort,String fileName,int originalAskerChordId) {
        super(MessageType.ASK_DELETE, senderPort, receiverPort);
        this.fileName =fileName;
        this.originalAskerChordId=originalAskerChordId;
    }

    public String getFileName() {
        return fileName;
    }

    public int getOriginalAskerChordId() {
        return originalAskerChordId;
    }
}

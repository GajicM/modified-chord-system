package servent.message;

import app.ServentInfo;

import java.util.Set;

public class RequestTokenMessage extends BasicMessage{
    int sequenceNumber;
    int requestorChordId;
    Set<ServentInfo> alreadySentTo;
    public RequestTokenMessage(int senderPort, int receiverPort, int sequenceNumber,int requestorChordId,Set<ServentInfo> alreadySentTo) {
        super(MessageType.TOKEN_REQUEST, senderPort, receiverPort);
        this.sequenceNumber=sequenceNumber;
        this.requestorChordId=requestorChordId;
        this.alreadySentTo=alreadySentTo;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getRequestorChordId() {
        return requestorChordId;
    }

    public Set<ServentInfo> getAlreadySentTo() {
        return alreadySentTo;
    }

    @Override
    public String toString() {
        return super.toString() + "requestorID" + requestorChordId +  "  alreadySentTo: "+ alreadySentTo;
    }
}

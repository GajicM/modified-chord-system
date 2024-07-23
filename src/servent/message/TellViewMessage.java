package servent.message;

import app.file.SavedFile;

import java.util.List;

public class TellViewMessage extends BasicMessage{
    private List<SavedFile> fileList;
    private final int originalAsker;
    public TellViewMessage(int senderPort, int receiverPort, List<SavedFile> files, int originalAsker) {
        super(MessageType.TELL_VIEW, senderPort, receiverPort);
        this.fileList=files;
        this.originalAsker = originalAsker;
    }

    public List<SavedFile> getFileList() {
        return fileList;
    }

    public int getOriginalAsker() {
        return originalAsker;
    }
}

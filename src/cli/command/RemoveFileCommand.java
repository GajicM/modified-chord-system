package cli.command;

import app.AppConfig;
import app.file.SavedFile;

import java.io.File;

public class RemoveFileCommand  implements CLICommand{
    @Override
    public String commandName() {
        return "remove_file";
    }

    @Override
    public void execute(String args) {

        try {
            if(AppConfig.chordState.removeValue(args,AppConfig.myServentInfo.getChordId())){
                AppConfig.timestampedStandardPrint("Succesfully deleted");
            }else {
                AppConfig.timestampedStandardPrint("Please wait");

            }
        } catch (Exception e) {
            AppConfig.timestampedStandardPrint("file doesnt exist");
        }
    }
}

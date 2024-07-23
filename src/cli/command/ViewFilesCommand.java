package cli.command;

import app.AppConfig;
import app.file.SavedFile;

import java.util.List;

public class ViewFilesCommand  implements CLICommand{
    @Override
    public String commandName() {
        return "view_files";
    }

    @Override
    public void execute(String args) {
        try {

            List<SavedFile> val=AppConfig.chordState.getValues(args,AppConfig.myServentInfo.getChordId());
            if (val == null) {
                AppConfig.timestampedStandardPrint("No such key: " + args);
            } else {
                AppConfig.timestampedStandardPrint("view_files execute  "+args + ": " + val);
            }
        } catch (NumberFormatException e) {
            AppConfig.timestampedErrorPrint("Invalid argument for view_files: " + args + ". Should be key, which is an int.");
        } catch (Exception e) {
            AppConfig.timestampedStandardPrint("Please wait...");
        }
    }
}

package app.file;

import app.AppConfig;

import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * This class contains* all that i need to send as messages so it can be saved to multiple nodes
 *
 * */
public class SavedFile implements Serializable {
    @Serial
    private static final long serialVersionUID = -8558031125520315033L;
    public String content;
    public String path;
    public String name;
    public List<Integer> nodes;
    public boolean isPublic;
    public SavedFile(String content, String path, String name, List<Integer> nodes) {
        this.content = content;
        this.path = path;
        this.name = name;
        this.nodes = nodes;
    }
    public SavedFile(File f){
        this.path=f.getPath();
        this.name=f.getName();
        readFile(f);
    }
    public SavedFile(File f,String s){
        this.path=f.getPath();
        this.name=f.getName();
        readFile(f);
        if(s.equalsIgnoreCase("public"))
            isPublic=true;
        else if(s.equalsIgnoreCase("private"))
            isPublic=false;
        else AppConfig.timestampedErrorPrint("public/private not written good");

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getNodes() {
        return nodes;
    }

    public void setNodes(List<Integer> nodes) {
        this.nodes = nodes;
    }


    @Override
    public String toString() {
        return "SavedFile{" +
                "content='" + content + '\'' +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", public='"+isPublic+"'"+
                '}';
    }

    public void readFile(File f){
        if(f.exists() && !f.isDirectory()) {
            try {
                Scanner s=new Scanner(f);
                StringBuilder sb=new StringBuilder();
                while(s.hasNext()){
                    sb.append(s.nextLine());
                }
                content=sb.toString();
            } catch (FileNotFoundException e) {
                AppConfig.timestampedStandardPrint("Error while reading file ");
            }
        }else if (!f.exists()){
            AppConfig.timestampedStandardPrint("file doesnt really exist");
            AppConfig.timestampedStandardPrint(f.getAbsolutePath());
        }else{
            AppConfig.timestampedStandardPrint("file is directory");
        }
    }
}

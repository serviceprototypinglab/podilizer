package ch.zhaw.file_operations;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ProjectCreator {
    String outPath;

    public ProjectCreator(String outPath) {
        this.outPath = outPath;
    }
    protected List<String> readDescriptor(){
        Scanner s = null;
        try {
            s = new Scanner(new File(outPath + "/PodilizerDescriptor.txt"));
        } catch (FileNotFoundException e) {
            System.err.print("Podilizer descriptor is not found!!!");
        }
        ArrayList<String> list = new ArrayList<>();
        while (s.hasNext()){
            list.add(s.next());
        }
        s.close();
        return list;
    }

}

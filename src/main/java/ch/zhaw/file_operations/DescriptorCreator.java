package ch.zhaw.file_operations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DescriptorCreator {
    public static void createDescriptor(List<String> paths, String newPath, String descriptorName){
        try {
            FileWriter fileWriter = new FileWriter(newPath + "/" + descriptorName);
            for (String str :
                    paths) {
                fileWriter.write(str + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static List<String> readDescriptor(String outPath, String descriptorName){
        Scanner s = null;
        try {
            s = new Scanner(new File(outPath + "/" + descriptorName));
        } catch (FileNotFoundException e) {
            System.err.print("Podilizer descriptor is not found!!! Please use 'translate' option before.");
        }
        ArrayList<String> list = new ArrayList<>();
        while (s.hasNext()){
            list.add(s.next());
        }
        s.close();
        return list;
    }
}

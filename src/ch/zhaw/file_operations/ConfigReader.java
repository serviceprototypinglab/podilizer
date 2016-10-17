package ch.zhaw.file_operations;

import org.ho.yaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;

public  class ConfigReader {

    public static YmlEntity getConfig() {
        YmlEntity config = null;
        try {
            config = Yaml.loadType(new File("/home/dord/IdeaProjects/service_tooling_initiative/jyaml.yml"), YmlEntity.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return config;
    }
}

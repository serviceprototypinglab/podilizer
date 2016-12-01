package ch.zhaw.file_operations;

import org.ho.yaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;

public  class ConfigReader {
    /**
     * Reads the .yml config file
     * @return the {@code YmlEntity} PoJo which represents config file
     */
    public static YmlEntity getConfig() {
        YmlEntity config = null;
        try {
            config = Yaml.loadType(new File("jyaml.yml"), YmlEntity.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return config;
    }
}

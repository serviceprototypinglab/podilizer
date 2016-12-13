package ch.zhaw.file_operations;

import org.ho.yaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

public class ConfigReader {
    /**
     * Reads the .yml config file
     *
     * @return the {@code YmlEntity} PoJo which represents config file
     */
    public static YmlEntity getConfig() {
        YmlEntity config = null;
        try {
            File confFile = new File("jyaml.yml");
            config = Yaml.loadType(confFile, YmlEntity.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return config;
    }
}

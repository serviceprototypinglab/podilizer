package ch.zhaw.file_operations;

import org.ho.yaml.Yaml;

import java.io.*;

public class ConfigReader {
    /**
     * Reads the .yml config file
     *
     * @param confPath is {@code String} that represents path to folder with config files
     * @return the {@code YmlEntity} PoJo which represents config file
     */
    public static YmlEntity getConfig(String confPath) {
        YmlEntity config = null;
        try {
            config = Yaml.loadType(new File(confPath + "/jyaml.yml"), YmlEntity.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        try {
//            awsAccessKeyId = Yaml.loadType(new File(""), YmlEntity.class).getAwsAccessKeyId();
//            awsSecretAccessKey = Yaml.loadType(new File(""), YmlEntity.class).getAwsSecretAccessKey();
//            regionName = Yaml.loadType(new File(""), YmlEntity.class).getAwsRegion();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        return config;
    }
}

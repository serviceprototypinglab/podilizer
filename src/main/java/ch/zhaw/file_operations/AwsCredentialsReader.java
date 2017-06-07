package ch.zhaw.file_operations;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AwsCredentialsReader {
    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private String region;
    // TODO: 4/6/17 Implement aws user's configurations handling
    /**
     * Reads credentials from aws default config file
     */
    public void read(){
        readFromFile("/.aws/credentials");
        readFromFile("/.aws/config");
    }
    private void readFromFile(String file){
        String awsCredentialsPath = System.getProperty("user.home") + file;
        try {
            List<String> lines = Files.readAllLines(Paths.get(awsCredentialsPath), Charset.forName("UTF-8"));
            for (String str :
                    lines) {
                readLine(String.valueOf(str));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public String getRegion() {
        return region;
    }

    /**
     * Reads aws keys from line
     * @param line {@link String}
     */
    private void readLine(String line){
        String accessKeyAttr = "aws_access_key_id = ";
        String secretKeyAttr = "aws_secret_access_key = ";
        String regionAttr = "region = ";
        if (line.startsWith(accessKeyAttr)){
            awsAccessKeyId = line.split(accessKeyAttr)[1];
        }
        if (line.startsWith(secretKeyAttr)){
            awsSecretAccessKey = line.split(secretKeyAttr)[1];
        }
        if (line.startsWith(regionAttr)){
            region = line.split(regionAttr)[1];
        }

    }
}

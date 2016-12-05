package ch.zhaw.file_operations;


/**
 * PoJo which represents .yml config file
 */
public class YmlEntity {
    private String path;
    private String newPath;
    private String region;
    private String role;
    private String awsAccessKeyId;
    private String awsSecretAccessKey;

    public String getPath() {
        return path;
    }

    public String getNewPath() {
        return newPath;
    }

    public String getRegion() {
        return region;
    }

    public String getRole() {
        return role;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }
}

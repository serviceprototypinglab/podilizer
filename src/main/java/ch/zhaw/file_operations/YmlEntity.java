package ch.zhaw.file_operations;


/**
 * PoJo which represents .yml config file
 */
public class YmlEntity {
    private String path;
    private String newPath;
    private String awsRegion;
    private String awsRole;
    private String awsAccessKeyId;
    private String awsSecretAccessKey;

    public String getPath() {
        return path;
    }

    public String getNewPath() {
        return newPath;
    }

    public String getAwsRegion() {
        return awsRegion;
    }

    public String getAwsRole() {
        return awsRole;
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

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }

    public void setAwsRole(String awsRole) {
        this.awsRole = awsRole;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }
}

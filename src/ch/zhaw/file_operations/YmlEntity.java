package ch.zhaw.file_operations;

public class YmlEntity {
    private String path;
    private String newPath;
    private String fileName;
    private String region;
    private String role;
    private String mavenHome;

    public String getPath() {
        return path;
    }

    public String getNewPath() {
        return newPath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRegion() {
        return region;
    }

    public String getRole() {
        return role;
    }

    public String getMavenHome() {
        return mavenHome;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setMavenHome(String mavenHome) {
        this.mavenHome = mavenHome;
    }
}

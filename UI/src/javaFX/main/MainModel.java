package javaFX.main;


public class MainModel {

    private String loadedFilePath;
    private int currentVersion;

    // Getter and Setter for loaded file path
    public String getLoadedFilePath() {
        return loadedFilePath;
    }

    public void setLoadedFilePath(String loadedFilePath) {
        this.loadedFilePath = loadedFilePath;
    }

    // Getter and Setter for current version
    public int getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(int currentVersion) {
        this.currentVersion = currentVersion;
    }
}

package sheetView.main;

import dto.impl.SheetDTOImpl;
import javafx.application.Platform;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class VersionRefresher {

    private static final String SERVER_URL = "http://localhost:8080/shticell";
    private final SheetViewMainController sheetViewMainController;
    private Timer timer;
    private int latestVersion;

    public VersionRefresher(SheetViewMainController sheetViewMainController) {
        this.sheetViewMainController = sheetViewMainController;
        this.latestVersion = 0;
    }

    public void startRefreshing() {
        timer = new Timer(true);
        timer.schedule(new VersionCheckTask(), 0, 2000);  // בדיקה כל 2 שניות
    }

    public void stopRefreshing() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public int getLatestVersion() {
        return latestVersion;
    }

    private class VersionCheckTask extends TimerTask {
        @Override
        public void run() {
            String finalUrl = HttpUrl
                    .parse(Constants.GET_LATEST_VERSION)
                    .newBuilder()
                    .addQueryParameter("sheetName", sheetViewMainController.getCurrentSheetName())
                    .build()
                    .toString();

            HttpClientUtil.runAsync(finalUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> sheetViewMainController.showErrorAlert("Failed to check for new version: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String latestVersionStr = response.body().string();
                        int newLatestVersion = Integer.parseInt(latestVersionStr);
                        SheetDTOImpl currentSheet = (SheetDTOImpl) sheetViewMainController.getCurrentSheet();
                        latestVersion = newLatestVersion; // עדכון הגרסה האחרונה

                        Platform.runLater(() -> {
                            if (newLatestVersion > currentSheet.getVersion()) {
                                sheetViewMainController.showVersionUpdateHint(latestVersion);
                            }
                        });
                    }
                }
            });
        }
    }
}

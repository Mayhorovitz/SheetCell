package sheetsManagement.components.availableSheets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.impl.SheetSummaryDTO;
import javafx.application.Platform;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import util.Constants;
import util.http.HttpClientUtil;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class AvailableSheetRefresher extends TimerTask {

    private final Consumer<List<SheetSummaryDTO>> tableConsumer;
    private boolean isActive = true;

    public AvailableSheetRefresher(Consumer<List<SheetSummaryDTO>> tableConsumer) {
        this.tableConsumer = tableConsumer;
    }

    @Override
    public void run() {
        if (!isActive) {
            return;
        }

        HttpClientUtil.runAsync(Constants.AVAILABLE_SHEETS_PAGE, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    // Handle failure (e.g., show an error message to the user)
                    System.err.println("Failed to load available sheets: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    List<SheetSummaryDTO> availableSheets = new Gson().fromJson(responseBody, new TypeToken<List<SheetSummaryDTO>>() {}.getType());

                    Platform.runLater(() -> tableConsumer.accept(availableSheets));
                } else {
                    Platform.runLater(() -> {
                        // Handle non-200 response (e.g., show an error message to the user)
                        System.err.println("Failed to load available sheets: " + response.message());
                    });
                }
            }
        });
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}

package util;

public class Constants {
    // Server resources locations
    public static final String BASE_DOMAIN = "localhost";

    private static final String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private static final String CONTEXT_PATH = "/shticell";
    private static final String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public static final String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public static final String AVAILABLE_SHEETS_PAGE = FULL_SERVER_PATH + "/availableSheets";;
    public static final String SHEET_PAGE = FULL_SERVER_PATH + "/sheet";
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/main/appMain.fxml";
    public static final String PERMISSIONS_TABLE_PAGE = FULL_SERVER_PATH + "/permissionsTable";
    public static final String UPLOAD_SHEET_PAGE = FULL_SERVER_PATH + "/uploadSheet";
    public static final String PERMISSION_REQUEST_PAGE = FULL_SERVER_PATH + "/permissionRequest";
    public static final String PERMISSION_RESPONSE_PAGE =  FULL_SERVER_PATH + "/permissionResponse";
    public static final String OWNED_SHEETS_PENDING_REQUESTS_PAGE =  FULL_SERVER_PATH +"/ownedSheetsPendingRequests";
    public static final String GET_SHEET_VERSION =  FULL_SERVER_PATH +"/getSheetVersion";

}
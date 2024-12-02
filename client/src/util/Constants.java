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

    public static final String GET_SHEET =  FULL_SERVER_PATH + "/getSheet" ;
    public static final String UPDATE_CELL =  FULL_SERVER_PATH + "/updateCell" ;
    public static final String GET_LATEST_VERSION =  FULL_SERVER_PATH + "/getLatestVersion" ;

    public static final String GET_UNIQUE_VALUES =  FULL_SERVER_PATH + "/getUniqueValues" ;
    public static final String FILTER_SHEET =  FULL_SERVER_PATH + "/filterSheet" ;
    public static final String SORT_SHEET =  FULL_SERVER_PATH + "/sortSheetRange" ;

    public static final String ADD_RANGE =  FULL_SERVER_PATH + "/addRange" ;
    public static final String DELETE_RANGE =  FULL_SERVER_PATH + "/deleteRange" ;

    public static final String UPDATE_BACKGROUND =  FULL_SERVER_PATH + "/updateBackgroundColor" ;
    public static final String UPDATE_TEXT =  FULL_SERVER_PATH + "/updateTextColor" ;
    public static final String RESET_CELL_DESIGN =  FULL_SERVER_PATH + "/resetCellDesign" ;
    public static final String GET_SHEET_VERSION =  FULL_SERVER_PATH +"/getSheetVersion";
    public static final String SINGLE_DYNAMIC_ANALYSIS = FULL_SERVER_PATH +"/singleDynamicAnalysis";
    public static final String DYNAMIC_ANALYSIS =  FULL_SERVER_PATH +"/dynamicAnalysis";

    public final static int REFRESH_RATE = 2000;

    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";
    public final static String CHAT_LINES_LIST = FULL_SERVER_PATH + "/chat";
    public final static String SEND_CHAT_LINE = FULL_SERVER_PATH + "/pages/chatroom/sendChat";
    public final static String CHAT_LINE_FORMATTING = "%tH:%tM:%tS | %.10s: %s%n";


}
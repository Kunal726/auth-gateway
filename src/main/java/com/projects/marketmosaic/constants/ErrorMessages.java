package com.projects.marketmosaic.constants;

public final class ErrorMessages {
    private ErrorMessages() {
        // Private constructor to prevent instantiation
    }

    // Auth related error messages
    public static final String NO_TOKEN_FOUND = "No valid token found";
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String TOKEN_BLACKLISTED = "Token has been invalidated";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
    public static final String EMAIL_IN_USE = "Email already in use";
    public static final String USERNAME_EXISTS = "Username already exists";
    public static final String INVALID_TOKEN_FORMAT = "Invalid token format";
    public static final String JWT_PARSE_ERROR = "Error parsing JWT token";
    public static final String INVALID_DATA = "Please enter valid data";

    // File related error messages
    public static final String INVALID_FILE_TYPE = "Invalid file type. Only image files are allowed";
    public static final String FILE_SIZE_EXCEEDED = "File size exceeds maximum limit";
    public static final String FILE_UPLOAD_ERROR = "Error uploading file";
    public static final String FILE_DELETE_ERROR = "Error deleting file";

    // Address related error messages
    public static final String ADDRESS_NOT_FOUND = "Address not found";
    public static final String UNAUTHORIZED_ADDRESS_ACCESS = "Unauthorized to access this address";
    public static final String DEFAULT_ADDRESS_EXISTS = "A default address already exists";

    // Seller related error messages
    public static final String SELLER_NOT_FOUND = "Seller not found";
    public static final String SELLER_STATUS_PENDING = "Seller status is pending approval";
    public static final String SELLER_STATUS_REJECTED = "Seller status has been rejected";
    public static final String SELLER_STATUS_SUSPENDED = "Seller account is suspended";

    // Generic error messages
    public static final String SYSTEM_ERROR = "An unexpected error occurred";
    public static final String VALIDATION_ERROR = "Validation error";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String FORBIDDEN = "Forbidden";
}
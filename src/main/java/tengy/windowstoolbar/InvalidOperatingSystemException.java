package tengy.windowstoolbar;

public final class InvalidOperatingSystemException extends RuntimeException {

    InvalidOperatingSystemException() {
        super("Invalid operation system detected");
    }

}

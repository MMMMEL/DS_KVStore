package utility;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceLog {
    private final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void warnLog (String message) {
        logger.log(Level.WARNING, message);
    }

    public static void infoLog (String message) {
        logger.log(Level.INFO, message);
    }
}

package mods.belgabor.mcpatcher;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Belgabor on 23.06.2016.
 */
public class MCPatcherLogger {
    public static Logger logger = LogManager.getLogger(MCPatcherConstants.MOD_ID);

    public static void log(Level level, String message, Object ... args) {
        if (logger != null)
            logger.log(level, String.format(message, args));
        else
            System.out.println(String.format(message, args));
    }
    
    public static void info(String message, Object ... args) {
        log(Level.INFO, message, args);
    }

    public static void error(String message, Object ... args) {
        log(Level.ERROR, message, args);
    }

}

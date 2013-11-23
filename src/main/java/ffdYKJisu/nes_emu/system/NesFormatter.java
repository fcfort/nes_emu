/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ffdYKJisu.nes_emu.system;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.LogRecord;

/**
 * This class is largely ripped from SimpleFormatter.java
 * @author Administrator
 */
public class NesFormatter extends java.util.logging.Formatter {

    Date dat = new Date();
    private final static String format = "{0,date} {0,time}";
    private MessageFormat formatter;

    private Object args[] = new Object[1];

    // Line separator string.  This is the value of the line.separator
    // property at the moment that the SimpleFormatter was created.
    private String lineSeparator = (String) java.security.AccessController.doPrivileged(
               new sun.security.action.GetPropertyAction("line.separator"));

    /**
     * Format the given LogRecord.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord record) {
	StringBuffer sb = new StringBuffer();
	// Minimize memory allocations here.
	dat.setTime(record.getMillis());
	args[0] = dat;
	StringBuffer text = new StringBuffer();
	if (formatter == null) {
	    formatter = new MessageFormat(format);
	}
	formatter.format(args, text, null);
	
	// Append date
	sb.append(text);
	sb.append(" ");
	// Append class/method name
	if (record.getSourceClassName() != null) {	
	    sb.append(record.getSourceClassName());
	} else {
	    sb.append(record.getLoggerName());
	}
	// Append function name
	if (record.getSourceMethodName() != null) {	
	    sb.append(" ");
	    sb.append(record.getSourceMethodName());
		sb.append("()");
	}
	
	sb.append(", ");
	
	String message = formatMessage(record);
	sb.append(record.getLevel().getLocalizedName());
	sb.append(": ");
	sb.append(message);
	sb.append(lineSeparator);
	
	
	if (record.getThrown() != null) {
	    try {
	        StringWriter sw = new StringWriter();
	        PrintWriter pw = new PrintWriter(sw);
	        record.getThrown().printStackTrace(pw);
	        pw.close();
		sb.append(sw.toString());
	    } catch (Exception ex) {
	    }
	}
	return sb.toString();
    }
}

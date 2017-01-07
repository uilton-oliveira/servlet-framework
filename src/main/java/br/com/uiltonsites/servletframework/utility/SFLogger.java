/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.uiltonsites.servletframework.utility;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * This class encapsulate an Logger from Log4j to implement an Token.
 *
 * @author Uilton Oliveira <uilton.dev@gmail.com>
 */
public class SFLogger implements org.slf4j.Logger {

    /**
     * The fully qualified name of the Logger class. See also the
     * getFQCN method.
     */
    protected static final String FQCN = SFLogger.class.getName();
    public String token;
    
    protected final String TAG_START = "##### START #####";
    protected final String TAG_END = "##### END #####";
    public final Logger target;
    
    public static final String LEVEL_OFF = "OFF";
    public static final String LEVEL_TRACE = "TRACE";
    public static final String LEVEL_DEBUG = "DEBUG";
    public static final String LEVEL_INFO = "INFO";
    public static final String LEVEL_WARN = "WARN";
    public static final String LEVEL_ERROR = "ERROR";
    public static final String LEVEL_FATAL = "FATAL";
    
    public static String PREFIX = "";
    public static String SUFIX = "";
    

    private SFLogger(Class clazz) {
        this.target = Logger.getLogger(clazz);
        token = SFToken.getToken();
    }
    
    private SFLogger(String name) {
        this.target = Logger.getLogger(name);
        token = SFToken.getToken();
    }
    
    private SFLogger(Class clazz, String token) {
        this.target = Logger.getLogger(clazz);
        this.token = token;
    }
    
    private SFLogger(String name, String token) {
        this.target = Logger.getLogger(name);
        this.token = token;
    }
    
    static public SFLogger getLogger(String name) {
        SFLogger self = new SFLogger(name);
        return self;
    }

    static public SFLogger getLogger(Class clazz) {
        SFLogger self = new SFLogger(clazz);
        return self;
    }
    
    static public SFLogger getLogger(String name, String token) {
        SFLogger self = new SFLogger(name, token);
        return self;
    }

    static public SFLogger getLogger(Class clazz, String token) {
        SFLogger self = new SFLogger(clazz, token);
        return self;
    }

    public void newToken() {
        this.token = SFToken.getToken();
    }
    
    public void newToken(String token) {
        this.token = token;
    }
    
    public void log(String message, Level level, Throwable t) {
        
        if (!isLevelEnabled(level)) return;
        target.log(FQCN, level, tokenFormat(message), t);
    }
    
    public void log(String message, Level level) {
        if (!isLevelEnabled(level)) return;
        target.log(FQCN, level, tokenFormat(message), null);
    }
    
    public void log(Level level, String message, Throwable t) {
        if (!isLevelEnabled(level)) return;
        target.log(FQCN, level, tokenFormat(message), t);
    }
    
    public void log(Level level, String message) {
        if (!isLevelEnabled(level)) return;
        target.log(FQCN, level, tokenFormat(message), null);
    }
    
    public void log(Level level, Throwable t) {
        if (!isLevelEnabled(level)) return;
        target.log(FQCN, level, tokenFormat(t.getMessage()), t);
    }
    
    public void log(String message, String level) {
        Level lvl = Level.toLevel(level);
        if (!isLevelEnabled(lvl)) return;
        target.log(FQCN, lvl, tokenFormat(message), null);
    }
    
    
    public void log(String message, String level, Throwable t) {
        Level lvl = Level.toLevel(level);
        if (!isLevelEnabled(lvl)) return;
        target.log(FQCN, lvl, tokenFormat(message), t);
    }
        
    // not working
//    public void all(Object message) {
//        target.log(FQCN, Level.ALL, tokenFormat(message), null);
//    }

    @Override
    public void trace(String message, Throwable t) {
        if (!isLevelEnabled(Level.TRACE)) return;
        target.log(FQCN, Level.TRACE, tokenFormat(message), t);
    }
    
    public void trace(Throwable t) {
        if (!isLevelEnabled(Level.TRACE)) return;
        target.log(FQCN, Level.TRACE, tokenFormat(t.getMessage()), t);
    }
    
    @Override
    public void trace(String message) {
        if (!isLevelEnabled(Level.TRACE)) return;
        target.log(FQCN, Level.TRACE, tokenFormat(message), null);
    }

    @Override
    public void debug(String message, Throwable t) {
        if (!isLevelEnabled(Level.DEBUG)) return;
        target.log(FQCN, Level.DEBUG, tokenFormat(message), t);
    }
    
    @Override
    public void debug(String message) {
        if (!isLevelEnabled(Level.DEBUG)) return;
        target.log(FQCN, Level.DEBUG, tokenFormat(message), null);
    }
    
    public void debug(Throwable t) {
        if (!isLevelEnabled(Level.DEBUG)) return;
        target.log(FQCN, Level.DEBUG, tokenFormat(t.getMessage()), t);
    }
    
    @Override
    public void info(String message, Throwable t) {
        if (!isLevelEnabled(Level.INFO)) return;
        target.log(FQCN, Level.INFO, tokenFormat(message), t);
    }
    
    @Override
    public void info(String message) {
        if (!isLevelEnabled(Level.INFO)) return;
        target.log(FQCN, Level.INFO, tokenFormat(message), null);
    }
    
    public void info(Throwable t) {
        if (!isLevelEnabled(Level.INFO)) return;
        target.log(FQCN, Level.INFO, tokenFormat(t.getMessage()), t);
    }
    
    @Override
    public void warn(String message, Throwable t) {
        if (!isLevelEnabled(Level.WARN)) return;
        target.log(FQCN, Level.WARN, tokenFormat(message), t);
    }
    
    @Override
    public void warn(String message) {
        if (!isLevelEnabled(Level.WARN)) return;
        target.log(FQCN, Level.WARN, tokenFormat(message), null);
    }
    
    public void warn(Throwable t) {
        if (!isLevelEnabled(Level.WARN)) return;
        target.log(FQCN, Level.WARN, tokenFormat(t.getMessage()), t);
    }
    
    @Override
    public void error(String message, Throwable t) {
        if (!isLevelEnabled(Level.ERROR)) return;
        target.log(FQCN, Level.ERROR, tokenFormat(message), t);
    }
    
    public void error(Throwable t) {
        if (!isLevelEnabled(Level.ERROR)) return;
        target.log(FQCN, Level.ERROR, tokenFormat(t.getMessage()), t);
    }
    
    @Override
    public void error(String message) {
        if (!isLevelEnabled(Level.ERROR)) return;
        target.log(FQCN, Level.ERROR, tokenFormat(message), null);
    }
    
    public void fatal(String message, Throwable t) {
        if (!isLevelEnabled(Level.FATAL)) return;
        target.log(FQCN, Level.FATAL, tokenFormat(message), t);
    }
    
    public void fatal(String message) {
        if (!isLevelEnabled(Level.FATAL)) return;
        target.log(FQCN, Level.FATAL, tokenFormat(message), null);
    }
    
    public void fatal(Throwable t) {
        if (!isLevelEnabled(Level.FATAL)) return;
        target.log(FQCN, Level.FATAL, tokenFormat(t.getMessage()), t);
    }
    
    public void start() {
        target.log(FQCN, Level.TRACE, start(""), null);
    }
    
    
    public void end() {
        target.log(FQCN, Level.TRACE, end(""), null);
    }
    
    public boolean isLevelEnabled(Level level) {
        switch(level.toString()) {
            case LEVEL_OFF:
                return false;
            case LEVEL_TRACE:
                if (!isTraceEnabled()) return false;
                break;
            case LEVEL_DEBUG:
                if (!isDebugEnabled()) return false;
                break;
            case LEVEL_INFO:
                if (!isInfoEnabled()) return false;
                break;
            case LEVEL_WARN:
                if (!isWarnEnabled()) return false;
                break;
            case LEVEL_ERROR:
                if (!isErrorEnabled()) return false;
                break;
        }
        return true;
    }
    

    @Override
    public boolean isTraceEnabled() {
        return target.isTraceEnabled();
    }
    
    @Override
    public boolean isDebugEnabled() {
        return target.isDebugEnabled();
    }
    
    @Override
    public boolean isInfoEnabled() {
        return target.isInfoEnabled();
    }

    
    /**
     * For formatted messages, first substitute arguments and then log.
     *
     * @param format
     * @param arg1
     * @param arg2
     */
    private FormattingTuple format(String format, Object arg1, Object arg2) {
        FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
        return tp;
    }

    /**
     * For formatted messages, first substitute arguments and then log.
     *
     * @param format
     * @param arguments a list of 3 ore more arguments
     */
    private FormattingTuple format(String format, Object... arguments) {
        FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
        return tp;
    }
    
    
    private String tokenFormat(Object msg) {
        if (msg == null) {
            return "";
        }
        if (token != null && !token.isEmpty()) {
            return "["+token+"] " + getPrefix() + msg.toString() + getSufix();
        } else {
            return getPrefix() + msg.toString() + getSufix();
        }
    }
    
    private String start(String msg) {
        if (msg != null && !msg.isEmpty()) {
            return "["+token+"] #" + msg+" => " + TAG_START;
        } else {
            return "["+token+"] " + TAG_START;
        }
    }
    
    private String end(String msg) {
        if (msg != null && !msg.isEmpty()) {
            return "["+token+"] #" + msg+" => " + TAG_END;
        } else {
            return "["+token+"] " + TAG_END;
        }
    }
    
    public String getPrefix() {
        if (PREFIX == null || PREFIX.isEmpty()) {
            return "";
        } else {
            return "[" + PREFIX + "] ";
        }
    }
    
    public String getSufix() {
        if (SUFIX == null || SUFIX.isEmpty()) {
            return "";
        } else {
            return " [" + SUFIX + "]";
        }
    }

    @Override
    public String getName() {
        return "ServletFramework";
    }

    @Override
    public void trace(String string, Object o) {
        
        if (!isLevelEnabled(Level.TRACE)) return;
        
        if (o instanceof Throwable) {
            target.log(FQCN, Level.TRACE, tokenFormat(string), (Throwable)o);
        } else {
            FormattingTuple ft = format(string, o);
            target.log(FQCN, Level.TRACE, tokenFormat(ft.getMessage()), ft.getThrowable());
        }
    }

    @Override
    public void trace(String string, Object o, Object o1) {
        if (!isLevelEnabled(Level.TRACE)) return;
        
        FormattingTuple ft = format(string, o, o1);
        target.log(FQCN, Level.TRACE, tokenFormat(ft.getMessage()), ft.getThrowable());
        
    }

    @Override
    public void trace(String string, Object... os) {
        if (!isLevelEnabled(Level.TRACE)) return;
        
        FormattingTuple ft = format(string, os);
        target.log(FQCN, Level.TRACE, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return target.isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String string) {
        if (!isLevelEnabled(Level.TRACE)) return;
        
        FormattingTuple ft = format(string);
        target.log(FQCN, Level.TRACE, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void trace(Marker marker, String string, Object o) {
        if (!isLevelEnabled(Level.TRACE)) return;
        FormattingTuple ft = format(string, o);
        target.log(FQCN, Level.TRACE, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void trace(Marker marker, String string, Object o, Object o1) {
        if (!isLevelEnabled(Level.TRACE)) return;
        FormattingTuple ft = format(string, o, o1);
        target.log(FQCN, Level.TRACE, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void trace(Marker marker, String string, Object... os) {
        if (!isLevelEnabled(Level.TRACE)) return;
        FormattingTuple ft = format(string, os);
        target.log(FQCN, Level.TRACE, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void trace(Marker marker, String string, Throwable thrwbl) {
        if (!isLevelEnabled(Level.TRACE)) return;
        target.log(FQCN, Level.TRACE, tokenFormat(string), thrwbl);
    }

    @Override
    public void debug(String string, Object o) {
        if (!isLevelEnabled(Level.DEBUG)) return;
        
        if (o instanceof Throwable) {
            target.log(FQCN, Level.DEBUG, tokenFormat(string), (Throwable)o);
        } else {
            FormattingTuple ft = format(string, o);
            target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
        }
    }

    @Override
    public void debug(String string, Object o, Object o1) {
        if (!isLevelEnabled(Level.DEBUG)) return;
        
        FormattingTuple ft = format(string, o, o1);
        target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void debug(String string, Object... os) {
        if (!isLevelEnabled(Level.DEBUG)) return;
        
        FormattingTuple ft = format(string, os);
        target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
        
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return target.isDebugEnabled();
    }

    @Override
    public void debug(Marker marker, String string) {
        if (!isLevelEnabled(Level.DEBUG)) return;
        
        FormattingTuple ft = format(string);
        target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void debug(Marker marker, String string, Object o) {
        if (!isLevelEnabled(Level.DEBUG)) return;
        
        FormattingTuple ft = format(string, o);
        target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void debug(Marker marker, String string, Object o, Object o1) {
        if (!isLevelEnabled(Level.DEBUG)) return;
        
        FormattingTuple ft = format(string, o, o1);
        target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void debug(Marker marker, String string, Object... os) {
        if (!isLevelEnabled(Level.DEBUG)) return;
        
        FormattingTuple ft = format(string, os);
        target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void debug(Marker marker, String string, Throwable thrwbl) {
        if (!isLevelEnabled(Level.DEBUG)) return;
        
        target.log(FQCN, Level.DEBUG, tokenFormat(string), thrwbl);
    }

    @Override
    public void info(String string, Object o) {
        if (!isLevelEnabled(Level.INFO)) return;
        
        if (o instanceof Throwable) {
            target.log(FQCN, Level.INFO, tokenFormat(string), (Throwable)o);
        } else {
            FormattingTuple ft = format(string, o);
            target.log(FQCN, Level.INFO, tokenFormat(ft.getMessage()), ft.getThrowable());
        }
        
    }

    @Override
    public void info(String string, Object o, Object o1) {
        if (!isLevelEnabled(Level.INFO)) return;
        
        FormattingTuple ft = format(string, o, o1);
        target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void info(String string, Object... os) {
        if (!isLevelEnabled(Level.INFO)) return;
        
        FormattingTuple ft = format(string, os);
        target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return target.isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String string) {
        if (!isLevelEnabled(Level.INFO)) return;
        
        FormattingTuple ft = format(string);
        target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void info(Marker marker, String string, Object o) {
        if (!isLevelEnabled(Level.INFO)) return;
        
        FormattingTuple ft = format(string, o);
        target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void info(Marker marker, String string, Object o, Object o1) {
        if (!isLevelEnabled(Level.INFO)) return;
        
        FormattingTuple ft = format(string, o, o1);
        target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void info(Marker marker, String string, Object... os) {
        if (!isLevelEnabled(Level.INFO)) return;
        
        FormattingTuple ft = format(string, os);
        target.log(FQCN, Level.DEBUG, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void info(Marker marker, String string, Throwable thrwbl) {
        if (!isLevelEnabled(Level.INFO)) return;
        
        target.log(FQCN, Level.DEBUG, tokenFormat(string), thrwbl);
    }

    @Override
    public boolean isWarnEnabled() {
        if (target.getLoggerRepository().isDisabled(Level.WARN_INT)) {
            return false;
        }
        return Level.WARN.isGreaterOrEqual(target.getEffectiveLevel());
    }

    @Override
    public void warn(String string, Object o) {
        if (!isLevelEnabled(Level.WARN)) return;
        
        if (o instanceof Throwable) {
            target.log(FQCN, Level.WARN, tokenFormat(string), (Throwable)o);
        } else {
            FormattingTuple ft = format(string, o);
            target.log(FQCN, Level.WARN, tokenFormat(ft.getMessage()), ft.getThrowable());
        }
    }

    @Override
    public void warn(String string, Object... os) {
        if (!isLevelEnabled(Level.WARN)) return;
        
        FormattingTuple ft = format(string, os);
        target.log(FQCN, Level.WARN, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void warn(String string, Object o, Object o1) {
        if (!isLevelEnabled(Level.WARN)) return;
        
        FormattingTuple ft = format(string, o, o1);
        target.log(FQCN, Level.WARN, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        if (target.getLoggerRepository().isDisabled(Level.WARN_INT)) {
            return false;
        }
        return Level.WARN.isGreaterOrEqual(target.getEffectiveLevel());
    }

    @Override
    public void warn(Marker marker, String string) {
        if (!isLevelEnabled(Level.WARN)) return;
        
        FormattingTuple ft = format(string);
        target.log(FQCN, Level.WARN, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void warn(Marker marker, String string, Object o) {
        if (!isLevelEnabled(Level.WARN)) return;
        
        FormattingTuple ft = format(string, o);
        target.log(FQCN, Level.WARN, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void warn(Marker marker, String string, Object o, Object o1) {
        if (!isLevelEnabled(Level.WARN)) return;
        
        FormattingTuple ft = format(string, o, o1);
        target.log(FQCN, Level.WARN, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void warn(Marker marker, String string, Object... os) {
        if (!isLevelEnabled(Level.WARN)) return;
        
        FormattingTuple ft = format(string, os);
        target.log(FQCN, Level.WARN, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void warn(Marker marker, String string, Throwable thrwbl) {
        if (!isLevelEnabled(Level.WARN)) return;
        
        target.log(FQCN, Level.WARN, tokenFormat(string), thrwbl);
    }

    @Override
    public boolean isErrorEnabled() {
        if (target.getLoggerRepository().isDisabled(Level.ERROR_INT)) {
            return false;
        }
        return Level.ERROR.isGreaterOrEqual(target.getEffectiveLevel());
    }

    public boolean isFatalEnabled() {
        if (target.getLoggerRepository().isDisabled(Level.FATAL_INT)) {
            return false;
        }
        return Level.FATAL.isGreaterOrEqual(target.getEffectiveLevel());
    }

    @Override
    public void error(String string, Object o) {
        if (!isLevelEnabled(Level.ERROR)) return;
        
        if (o instanceof Throwable) {
            target.log(FQCN, Level.ERROR, tokenFormat(string), (Throwable)o);
        } else {
            FormattingTuple ft = format(string, o);
            target.log(FQCN, Level.ERROR, tokenFormat(ft.getMessage()), ft.getThrowable());
        }
    }

    @Override
    public void error(String string, Object o, Object o1) {
        if (!isLevelEnabled(Level.ERROR)) return;
        
        FormattingTuple ft = format(string, o, o1);
        target.log(FQCN, Level.ERROR, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void error(String string, Object... os) {
        if (!isLevelEnabled(Level.ERROR)) return;
        
        FormattingTuple ft = format(string, os);
        target.log(FQCN, Level.ERROR, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        if (target.getLoggerRepository().isDisabled(Level.ERROR_INT)) {
            return false;
        }
        return Level.ERROR.isGreaterOrEqual(target.getEffectiveLevel());
    }

    @Override
    public void error(Marker marker, String string) {
        if (!isLevelEnabled(Level.ERROR)) return;
        
        FormattingTuple ft = format(string);
        target.log(FQCN, Level.ERROR, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void error(Marker marker, String string, Object o) {
        if (!isLevelEnabled(Level.ERROR)) return;
        
        FormattingTuple ft = format(string, o);
        target.log(FQCN, Level.ERROR, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void error(Marker marker, String string, Object o, Object o1) {
        if (!isLevelEnabled(Level.ERROR)) return;
        
        FormattingTuple ft = format(string, o, o1);
        target.log(FQCN, Level.ERROR, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void error(Marker marker, String string, Object... os) {
        if (!isLevelEnabled(Level.ERROR)) return;
        
        FormattingTuple ft = format(string, os);
        target.log(FQCN, Level.ERROR, tokenFormat(ft.getMessage()), ft.getThrowable());
    }

    @Override
    public void error(Marker marker, String string, Throwable thrwbl) {
        if (!isLevelEnabled(Level.ERROR)) return;
        
        target.log(FQCN, Level.ERROR, tokenFormat(string), thrwbl);
    }

    
}

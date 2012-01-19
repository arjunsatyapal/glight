package com.google.light.util;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

public class ServletUtils {
	/**
     * Forwards request and response to given path. Handles any
     * exceptions caused by forward target by printing them to logger.
     * 
     * @param request
     * @param response
     * @param path
     */
    public static void forward(final HttpServletRequest request,
            final HttpServletResponse response, final String path, Logger log) {
        try {
            final RequestDispatcher rd = request.getRequestDispatcher(path);
            rd.forward(request, response);
        }
        catch (final Throwable tr) {
            if (log!=null && log.isErrorEnabled()) {
                log.error("Cought Exception: " + tr.getMessage());
                log.debug("StackTrace:", tr);
            }
        }
    }
    
    public static void forward(final HttpServletRequest request,
            final HttpServletResponse response, final String path) {
    	forward(request, response, path, null);
    }

}

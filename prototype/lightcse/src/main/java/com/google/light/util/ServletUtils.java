package com.google.light.util;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
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
    
    
	public static String createUrl(String base, Object... queryKeysAndValues)
			throws URIException {
		Object lastKey = null;
		NameValuePair[] pairs = new NameValuePair[queryKeysAndValues.length/2];
		for(int i=0;i<queryKeysAndValues.length;i++) {
			if(i%2==0)
				lastKey = queryKeysAndValues[i];
			else
				pairs[i/2] = new NameValuePair(lastKey.toString(), queryKeysAndValues[i].toString());
				
		}
		HttpMethod method = new GetMethod(base);
		method.setQueryString(pairs);

		return method.getURI().getEscapedURI();

	}
	
	public static String getBaseUrl(HttpServletRequest req) {
	    String scheme = req.getScheme();             // http
	    String serverName = req.getServerName();     // hostname.com
	    int serverPort = req.getServerPort();        // 80
	    String contextPath = req.getContextPath();   // /mywebapp

	    return scheme+"://"+serverName+":"+serverPort+contextPath;
	}

}

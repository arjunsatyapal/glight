package com.google.light.util;

import javax.servlet.http.HttpServletRequest;

public interface AjaxService {
	Object execute(HttpServletRequest req);
}

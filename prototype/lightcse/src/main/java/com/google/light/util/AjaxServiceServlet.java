package com.google.light.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.light.lessons.AddLinkToLessonService;
import com.google.light.lessons.CreateLessonService;
import com.google.light.lessons.RemoveLinkFromLessonService;

public class AjaxServiceServlet extends HttpServlet {
	
	public AjaxServiceServlet() {
		
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Gson gson = new GsonBuilder().serializeNulls().create();
		String path = req.getServletPath()+req.getPathInfo();
		AjaxService service = getService(path.substring("/ajax/".length()));
		if(service != null) {
			resp.getWriter().print(gson.toJson(service.execute(req)));
			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");
		}
	}
	
	// FIXME, really dirty implementation
	private AjaxService getService(String name) {
		if(name.equals("createLesson")) {
			return new CreateLessonService();
		}
		if(name.equals("addLinkToLesson")) {
			return new AddLinkToLessonService();
		}
		if(name.equals("removeLinkFromLesson")) {
			return new RemoveLinkFromLessonService();
		}
		
		return null;
	}
	
}

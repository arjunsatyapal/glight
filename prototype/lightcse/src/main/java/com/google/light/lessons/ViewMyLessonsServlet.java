package com.google.light.lessons;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.light.util.ServletUtils;

public class ViewMyLessonsServlet extends HttpServlet {
	private static final Logger log = LoggerFactory
		    .getLogger(ViewMyLessonsServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		LessonDAO ldao = new LessonDAO();

		List<LessonEntity> lessons = ldao.list();
		request.setAttribute("lessons", lessons);
		request.setAttribute("lessonsCount", lessons.size());
		request.setAttribute("baseUrl", ServletUtils.getBaseUrl(request));
		
		ServletUtils.forward(request, response, "mylessons.jsp", log);
	}

}
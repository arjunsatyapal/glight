package com.google.light.lessons;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.light.search.ReviewResourceServlet;
import com.google.light.search.ReviewResourceServlet.LessonDTO;
import com.google.light.util.ServletUtils;

public class ViewLessonServlet extends HttpServlet {
	private static final Logger log = LoggerFactory
		    .getLogger(ViewLessonServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		LessonDAO ldao = new LessonDAO();
		LessonEntity lesson = null;
		try {
			lesson = ldao.find(Long.parseLong(request.getParameter("id")));
		} catch (Exception e) {
			throw new RuntimeException("Lesson not found!");
		}
		
		request.setAttribute("lesson", new Gson().toJson(lesson));
		
		ServletUtils.forward(request, response, "lesson.jsp", log);
	}

}
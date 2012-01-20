package com.google.light.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.light.lessons.LessonDAO;
import com.google.light.lessons.LessonEntity;
import com.google.light.util.ServletUtils;

public class ReviewResourceServlet extends HttpServlet {
	private static final Logger log = LoggerFactory
		    .getLogger(ReviewResourceServlet.class);
	
	public static class LessonDTO {
		long id;
		String name;
		boolean hasThisLink;
		public LessonDTO(long id, String name, boolean hasThisLink) {
			this.id = id;
			this.name = name;
			this.hasThisLink = hasThisLink;
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String link = request.getParameter("link");
		if(link == null) {
			throw new RuntimeException("No link");
		}
		request.setAttribute("link", StringEscapeUtils.escapeHtml(link));
		request.setAttribute("jslink", StringEscapeUtils.escapeJavaScript(link));
		
		String query = request.getParameter("q");
		String backToSearchUrl = "/";
		if(query != null && !"".equals(query)) {
			backToSearchUrl = ServletUtils.createUrl("/search","q",query);
		}
		request.setAttribute("backToSearchUrl", StringEscapeUtils.escapeJavaScript(backToSearchUrl));
		
		LessonDAO ldao = new LessonDAO();
		ArrayList<LessonDTO> lessonDTOList = new ArrayList<LessonDTO>();
		for(LessonEntity lesson : ldao.list()) {
			lessonDTOList.add(new LessonDTO(lesson.getId(), lesson.getName(), lesson.hasLink(link)));
		}
		request.setAttribute("lessons", new Gson().toJson(lessonDTOList));
		
		ServletUtils.forward(request, response, "review.jsp", log);
	}

}

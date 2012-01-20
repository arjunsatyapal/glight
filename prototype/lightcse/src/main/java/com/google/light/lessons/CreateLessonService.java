package com.google.light.lessons;

import javax.servlet.http.HttpServletRequest;

import com.google.light.util.AjaxService;

import com.google.light.search.ReviewResourceServlet.LessonDTO;

public class CreateLessonService implements AjaxService {
	
	@Override
	public Object execute(HttpServletRequest req) {
		LessonDAO dao = new LessonDAO();
		String name = req.getParameter("name");
		if(name!=null && !"".equals(name)) {
			LessonEntity lesson = new LessonEntity();
			lesson.setName(name);
			dao.put(lesson);
			return new LessonDTO(lesson.getId(),lesson.getName(), false);
		}
		return "ERROR";
	}
	
}

package com.google.light.lessons;

import javax.servlet.http.HttpServletRequest;

import com.google.light.util.AjaxService;

public class AddLinkToLessonService implements AjaxService {

	@Override
	public Object execute(HttpServletRequest req) {
		LessonDAO dao = new LessonDAO();
		String lessonId = req.getParameter("lesson");
		String link = req.getParameter("link");
		LessonEntity lesson = null;
		try {
			lesson = dao.find(Long.parseLong(lessonId));
		} catch (NumberFormatException e) {
			
		}
		if(lesson!=null && link!=null && !"".equals(link)) {
			lesson.addLink(link);
			dao.put(lesson);
			return "OK";
		}
		return "ERROR";
	}
	
}

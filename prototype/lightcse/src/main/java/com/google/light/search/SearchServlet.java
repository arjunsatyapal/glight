package com.google.light.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class SearchServlet extends HttpServlet {
	private static final Logger log = LoggerFactory
		    .getLogger(SearchServlet.class);
	
	public static class LessonDTO {
		long id;
		String name;
		List<String> links = new ArrayList<String>();
		public LessonDTO(long id, String name) {
			this.id = id;
			this.name = name;
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int page = 1;
		try {
			page = Integer.parseInt(request.getParameter("page"));
		} catch (Exception e) {
			
		}
		
		String query = request.getParameter("q");
		if(query == null)
			query = "";
		boolean hasNextPage = false;
		String suggestion = null;
		String suggestionQuery = null;
		List<SearchResultItem> searchResults = new ArrayList<SearchResultItem>();
		Set<String> presentedLinks = new HashSet<String>();
		
		if(!"".equals(query)) {
			SearchResult searchResult = new GSSSearchProvider().search(query, page);
			hasNextPage = searchResult.hasNextPage();
			suggestion = searchResult.getSuggestion();
			suggestionQuery = searchResult.getSuggestionQuery();
			for(SearchResultItem result : searchResult.getItems()) {
				presentedLinks.add(result.getLink());
				result.setLink(
						StringEscapeUtils.escapeHtml(result.getLink())
						);
				searchResults.add(result);
			}
		}
		
		request.setAttribute("query", StringEscapeUtils.escapeHtml(query));
		request.setAttribute("searchResults", searchResults);
		request.setAttribute("searchResultsCount", searchResults.size());
		request.setAttribute("hasNextPage", hasNextPage);
		if(hasNextPage)
			request.setAttribute("nextPageLink", StringEscapeUtils.escapeHtml(ServletUtils.createUrl("/search","q",query,"page",""+(page+1))));

		request.setAttribute("hasPrevPage", page>1);
		if(page>1) {
			request.setAttribute("prevPageLink", StringEscapeUtils.escapeHtml(ServletUtils.createUrl("/search","q",query,"page",""+(page-1))));
		}
		
		request.setAttribute("page", page);
		
		request.setAttribute("showSuggestion", suggestion != null);
		if(suggestion != null) {
			request.setAttribute("suggestion", suggestion);
			request.setAttribute("suggestionUrl", StringEscapeUtils.escapeHtml(ServletUtils.createUrl("/search","q",suggestionQuery)));
		}
		
		request.setAttribute("showNoResultsMessage", page==1 && searchResults.size() == 0 && !"".equals(query));
		
		
		LessonDAO ldao = new LessonDAO();
		ArrayList<LessonDTO> lessonDTOList = new ArrayList<LessonDTO>();
		for(LessonEntity lesson : ldao.list()) {
			LessonDTO lessonDTO = new LessonDTO(lesson.getId(), lesson.getName());
			for(String link : lesson.getLinks()) {
				if(presentedLinks.contains(link))
					lessonDTO.links.add(link);
			}
			lessonDTOList.add(lessonDTO);
		}
		request.setAttribute("lessons", new Gson().toJson(lessonDTOList));
		
		
		ServletUtils.forward(request, response, "search.jsp", log);
	}
	
}

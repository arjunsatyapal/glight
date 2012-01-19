package com.google.light.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.light.util.ServletUtils;

import web.IndexServlet;

public class SearchServlet extends HttpServlet {
	private static final Logger log = LoggerFactory
		    .getLogger(SearchServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String query = request.getParameter("q");
		List<SearchResult> searchResults;
		if(query != null && !"".equals(query)) {
			searchResults = new CSESearchProvider().search(query);
		} else {
			searchResults = new ArrayList<SearchResult>();
		}
		request.setAttribute("searchResults", searchResults);
		request.setAttribute("searchResultsCount", searchResults.size());
		
		ServletUtils.forward(request, response, "search.jsp", log);
	}
	
}

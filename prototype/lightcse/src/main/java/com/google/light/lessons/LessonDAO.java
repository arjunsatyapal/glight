package com.google.light.lessons;

import java.util.List;

import com.google.light.util.AbstractDAO;

public class LessonDAO extends AbstractDAO<LessonEntity> {

	@Override
	protected Class<LessonEntity> getEntityClass() {
		return LessonEntity.class;
	}
	

	public List<LessonEntity> list() {
		return odao.ofy().query(getEntityClass()).list();
	}
	
}

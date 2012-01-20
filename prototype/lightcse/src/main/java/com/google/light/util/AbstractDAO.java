package com.google.light.util;

import java.util.List;

import com.google.light.lessons.LessonEntity;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.helper.DAOBase;

public abstract class AbstractDAO<T> {
	protected ObjectifyDAO odao;

	static {
		ObjectifyService.register(LessonEntity.class);
	}
	public static class ObjectifyDAO extends DAOBase {}
	
	public AbstractDAO() {
		odao = new ObjectifyDAO();
	}
	
	abstract protected Class<T> getEntityClass();
	
	public void delete(long id) {
		odao.ofy().delete(getEntityClass(), id);
	}

	public T find(long id) {
		return odao.ofy().find(getEntityClass(), id);
	}

	public void put(T... id) {
		odao.ofy().put(id);
	}

}

package com.google.light.lessons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class LessonEntity {
	@Id
	Long id;
	String name;
	List<String> links = new ArrayList<String>();
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getLinks() {
		return Collections.unmodifiableList(links);
	}
	public void addLink(String link) {
		if(!hasLink(link))
			links.add(link);
	}
	public void removeLink(String link) {
		links.remove(link);
	}
	public boolean hasLink(String link) {
		return links.contains(link);
	}
}

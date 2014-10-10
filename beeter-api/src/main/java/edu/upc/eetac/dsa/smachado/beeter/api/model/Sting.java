package edu.upc.eetac.dsa.smachado.beeter.api.model;

public class Sting {
	
	private String stingid;
	private String username;
	private String author;
	private String subject;
	private String content;
	private long lastModified;
	private long creationTimestamp;
	 
	 public String getStingid() {
		return stingid;
	}
	public void setStingid(String stingid) {
		this.stingid = stingid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public long getLastModified() {
		return lastModified;
	}
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	public long getCreationTimestamp() {
		return creationTimestamp;
	}
	public void setCreationTimestamp(long creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	

}

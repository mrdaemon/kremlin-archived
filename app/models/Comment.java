package models;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;
import play.data.validation.*;

@Entity
public class Comment extends Model{
	@Required
	public String author;
	
	@Required
	public Date postedOn;
	
	@Lob
	@Required
	@MaxSize(10000)
	public String body;
	
	@ManyToOne
	@Required
	public ImagePost parent;
	
	public Comment(ImagePost parent, String author, String body) {
		this.parent = parent;
		this.author = author;
		this.body = body;
		this.postedOn = new Date();
	}
	
	public String toString() {
		if(body.length() > 50) {
			return body.substring(0, 50) + "[...]";
		} else {
			return body;
		}
	}

}

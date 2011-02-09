package models;

import java.io.File;
import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;
import play.data.validation.*;

@Entity
public class ImagePost extends Model {
	@MaxSize(140)
	public String note;
	public Date postedOn;
	
	@ManyToOne
	public User postedBy;
	
	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL)
	public List<Comment> comments;
	
	@ManyToMany(cascade=CascadeType.PERSIST)
	public Set<Tag> tags;
	
	@OneToOne
	public Image image;
	
	public ImagePost(Image image, User author, String note) {
		this.image = image;
		this.note = note;
		this.postedBy = author;
		this.postedOn = new Date();
		this.comments = new ArrayList<Comment>();
	}
}

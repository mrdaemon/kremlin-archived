package models;

import java.io.File;
import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class ImagePost extends Model {
	public String name;
	public Date postedOn;
	
	@ManyToOne
	public User postedBy;
	
	@OneToMany(mappedBy="parent", cascade=CascadeType.ALL)
	public List<Comment> comments;
	
	@ManyToMany(cascade=CascadeType.PERSIST)
	public Set<Tag> tags;
	
	@OneToOne
	public Image image;
	
	public ImagePost(User author, String name, Image image) {
		this.postedBy = author;
		this.name = name;
		this.image = image;
		this.postedOn = new Date();
	}
}

package models;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;
import play.data.validation.*;

@Entity
public class Tag extends Model implements Comparable<Tag> {
	@Required
	public String name;
	
	private Tag(String name) {
		this.name = name;
	}
	
	public static Tag findorCreateByName(String name) {
		Tag tag = Tag.find("byName", name).first();
		if(tag == null) {
			tag = new Tag(name);
		}
		return tag;
	}
	
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(Tag o) {
		return name.compareTo(o.name);
	}
	
}

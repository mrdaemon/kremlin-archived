package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.data.validation.Required;
import play.db.jpa.Blob;
import play.db.jpa.Model;

import utils.Checksum;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames="file_sha1"))
public class Image extends Model {
	@Required
	public String filename;
	
	@Required
	@Column(name="file_sha1", unique=true, nullable=false)
	public String checksum;
	
	@Required
	public Blob imagefile;
	
	//public Blob thumbnailfile;
	
	@PrePersist
	protected void onCreate(){
		checksum = Checksum.generateSHA1Checksum(imagefile.getFile());
	}
	
	@PreUpdate
	protected void onUpdate(){
		checksum = Checksum.generateSHA1Checksum(imagefile.getFile());
	}
	
	
}

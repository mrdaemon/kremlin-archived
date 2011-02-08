package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.db.jpa.Blob;
import play.db.jpa.Model;

import utils.Checksum;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames="file_sha1"))
public class Image extends Model {
	public String filename;
	
	@Column(name="file_sha1", unique=true, nullable=false)
	public String checksum;
	
	public Blob imagefile;
	
	@PrePersist
	protected void onCreate(){
		filename = imagefile.getFile().getName();
		checksum = Checksum.generateSHA1Checksum(imagefile.getFile());
	}
	
	@PreUpdate
	protected void onUpdate(){
		filename = imagefile.getFile().getName();
		checksum = Checksum.generateSHA1Checksum(imagefile.getFile());
	}
}

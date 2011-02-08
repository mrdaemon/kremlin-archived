package models;

import java.util.*;
import javax.persistence.*;

import play.db.jpa.*;
import play.libs.Crypto;

@Entity
public class User extends Model {
	
	public String email;
	public String passwordHash;
	public String name;
	public boolean isAdmin;
	
	public User(String email, String password, String name) {
		this.email = email;
		this.passwordHash = Crypto.passwordHash(password);
		this.name = name;
		this.isAdmin = false;
	}
	
	public static User login(String email, String password) {
		return find("byEmailAndPasswordHash", email, Crypto.passwordHash(password)).first();
	}
	
	@Override
	public String toString(){
		return this.name;
	}
}

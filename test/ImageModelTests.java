import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;

import models.Image;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import play.db.jpa.Blob;
import play.test.Fixtures;
import play.test.UnitTest;
import utils.Checksum;

public class ImageModelTests extends UnitTest {
	
	private String KNOWN_CHECKSUM = "ff1e363261ff8a0db1fb526cd6295c8a8212dd32";
	private File f;
	private String ftype;
	
	@Before
	public void setup() {
		Fixtures.deleteAll();
		f = new File(getClass().getResource("testimage.jpg").getFile());
		ftype = new MimetypesFileTypeMap().getContentType(f);
	}
	
	@After
	public void clean() {
		Fixtures.deleteAll();
	}
	
	@Test
	public void storeAndLoadImage() throws IOException{
		FileInputStream fis = new FileInputStream(f);
		
		Blob imgblob = new Blob();
		imgblob.set(fis, ftype);
		
		Image testimg = new Image();
		testimg.filename = f.getName();
		testimg.imagefile = imgblob;
		testimg.save();
		
		// Tests
		assertNotNull(testimg);
		assertEquals(f.getName(), testimg.filename);
		assertEquals(KNOWN_CHECKSUM, testimg.checksum);
		assertEquals(Checksum.generateSHA1Checksum(f), testimg.checksum);
	}
	
	@Test(expected= javax.persistence.PersistenceException.class)
	public void imageFileUniquenessConstraints() throws FileNotFoundException {
		// Save a first file.
		FileInputStream fis = new FileInputStream(f);
		
		Blob imgblob = new Blob();
		imgblob.set(fis, ftype);
		
		Image testimg = new Image();
		testimg.filename = f.getName();
		testimg.imagefile = imgblob;
		testimg.save();
		
		// Attempt to save a duplicate file.
		// It should fail the Unique constraint.
		FileInputStream dfis = new FileInputStream(f);
		
		Blob dimgblob = new Blob();
		dimgblob.set(dfis, ftype);
		
		Image duplicateimg = new Image();
		duplicateimg.filename = f.getName();
		duplicateimg.imagefile = dimgblob;
		duplicateimg.save();
	}
}

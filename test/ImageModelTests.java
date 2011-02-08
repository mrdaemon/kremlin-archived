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

import testutils.FixtureHelpers;

public class ImageModelTests extends UnitTest {
	
	private final String KNOWN_CHECKSUM =
		"ff1e363261ff8a0db1fb526cd6295c8a8212dd32"; // SHA1 for "testimage.jpg"
	
	/* Fixtures */
	private File f; // File reference for image to test 
	private String ftype; // Above file's mimetype.
	
	/**
	 * Setup fixtures before each test
	 */
	@Before
	public void setup() {
		Fixtures.deleteAll();
		f = new File(getClass().getResource("testimage.jpg").getFile());
		ftype = new MimetypesFileTypeMap().getContentType(f);
	}
	
	/**
	 * Cleanup fixtures after each test
	 */
	@After
	public void clean() {
		FixtureHelpers.delortAllImageBlobs();
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
		
		// FIXME: Fixtures cleanup hook, 
		// ensures created file gets deleted when test is done.
		// This is bad and I should feel bad. Sorry.
		FixtureHelpers.registerImageBlob(testimg);
		
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
		
		// FIXME: Fixtures cleanup hook
		FixtureHelpers.registerImageBlob(testimg);
		
		// Attempt to save a duplicate file.
		// It should fail the Unique constraint.
		FileInputStream dfis = new FileInputStream(f);
		
		Blob dimgblob = new Blob();
		dimgblob.set(dfis, ftype);
		
		// FIXME: Fixtures cleanup hook
		FixtureHelpers.registerImageBlob(dimgblob);
		
		Image duplicateimg = new Image();
		duplicateimg.filename = f.getName();
		duplicateimg.imagefile = dimgblob;
		duplicateimg.save();
		
	}
}

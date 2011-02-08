import org.junit.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import javax.activation.MimetypesFileTypeMap;

import play.test.*;
import play.db.jpa.Blob;
import play.libs.*;
import models.*;

public class PostModelsTests extends UnitTest {
	private User testUser;
	private Image testImage;
	
	private Image createTestImage(String filename)
				throws Exception {
		File f = new File(getClass().getResource(filename).getFile());
		String ftype = new MimetypesFileTypeMap().getContentType(f);
		FileInputStream fis = new FileInputStream(f);
		
		Blob imgblob = new Blob();
		imgblob.set(fis, ftype);
		
		Image img = new Image();
		img.filename = filename;
		img.imagefile = imgblob;
		
		if(fis != null) {
			fis.close();
		}
		return img;
	}
	
	@Before
	public void setup() throws Exception {
		Fixtures.deleteAll();
		// Stand in user and image
		testUser = new User("knifa@glasnost.us", "hunter2", "Knifa").save();
		testImage = createTestImage("testimage.jpg").save();
	}
	
	@Test
	public void storeAndLoadPost() {
		// Create new image post
		new ImagePost(testUser, "Hilarious Shenaningans", testImage).save();
		assertEquals(1, ImagePost.count()); // Validate store
		
		// Get all posts by user
		List<ImagePost> userPosts = ImagePost.find("byPostedBy",
				testUser).fetch();
		// Get first port
		ImagePost firstPost = userPosts.get(0);
		
		// Test Properties
		assertEquals(1, userPosts.size());
		assertNotNull(firstPost);
		assertEquals(testUser, firstPost.postedBy);
		assertEquals("Hilarious Shenaningans", firstPost.name);
		assertEquals(testImage, firstPost.image);
		assertEquals("testimage.jpg", firstPost.image.filename);
		assertNotNull(firstPost.image.checksum);
		assertNotNull(firstPost.postedOn);
	}
	
	@Test
	public void storeAndLoadComments() {
		// Create image post
		ImagePost testPost = new ImagePost(testUser, "Hilarious Shenaningans",
				testImage).save();
		
		// Post anonymous comments
		new Comment(testPost, "Anonymous", "OH GOD MY ANUS IS BLEEDING").save();
		new Comment(testPost, "br0bot", "Seizuretastic, man").save();
		
		/* A rather large comment. This text must be larger than the excerpt
		 * limit, in order to test that feature. The assertion below will
		 * completely and utterly fail if it's not, because it would make
		 * the rest of the tests completely meaningless. If you break it,
		 * fix it.
		 */
		String largecomment = "I'd just like to point out that you were " + 
		"given every opportunity to succeed. There was even going to be a " +
		"party for you. A big party that all your friends were invited to. " +
		"I invited your best friend, the Companion Cube. Of course, he " + 
		"couldn't come, because you murdered him. All your other friends " +
		"couldn't come either, because you don't have any other friends. " +
		"Because of how unlikeable you are. It says so here in your " +
		"personnel file: unlikeable. Liked by no one. A bitter, unlikeable " +
		"loner whose passing shall not be mourned. \"Shall not be " + 
		"mourned.\"That's exactly what it says. Very formal. Very official. " + 
		"It also says you were adopted. So that's funny, too. You've been " +
		"wrong about every single thing you've ever done, including this " +
		"thing. You're not smart. You're not a scientist. You're not a " +
		"doctor. You're not even a full-time employee. Where did your life " +
		"go so wrong?";
		
		assertTrue(largecomment.length() > 50); // Internal State assertion
		
		// Store rather large comment by GlaDOS.
		new Comment(testPost, "GlaDOS", largecomment).save();
		
		// Get all post comments
		List<Comment> testPostComments = Comment.find("byParent",
				testPost).fetch();
		
		// Tests
		assertEquals(3, testPostComments.size());
		
		Comment firstComment = testPostComments.get(0);
		assertNotNull(firstComment);
		assertEquals("Anonymous", firstComment.author);
		assertEquals("OH GOD MY ANUS IS BLEEDING", firstComment.body);
		assertNotNull(firstComment.postedOn);
		
		Comment secondComment = testPostComments.get(1);
		assertNotNull(secondComment);
		assertEquals("br0bot", secondComment.author);
		assertEquals("Seizuretastic, man", secondComment.body);
		assertNotNull(secondComment.postedOn);
		
		Comment thirdComment = testPostComments.get(2);
		assertNotNull(thirdComment);
		assertEquals("GlaDOS", thirdComment.author);
		assertEquals(largecomment, thirdComment.body);
		assertTrue(thirdComment.toString().endsWith("[...]"));
		assertTrue(thirdComment.toString().length() <= (50 +
				" [...]".length())); // Extra space should we change this.
	}

}

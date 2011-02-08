package testutils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;

import models.Image;
import play.db.jpa.Blob;

/**
 * Utilitarian/Convenience class to facilitate fixture
 * set up and tear down for unit tests that require files in
 * {@link play.db.jpa.Blob Blob}s and instances of the Image model,
 * fully populated.
 * 
 * Blobs are ridiculously annoying to populate due to how high level
 * they are, and they provide absolutely no way of deleting the file
 * they store in order to avoid transaction and database vs. filesystem
 * sync. issues. So to avoid boilerplate and leftover files all over
 * the attachment area, this dirty, ugly, hack-filled code was born.
 * 
 * Also, I am told Blob.getFile() might go away in the future. Bleh.
 * 
 * <p><em>NOT FOR PRODUCTION USE, HUMAN CONSUMPTION OR GENERAL 
 * HURR, HERPING AND DERPING! YOU HAVE BEEN WARNED!<em></p>
 * 
 * 
 * @author Alexandre Gauthier
 * @see models.Image
 */
public class FixtureHelpers {
	
	private static final List<File> KNOWN_FILES; // Blob backed File registry
	
	static {
		// Initialize the known files as an ArrayList
		KNOWN_FILES = new ArrayList<File>();
	}
	
	/**
	 * Create an instance of the Image model from a test file residing
	 * in the same directory where the Test class lives.
	 * 
	 * The returned Image object will have all of its fields populated with
	 * the correct values, and a reference to a File object pointing to the
	 * file on disk backing the Blob datatype behind 'imagetype' will be 
	 * saved, and correctly deleted when delortAllImageBlobs() is called.
	 * 
	 * @param filename - Name of the test file ("testimage.png")
	 * @return Image model instance
	 * @throws Exception
	 */
	public static Image createTestImage(String filename) throws Exception {
		
		/* xxx - DIRTY DIRTY HACK BELOW - xxx
		 * 
		 * I am really REALLY not proud of this :(
		 * It is the dirtiest hack ever, but it works.
		 * Get the caller's class by filling in a stack trace, and then
		 * getting the resource files off that. Oh god.
		 * 
		 * This is just for unit tests so I can remain lazy and read
		 * the resource files relevant for any test class in a semi-portable
		 * way. I'm sorry, and I wouldn't really use that in production.
		 */
		String cname = new Throwable().fillInStackTrace().getStackTrace()[1]
			                                                   .getClassName();
		Class<?> caller = Class.forName(cname); 
			
		
		File f = new File(caller.getResource(filename).getFile());
		String ftype = new MimetypesFileTypeMap().getContentType(f);
		FileInputStream fis = new FileInputStream(f);

		Blob imgblob = new Blob();
		imgblob.set(fis, ftype);

		Image img = new Image();
		img.filename = filename;
		img.imagefile = imgblob;

		if (fis != null) {
			fis.close();
		}
		/* Hold a reference to the file object before returning,
		   so we can clean things up during fixtures teardown. */
		registerImageBlob(img);
		return img;
	}
	
	/**
	 * Register the file backing an Image model.
	 * 
	 * Ensures the FixtureHelpers class holds a reference to
	 * the File object behind the Image Blob field, for deletion.
	 * 
	 * Registering an Image model instance will ensure its file
	 * is deleted by delortAllImageBlobs().
	 * 
	 * @param img Image model instance
	 */
	public static void registerImageBlob(Image img) {
		registerImageBlob(img.imagefile);
	}
	
	/**
	 * Register the file backing a Blob field type.
	 * 
	 * @see #registerImageBlob(Image)
	 * @param blob Blob file
	 */
	public static void registerImageBlob(Blob blob) {
		File f = blob.getFile();
		if(!KNOWN_FILES.contains(f)){
			KNOWN_FILES.add(f);
		}
	}
	
	/**
	 * Delete the files backing all the known
	 * Image instances, currently existing or not.
	 */
	public static void delortAllImageBlobs() {
		if(!KNOWN_FILES.isEmpty()) {
			for(File f: KNOWN_FILES){
				KNOWN_FILES.remove(f);
				deleteFile(f);
			}
		}
	}

	/**
	 * Delete the file backing the Blob datatype of an
	 * Image model instance. They are <em>not</em> 
	 * automatically cleaned on delete.
	 * 
	 * If you created the Image through one of this Class' functions, 
	 * such as {@link createTestImage(String filename)}, it will
	 * be reaped by{@link delortAllImageBlobs()}.
	 * 
	 * It is safe to call this on any Image instance,
	 * the FixturesHelper class will deregister them if it knows
	 * about them. Go nuts.
	 * 
	 * @param img  Image model instance
	 * @see models.Image
	 */
	public static void delortImageBlob(Image img) {
		File f = img.imagefile.getFile();
		if(KNOWN_FILES.contains(f)){
			KNOWN_FILES.remove(f);
			deleteFile(f);
		}
	}
	
	/**
	 * Private method to delete a File.
	 * Tries to do this sanely, with fallbacks.
	 * 
	 * @param f File to delete
	 */
	private static void deleteFile(File f){
		if(f.exists()){
			// Try to delete file, should it fail,
			// queue the deletion on JVM Exit.
			// Unix systems shouldn't really encounter that issue.
			if(!f.delete()){
				f.deleteOnExit();
			}
		}
	}
}

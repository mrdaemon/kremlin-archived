import java.io.InputStream;
import java.util.List;

import models.Image;
import models.ImagePost;
import models.User;
import play.Play;
import play.db.jpa.Blob;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;
import play.vfs.VirtualFile;

import javax.activation.MimetypesFileTypeMap;

/**
 * Bootstrap job, handles bringing up the application and populating the
 * database on first run.
 * 
 * @author Alexandre Gauthier
 */
@OnApplicationStart
public class Bootstrap extends Job {

	/**
	 * Application Initialization Job
	 */
	public final void doJob() {
		// Load initial data on first start.
		if (User.count() == 0) {
			// Database data comes from the YAML document
			Fixtures.load("initial-data.yml");

			/**
			 * I have NO IDEA how to properly deserialize a File object or
			 * anything remotely close to it, as the Blob class and its magical
			 * binders are way too opaque, and once more it annoys me.
			 * 
			 * Fixtures won't load byte[]s from YAML as of Play! 1.1, it is a
			 * known bug, too. So I'm just going to assemble the file object
			 * manually using VirtualFile, which I discovered by peeking at the
			 * Fixtures class in play.
			 * 
			 * @see http://bit.ly/fwiQtl
			 */
			VirtualFile initialImage = null;
			String fname = "stallman_loljava.png";

			try {
				for (VirtualFile vf : Play.javaPath) {
					initialImage = vf.child(fname);
					if (initialImage != null && initialImage.exists()) {
						break;
					}
				}

				InputStream is = Play.classloader.getResourceAsStream(fname);
				if (is == null) {
					throw new RuntimeException("Couldn't find file " + fname
							+ ", opened stream was null.");
				}

			} catch (Throwable e) {
				throw new RuntimeException("Cannot load image " + fname
						+ " as a fixture: " + e.getMessage(), e);
			}

			// Construct new Image model from retrieved file
			Blob filedata = new Blob();
			filedata.set(initialImage.inputstream(), new MimetypesFileTypeMap()
					.getContentType(initialImage.getRealFile()));

			Image imgmodel = new Image();
			imgmodel.imagefile = filedata;

			// Commit transaction
			imgmodel.save();

			/*
			 * Attach image to ImagePost if everything feels sane. I can't
			 * fathom how we could have made it so far with such a failure
			 * without obtaining a well deserved exception, but better safe than
			 * sorry. I don't want to be debugging this in three months because
			 * someone broke the initial data in a creative way and no test
			 * units were written.
			 */
			if (ImagePost.count() == 1) {
				List<ImagePost> storedPosts = ImagePost.findAll();
				(storedPosts.get(0).image = imgmodel).save();
			} else {
				throw new RuntimeException("Loading the initial YAML data "
						+ "resulted in an unexpected ImagePost count.");
			}

		}
	}
}

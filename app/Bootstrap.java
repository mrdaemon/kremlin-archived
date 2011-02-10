import java.io.InputStream;
import java.util.List;

import models.Comment;
import models.Image;
import models.ImagePost;
import models.User;
import play.Play;
import play.db.jpa.Blob;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;
import play.vfs.VirtualFile;
import play.Logger;

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
		Logger.info("--------------------- INIT ----------------------");
		Logger.info("INIT: Kremlin Image Board System - Version 0.0.0");
		Logger.info("Copyright (c) glasnost 2005-2012");
		
		Logger.info("Initializing Model...");
		
		// Load initial data on first start.
		if (User.count() == 0) {
			
			Logger.warn("Database empty or using in-memory HSQL backend.");
			
			Logger.info("Loading initial database contents. This may take "
					+ "a while, please be patient.");
			
			Logger.debug("Loading factory fixtures from 'initial-data.yml'");
			
			// Database data comes from the YAML document
			Fixtures.load("initial-data.yml");
			
			Logger.debug("Fixtures successfully loaded");
			
			/*
			 * Only proceed to generate image if everything feels sane. I can't
			 * fathom how we could have made it so far with such a failure
			 * without obtaining a well deserved exception, but better safe
			 * than sorry. I don't want to be debugging this in three months
			 * because someone broke the initial data in a creative way and 
			 * no test units were written targeting first boot.
			 */
			if (ImagePost.count() == 1) {
				Logger.debug("ImagePost model seems sane, count() is 1");
			} else {
				Logger.fatal("Unexpected post count after initial data load, "
						+ "not going to solve the halting problem. Aborting.");
				
				throw new RuntimeException("Loading the initial YAML data "
						+ "resulted in an unexpected ImagePost count.");
			}
			
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

			Logger.debug("Creating initial Image model instance...");
			
			try {
				Logger.debug("Searching for file '" + fname
						+ "' in CLASSPATH...");
				for (VirtualFile vf : Play.javaPath) {
					initialImage = vf.child(fname);
					if (initialImage != null && initialImage.exists()) {
						Logger.debug("Found matching Resource '" + fname
								+ "' at " + vf.getRealFile().getPath());
						break;
					}
				}

				InputStream is = Play.classloader.getResourceAsStream(fname);
				if (is == null) {
					Logger.fatal("InputStream for '" + fname + "' is null!");
					throw new RuntimeException("Couldn't find file " + fname
							+ ", opened stream was null.");
				}

			} catch (Throwable e) {
				throw new RuntimeException("Cannot load image " + fname
						+ " as a fixture: " + e.getMessage(), e);
			}
			
			Logger.debug("Constructing Blob from image file '" + fname
					+ " for first post data field.");

			// Construct new Image model from retrieved file
			Blob filedata = new Blob();
			filedata.set(initialImage.inputstream(), new MimetypesFileTypeMap()
					.getContentType(initialImage.getRealFile()));

			Logger.debug("Attaching blob to Image model...");
			
			Image imgmodel = new Image();
			imgmodel.imagefile = filedata;
			
			Logger.debug("Committing transaction for Image model...");

			// Commit transaction
			imgmodel.save();
			
			Logger.debug("Initial Image stored to database and filesystem.");
			
			Logger.debug("File: " + imgmodel.imagefile.getFile().getPath());
			
			Logger.debug("Attaching Image to first post...");
			List<ImagePost> storedPosts = ImagePost.findAll();
			
			storedPosts.get(0).image = imgmodel;
			storedPosts.get(0).save();
			
			Logger.debug("Validating insertion...");
			
			storedPosts.get(0).refresh();
			if (storedPosts.get(0).image != null) {
				Logger.debug("Found image attached to Post");
				if(storedPosts.get(0).image.imagefile.exists()) {
					Logger.debug("File found, insertion was valid.");
				} else {
					Logger.error("Urgh - Image was attached, but file "
							+ "is missing! Aborting...");
					throw new RuntimeException("Image model on PostImage "
							+ "parent missing on disk post first boot init.");
				}
			} else {
				RuntimeException e = new RuntimeException("ImagePost.image was "
						+ "null! Aborting execution, please run checks.");
				Logger.error(e, e.getMessage());
				throw e;
				
			}
			Logger.info("------ First boot initialization complete! ------");
		}
		/* Display amusing statistics on boot in the logs. So very fancy. */
		String row = "%1$5s %2$-5s";
		Logger.info(row, User.count(), "Users");
		Logger.info(row, Image.count(), "Images");
		Logger.info(row, Comment.count(), "Comments");
		// Logger.info(row, Tag.count(), "Tags");
		Logger.info("----------- Image Board System Ready ------------");

	}
}

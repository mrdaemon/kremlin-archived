package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class ImageRenderer extends Controller {
	
	public static void renderPostImage(Long id) {
		Image img = Image.findById(id);
		renderBinary(img.imagefile.get());
	}

}

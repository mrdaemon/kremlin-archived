package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        ImagePost latest = ImagePost.find("order by postedOn desc").first();
        List<ImagePost> previous = ImagePost.find(
        		"order by postedOn desc"
        		).from(1).fetch(10);
        render(latest, previous);
    }

}
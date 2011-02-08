package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

import play.exceptions.UnexpectedException;

public class Checksum {
	public static String generateSHA1Checksum(File f) {
		FileInputStream fis;
		String checksum;
		try {
			fis = new FileInputStream(f);
			checksum = DigestUtils.shaHex(fis);
		} catch (FileNotFoundException e) {
			throw new UnexpectedException(
					"File disappeared before we could checksum it?", e);
		} catch (IOException e) {
			throw new UnexpectedException(
					"There was some IO Error while processing the file", e);
		}
		return checksum;
	}
}

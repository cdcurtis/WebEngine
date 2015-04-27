package edu.ucr.files;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestParseFiles {

	private ParseFiles parseFiles;
	
	@Before
	public void setup() {
		parseFiles = new ParseFiles(2);
	}
	
	@Test
	public void testReadFileAsXML() {
		parseFiles.readFile("testInput/file0.dld");
	}

}

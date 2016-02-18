import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class TextBuddyTest {
	
	private static ByteArrayOutputStream outContent;
	@Test
	public void testAdd() {
		outContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent));
		
		TextBuddy.setFileName("myfile.txt");
		TextBuddy.executeAdd("add buy butter for tomorrow");
		String addedSuccessText = "added to myfile.txt: \"buy butter for tomorrow\"\n";
		assertEquals("add success", addedSuccessText, outContent.toString());
		
		outContent.reset();
	}
	
	@Test
	public void testClear() {
		outContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent));
		
		TextBuddy.setFileName("myfile.txt");
		String clearSuccessText = "all contents deleted from myfile.txt\n";
		TextBuddy.executeClear();
		assertEquals("clear success", clearSuccessText, outContent.toString());
		
		outContent.reset();
	}
}

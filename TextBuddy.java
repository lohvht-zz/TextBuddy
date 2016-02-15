import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;

/*
 * ASSUMPTIONS 
 * 0)	Files are saved after each operation 
 * 1) 	file saving is done by writing to another temporary file and changing its name to the original file name, while 
 * 	 	deleting the older original file 
 * 2) 	When add command is given without any other words after, no action is taken, prints out add has failed 
 * 3)	Each time the add command is given, it builds up on the text file LINE by LINE 
 * 4)	If no parameters given after running TextBuddy (e.g. "C:> java TextBuddy" or any of its equivalents), program will 
 * 		terminate immediately
 * 5)	Each add command will add the TEXT input only and not the line number 
 * 			e.g. c:> java TextBuddy mytextfile.txt 
 * 			 	Welcome to TextBuddy. mytextfile.txt is ready for use 
 *			 	command: add jump over the moon 
 *			 	added to mytextfile.txt: "jump over the moon" 
 *			 	command: display 
 *			 	1. jump over the moon 
 *			 	command: exit 
 *			 	c:>type mytextfile.txt 
 *			 	jump over the moon 
 *			 	c:>
 *6)	if the input filename is not proper (no extension), TextBuddy will automatically format it into a text file (.txt)
 */
class TextBuddy {
	private static final String NAME_TEMP_FILE = "editedTemp.txt";
	private static final String MESSAGE_COMMAND_PROMPT = "Command: ";
	private static final String MESSAGE_HELP_SHEET = "add <TEXT>        --adds the text to the file currently open\n"
			+ "delete <LINENUM>        --removes the text written on that line\n"
			+ "display        --shows the contents of the text file with all changes made to it before it was saved, "
			+ "this DOES NOT save the document\n" + "save        --saves all changes made to the text document\n"
			+ "exit        --saves the document first before exiting Text Buddy\n"
			+ "clear        --wipes and removes all the data currently stored in the text file, THIS ACTION IS NOT REVERSIBLE\n";
	private static final String MESSAGE_FILE_READY = "Welcome to Text Buddy, %1$s is ready for use!\n";
	private static final String MESSAGE_CLEAR_TEXT_MESSAGE = "all contents deleted from %1$s\n";
	private static final String MESSAGE_FILE_IS_EMPTY = "%1$s is empty";
	private static final String ERROR_FILE_REPLACEMENT = "Something has gone wrong with file replacement";
	private static final String ERROR_NO_FILE_STRING_DETECTED = "No file input stated, terminating program";
	private static final String ERROR_DELETE_FORMAT_INCORRECT = "Wrong format for delete, parameter should be a number";
	private static final String ERROR_COULD_NOT_RENAME_FILE = "Could not rename file";
	private static final String ERROR_COMMAND_INVALID = "%1$s is not valid, please try again, enter <help> without the "
			+ "arrow brackets to get help on commands\n";
	private static final String ERROR_LINE_NUMBER_DOES_NOT_EXIST = "line number %1$s does not exist";
	
	private static boolean isRunning = true;
	private static Scanner inputScanner = new Scanner(System.in);
	private static String fileName = null;

	public static void main(String args[]) throws IOException {
		if (!isCommandLineValid(args)) {
			promptToUser(ERROR_NO_FILE_STRING_DETECTED);
			return;
		}
		fileName = formatFileName(args);
		runTextBuddy();
	}
	
	private static void runTextBuddy() throws IOException {
		promptToUser(MESSAGE_FILE_READY, fileName);

		LinkedList<String> tempContents = new LinkedList<String>();
		BufferedReader fileReader = createReader(fileName);
		BufferedWriter tempFileWriter = createWriter(NAME_TEMP_FILE);
		copyFileContentToTempContents(fileReader, tempContents);

		while (isRunning) {
			promptToUser(MESSAGE_COMMAND_PROMPT);

			String[] input = getInputs(inputScanner);
			String command = getCommandString(input);

			switch (command) {
			case "add":
				addCommand(tempContents, input);
				saveAndWriteToFile(tempContents, tempFileWriter, fileReader);
				fileReader = createReader(fileName);
				tempFileWriter = createWriter(NAME_TEMP_FILE);
				copyFileContentToTempContents(fileReader, tempContents);
				break;

			case "display":
				displayCommand(tempContents);
				break;

			case "delete":
				deleteCommand(tempContents, input);
				
				saveAndWriteToFile(tempContents, tempFileWriter, fileReader);
				fileReader = createReader(fileName);
				tempFileWriter = createWriter(NAME_TEMP_FILE);
				copyFileContentToTempContents(fileReader, tempContents);
				break;

			case "exit":
				setRunning();
				saveAndWriteToFile(tempContents, tempFileWriter, fileReader);
				break;

			case "clear":
				clearCommand(tempContents);
				
				saveAndWriteToFile(tempContents, tempFileWriter, fileReader);
				fileReader = createReader(fileName);
				tempFileWriter = createWriter(NAME_TEMP_FILE);
				break;

			case "help":
				promptToUser(MESSAGE_HELP_SHEET);
				break;

			default:
				promptToUser(ERROR_COMMAND_INVALID, command);
				break;
			}
		}
		inputScanner.close();
	}

	private static boolean isCommandLineValid(String[] args) {
		return (!(args.length == 0));
	}

	private static String formatFileName(String[] args) {
		String text = getCommandString(args);
		if (text.indexOf(".") < 0) {
			text = text + ".txt";
		}
		return text;
	}

	public static void promptToUser(String message) {
		System.out.println(message);
	}
	
	public static void promptToUser(String message, String name){
		System.out.printf(message, name);
	}

	private static File createFile(String fileName) {
		File file = new File(fileName);
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	private static BufferedReader createReader(String fileName) throws FileNotFoundException {
		File file = createFile(fileName);
		FileInputStream fileIn = new FileInputStream(file);
		InputStreamReader inputStreamReader = new InputStreamReader(fileIn, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(inputStreamReader);
		return reader;
	}

	private static BufferedWriter createWriter(String fileName) throws FileNotFoundException {
		File file = createFile(fileName);
		FileOutputStream fileOut = new FileOutputStream(file);
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOut, StandardCharsets.UTF_8);
		BufferedWriter writer = new BufferedWriter(outputStreamWriter);
		return writer;
	}

	private static void copyFileContentToTempContents(BufferedReader fileReader, LinkedList<String> tempContents) {
		String fileLineContent;
		try {
			while ((fileLineContent = fileReader.readLine()) != null) {
				tempContents.add(fileLineContent);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String[] getInputs(Scanner inputScanner) {
		return inputScanner.nextLine().split(" ", 2);
	}

	public static String getCommandString(String[] input) {
		return input[0];
	}

	private static void addCommand(LinkedList<String> tempContents, String[] input) {
		String textToBeAdded = getUserResponse(input);
		tempContents.add(textToBeAdded);
		System.out.println("added to " + fileName + ": \"" + textToBeAdded + "\"");
	}

	private static void displayCommand(LinkedList<String> tempContents) {
		if (getTotalNumberOfLines(tempContents) == 0) {
			System.out.println(fileName + " is empty");
			return;
		}
		int counter = 1;
		for (String lineContent : tempContents) {
			System.out.println(counter + ". " + lineContent);
			counter++;
		}
	}

	private static void deleteCommand(LinkedList<String> tempContents, String[] input) {
		int totalNumberOfLines = getTotalNumberOfLines(tempContents);
		if (totalNumberOfLines == 0) {
			promptToUser(MESSAGE_FILE_IS_EMPTY, fileName);
		} else {
			try {
				int lineNumberToBeDeleted = getLineNumberToDelete(input);
				if (totalNumberOfLines < lineNumberToBeDeleted) {
					promptToUser(ERROR_LINE_NUMBER_DOES_NOT_EXIST, String.valueOf(lineNumberToBeDeleted));
					return; 
				}
				int tempContentsIndexToRemove = lineNumberToBeDeleted-1;
				String removedLine = getLineRemoved(tempContents, tempContentsIndexToRemove);
				promptToUser(removedLine);
			} catch (NumberFormatException e) {
				promptToUser(ERROR_DELETE_FORMAT_INCORRECT);
			}
		}
	}

	public static String getLineRemoved(LinkedList<String> tempContents, int tempContentsIndexToRemove) {
		return "deleted from " + fileName + ": " + String.valueOf(tempContents.remove(tempContentsIndexToRemove));
	}

	public static int getLineNumberToDelete(String[] input) {
		return Integer.valueOf(getUserResponse(input));
	}

	public static int getTotalNumberOfLines(LinkedList<String> tempContents) {
		return tempContents.size();
	}

	public static String getUserResponse(String[] input) {
		return input[1];
	}

	private static void setRunning() {
		isRunning = !isRunning;
	}

	private static void saveAndWriteToFile(LinkedList<String> tempContents, BufferedWriter tempFileWriter, 
			BufferedReader fileReader) throws IOException {
		writeToFile(tempContents, tempFileWriter);
		replaceOldOriginalFile(tempFileWriter, fileReader);
	}

	private static void writeToFile(LinkedList<String> tempContents, BufferedWriter tempFileWriter) {
		try {
			while (!tempContents.isEmpty()) {
				tempFileWriter.write(tempContents.remove());
				tempFileWriter.newLine();
			}
			tempFileWriter.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void replaceOldOriginalFile(BufferedWriter tempFileWriter, BufferedReader fileReader) {
		try {
			tempFileWriter.close();
			fileReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File file = createFile(fileName);
		File tempFile = createFile(NAME_TEMP_FILE);

		if (!file.delete()) {
			promptToUser(ERROR_FILE_REPLACEMENT);
			return;
		}
		if (!tempFile.renameTo(file)) {
			promptToUser(ERROR_COULD_NOT_RENAME_FILE);
		}
	}

	private static void clearCommand(LinkedList<String> tempContents) {
		tempContents.clear();
		promptToUser(MESSAGE_CLEAR_TEXT_MESSAGE, fileName);
	}
}

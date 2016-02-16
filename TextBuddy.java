import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

/*This class compares the 1st word of each line such that it sorts it by alphabetically order by the first word and not the
 * lexicographical order of the while string
 */
class lineCompare implements Comparator<String>{
	public int compare(String firstString, String secondString){
		String firstWord = firstString.split(" ")[0];
		String secondWord = secondString.split(" ")[0];
		return firstWord.compareTo(secondWord);
	}
}
class TextBuddy {
	private static final String NAME_TEMP_FILE = "editedTemp.txt";
	//MESSAGES
	private static final String MESSAGE_COMMAND_PROMPT = "Command: ";
	private static final String MESSAGE_HELP_SHEET = "add <TEXT>        --adds the text to the file currently open\n"
			+ "delete <LINENUM>        --removes the text written on that line\n"
			+ "display        --shows the contents of the text file with all changes made to it before it was saved, "
			+ "this DOES NOT save the document\n" + "save        --saves all changes made to the text document\n"
			+ "exit        --saves the document first before exiting Text Buddy\n"
			+ "clear		--wipes and removes all the data currently stored in the text file, THIS ACTION IS NOT REVERSIBLE\n"
			+ "search		--sorts the current list line by line in alphabetical order.\n" ;
	private static final String MESSAGE_FILE_READY = "Welcome to Text Buddy, %1$s is ready for use!\n";
	private static final String MESSAGE_CLEAR_TEXT_MESSAGE = "all contents deleted from %1$s\n";
	private static final String MESSAGE_FILE_IS_EMPTY = "%1$s is empty\n";
	private static final String MESSAGE_LIST_SORTED = "%1$s is sorted, type \"display\" to show the updated list\n";
	private static final String MESSAGE_RETRIEVED_SEARCHED_WORD_INDEX = "search results for the word \"%1$s\"\n";
	private static final String MESSAGE_LINE_PRINTED = "%1$d. %2$s\n";
	private static final String MESSAGE_STRING_SUCCESSFULLY_ADDED = "added to %1$s: \"%2$s\"\n";
	private static final String MESSAGE_TEXT_DOCUMENT_IS_EMPTY = "%1$s is empty\n";
	//ERROR MESSAGES
	private static final String ERROR_FILE_REPLACEMENT = "Something has gone wrong with file replacement";
	private static final String ERROR_NO_FILE_STRING_DETECTED = "No file input stated, terminating program";
	private static final String ERROR_DELETE_FORMAT_INCORRECT = "Wrong format for delete, parameter should be a number";
	private static final String ERROR_COULD_NOT_RENAME_FILE = "could not rename file";
	private static final String ERROR_COMMAND_INVALID = "%1$s is not valid, please try again, enter <help> without the "
			+ "arrow brackets to get help on commands\n";
	private static final String ERROR_LINE_NUMBER_DOES_NOT_EXIST = "line number %1$s does not exist\n";
	private static final String ERROR_SEARCHED_WORD_IS_NOT_FOUND = "Searched word %1$s cannot be found on the text document\n";
	
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
		while (isRunning) {
			promptToUser(MESSAGE_COMMAND_PROMPT);
			String[] input = getInputs(inputScanner);
			String command = getCommandString(input);

			switch (command) {
				case "add":
					addCommand(input);					
					break;
	
				case "display":
					displayCommand();
					break;
	
				case "delete":
					deleteCommand(input);
					break;
	
				case "exit":
					exitCommand();
					break;
	
				case "clear":
					clearCommand();
					break;
	
				case "help":
					promptToUser(MESSAGE_HELP_SHEET);
					break;
				
				case "sort":
					sortCommand();
					break;
				
				case "search":
					searchCommand(input);
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
	
	public static void promptToUser(String message, int number, String name){
		System.out.printf(message, number, name);
	}
	
	public static void promptToUser(String message, String name1, String name2){
		System.out.printf(message, name1, name2);
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

	private static void addCommand(String[] input) {
		try {
			BufferedReader fileReader = createReader(fileName);
			BufferedWriter tempFileWriter = createWriter(NAME_TEMP_FILE);
			LinkedList<String> tempContents = new LinkedList<String>();
			copyFileContentToTempContents(fileReader, tempContents);
			
			String textToBeAdded = getUserResponse(input);
			tempContents.add(textToBeAdded);
			promptToUser(MESSAGE_STRING_SUCCESSFULLY_ADDED, fileName, textToBeAdded);
			
			saveAndWriteToFile(tempContents, tempFileWriter, fileReader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void displayCommand() {
		try {
			BufferedReader fileReader = createReader(fileName);
			LinkedList<String> tempContents = new LinkedList<String>();
			copyFileContentToTempContents(fileReader, tempContents);
			
			if (getTotalNumberOfLines(tempContents) == 0) {
				promptToUser(MESSAGE_TEXT_DOCUMENT_IS_EMPTY, fileName);
				return;
			}
			int counter = 1;
			for (String lineContent : tempContents) {
				promptToUser(MESSAGE_LINE_PRINTED, counter, lineContent);
				counter++;
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void deleteCommand(String[] input) {
		try {
			BufferedReader fileReader = createReader(fileName);
			BufferedWriter tempFileWriter = createWriter(NAME_TEMP_FILE);
			LinkedList<String> tempContents = new LinkedList<String>();
			copyFileContentToTempContents(fileReader, tempContents);
			
			int totalNumberOfLines = getTotalNumberOfLines(tempContents);
			if (totalNumberOfLines == 0) {
				promptToUser(MESSAGE_FILE_IS_EMPTY, fileName);
			} else {
				int lineNumberToBeDeleted = getLineNumberToDelete(input);
				if (totalNumberOfLines < lineNumberToBeDeleted) {
					promptToUser(ERROR_LINE_NUMBER_DOES_NOT_EXIST, String.valueOf(lineNumberToBeDeleted));
					return; 
				}
				int tempContentsIndexToRemove = lineNumberToBeDeleted-1;
				String removedLine = getLineRemoved(tempContents, tempContentsIndexToRemove);
				promptToUser(removedLine);
			}
			saveAndWriteToFile(tempContents, tempFileWriter, fileReader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			promptToUser(ERROR_DELETE_FORMAT_INCORRECT);
		}
	}
	
	public static void sortCommand(){
		try {
			BufferedReader fileReader = createReader(fileName);
			BufferedWriter tempFileWriter = createWriter(NAME_TEMP_FILE);
			LinkedList<String> tempContents = new LinkedList<String>();
			copyFileContentToTempContents(fileReader, tempContents);
			
			lineCompare lineComparator = new lineCompare();
			Collections.sort(tempContents, lineComparator);
			promptToUser(MESSAGE_LIST_SORTED, fileName);
			
			saveAndWriteToFile(tempContents, tempFileWriter, fileReader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean isContainSubstring(String sourceString, String substring) {
	    int substringLength = getStringLength(substring);
	    
	    if (substringLength == 0){
	        return true;
	    }
	        
	    char subStringFirstLowerCaseChar = Character.toLowerCase(substring.charAt(0));
	    char subStringFirstUpperCaseChar = Character.toUpperCase(substring.charAt(0));

	    for (int i = getStringLength(sourceString) - substringLength; i >= 0; i--) {
	        char sourceCharacterAt = sourceString.charAt(i);
	        
	        if (sourceCharacterAt != subStringFirstLowerCaseChar && sourceCharacterAt != subStringFirstUpperCaseChar){
	            continue;
	        }
	        
	        if (sourceString.regionMatches(true, i, substring, 0, substringLength)){
	            return true;
	        }
	    }

	    return false;
	}
	
	public static void searchCommand(String[] input){
		try {
			BufferedReader fileReader = createReader(fileName);
			LinkedList<String> tempContents = new LinkedList<String>();
			copyFileContentToTempContents(fileReader, tempContents);
				
			String wordToSearchFor = getUserResponse(input);
			ArrayList<Integer> indexesOfWordInstanceFound = getSearchedWordLineIndexes(tempContents, wordToSearchFor);
				
			if(indexesOfWordInstanceFound.isEmpty()){
					promptToUser(ERROR_SEARCHED_WORD_IS_NOT_FOUND, wordToSearchFor);
			} else {
				promptToUser(MESSAGE_RETRIEVED_SEARCHED_WORD_INDEX, wordToSearchFor);
				printSearchResults(tempContents, indexesOfWordInstanceFound);
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Integer> getSearchedWordLineIndexes(LinkedList<String> tempContents, String wordToSearchFor){
		ArrayList<Integer> indexesOfWordInstanceFound = new ArrayList<Integer>();
		int listSize = getTotalNumberOfLines(tempContents);
		
		for(int i = 0; i < listSize; i++){
			String lineString = tempContents.get(i);
			if(isContainSubstring(lineString, wordToSearchFor)){
				indexesOfWordInstanceFound.add(i);
			}
		}
		return indexesOfWordInstanceFound;
	}
	
	public static void printSearchResults(LinkedList<String> tempContents, ArrayList<Integer> indexesOfWordInstanceFound){
		for(int index : indexesOfWordInstanceFound){
			int counter = 1+index;
			String lineContent = tempContents.get(index);
			promptToUser(MESSAGE_LINE_PRINTED, counter, lineContent);
		}
	}
	
	public static int getStringLength(String string) {
		return string.length();
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

	private static void exitCommand() {
		isRunning = !isRunning;
	}

	private static void saveAndWriteToFile(LinkedList<String> tempContents, BufferedWriter tempFileWriter, 
			BufferedReader fileReader) throws IOException {
		saveChangeAndCloseStreams(tempContents, tempFileWriter, fileReader);
		replaceOldOriginalFile();
	}

	private static void saveChangeAndCloseStreams(LinkedList<String> tempContents, BufferedWriter tempFileWriter, BufferedReader fileReader) {
		try {
			while (!tempContents.isEmpty()) {
				tempFileWriter.write(tempContents.remove());
				tempFileWriter.newLine();
			}
			tempFileWriter.close();
			fileReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void replaceOldOriginalFile(){
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

	private static void clearCommand() {
		replaceOldOriginalFile();
		promptToUser(MESSAGE_CLEAR_TEXT_MESSAGE, fileName);
	}
}

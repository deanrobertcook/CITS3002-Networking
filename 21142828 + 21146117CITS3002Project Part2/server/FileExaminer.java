package server;

public class FileExaminer {
	  private String fullPath;
	  private char pathSeparator, extensionSeparator;

	  public FileExaminer(String str, char sep, char ext) {
		  fullPath = str;
		  pathSeparator = sep;
		  extensionSeparator = ext;
	  }

	  public String extension() {
		  int dot = fullPath.lastIndexOf(extensionSeparator);
		  return fullPath.substring(dot + 1);
	  }

	  public String filename() { // gets filename without extension
		  int dot = fullPath.lastIndexOf(extensionSeparator);
		  if (dot == -1) {
			  dot = fullPath.length();
		  }
		  int sep = fullPath.lastIndexOf(pathSeparator);
		  return fullPath.substring(sep + 1, dot);
	  }

	  public String path() {
		  int sep = fullPath.lastIndexOf(pathSeparator);
		  return fullPath.substring(0, sep);
	  }

	}
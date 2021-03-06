package cz.muni.stanse.configuration.source_enumeration;

import cz.muni.stanse.utils.ClassLogger;
import cz.muni.stanse.utils.FileAlgo;
import cz.muni.stanse.utils.Make;

import java.io.File;
import java.io.FileFilter;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class DirectorySourceEnumerator extends
                                           ReferencedSourceCodeFileEnumerator {

    // public section

    public DirectorySourceEnumerator(final String startDirectory,
                              final String extension,
                              final boolean searchSubdirectories) {
        super(startDirectory);
        this.extensions = Make.linkedList(extension.toUpperCase());
        this.searchSubdirectories = searchSubdirectories;
	System.err.println("\n=== BEWARE ===\nRunning in directory mode. " +
		"Note that this won't work well for C files which need\n" +
		"special flags on the CC command line. Preferred way is to " +
		"use --makefile\noption or generate a jobfile using stcc and " +
		"use --jobfile.\n");
    }

    public boolean getSearchSubdirectories() {
        return searchSubdirectories;
    }

    @Override
    public List<String> getSourceCodeFiles() throws SourceCodeFilesException {
	File dir = new File(getReferenceFile());
	if (!dir.exists() || !dir.isDirectory()) {
	    ClassLogger.error(this, "Cannot use directory '" +
			    dir.getAbsolutePath() + "'. It does not exist or " +
			    "is not a directory.");
	    return Make.<String>linkedList();
	}
        return toStringList(FileAlgo.enumerateFiles(dir,
                        new FileFilter() {
                        @Override public boolean accept(File file) {
                            return (file.isDirectory()) ?
                                        getSearchSubdirectories() :
                                        getExtensions().contains(
                                            FileAlgo.getExtension(
                                                file.toString().toUpperCase()));
                        }}, getSearchSubdirectories()));
    }

    // private section

    private static List<String> toStringList(final Set<File> fileSet) {
        final List<String> result = new LinkedList<String>();
        for (File file : fileSet)
            result.add(file.toString());
        return result;
    }

    private List<String> getExtensions() {
        return extensions;
    }

    private final List<String> extensions;
    private final boolean searchSubdirectories;
}

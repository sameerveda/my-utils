package sam.io.fileutils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import sam.io.fileutils.FilesWalker.FileWalkResult;

//VERSION = 1.21;
public final class FilesUtils {

	private FilesUtils() {}

	public static void openFile(File file) throws IOException{ FileOpener.openFile(file); }
	public static void openFileNoError(File file){ FileOpenerNE.openFile(file); }
	public static void openFileLocationInExplorer(File file) throws IOException { FileOpener.openFileLocationInExplorer(file); }
	public static void openFileLocationInExplorerNoError(File file) { FileOpenerNE.openFileLocationInExplorer(file); }

	/** {@link FilesUtilsIO#listDirsFiles(Path)} */
	public static FileWalkResult listDirsFiles(Path path) throws IOException { return FilesUtilsIO.listDirsFiles(path); }
	/** {@link FilesUtilsIO#pipe(InputStream,OutputStream)} */
	public long pipe(InputStream is, OutputStream os) throws IOException  { return FilesUtilsIO.pipe(is,os); }
	/** {@link FileUtilsIO#pipe0(InputStream,OutputStream)} */
	/** {@link FilesUtilsIO#deleteDir(Path)} */
	public static void deleteDir(Path dir) throws IOException {  FilesUtilsIO.deleteDir(dir); }
}

package com.izforge.izpack.api.merge;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

/**
 * Interface to merge in a given output stream
 *
 * @author Anthonin Bonnefoy
 */
public interface Mergeable
{

    File find(FileFilter fileFilter);

    List<File> recursivelyListFiles(FileFilter fileFilter);

    void merge(java.util.zip.ZipOutputStream outputStream);
}

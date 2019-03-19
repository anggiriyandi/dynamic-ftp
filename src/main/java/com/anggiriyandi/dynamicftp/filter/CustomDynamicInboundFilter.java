/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.anggiriyandi.dynamicftp.filter;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 *
 * @author anggi
 */
public class CustomDynamicInboundFilter implements FileListFilter<FTPFile> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDynamicInboundFilter.class);
    private File processFolderPath;

    private final Map<String, Long> fileSize = new HashMap<>();

    public String getProcessFolderPath() {
        if (processFolderPath != null) {
            return processFolderPath.getAbsolutePath();
        } else {
            return null;
        }
    }

    public void setProcessFolderPath(String processFolderPath) {
        LOGGER.info("set processFolderPath : {}", processFolderPath);
        if (!StringUtils.isEmpty(processFolderPath)) {
            File f = new File(processFolderPath);

            this.processFolderPath = f;
            LOGGER.info(
                    "The destination path for the process folder has been set to {}",
                    this.processFolderPath);
        }
    }

    public List<FTPFile> filterFiles(FTPFile[] files) {
        List<FTPFile> result = new ArrayList<FTPFile>();
        // only proceed in case proper destination directories have been added
        if (processFolderPath != null) {
            for (FTPFile file : files) {
                // only take the ones that are files

                if (file.getType() == FTPFile.FILE_TYPE) {
                    // now check whether or not this file has already been
                    // downloaded
                    LOGGER.info("CHECKING FILE TYPE");
                    if (doesFileExist(file.getName())) {
                        LOGGER.debug(
                                "The file {} already exists within any of the folders [{}], skip download",
                                file.getName(), new Object[]{
                            processFolderPath.getAbsolutePath()});
                    } else if (validateFileSize(file)) {
                        if (validatLastModified(file)) {
                            result.add(file);
                            fileSize.remove(file.getName());
                            LOGGER.info("File {} added to the download list",
                                    file.getName());
                        } else {
                            LOGGER.debug("Skip file {} as it's not of new file",
                                    file.getName());
                        }
                    }
                } else {
                    LOGGER.debug("Skip file {} as it's not of type file",
                            file.getName());
                }
            }
        } else {
            LOGGER.debug("The destination path is NULL, cannot filter for any files");
        }
        return result;
    }

    private boolean doesFileExist(String filename) {

        if (processFolderPath == null || StringUtils.isEmpty(filename)) {
            LOGGER.warn("Either one of the two destination paths is null or the given filename argument is empty");
            return false;
        }

        File f1 = new File(processFolderPath, filename);

        return f1.exists() ? true : false;
    }

    private boolean validateFileSize(FTPFile file) {
        if (this.fileSize.get(file.getName()) == null) {
            fileSize.put(file.getName(), file.getSize());
            return false;
        }

        if (this.fileSize.get(file.getName()).compareTo(file.getSize()) == 0) {
            return true;
        } else {
            fileSize.put(file.getName(), file.getSize());
            return false;
        }
    }

    private boolean validatLastModified(FTPFile file) {
        LocalDate sekarang = LocalDate.now();
        Date lastModified = file.getTimestamp().getTime();

        LocalDate date = lastModified.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (sekarang.equals(date)) {
            return true;
        } else {
            return false;
        }
    }
}

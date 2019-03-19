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
import org.springframework.integration.file.remote.FileInfo;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author anggi
 */
public class CustomDynamicSftpInboundFilter implements FileListFilter {

    private Pattern pattern;
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomDynamicSftpInboundFilter.class);
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
            LOGGER.debug(
                    "The destination path for the process folder has been set to {}",
                    this.processFolderPath);
        }
    }

    public void setPattern(String patternString) {
        LOGGER.info("====== set pattern");
        this.pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public List filterFiles(Object[] objects) {
        ArrayList filteredFiles = new ArrayList();
        LOGGER.info("========== masuk ke list filter");
        if (pattern == null) {
            LOGGER.info("========== pattern null");
        }

        if (objects == null) {
            LOGGER.info("========== object account");
        }
        
        LOGGER.info("======= object lenght =  {}", objects.length);

        if (pattern != null && objects != null && objects.length > 0) {
            Object entry = null;
            LOGGER.info("======= masuk ke process ");
            
            for (Object file : objects) {
                LOGGER.info("instance of object : {}",objects.getClass());
                Matcher matcher;
                if (file instanceof File) {
                    matcher = pattern.matcher(((File) file).getName());
                    LOGGER.info("======= type file = file");
                } else if (file instanceof FileInfo) {
                    matcher = pattern.matcher(((FileInfo) file).getFilename());
                    LOGGER.info("======= type file = file info");
                } else if (file instanceof FTPFile) {
                    matcher = pattern.matcher(((FTPFile) file).getName());
                    LOGGER.info("======= type file = ftp info");
                }else {
                    LOGGER.info("==== masuk ke continnue");
                    continue;
                }
                if (matcher.find()) {
                    if (entry != null) {
                        //compare the two entries
                        if (file instanceof File) {
                            if (((File) entry).lastModified() < ((File) file).lastModified()) {
                                entry = file;
                            }
                        } else {
                            if (((FileInfo) entry).getModified() < ((FileInfo) file).getModified()) {
                                entry = file;
                            }
                        }
                    } else {
                        entry = file;
                    }
                }
            }
            if (entry != null) {
                filteredFiles.add(entry);
            }
        }
        return filteredFiles;
    }

//     private boolean validateFileSize(File file) {
//        if (this.fileSize.get(file.getName()) == null) {
//            fileSize.put(file.getName(), file.);
//            return false;
//        }
//
//        if (this.fileSize.get(file.getName()).compareTo(file.getSize()) == 0) {
//            return true;
//        } else {
//            fileSize.put(file.getName(), file.getSize());
//            return false;
//        }
//    }
//
//    private boolean validatLastModified(FTPFile file) {
//        LocalDate sekarang = LocalDate.now();
//        Date lastModified = file.getTimestamp().getTime();
//
//        LocalDate date = lastModified.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//        if (sekarang.equals(date)) {
//            return true;
//        } else {
//            return false;
//        }
//    }
}

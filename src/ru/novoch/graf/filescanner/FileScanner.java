package ru.novoch.graf.filescanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Проверка наличия указанного списка файлов в директории <br>
 * Настройки хранятся в FileScanner.properties
 * @author Nikan
 *
 */
public class FileScanner {
	private static Logger logger;
	private static String pattern = "yyyyMMddhhmmss";
	private static Integer search_cnt = 0;
	
	public static void main(String[] args) {
		
		logger = Logger.getLogger("FileScannerLog");
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());
		try {
			FileHandler fh = new FileHandler("FileScannerLog.log");
			logger.addHandler(fh);
			fh.setFormatter(new SimpleFormatter());
		} catch (SecurityException|IOException e) {
			logger.log(Level.SEVERE, "Произошла ошибка при работе с FileHandler.", e);
		}
		
	    String file_dir = null;
	    List<String> file_names = new ArrayList<String>();
	    Level trace_level = null;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("FileScanner.properties"));
			String line;
			while ((line = reader.readLine()) != null) {
				if ("#".equals(line.substring(0, 0)))
					continue;
				else if ("file_dir=".equals(line.substring(0, 9)))
			    	file_dir = line.substring(9);
			    else if ("file_name=".equals(line.substring(0, 10))) 
			    	file_names.add(line.substring(10)); 
			    else if ("trace_level=".equals(line.substring(0, 12))) 
			    	switch (line.substring(12)) {
			    	case ("OFF"): trace_level = Level.OFF; break;
			    	case ("INFO"): trace_level = Level.INFO; break;
			    	case ("SEVERE"): trace_level = Level.SEVERE; break;
			    	default: trace_level = Level.INFO; break;
			    	}	
			}
			reader.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Произошла ошибка при работе с FileReader.", e);
		}
		
		logger.setLevel(trace_level);
		
		if (Objects.nonNull(file_dir) && !file_names.isEmpty()) {
			logger.log(Level.SEVERE,"BEGIN search");
			for(String file_name: file_names) {
				logger.log(Level.SEVERE,"--- search FILE: "+file_name);
				File target = searchFileByDeepness(file_dir,file_name);
			}
			logger.log(Level.SEVERE,"END search. Count: "+search_cnt);
		}
	}
	
	public static File searchFileByDeepness(final String directoryName, final String fileName) {
		File target = null;
		if(directoryName != null && fileName != null) {
		    File directory = new File(directoryName);
		    if(directory.isDirectory()) {
		    	logger.log(Level.INFO,"Search in directory: "+directoryName);
		        File file = new File(directoryName, fileName);
		        if(file.isFile()) {
		            target = file;
		            logger.log(Level.SEVERE,"search RESULT: file "+fileName+" was found in "+directoryName);
					search_cnt += 1;
		        }
		        //else {
		            List<File> subDirectories = getSubDirectories(directory);
		            do {
		                List<File> subSubDirectories = new ArrayList<File>();
		                for(File subDirectory : subDirectories) {
		                    File fileInSubDirectory = new File(subDirectory, fileName);
		                    logger.log(Level.INFO,"Search in directory: "+subDirectory);
		                    if(fileInSubDirectory.isFile()) {
		                    	logger.log(Level.SEVERE,"search RESULT: file "+fileName+" was found in "+subDirectory.getPath());
		    					search_cnt += 1;
		    					target = fileInSubDirectory;
		                    }
		                    subSubDirectories.addAll(getSubDirectories(subDirectory));
		                }
		                subDirectories = subSubDirectories;
		            } while(subDirectories != null && ! subDirectories.isEmpty());
		        //}
		    }
		}
		  return target;
		}

		private static List<File> getSubDirectories(final File directory) {
		    File[] subDirectories = directory.listFiles(new FilenameFilter() {
		        @Override
		        public boolean accept(final File current, final String name) {
		            return new File(current, name).isDirectory();
		        }
		    });
		    return Arrays.asList(subDirectories);
		}
}
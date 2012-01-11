package com.google.closure.template.unofficial.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;

/**
 * Used for compile Soy Templates to JS source.
 * 
 * @goal closure-templates
 */
public class ClosureTempalatesToJsMojo extends AbstractMojo {
    /**
     * Source folder which will be traversed recursively for all Soy templates.
     * 
     * @parameter
     */
    private String srcDir;
    
    /**
     * Destination folder where compiled js will be created.
     * 
     * @parameter
     */
    private String destDir;
    
    /**
     * Directory containing SoyToJsSrcCompiler.jar
     * 
     * @parameter
     */
    private String soyToJsSrcCompilerDir;
    
    
    
    public void execute() throws MojoExecutionException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(srcDir), "srcDir is not set.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(destDir), "destDir is not set.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(soyToJsSrcCompilerDir), "soyToJsSrcCompilerDir is not set.");

        
        String srcPath = getFileName(srcDir);
        String destPath = getFileName(destDir);
        

        StringBuilder stringBuilder = new StringBuilder();

        String[] extensions = {"soy"};
        Collection<File> fileList = FileUtils.listFiles(new File(srcPath), extensions, true);
        
        for (File currFile : fileList) {
            String relativePath = getRelativeFilePath(srcPath, currFile.getAbsolutePath());
            String destJsAbsPath = getDestinationPath(destPath, relativePath);
            compileSoy(currFile.getAbsolutePath(), destJsAbsPath);
        }
        
        getLog().info(stringBuilder.toString());
    }
    
    
    private void compileSoy(String srcSoyPath, String destJsAbsPath) {
        String soyDestDir = FilenameUtils.getPrefix(destJsAbsPath) + FilenameUtils.getPath(destJsAbsPath);
        File dir = new File(soyDestDir);
        
        if(!dir.exists()) {
            Preconditions.checkArgument(dir.mkdirs(), "Failed to create dir : " + soyDestDir);
            getLog().info("Created : " + soyDestDir);
        }
        
        String cmd = new StringBuilder("java -jar ")
                .append(FilenameUtils.normalizeNoEndSeparator(soyToJsSrcCompilerDir))
                .append("/SoyToJsSrcCompiler.jar")
                .append(" --outputPathFormat ")
                .append(destJsAbsPath)
                .append(".js")
                .append(" --shouldGenerateJsdoc --shouldProvideRequireSoyNamespaces ")
                .append(srcSoyPath)
                .toString();

        getLog().info("Executing command = " + cmd);
        try {
            Process child = Runtime.getRuntime().exec(cmd);
            child.waitFor();
            
            if (child.exitValue() != 0) {
              handleUnsuccessfulExit(child);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get Path for generated Soy.js.
     * @param destPath
     * @param relativePath
     * @return
     */
    private String getDestinationPath(String destPath, String relativePath) {
        File file = new File(destPath, relativePath);
        return file.getAbsolutePath();
    }

    /**
     * This method insures that the the returned fileNames are absolute fileNames, and dont
     * end with "/"
     * 
     * @param providedName
     * @return
     */
    private String getFileName(String providedName) {
        if (providedName.startsWith("/")) {
            // user provided an absolute path.
            return FilenameUtils.normalizeNoEndSeparator(providedName);
        }
        
        // Its a relative path.
        File currDir = new File(System.getProperty("user.dir"));
        File path = new File(currDir.getAbsolutePath(), providedName);
        
        return FilenameUtils.normalize(path.getAbsolutePath());
    }
    
    
    private String getRelativeFilePath(String parent, String absoluteFileName) {
        String parentDirPath = parent + "/";
        
        return absoluteFileName.replaceFirst(parentDirPath, "");
    }
    
    private void handleUnsuccessfulExit(Process child) throws IOException {
      String errStream = CharStreams.toString(new InputStreamReader(child.getErrorStream()));
      getLog().info("Failed with following error message : \n" + errStream);
      System.exit(child.exitValue());
    }
}
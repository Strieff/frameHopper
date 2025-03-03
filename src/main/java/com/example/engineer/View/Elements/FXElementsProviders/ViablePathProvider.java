package com.example.engineer.View.Elements.FXElementsProviders;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ViablePathProvider {
    private static final List<File> files = new ArrayList<>();

    public static File getFile(File file){
        try{
            if (System.getProperty("os.name").toLowerCase().contains("win"))
                return getWindowsShortPath(file);
            else
                return getLinuxMacSymLink(file);
        }catch(Exception e){
            return null;
        }
    }

    private static File getWindowsShortPath(File file) throws Exception{
        var pb = new ProcessBuilder("cmd", "/c", "for %I in (\"" + file.getAbsolutePath() + "\") do @echo %~sfI");
        pb.redirectErrorStream(true);
        var process = pb.start();
        var scanner = new java.util.Scanner(process.getInputStream());
        var shortPath = scanner.hasNext() ? scanner.nextLine() : null;
        scanner.close();

        System.out.println(shortPath);

        return shortPath != null ? new File(shortPath) : null;
    }

    private static File getLinuxMacSymLink(File file) throws Exception{
        var originalPath = file.toPath();
        var symlinkPath = Paths.get("/tmp/video_symlink.mp4");

        File symLinkFile = null;

        // Create symlink if it doesn't exist
        if (!Files.exists(symlinkPath)) {
            Files.createSymbolicLink(symlinkPath, originalPath);
            symLinkFile = symlinkPath.toFile();
            symLinkFile.deleteOnExit();
            files.add(symLinkFile);
        }

        return symLinkFile;
    }

    public static File getFallbackFile(File file) throws Exception {
        Path tempDir = Files.createTempDirectory("safe_video_");
        var tempDirFile = tempDir.toFile();
        tempDirFile.deleteOnExit(); // Mark directory for deletion
        files.add(tempDirFile);

        Path tempPath = tempDir.resolve("tmp."+ FilenameUtils.getExtension(file.getAbsolutePath()));
        Files.copy(file.toPath(), tempPath, StandardCopyOption.REPLACE_EXISTING);

        var tempFile = tempPath.toFile();
        files.add(tempFile);
        tempFile.deleteOnExit(); // Mark file for deletion

        System.out.println(tempFile.getAbsolutePath());

        return tempFile;
    }

    public static void clearFiles(){
        files.forEach(f -> {
            if(f.isDirectory())
                Arrays.stream(Objects.requireNonNull(f.listFiles())).forEach(fi -> {
                    try {
                        Files.delete(fi.toPath());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            else {
                try {
                    if(f.exists())
                        Files.delete(f.toPath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}

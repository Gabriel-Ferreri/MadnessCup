package Coocos.madnessCup.utils;

import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

public class DirectoryManager {

    private static final Path DIMENSIONS_PATH = Paths.get("world", "dimensions", "minecraft");
    private static final String TEMPLATE_NAME = "reincarnation_template";

    // Copying the dimension name requested so it gets sent to the file copy function
    public static void copyDimension(String dimensionName) {
        try {
            Path source = DIMENSIONS_PATH.resolve(TEMPLATE_NAME);
            Path destination = DIMENSIONS_PATH.resolve(dimensionName);

            if (!Files.exists(source)) throw new IOException("Template dimension does not exist: " + source);
            if (Files.exists(destination)) throw new IOException("Dimension already exists: " + dimensionName);

            copyDirectory(source, destination);
        }
        catch (IOException e) {
            Bukkit.getLogger().severe("Failed to copy dimension: " + dimensionName);
        }
    }

    // Copying the dimension to delete name
    public static void deleteDimension(String dimensionName) {

        Path dimension = DIMENSIONS_PATH.resolve(dimensionName);

        if (!Files.exists(dimension)) return;

        try {
            deleteDirectory(dimension);
        }
        catch (RuntimeException e) {
            Bukkit.getLogger().severe("Failed to delete dimension '" + dimensionName + "': " + e.getMessage());
        }
    }

    // Actually copying the directory
    private static void copyDirectory(Path source, Path destination) {

        try {
            try (Stream<Path> paths = Files.walk(source)) {

                paths.forEach(sourcePath -> {
                    try {
                        Path destinationPath = destination.resolve(source.relativize(sourcePath));

                        if (Files.isDirectory(sourcePath)) {
                            Files.createDirectories(destinationPath);
                        } else {
                            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Actually deleting the directory
    private static void deleteDirectory(Path directory) {
        try {
            try (Stream<Path> paths = Files.walk(directory)) {
                // Delete roots first to actually delete everything
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
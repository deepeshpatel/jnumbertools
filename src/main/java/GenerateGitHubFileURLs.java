import java.nio.file.*;
import java.io.IOException;
import java.util.stream.Stream;

public class GenerateGitHubFileURLs {
    public static void main(String[] args) {
        // Check if input folder path is provided

        // Input folder path from command-line argument
        String inputPath = "/Users/deepesh/IdeaProjects/jnumbertools/src";
        Path startPath = Paths.get(inputPath);

        // Base URL for GitHub repository
        String baseUrl = "https://github.com/deepeshpatel/jnumbertools/blob/main/src/";

        try {
            // Walk the file tree recursively, filter for .java files, and print relative URLs
            try (Stream<Path> paths = Files.walk(startPath)) {
                paths
                        .filter(Files::isRegularFile) // Only process regular files
                        .filter(path -> path.toString().endsWith(".java")) // Only .java files
                        .map(path -> startPath.relativize(path)) // Get relative path from input folder
                        .map(relativePath -> baseUrl + relativePath.toString().replace("\\", "/")) // Prepend base URL and normalize separators
                        .forEach(System.out::println); // Print each URL
            }
        } catch (IOException e) {
            System.err.println("Error walking directory: " + e.getMessage());
            System.exit(1);
        }
    }
}
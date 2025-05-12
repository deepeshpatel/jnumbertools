package io.github.deepeshpatel.jnumbertools;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class MarkdownFilesTest {

    // List all MD files to test (relative to project root)
    private static final List<String> MD_FILES = List.of(
            "README.md",
            "docs/calculator/README.md",
            "docs/combinations/README.md",
            "docs/numbersystem/README.md",
            "docs/permutations/README.md",
            "docs/products/README.md",
            "docs/ranking/README.md",
            "docs/sets/README.md"
    );

    @Test
    void testAllMarkdownFiles() {
        Path projectRoot = Paths.get("").toAbsolutePath();

        MD_FILES.forEach(filePath -> {
            Path path = projectRoot.resolve(filePath);
            assertTrue(Files.exists(path), "Missing file: " + filePath);

            try {
                String content = Files.readString(path);
                testLinksInContent(content, projectRoot, path);
            } catch (Exception e) {
                fail("Error processing " + filePath + ": " + e.getMessage());
            }
        });
    }

    private void testLinksInContent(String content, Path projectRoot, Path mdFilePath) {
        Pattern linkPattern = Pattern.compile("\\[.*?\\]\\((.*?)\\)");
        Matcher matcher = linkPattern.matcher(content);
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        while (matcher.find()) {
            String link = matcher.group(1);

            // Skip special links
            if (link.startsWith("#") || link.startsWith("mailto:") || link.startsWith("tel:")) {
                continue;
            }

            try {
                if (link.startsWith("http")) {
                    // Test HTTP links
                    testHttpLink(client, link);
                } else {
                    // Test local file links
                    testLocalLink(projectRoot, mdFilePath, link);
                }
            } catch (Exception e) {
                fail("[" + mdFilePath + "] Broken link '" + link + "': " + e.getMessage());
            }
        }
    }

    private void testHttpLink(HttpClient client, String link) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(link))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<Void> response = client.send(request,
                HttpResponse.BodyHandlers.discarding());

        assertTrue(response.statusCode() < 400,
                "Broken external link: " + link +
                        " (Status: " + response.statusCode() + ")");
    }

    private void testLocalLink(Path projectRoot, Path mdFilePath, String link) {
        // Handle different relative path cases
        Path resolvedPath;
        if (link.startsWith("/")) {
            // Root-relative path (e.g., "/docs/image.png")
            resolvedPath = projectRoot.resolve(link.substring(1));
        } else {
            // Document-relative path (e.g., "resources/image.png")
            resolvedPath = mdFilePath.getParent().resolve(link);
        }

        assertTrue(Files.exists(resolvedPath),
                "Missing local file: " + link +
                        "\nResolved to: " + resolvedPath +
                        "\nReferenced from: " + mdFilePath);
    }
}
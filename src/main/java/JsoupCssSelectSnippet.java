import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class JsoupCssSelectSnippet {

    private static Logger LOGGER = LoggerFactory.getLogger(JsoupCssSelectSnippet.class);

    private static String CHARSET_NAME = "utf8";

    public static void main(String[] args) {

        {
            // Check how many arguments were passed in
            if (args.length != 2) {
                LOGGER.error("Proper Usage is: <your_bundled_app>.jar <input_origin_file_path> <input_other_sample_file_path>");
                System.exit(0);
            }
        }

        // Jsoup requires an absolute file path to resolve possible relative paths in HTML,
        // so providing InputStream through classpath resources is not a case

//        String cssQuery = "div[id=\"success\"] button[class*=\"btn-primary\"]";
        String cssQuery = "div[class=\"panel-body\"] a[href=\"#ok\"]";
        String resourceOriginPath = args[0];
        String resourceOtherFile = args[1];

        Optional<Elements> originElementsOpt = findElementsByQuery(new File(resourceOriginPath), cssQuery);
        Optional<Elements> otherElementsOpt = findElementsByQuery(new File(resourceOtherFile), cssQuery);

        if (originElementsOpt.toString().equals(otherElementsOpt.toString())) {
            LOGGER.info("There is no diff Nothing to print");
            System.exit(0);
        }

        Optional<List<String>> otherElementsAttrsOpts = getElementsAttrsOpts(otherElementsOpt);


        otherElementsAttrsOpts.ifPresent(attrsList ->
                attrsList.stream().
                        forEach(attrs ->
                                LOGGER.info("Target element attrs: [{}]", attrs)
                        )
        );
    }

    private static Optional<List<String>> getElementsAttrsOpts(Optional<Elements> elementsOpt) {
        return elementsOpt.map(buttons ->
                {
                    List<String> stringifiedAttrs = new ArrayList<>();

                    buttons.forEach(button -> {
                        String tag = button.tagName();
                        stringifiedAttrs.add(tag);
                        stringifiedAttrs.add(
                                button.attributes().asList().stream()
                                        .map(attr -> attr.getKey() + " = " + attr.getValue())
                                        .collect(Collectors.joining(", ")));

                        stringifiedAttrs.add(tag);
                    });
                    return stringifiedAttrs;
                }
        );
    }

    private static Optional<Elements> findElementsByQuery(File htmlFile, String cssQuery) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());

            return Optional.of(doc.select(cssQuery));

        } catch (IOException e) {
            LOGGER.error("Error reading [{}] file", htmlFile.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

}
package attatrol.ahsm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import attatrol.ahsm.site.InitialSiteVerifier;
import attatrol.ahsm.site.SiteContent;
import attatrol.ahsm.site.SiteContentMerger;
import attatrol.ahsm.site.SiteParser;
import attatrol.ahsm.site.XrefCopier;

public class Main {
    
    /**
     * Content of help html.
     */
    public static final String HELP_FILENAME = "ahsm-help.html";
    public static final String SOURCE_LINK = "https://github.com/attatrol/ahsm";
    public static final String HELP_HTML_LINK = "ahsm-help.html";
    public static final String[] HELP_TEXT = {
            "This is symmetric difference generated from two maven-site reports.",
            "All matching rows from each report are deleted, then remaining rows are merged into single report.",
            "Currently number of identical lines in each report is not taken into account.", };
    
    /**
     * Messages for errors.
     */
    public static final String MSG_WRONG_NUMBER_OF_ARGS = "Not enough command line args, need at least 3.";
    public static final String MSG_BAD_PATH = "Failed to resolve path to site: ";
    public static final String MSG_NOT_EXISTS = "Site directory doesnt exist: ";
    public static final String MSG_BAD_SITE = "Failed to locate essentual files that belong to the site: ";
    public static final String MSG_IDENTICAL_INPUT = "Both input sites have the same path.";
    public static final String MSG_ALTERNATE_TRY = "Failed to copy files from first site, trying with second";
    public static final String MSG_PREPARATION_FAILURE = "Failed to pass initial integrity testing.";
    public static final String MSG_MERGE_FAILURE = "Failed to merge file properly";

    /**
     * Stage success messages.
     */
    public static final String MSG_PREPARATION_SUCCESS = "Successfull preparation stage.";
    public static final String MSG_GENERAL_SUCCESS = "Merging success.";

    /**
     * Help message.
     */
    public static final String MSG_HELP = "This program creates symmetric difference from two maven site reports\n"
            + "generated for checkstyle build.\n" + "It has 3 obligatory command line arguments:\n"
            + "First one is the project name,\n"
            + "two next are the links to sites, already generated by maven-site-plugin,\n"
            + "forth argument is a destination for the result site, it is facultative,"
            + " if skipped, you will find the result site in your home directory.\n"
            + "Any other arguments will be skipped.";

    public static final String MISC_ENCODING = "UTF-8";

    private static String projectName;

    /**
     * Executes pre-processing checks for cli arguments, then executes 2 processing stages.
     * @param args
     *        , cli arguments.
     */
    public static void main(String... args)
    {
        if (args.length >= 2) {
            Path pathSite1 = null;
            Path pathSite2 = null;
            Path resultPath = null;
            // preparation stage of the process
            try {
                projectName = args[0];
                // resolving paths
                pathSite1 = Paths.get(args[1]);
                pathSite2 = Paths.get(args[2]);
                resultPath = args.length >= 4 ? Paths.get(args[3])
                        : Paths.get(System.getProperty("user.home"))
                                .resolve("ahsm_report_"
                                        + new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss")
                                                .format(Calendar.getInstance().getTime()));
                preparationStage(pathSite1, pathSite2, resultPath);
                System.out.println(MSG_PREPARATION_SUCCESS);
                // merging stage
                try {
                    mergingStage(pathSite1, pathSite2, resultPath);
                    System.out.println(MSG_GENERAL_SUCCESS);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    System.out.println(MSG_MERGE_FAILURE);
                }
            }
            catch (InvalidPathException e) {
                e.printStackTrace();
                System.out.print(MSG_HELP);
                System.out.println(MSG_BAD_PATH);
            }
            catch (IllegalArgumentException | IOException e) {
                e.printStackTrace();
                System.out.print(MSG_HELP);
                System.out.println(MSG_PREPARATION_FAILURE);
            }
        }
        else {
            System.out.print(MSG_HELP);
            System.out.println(MSG_WRONG_NUMBER_OF_ARGS);
        }
    }

    /**
     * Perform preparation stage of the process.
     */
    private static void preparationStage(Path pathSite1, Path pathSite2, Path resultPath)
            throws IOException {
        FilesystemUtils.createDirectory(resultPath);
        initialVerification(pathSite1, pathSite2);
        copyImmutables(pathSite1, pathSite2, resultPath);

    }

    /**
     * Performs main, merging, stage of the process
     */
    private static void mergingStage(Path pathSite1, Path pathSite2, Path resultPath)
            throws IOException {
        final SiteContent site1Content = getContent(pathSite1);
        final SiteContent site2Content = getContent(pathSite2);
        final String resultSiteId = projectName + " - "
                + resultPath.getName(resultPath.getNameCount() - 1).toString();
        final SiteContent mergedContent = new SiteContentMerger(site1Content, site2Content,
                resultSiteId).merge();
        SiteParser.exportToFile(mergedContent, resultPath.resolve(projectName + "_merged.html"));
        XrefCopier.copyXrefFiles(mergedContent.getFileTables(), pathSite1, pathSite2, resultPath);
    }

    /**
     * Checks file structure of the input sites.
     * @param pathSite1
     *        input site path 1
     * @param pathSite2
     *        input site path 2
     * @throws IllegalArgumentException
     *         on failure of any check.
     */
    private static void initialVerification(Path pathSite1, Path pathSite2)
            throws IllegalArgumentException {
        if (!Files.isDirectory(pathSite1)) {
            throw new IllegalArgumentException(MSG_NOT_EXISTS + pathSite1);
        }
        if (!Files.isDirectory(pathSite2)) {
            throw new IllegalArgumentException(MSG_NOT_EXISTS + pathSite2);
        }
        if (pathSite1.equals(pathSite2)) {
            throw new IllegalArgumentException(MSG_IDENTICAL_INPUT);
        }
        if (!InitialSiteVerifier.verifySite(pathSite1, projectName)) {
            throw new IllegalArgumentException(MSG_BAD_SITE + pathSite1);
        }
        if (!InitialSiteVerifier.verifySite(pathSite2, projectName)) {
            throw new IllegalArgumentException(MSG_BAD_SITE + pathSite2);
        }

    }

    /**
     * Copies supplementary files (those, which aren't connected with report) from input sites to
     * the result site.
     * @param pathSite1
     *        input site path 1
     * @param pathSite2
     *        input site path 2
     * @param resultPath
     *        result site path
     * @throws IOException
     *         on general i/o failure.
     */
    private static void copyImmutables(Path pathSite1, Path pathSite2, Path resultPath)
            throws IOException {
        try {
            InitialSiteVerifier.copyImmutableSiteFiles(pathSite1, resultPath);
        }
        catch (Exception e) {
            // second try with sister site
            e.printStackTrace();
            System.out.println(MSG_ALTERNATE_TRY);
            FilesystemUtils.createDirectory(resultPath);
            InitialSiteVerifier.copyImmutableSiteFiles(pathSite2, resultPath);
        }

    }

    /**
     * Parses site data into SiteContent.
     * @param pathSite1
     *        path to site directory.
     * @return resulting SiteContent.
     * @throws IOException
     *         on parsing failure.
     */
    private static SiteContent getContent(Path pathSite1)
            throws IOException {
        final Path subject1 = pathSite1.resolve(projectName + ".html");
        return SiteParser.parse(subject1,
                pathSite1.getName(pathSite1.getNameCount() - 1).toString());
    }

}

package attatrol.ahsm;

import java.io.IOException;
import java.nio.file.Files;
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
   * Messages for errors.
   */
  public static final String MSG_WRONG_NUMBER_OF_ARGS = "Not enough command line args, need at least 4.";
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
      + "forth argument is a destination for the difference site, it is facultative.\n"
      + "Any other arguments will be skipped.";


  public static final String MISC_ENCODING = "UTF-8";


  private static String projectName;

  /**
   * Executes preprocessing checks for cli args,
   * then executes 2 processing stages.
   * 
   * @param args
   *          , cli arguments.
   */
  public static void main(String... args) {
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
        // TODO test with default path
        resultPath = args.length >= 4 ? Paths.get(args[3])
            : Paths.get(System.getProperty("user.home"))
            .resolve("ahsm_report_"
                      + new SimpleDateFormat("yyyyMMdd_HHmmss")
                      .format(Calendar.getInstance().getTime()));
        preparationStage(pathSite1, pathSite2, resultPath);
        System.out.println(MSG_PREPARATION_SUCCESS);
        // merging stage
        try {
          mergingStage(pathSite1, pathSite2, resultPath);
          System.out.println(MSG_GENERAL_SUCCESS);
        } catch (IOException e) {
          e.printStackTrace();
          System.out.println(MSG_MERGE_FAILURE);
        }
      } catch (IllegalArgumentException | IOException e) {
        e.printStackTrace();
        System.out.print(MSG_HELP);
        System.out.println(MSG_PREPARATION_FAILURE);
      }
    } else {
      System.out.print(MSG_HELP);
      System.out.println(MSG_WRONG_NUMBER_OF_ARGS);
    }
  }

  /**
   * Perform preparation stage of the process.
   */
  private static void preparationStage(Path pathSite1, Path pathSite2, Path resultPath) throws IOException {
    FilesystemUtils.createDirectory(resultPath);
    initialVerification(pathSite1, pathSite2);
    copyImmutables(pathSite1, pathSite2, resultPath);
    
  }
  
  /**
   * Performs main, merging, stage of the process
   */
  private static void mergingStage(Path pathSite1, Path pathSite2, Path resultPath) throws IOException {
    final SiteContent site1Content = getContent(pathSite1);
    final SiteContent site2Content = getContent(pathSite2);
    final SiteContent mergedContent = new SiteContentMerger(site1Content, site2Content).merge();
    exportToFile(mergedContent, resultPath, pathSite1);
    XrefCopier.copyXrefFiles(mergedContent.getFileTables(), pathSite1, pathSite2, resultPath);
  }

  /**
   * Checks file structure of the input sites.
   * 
   * @param pathSite1 input site path 1
   * @param pathSite2 input site path 2
   * @throws IllegalArgumentException on failure of any check.
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
   * Copies supplementary files (those, which arent connected with report) 
   * from input sites to the result site.
   * 
   * @param pathSite1 input site path 1
   * @param pathSite2 input site path 2
   * @param resultPath result site path
   * @throws IOException on general i/o failure.
   */
  private static void copyImmutables(Path pathSite1, Path pathSite2, Path resultPath)
      throws IOException {
    try {
      InitialSiteVerifier.copyImmutableSiteFiles(pathSite1, resultPath);
    } catch (Exception e) {
      // second try with sister site
      e.printStackTrace();
      System.out.println(MSG_ALTERNATE_TRY);
      FilesystemUtils.createDirectory(resultPath);
      InitialSiteVerifier.copyImmutableSiteFiles(pathSite2, resultPath);
    }

  }
  
  /**
   * Parses site data into SiteContent.
   * 
   * @param pathSite1 path to site directory.
   * @return resulting SiteContent.
   * @throws IOException on parsing failure.
   */
  private static SiteContent getContent(Path pathSite1) throws IOException {
    final Path subject1 = pathSite1.resolve(projectName + ".html");
    return SiteParser.parse(subject1,
        pathSite1.getName(pathSite1.getNameCount() - 1).toString());
  }

  /**
   * Export SiteContent into file.
   * 
   * @param mergedContent result site content.
   * @param resultPath path to the result site.
   * @param inputPathSite, used as template/ TODO delete it ASAP
   * @throws IOException on i/o failure.
   */
  private static void exportToFile(SiteContent mergedContent, Path resultPath, Path inputPathSite)
      throws IOException {
    final Path templatePath = inputPathSite.resolve(projectName + ".html");
    SiteParser.exportToFile(mergedContent, resultPath, templatePath, projectName);
  }

}

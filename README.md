# ahsm
ad hoc site merger - for creating a symmetrical difference from two maven:site reports.
It is useful when you have two different large maven reports and want to know difference between them.
It checks integrity of both sites, deletes identical lines present in both sites, merges residues into result site.

depends on: jsoup-1.8.3

usage example:

You have 2 different checkstyle repos, original and forked, for each of them do 1-4:
  1. mvn clean install
  2. go to checkstyle-tester directory, uncomment all links in projects-to-test-on.properties, edit my_check.xml
  3. execute ./launch.sh -Dcheckstyle.config.location=my_check.xml
  4. copy AND RENAME generated site from checkstyle-tester/target, both sites should have different terminal directories, for example cp -R ./checkstyle-tester/target/site ./checkstyle-tester/site_forked
  5. now you have two sites (i.e. site_vanilla, site_forked), execute this utility with 4 command line args: 
        - name of the project
        - name of the first site
        - name of the second site
        - name of the resulting site
        i.e. java ./ahsm.jar -jar checkstyle ~/eclipse_workspace/tester-checkstyle/checkstyle-tester/site_vanilla ~/eclipse_workspace/tester-checkstyle/checkstyle-tester/site_forked ~/Documents/eclipse_workspace/tester-checkstyle/checkstyle-tester/site_result
  6. you may want to replace original css with one with distinctive colours from here 
    https://github.com/attatrol/attatrol.github.io/tree/master/site_result/css

    example of result site here:
    http://attatrol.github.io/site_result2_rebase/checkstyle_merged.html

+. Phylr Server Installation:
----------------------------------------------------------------------------
0. prerequites:
 Tomcat 6.0, Java 1.5, Ant

1. Check out Phylr codebase
 + svn co http://phylr.googlecode.com/svn/trunk phylr

2. Compile the basic server
 + cd phylr/oclcSRW
 + ant
 + cd ..

3. Set up the Lucene adapter
 + cd oclcLuceneAdapter
 + ant
 + cp dist/*.jar ../oclcSRW/web/WEB-INF/lib
 + cp lib/lucene-core-2.4.0.jar ../oclcSRW/web/WEB-INF/lib
 + cd ../oclcSRW
 + ant

4. Deploy
 + copy dist/SRW.war to $CATALINA_HOME/webapps
 + restart tomcat
 + test that the basic service works at
 http://localhost:8080/SRW/search/test

5. Configure
 + edit the configuration files in phylr/srwconf to contain your
 settings
 + copy these configuration files to
 $CATALINA_HOME/webapps/SRW/WEB-INF/classes

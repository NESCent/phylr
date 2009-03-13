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
 + cd ..

4. Configure (configuration must be done before building, due to
problems that arise if the configuration files are modified after
deployment, see notes below)
 + cd oclcSRW/build/web/WEB-INF/classes
 + (edit the files there and then cd up to the oclcSRW level)
 + ant

4. Deploy
 + copy dist/SRW.war to $CATALINA_HOME/webapps
 + restart tomcat
 + test that the basic service works at
 http://localhost:8080/SRW/search/test
 + test that the extended services work

-------------------------------------------------------------------------------

Configuration issues

If the configuration files are modified after the webapp is deployed,
the app may fail to run properly. It is best to perform all
configuration before compiling, and deploy the war file. Do not modify
the contents of the application folder after it has been deployed.

The following will create a phylr-gsoc.war file in the target directory. 

# mvn package

This will also create a phylr-gsoc.jar file in the target directory. 

To compile the sources: 

# mvn compile

Before deploy, don't forget to update the file src/main/resources/SRWServer.props, 
basically you need to already created lucene index with org.nescent.phylr.lucene.NexmlIndexer. 
Put the path to the lucene index directory in the props file.

To run the NexmlIndexer with testing examples, try:

# mkdir data/phylr-index

# mvn -e exec:java \
  -Dexec.mainClass=org.nescent.phylr.lucene.NexmlIndexer \
  -Dexec.args="-d data/phylr-nexml/ -i data/phylr-index/ -m src/test/resources/predicates.txt"
  
 
==== DATABASE MIGRATION ====
For instructions on how to migrate a treebase database to a biosql database with phylodb extentions, please
refer to the comments in src/main/sql/TreeBaseBioSQLMig.sql
  



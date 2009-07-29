The following will create a phylr-gsoc.war file in the target directory. 

# mvn package

This will also create a phylr-gsoc.jar file in the target directory. 

To compile the sources: 

# mvn compile

Before deploy, don't forget to update the file src/main/resources/SRWServer.props, 
basically you need to already created lucene index with org.nescent.phylr.lucene.NexmlIndexer. 
Put the path to the lucene index directory in the props file.

To run the NexmlIndexer with testing examples, try:

# mvn -e exec:java \
  -Dexec.mainClass=org.nescent.phylr.lucene.NexmlIndexer \
  -Dexec.args="-d data/phylr-nexml/ -i data/phylr-index/ -m src/test/resources/predicates.txt"
  
To Migrate Data from TreeBase (PostgreSQL)
First create database
# createdb biosql

import treebase dump into the database
# psql biosql < treebase.sql

create biosql tables using the schema
# psql biosql < biosqldb-pg.sql

Because both treebase and biosql phylodb extension have a table name "node_path", 
so rename "node_path" to "bs_node_path" in biosql-phylodb-pg.sql
THen Create tables in the biosql-phylodb extension
# psql biosql < biosql-phylodb-pg.sql

Now import the ncbi taxonomy using the biosql import script
# load_ncbi_taxonomy.pl --diver Pg --download yes --dbname biosql

Then execute the migration script
# psql biosql < TreeBaseBioSQLMig.sql

All done. 



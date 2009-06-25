To create a war file:

# mvn package

To compile

# mvn compile

Before deploy, don't forget to update the file src/main/resources/SRWServer.props, 
basically you need to already created lucene index with nexmlLuceneIndexer. Put the
path to the lucene index directory in the props file.


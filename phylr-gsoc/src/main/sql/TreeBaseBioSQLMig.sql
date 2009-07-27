-- This script is used to migrate TreeBase database to a BioSQL database on PostgreSQL 8.3
-- Please follow these steps before running this
-- 1. create database
-- # createdb biosql

-- 2. import treebase dump (which I obtained from Bill Piel) into the database
-- # psql biosql < phylodb.sql

-- 3. create biosql tables using the schema
-- # psql biosql < biosqldb-pg.sql

-- 4. Because both treebase and biosql phylodb extension have a table name "node_path", 
-- so one of them needs to be renamed. 
-- open biosql-phylodb-pg.sql in a text editor, and rename "node_path" to "bs_node_path". 
-- (Please note you have to use "bs_node_path", as it'll be referred in this script!)

-- 5. Create tables in the biosql-phylodb extension
-- # psql biosql < biosql-phylodb-pg.sql

-- 6. Now import the ncbi taxonomy using the biosql import script. Please follow the biosql
-- installation instructions for this step. If you already have BioPerl, and BioPerlDB > 1.5.2, 
-- Then you can simply execute the following (The load_ncbi_taxonomy.pl) is in the biosql 
-- distribution:
-- # load_ncbi_taxonomy.pl --driver Pg --download yes --dbname biosql

-- 8. Finally, execute this migration script
-- # psql biosql < TreeBaseBioSQLMig.sql

ALTER TABLE tree ALTER COLUMN node_id DROP NOT NULL;
ALTER TABLE tree ALTER COLUMN name TYPE character varying(255);
ALTER TABLE tree ALTER COLUMN name DROP NOT NULL;

ALTER TABLE tree DROP CONSTRAINT tree_c1;

-- create a biodatabase record for TreeBase
INSERT INTO biodatabase VALUES (1, 'TreeBase', NULL, NULL);

INSERT INTO tree (tree_id, name, identifier, is_rooted, biodatabase_id) 
	SELECT trees.tree_id, trees.tree_title, trees.legacy_id, true, 1 from trees;

-- Insert data into node
INSERT INTO node (node_id, label, tree_id, left_idx, right_idx) 
	SELECT nodes.node_id, nodes.node_label, nodes.tree_id, nodes.left_id, nodes.right_id from nodes; 
	
UPDATE tree SET node_id = (SELECT trees.root FROM trees WHERE trees.tree_id = tree.tree_id);

ALTER TABLE tree ALTER COLUMN node_id SET NOT NULL;

INSERT INTO ontology (name, definition) VALUES ('TreeBase DB', 'Some TreeBase database terms');

-- TREE QUALIFIER VALUE

INSERT INTO term (name, ontology_id) SELECT 'tree.algorithm', ontology.ontology_id FROM ontology WHERE ontology.name = 'TreeBase DB';
INSERT INTO term (name, ontology_id) SELECT 'tree.type', ontology.ontology_id FROM ontology WHERE ontology.name = 'TreeBase DB';
INSERT INTO term (name, ontology_id) SELECT 'tree.software', ontology.ontology_id FROM ontology WHERE ontology.name = 'TreeBase DB';

INSERT INTO tree_qualifier_value (tree_id, term_id, value) 
	SELECT ts.tree_id, te.term_id, ts.software FROM trees ts, term te WHERE te.name = 'tree.algorithm';
	
INSERT INTO tree_qualifier_value (tree_id, term_id, value) 
	SELECT ts.tree_id, te.term_id, ts.tree_type FROM trees ts, term te WHERE te.name = 'tree.type';	

INSERT INTO tree_qualifier_value (tree_id, term_id, value) 
	SELECT ts.tree_id, te.term_id, ts.software FROM trees ts, term te WHERE te.name = 'tree.software';	

-- NODE_PATH
	
INSERT INTO bs_node_path (child_node_id, parent_node_id, distance) SELECT child_node_id, parent_node_id, distance FROM node_path;

-- EDGE

INSERT INTO edge (child_node_id, parent_node_id) SELECT child_id, parent_id FROM edges;

INSERT INTO term (name, ontology_id) SELECT 'edge.support', ontology.ontology_id FROM ontology WHERE ontology.name = 'TreeBase DB';

INSERT INTO edge_qualifier_value (edge_id, term_id, value) 
	SELECT e.edge_id, t.term_id, es.edge_support 
	FROM edge e, term t, edges es
	WHERE e.child_node_id = es.child_id AND e.parent_node_id = es.parent_id AND t.name='edge.support';
	
-- TAXON
INSERT INTO node_taxon (node_id, taxon_id) 
	SELECT n.node_id, t.taxon_id FROM nodes n, taxon t, taxa tx
	WHERE n.taxon_id = tx.taxon_id and tx.taxid = t.ncbi_taxon_id;
	
-- STUDY
INSERT INTO ontology (name, definition) VALUES ('Dublin Core', 'Dublin Core Metadata Initiative (DCMI)');
INSERT INTO term (name, ontology_id) SELECT 'identifier', ontology.ontology_id FROM ontology WHERE ontology.name = 'Dublin Core';
INSERT INTO term (name, ontology_id) SELECT 'contributor', ontology.ontology_id FROM ontology WHERE ontology.name = 'Dublin Core';
INSERT INTO term (name, ontology_id) SELECT 'title', ontology.ontology_id FROM ontology WHERE ontology.name = 'Dublin Core';
INSERT INTO term (name, ontology_id) SELECT 'abstract', ontology.ontology_id FROM ontology WHERE ontology.name = 'Dublin Core';
INSERT INTO term (name, ontology_id) SELECT 'study.doi', ontology.ontology_id FROM ontology WHERE ontology.name = 'TreeBase DB';

INSERT INTO tree_qualifier_value (tree_id, term_id, value) 
	SELECT tr.tree_id, te.term_id, s.study_id FROM trees tr, term te, study s, ontology o
	WHERE tr.study_id = s.study_id AND te.name='identifier' AND te.ontology_id = o.ontology_id AND o.name='Dublin Core';

INSERT INTO tree_qualifier_value (tree_id, term_id, value) 
	SELECT tr.tree_id, te.term_id, s.author FROM trees tr, term te, study s, ontology o
	WHERE tr.study_id = s.study_id AND te.name='contributor' AND te.ontology_id = o.ontology_id AND o.name='Dublin Core';

INSERT INTO tree_qualifier_value (tree_id, term_id, value) 
	SELECT tr.tree_id, te.term_id, s.title FROM trees tr, term te, study s, ontology o
	WHERE tr.study_id = s.study_id AND te.name='title' AND te.ontology_id = o.ontology_id AND o.name='Dublin Core';

INSERT INTO tree_qualifier_value (tree_id, term_id, value) 
	SELECT tr.tree_id, te.term_id, s.abstract FROM trees tr, term te, study s, ontology o
	WHERE tr.study_id = s.study_id AND te.name='abstract' AND te.ontology_id = o.ontology_id AND o.name='Dublin Core';
	
INSERT INTO tree_qualifier_value (tree_id, term_id, value) 
	SELECT tr.tree_id, te.term_id, s.doi FROM trees tr, term te, study s, ontology o
	WHERE tr.study_id = s.study_id AND te.name='study.doi' AND te.ontology_id = o.ontology_id AND o.name='TreeBase DB';
	
-- DROP TreeBase Tables
DROP TABLE edges, ncbi_names, ncbi_node_path, ncbi_nodes, node_path, nodes, study, taxa, taxon_variants, trees;

-- Rename table node_path back
ALTER TABLE bs_node_path RENAME TO node_path;



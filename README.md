lexicons
========

This project was primarily started to build lexicons for natural language processing tasks using Wikipedia.
Now it also includes an iterator interface to process the wikipedia xml dump and extract anchor tags from it.

To generate lexicons from the latest wikipedia dump, you must download the wikipedia dump along with freebase-to-wikipedia
and freebase types files from freebase wex dump.

Once you have those, set all the paths in config.property and run maven with the below command:

mvn scala:run -Dlauncher=lexicon

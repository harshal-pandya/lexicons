#!/bin/sh

grep -P "\t/location/location$" freebase-wex-2012-08-06-freebase_types.tsv > location.tsv
grep -P "\t/business/employer$" freebase-wex-2012-08-06-freebase_types.tsv > business.tsv
grep -P "\t/organization/organization$" freebase-wex-2012-08-06-freebase_types.tsv > organization.tsv
grep -P "\t/people/person$" freebase-wex-2012-08-06-freebase_types.tsv > person.tsv
grep -P "\t/music/composition$" freebase-wex-2012-08-06-freebase_types.tsv > songs.tsv
grep -P "\t/film/film$" freebase-wex-2012-08-06-freebase_types.tsv > film.tsv
grep -P "\t/base/tagit/man_made_thing$" freebase-wex-2012-08-06-freebase_types.tsv > man_made_thing.tsv
grep -P "\t/sports/sports_championship$" freebase-wex-2012-08-06-freebase_types.tsv > competition.tsv
grep -P "\t/time/recurring_event$" freebase-wex-2012-08-06-freebase_types.tsv > events.tsv
grep -P "\t/military/military_conflict$" freebase-wex-2012-08-06-freebase_types.tsv > battles.tsv
grep -P "\t/book/book$" freebase-wex-2012-08-06-freebase_types.tsv > book.tsv


awk -F'\t' '{ print $1 }' location.tsv | sort | uniq > location.ids
awk -F'\t' '{ print $1 }' business.tsv | sort | uniq > business.ids
awk -F'\t' '{ print $1 }' organization.tsv | sort | uniq > organization.ids
awk -F'\t' '{ print $1 }' person.tsv | sort | uniq > person.ids
awk -F'\t' '{ print $1 }' songs.tsv | sort | uniq > songs.ids
awk -F'\t' '{ print $1 }' film.tsv | sort | uniq > film.ids
awk -F'\t' '{ print $1 }' man_made_thing.tsv | sort | uniq > man_made_thing.ids
awk -F'\t' '{ print $1 }' competition.tsv | sort | uniq > competition.ids
awk -F'\t' '{ print $1 }' events.tsv | sort | uniq > events.ids
awk -F'\t' '{ print $1 }' battles.tsv | sort | uniq > battles.ids
awk -F'\t' '{ print $1 }' book.tsv | sort | uniq > book.ids
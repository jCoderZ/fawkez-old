#!/bin/bash
#
# Usage: ./tools/bin/pmd-ruleset-gen.sh | xmllint --format -
#

# path to the pmd rulesets, e.g. 'jar xvf /tool/pmd/2.0/lib/pmd.jar'
PMD_RULSET=tmp
echo '<?xml version="1.0"?>
<ruleset name="jcodersruleset">

   <description>
      This ruleset checks the jCoderZ Java code for bad stuff
   </description>'

files=$(find $PMD_RULSET -type f -name "*.xml")
for i in $files
do
   xsltproc --param "type" "'"$(basename $i)"'" \
      tools/xml/stylesheets/pmd-rules.xsl \
      $i 
done
echo '</ruleset>'   

#!/bin/bash
#
# $Id$
#
# This script lists all tags made by CC, e.g. BUILD_224, on the
# current cvs module.
#
# Author: michael.griffel@gmail.com
#
#                                    ``Never trust a computer
#                                      you can't throw out a window.''
#                                          -- Steve Wozniaks
#

#  number of CVS tag that are not removed.
KEEP=30

prop()
{
   case $prop_theme in
      smooth)     chars='->)|(<-+'     ;;
      bubble)     chars='.o0O8O0o'     ;;
      *)          chars='|/-\\'         ;;
   esac

   n=${#chars}
   pos=0;
   while read i; do
      trap 'break' INT  # terminate with CTRL-C
      echo -n ${chars:$pos:1}; tput cub1
      pos=$((($pos + 1) % $n))
   done
}

Help()
{
   echo ""
   echo "Controls CVS tags from cruise control"
   echo ""
   echo "Usage:"
   echo "  --help                    ...this message."
   echo "  -l|--list                 ...list all cruise control CVS tags."
   echo "  -r|--remove               ...remove all old cruise control CVS tags"
   echo "                               (keep the $KEEP newest tags)."
   echo ""
   exit 1
}

GetCvsTags()
{
   TAGS=$(cvs -Q -z9 log | \
      awk '
         BEGIN { pattern="BUILD" }
         /^keyword substitution: / { found = 0 }
                    { if (found && match($0, pattern)>0) # label pattern found
                        {
                            sub("[ \t][ \t]*", ""); # remove blanks and tabs
                            sub(":.*","");          # remove version, e.g. 1.95
                            print
                         }
                    }
         /^symbolic names:/        { found = 1 }
         ' | \
      sort -u -t _ -k 2,2n)
}

ListCvsTags()
{
   GetCvsTags
   for i in $TAGS; do
      echo "$i"
   done
}

RemoveCvsTags()
{
   GetCvsTags
   count=$(echo $TAGS | wc -w)
   border=$((count - KEEP))
   i=0;
   echo "trying to remove $border tags, keeping $KEEP"
   for t in $TAGS; do
      trap 'break' INT
      i=$((i + 1))
      if [ "$i" -gt "$border" ]; then
         break
      fi
      echo -n "removing tag $t ... ($i/$count) "
      cvs rtag -d "$t" fawkez-dev 2>&1 | prop
      echo "[REMOVED]"
   done
}

case "$*" in
   --list|-l)  ListCvsTags ;;
   --remove|-r)  RemoveCvsTags ;;
   *)          Help        ;;
esac

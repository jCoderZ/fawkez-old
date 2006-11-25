#!/bin/bash

#  nice propeller
prop()
{
   if [ "$prop_theme" != "off" ]; then
      case $prop_theme in
         smooth)     chars='->)|(<-+'     ;;
         bubble)     chars='.o0O8O0o'     ;;
         *)          chars='|/-\\'         ;;
      esac

      n=${#chars}
      pos=0;
      while read i; do
         echo -n ${chars:$pos:1}; tput cub1
         pos=$((($pos + 1) % $n))
      done
   else
      typeset last_log_message_count=0
      while read i; do
         # omit lines w/ whitespaces only
         trim_size=$(expr "$i" : '[ ]*')
         size=$(expr "$i" : '.*');
         if [ "$trim_size" -ne "$size" ]; then
            if [ "${i}." = "$last_log_message." ]; then
               last_log_message_count=$((last_log_message_count + 1))
            else
               if [ "$last_log_message_count" -gt 0 ]; then
                  echo "last message repeated $last_log_message_count times"
               fi
               last_log_message=$i
               last_log_message_count=0
               echo "${i}"
            fi
         fi
      done
   fi
}

#
#  main
#
if [ $# -ne 2 ]; then
   echo "Missing required arguments."
   echo "Usage: ${0##*/} <cc-tag> <dev-tag>"
   echo "Example: ${0##*/} BUILD_1929 FAWKEZ_0_2_0"
   exit -1
fi

CC_BUILD="$1"
TAG="$2"
VERSION=$(echo $TAG | sed -e 's/FAWKEZ_//g' | tr '_' '.')

tmpfile=${0##*/}.$$
trap 'rm -f $tmpfile' 0

echo -n "Creating developer release tag ${TAG} on cruise control tag ${CC_BUILD} ... "
# FIXME: use subversion
#(cvs -t rtag -r ${CC_BUILD} ${TAG} fawkez-dev 2>&1 || touch $tmpfile) | prop
echo ""
if [ -f "$tmpfile" ]; then
   echo "Failed to create cvs tag".
   exit -1
fi

# FIXME: use subversion
#cvs up -r ${TAG} -p CHANGES > CHANGES.${TAG}

echo "Sending e-mail notification to  mailing list ..."
echo "Hi jCoderZ,

The FawkeZ/${TAG} release is available.
This version is based on cruise control build \"${CC_BUILD}\".
--
greetings

The jCoderZ.org Team
" | mail -s "[ANN] FawkeZ/${TAG} release" \
   -a CHANGES.${TAG} announce@jcoderz.org

rm -f CHANGES.${TAG}
echo "developer release ${TAG} finished successfully."

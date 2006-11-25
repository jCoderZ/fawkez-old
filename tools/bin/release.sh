#!/bin/bash

if [ $# -ne 2 ]; then
   echo "Missing required arguments."
   echo "Usage: ${0##*/} <cc-tag> <tag>"
   echo "Example: ${0##*/} BUILD_1929 FAWKEZ_0_2_0"
   exit -1
fi

CC_BUILD="$1"
TAG="$2"
VERSION=$(echo $TAG | sed -e 's/FAWKEZ_//g' | tr '_' '.')

echo -n "Creating release tag ${TAG} based on cruise control tag ${CC_BUILD} ... "
svn copy https://www.jcoderz.org/svn/fawkez/cctags/${CC_BUILD} \
         https://www.jcoderz.org/svn/fawkez/tags/${TAG} \
      -m "Tagging the ${TAG} release of the fawkeZ project (based on cruise control tag ${CC_BUILD})."
      
echo "Sending e-mail notification to mailing list ..."
echo "Hi jCoderZ,

The fawkeZ/${VERSION} release is available!
This version is based on cruise control build \"${CC_BUILD}\".

For more details take a look into the 
https://www.jcoderz.org/fawkez/browser/tags/${TAG}/CHANGES file.
--
greetings

The jCoderZ.org Team
" | mail -s "fawkeZ/${VERSION} release available" \
   michael.griffel@gmail.com

rm -f /tmp/CHANGES.${TAG}
echo "Release ${TAG} finished successfully."

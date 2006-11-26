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

# copy release tarball to download area
ARTIFACTS_DIR="/var/www/public/cc/fawkez"
DOWNLOAD="/var/www/public/download"
TIMESTAMP=$(find ${ARTIFACTS_DIR}/buildresults/fawkez -name "index.html?log=*L${CC_BUILD}.*" \
	| sed -e 's/.*log//' | sed -e "s/L${CC_BUILD}.html//"
)
ls ${ARTIFACTS_DIR}/artifacts/fawkez/${TIMESTAMP}/*tar.gz || exit 1
TARBALL=$(find ${ARTIFACTS_DIR}/artifacts/fawkez/${TIMESTAMP}/ -name "*tar.gz")
echo "Copying tarball $TARBALL to download area ..."
test -d "${DOWNLOAD}/${VERSION}" || mkdir "${DOWNLOAD}/${VERSION}"
cp "${TARBALL}" "${DOWNLOAD}/${VERSION}/fawkez-${VERSION}.tar.gz"
chown www-data:www-data "${DOWNLOAD}/${VERSION}/fawkez-${VERSION}.tar.gz"
chmod 444 "${DOWNLOAD}/${VERSION}/fawkez-${VERSION}.tar.gz"
      
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
   -a "from: jCoderZ@googlemail.com" jcoderz-announce@jcoderz.org

rm -f /tmp/CHANGES.${TAG}
echo "Release ${TAG} finished successfully."

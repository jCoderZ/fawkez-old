#!/bin/bash
# TODO: port from cvs to subversion
#  nice propeller
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
      echo -n ${chars:$pos:1}; tput cub1
      pos=$((($pos + 1) % $n))
   done
}

#
#  main
#


if [ $# -ne 1 ]; then
   echo "Missing required arguments."
   echo "Usage: ${0##*/} <dev-tag>"
   echo "Example: ${0##*/} PHOENIX_0_6_0"
   exit -1
fi

TAG="$1"
TMPDIR=${TMPDIR:-"/tmp"}
YDRIVE=${YDRIVE:-"/ydrive"}
CVSROOT=":pserver:$USER@st-isidore:/cvs/root"
VERSION=$(echo $TAG | sed -e 's/PHOENIX_//g' | tr '_' '.')
export CVSROOT

#  sanity checks
test -d $YDRIVE || { echo "Cannot find y-drive: $YDRIVE"; exit -1; }
test -d $TMPDIR || { echo "Cannot find tmp dir: $TMPDIR"; exit -1; }

tmpfile=${0##*/}.$$
trap 'rm -f $tmpfile' 0

echo -n "Creating developer release tag ${TAG} ($VERSION) on HEAD... "
(cvs -t rtag -F ${TAG} phoenix 2>&1 || touch $tmpfile) | prop
echo ""
if [ -f "$tmpfile" ]; then
   echo "Failed to create cvs tag".
   exit -1
fi

pushd $TMPDIR
rm -rf ./phoenix
cvs -q co -r ${TAG} phoenix
pushd phoenix
JAVA_HOME=/tools/jdk/1.4.2
. ./cleanenv debug
ant dist
DIST_DIR="$YDRIVE/4QA/phoenix/$VERSION"
mkdir -p "$DIST_DIR"
test -d dist || exit -1
ls -al dist
cp -r dist/* "$DIST_DIR"
popd
rm -rf phoenix
popd
echo "Sending e-mail announcment ..."
echo " .---------------------------< ANNOUNCEMENT >--------------------------*
:|:
:|: A new PhoeniX/${VERSION} developer release is available on
:|: Y:\4QA\phoenix\\${VERSION} .
:|:
:|: Please copy the full distribution to this location:
:|: Q:\prod\phoenix\\${VERSION} .
:|:
:|: Thank you for your contribution to the phoenix project.
:|:
:|:                                         The phoenix developers.
:|:
 \`--------------------------< ANNOUNCEMENT >--------------------------*
" | mail -s "[phoenix] ${TAG} release" -c Andreas.Mandel@gmail.com \
   -c Michael.Griffel@gmail.com

echo "phoenix/$VERSION available on '$DIST_DIR'."

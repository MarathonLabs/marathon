#!/usr/bin/env sh

CMD=`command -v llvm-nm`

if [ $# -eq 0 ]; then
	echo "Usage: ${0##*/} FILEPATH [FILEPATH...]" >&2
	echo "" >&2
	exit 1
fi

for FILE in "$@"; do
	if [ -f "$FILE" ]; then
		"$CMD" -gU "$FILE" | /usr/bin/cut -d' ' -f3 | /usr/bin/xargs /usr/bin/swift-demangle | /bin/sed -E -e 's/^.* ---> ([^.]+\.[^.]+\.test[^.(]+\(\)).*$/\1/;t;d'
	fi
done

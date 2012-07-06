#!/bin/sh

usage() {
  echo "Usage: " >&2
  echo "$0 --train [chain|tree] model_path encoding train_corpus_path test_corpus_path" >&2
  echo "$0 --test model_path encoding test_corpus_path" >&2
  echo " chain: for linear chain CRFs model." >&2
  echo " tree: for tree CRFs model." >&2
  echo >&2
  echo "Example: $0 --train chain model.ser.gz utf-8 corpus/train.data corpus/test.data" >&2
  exit
}

if [ $# -lt 4 -o $# -gt 6 ]; then
	usage
fi

BASEDIR=`dirname $0`
JAVACMD="java -cp $BASEDIR/bin lang.TreeCRFTui "

if [ $1 = "--train" ]; then
	$JAVACMD $1 $2 $3 $4 $5 $6
elif [ $1 = "--test" ]; then
	$JAVACMD $1 $2 $3 $4
fi

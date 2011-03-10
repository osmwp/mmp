#!/bin/sh
#set -x

#Constants
LOGS_DATE_PATTERN="%Y-%m-%d"
DATE_PREFIX_PATTERN="%Y-%m"
SCRIPTS_PATTERN="stats_*"

#Check command
if [ $# -ne 4 ] ; then
	echo "Usage : $0 STATISTICS_SCRIPTS_FOLDER STATISTICS_LOGS_FOLDERS STATISTICS_CSV_FOLDER STATISTICS_DATE"
	exit 1
fi

#Initialize and check parameters
SCRIPTS_FOLDER=$1
if [ ! -d $SCRIPTS_FOLDER ] ; then
	echo "Unable to find STATISTICS_SCRIPTS_FOLDER : '$SCRIPTS_FOLDER'"
	exit 1
fi

for LOG_FOLDER in `echo $2 | tr ":" " "` ; do
	if [ ! -d $LOGS_FOLDER ] ; then
		echo "Unable to find STATISTICS_LOGS_FOLDER : '$LOGS_FOLDER'"
		exit 1
	fi
	LOGS_FOLDERS="$LOGS_FOLDERS $LOG_FOLDER"
done

CSV_FOLDER=$3
if [ ! -d $CSV_FOLDER ] ; then
	mkdir -p $CSV_FOLDER 2>/dev/null
fi
if [ ! -d $CSV_FOLDER ] ; then
	echo "Unable to find CSV_FOLDER : '$CSV_FOLDER'"
	exit 1
fi

STATS_DATE=$4

#Set DATE_PREFIX
CURRENT_DAY=`date -d "$STATS_DATE yesterday" "+%d"`
CURRENT_MONTH=`date -d "$STATS_DATE yesterday" "+%m"`
CURRENT_YEAR=`date -d "$STATS_DATE yesterday" "+%y"`
DATE_PREFIX=`date -d "$STATS_DATE yesterday" "+$DATE_PREFIX_PATTERN"`

#List DATES to aggregate
END_DAY=1
DATE_LIST=""
while [ $CURRENT_DAY -ge $END_DAY ] ; do
	DATE_LIST=$DATE_LIST" "`date -d "$CURRENT_MONTH/$CURRENT_DAY/$CURRENT_YEAR" "+$LOGS_DATE_PATTERN"`
	CURRENT_DAY=`expr $CURRENT_DAY - 1`
done

#Aggregation loop
SCRIPT_LIST=`ls $SCRIPTS_FOLDER`
for CURRENT_SCRIPT in `echo $SCRIPT_LIST` ; do
	$SCRIPTS_FOLDER/$CURRENT_SCRIPT "$LOGS_FOLDERS" "$CSV_FOLDER" "$DATE_PREFIX" "$DATE_LIST"
done	



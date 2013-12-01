tvtorrentrss
============

[![Still maintained](http://stillmaintained.com/lmivan/tvtorrentrss.png)](http://stillmaintained.com/lmivan/tvtorrentrss)

This project is a fork of the original [TV Torrent RSS Downloader](https://sourceforge.net/projects/tvtorrentrss/) project, to clean up the code a little bit and remove all features that I don't need and to add a new feature: *download all files from a rss source*.

To download my favourite TV shows I use [show RSS](http://showrss.karmorra.info) which provides a personalized rss feed with all my shows. As soon as a new show is released the feed is updated with that show. With the original version of TV Torrent RSS (which is from july 2010) everytime I add a new show to my show RSS page, I have to add it to the config file of the program. Also, If a episode is released more than one time (e.g. there is a REPACK version) the episode is not downloaded again. With my new feature it is not necessary to edit the configuration file because all the files in the rss are downloaded.

Usage
-----
Just download [this file](https://github.com/lmivan/tvtorrentrss/raw/releases/output/tvrss-20130102.zip) unzip it and configure the `tvrss.properties` file.
Add a line with a rss feed (if you have more than one rss source) and also add a location where the attachment files (usually .torrent files) will be downloaded.

```
#
# Feeds
########
feed1=http://showrss.karmorra.info/rss.php?user_id=18144&hd=null&proper=null
feed2=http://showrss.karmorra.info/rss.php?user_id=18145&hd=null&proper=null

#
# Output locations
##################
location1=/home/ivan/TEMP/AAA
location2=/home/ivan/TEMP/AAA/BBB
```

Now just execute the `tvrss.sh` script:
```
./run.sh
```

or run
```
java -jar tvrss.jar
```

And the attached files will be downloaded to the location folders configured. Now add it to your favorite torrent client and have fun :-)
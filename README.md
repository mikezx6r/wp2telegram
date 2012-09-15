wp2telegram
===========

This project will read a Wordpress export xml file and create appropriate pages and posts in Telegram markdown format. It will also create the basic index, archive and extra_info pages for Telegram.

At the moment, it doesn't create the category hierarchy anywhere, and will not do anything with comments.

If you have issues with the converter, please let me know, and I'll do my best to address it.

The code is written in Scala, and uses Gradle for building. 

To run the converter, [download[(https://github.com/mikezx6r/wp2telegram/downloads) the wp2telegram-1.0.0.zip file, unzip, and execute the appropriate file in the bin directory. 

The scripts assume you already have java installed and have JAVA_HOME defined.
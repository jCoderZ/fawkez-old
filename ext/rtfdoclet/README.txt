                                RTFDoclet README
                                ----------------

General information
-------------------
This jar file contain a javadoc extension (to be used by JAVA 2 or above).
 This javadoc extension, better known as doclet, will produce compact RTF
 documentation.

Goal
----
My main goal of this doclet was to produce documentation to be inserted
 as annexe for a conception document writen with the Winword wordprocessor.
 It is in no way issue to produce winhelp documentation or stuff like
 that. Moreover the RTF document is maybe not suitable for some
 wordprocessor, I didn't test it with other wordprocessor than Winword (97).
It should normaly fit but I can't warranty.

I provide the library for anybody that needed it: the copyright included
 with this library is open: I didn't want to restrict the usage of this
 library, if it could be usefull. So you could use it for commercial purpose,
 you could change it, and do all you want, but don't ask me any support, or
 warranty.

Files provided
--------------
Normaly you must find the library in a jar file. This jar file contain the
 classes needed ; you should also find the source in the sources directory.
 If you want to compile it, you must have JavaCC (I used version 0.8pre2)
 and JAVA 2 (or above). Moreover there should be the copyright file ; and an
 an exemple, named 'example.rtf' produced with my doclet.

Usage
-----
To use the doclet you should put the library into you CLASSPATH environment
 variable, or specified it to javadoc via the -classpath argument. You must
 specified that you want to use this doclet and not the default html format.
 You could specified the output file via the -filename argument. Example:

javadoc -classpath c:\jdk1.2.2\lib\tools.jar \
 -docletpath c:\Java\Doclets\RTF\RTFDoclet.jar -doclet RTFDoclet \
 -filename documentation.rtf toto.java


Option:
- if you add the '-collapseParameters' argument, parameters for a method
 will be writen on a single line (the output is more compact, and you
 will save trees when printing it! :-) ).

Note:
- this doclet is able to parse some basic html tag, like <p>, <BR>,
 <a href=""></a>, and <ul><li></ul> allowing you to put basic html to format
 your text. I just put the tag I was using, if you want the doclet to support
 more html tags, you could go into the JavaCC grammar and send me your changes
 8-).
- the -classpath ...\tools.jar is recommended by java web site but not necessary.


Feedback
--------
If you manage to find and correct bugs in my doclet, you are welcome to send
 to me changes you have done, to let me patch  this plug-in (but could it be
 buggy? ;-) )

Thanks
------
To David Cozens (David@cozens.freeserve.co.uk) for some usefull enhancement
 for the output (see Changes.txt)
To Joe Panko (jp99@sprintmail.com) to bringing some output features (see Changes.txt)
To Thierry Bodhuin (Thierry.Bodhuin@support-externe.space.alcatel.fr) for bug fixing
 in the parameters handling
To Thomas Zochler (Thomas.Zochler@partner.bmw.de) for correcting a UL tag
 related bug.

Nicolas Zin
nzin@yahoo.com

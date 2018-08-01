This project creates two jars:

#webscraper
a set of utilities for scraping HTML pages, managing cookies, etc.

#teetime
a library which provides an interface for making tee times in the PWCC tee time system.  it interacts with the back end web site, automating the process of
booking a tee time.

teetime depends on the webscraper library to handle the underlying HTTP/web work

#build
To build this project, execute:

`./build.sh`

This will use maven to package the two libraries as jar files that can then
be leveraged in other projects.

One other project that leverages these libraries is teetimepwcc.  This runs in a 
Liberty server and exposes a REST interface for searching and booking tee times.
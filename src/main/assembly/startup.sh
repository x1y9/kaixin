#!/bin/sh
cd `dirname "$0"`

# init db using: startup.sh db migrate server.yml
java -cp './classes:./lib/*' com.kaixin.app.MainApplication $*

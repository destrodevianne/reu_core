#!/bin/sh
java -Djava.util.logging.config.file=console.cfg -Xbootclasspath/p:./../libs/l2ft.jar -cp config/xml:./../libs/*:l2jlogin.jar l2r.tools.gsregistering.GameServerRegister -c
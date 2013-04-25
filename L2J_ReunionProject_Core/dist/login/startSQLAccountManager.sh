#!/bin/sh
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*:l2jlogin.jar l2r.tools.accountmanager.SQLAccountManager

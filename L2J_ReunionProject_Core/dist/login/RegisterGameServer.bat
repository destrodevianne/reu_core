@echo off
title L2J - Register Game Server
color 17
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;l2jlogin.jar l2r.tools.gsregistering.BaseGameServerRegister -c
pause
@echo off
title Login Server Console
color 0B
:start
echo Starting L2J Login Server.
echo.

java -Djava.util.logging.manager=l2r.util.L2LogManager -Xmx256m -Xbootclasspath/p:./../libs/l2ft.jar -cp config/xml;./../libs/*;l2jlogin.jar l2r.loginserver.L2LoginServer

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end

:restart
echo.
echo Admin Restarted Login Server.
echo.
goto start

:error
echo.
echo Login Server terminated abnormally!
echo.

:end
echo.
echo Login Server Terminated.
echo.
pause
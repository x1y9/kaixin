@setlocal enableDelayedExpansion
@set MAIN_CLASS=com.kaixin.app.MainApplication
@set DEPLOY_DIR=target\deploy

@java -version >nul 2>&1
@IF %ERRORLEVEL% NEQ 0 echo you should install java7 & goto error_end
@call mvn -version >nul 2>&1
@IF %ERRORLEVEL% NEQ 0 echo you should install maven & goto error_end
@call yarn -version >nul 2>&1
@IF %ERRORLEVEL% NEQ 0 echo you should install yarn & goto error_end
@call quasar --version >nul 2>&1
@IF %ERRORLEVEL% NEQ 0 echo you should yarn global add quasar-cli & goto error_end

:do_para
@if  "%1"=="help" goto do_help
@if  "%1"=="clean" goto do_clean
@if  "%1"=="run" goto do_run
@if  "%1"=="dev" goto do_dev
@if  "%1"=="client" goto do_client
@if  "%1"=="test" goto do_test
@if  "%1"=="eclipse" goto do_eclipse
@if  "%1"=="deploy" goto do_deploy
@if  "%1"=="package" goto do_package
@if  "%1"=="migrate" goto do_migrate
@if  "%1"=="resetdb" goto do_resetdb
@if  "%1"=="dump" goto do_dump
@if  "%1"=="restore" goto do_restore

:do_help
@echo.
@echo build script for kaixin framework
@echo usage: build [command] [para] 
@echo command can be:
@echo   clean              : clean the project
@echo   run                : run app server
@echo   dev                : run client in dev mode
@echo   client             : build client to app server
@echo   test               : run test
@echo   eclipse            : generate eclipse project
@echo   deploy             : deploy the app in dir %DEPLOY_DIR%
@echo   package            : package the app to single jar
@echo   resetdb            : delete and re-create database
@echo   migrate            : migrate the database
@echo   dump               : dump h2 database to backup.sql
@echo   restore            : restore h2 database from backup.sql
@echo.
@goto end

:do_clean
call mvn clean
cd client
call quasar clean
goto end

:do_dev
cd client
call yarn
call quasar dev
goto end

:do_client
cd client
call yarn
call quasar build
goto end

:do_run
@REM NUL for ctrl-c ignore in batch
call mvn compile exec:java -Dexec.mainClass="%MAIN_CLASS%" -Dexec.args="%2 %3 %4 %5 %6"< NUL
goto end

:do_test
call mvn test -Dtest=%2 < NUL
goto end

:do_eclipse
call mvn eclipse:eclipse
goto end

:do_deploy
cd client
@if NOT EXIST node_modules call yarn
call quasar clean
call quasar build
cd ..
call mvn clean
call mvn package
mkdir %DEPLOY_DIR%
move target\classes %DEPLOY_DIR%
move target\lib %DEPLOY_DIR%
xcopy src\main\assembly %DEPLOY_DIR% /E /I
goto end

:do_package
cd client
@if NOT EXIST node_modules call yarn
call quasar clean
call quasar build
cd ..
call mvn clean install
goto end

:do_migrate
call mvn compile exec:java -Dexec.mainClass="%MAIN_CLASS%"  -Dexec.args="db migrate server.yml"
goto end

:do_resetdb
del *.db
call mvn compile exec:java -Dexec.mainClass="%MAIN_CLASS%"  -Dexec.args="db migrate server.yml"
goto end

:do_dump
@REM backup to backup.sql
call mvn compile exec:java -Dexec.mainClass="org.h2.tools.Script"  -Dexec.args="-url jdbc:h2:./kaixin -user sa"
@echo please edit the sql to remove schema statement...
goto end

:do_restore
@REM restore from backup.sql after migrate
call mvn compile exec:java -Dexec.mainClass="org.h2.tools.RunScript"  -Dexec.args="-url jdbc:h2:./kaixin -user sa"
goto end

:error_end
@ver /ERROR >NUL 2>&1

:end


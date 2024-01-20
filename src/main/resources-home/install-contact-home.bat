SET  SOURCE_PATH=E:\src\github\contact
SET  APACHE_TOMCAT=tomcat10
SET  CURRENT_PATH=%~dp0
SET  CURRENT_FILENAME=%~nx0
SET  LC_ALL=ko_KR.UTF-8

ECHO %CURRENT_PATH% %CURRENT_FILENAME%

DATE /t
TIME /t

PUSHD %CURRENT_PATH%

CD  %SOURCE_PATH%
git stash
git clean -f
git	pull https://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@github.com/andold/contact.git
git  log --pretty=format:"%%h - %%an, %%ai:%%ar : %%s" -8

ECHO "clean"
CALL gradle clean -Pprofile=home -x test

ECHO "react npm install"
CD %SOURCE_PATH%\src\main\frontend
CALL npm install
CALL npm audit fix

ECHO "build"
CD %SOURCE_PATH%
git clean -f
CALL gradle build -Pprofile=home -x test

NET  stop %APACHE_TOMCAT%

@ECHO pause 4 seconds
TIMEOUT 4

POPD
CD  doc_base
@ECHO delete files
DEL  /F /S /Q * > nul
@ECHO deploy new files
jar  -xf %SOURCE_PATH%\build\libs\contact-0.0.1-SNAPSHOT.war

CD  ..
@ECHO copy this file from new
ECHO %CURRENT_FILENAME%
COPY /Y %SOURCE_PATH%\src\main\resources-home\%CURRENT_FILENAME%


NET  start %APACHE_TOMCAT%

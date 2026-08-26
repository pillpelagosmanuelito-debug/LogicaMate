@rem
@rem Gradle startup script for Windows.
@rem NOTE: gradle/wrapper/gradle-wrapper.jar is not bundled in this source
@rem drop (no network access at generation time). See docs/BUILD_REPORT.md.
@rem Run "gradle wrapper --gradle-version 8.7" once with a local Gradle
@rem install to regenerate it, or use "gradle" directly.
@rem

@if "%DEBUG%"=="" @echo off
setlocal

set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if not exist "%CLASSPATH%" (
  echo ERROR: %CLASSPATH% not found.
  echo Install Gradle 8.7 and run: gradle wrapper --gradle-version 8.7
  exit /b 1
)

if defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXE=java.exe
)

"%JAVA_EXE%" -Xmx64m -Xms64m -Dorg.gradle.appname=Gradle -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

endlocal

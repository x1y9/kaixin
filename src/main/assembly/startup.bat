@setlocal enableDelayedExpansion
@cd /D "%~dp0"

@echo init db using: startup db migrate server.yml
java -cp "./classes;./lib/*" com.kaixin.app.MainApplication %*

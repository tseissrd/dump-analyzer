set wd=%~dp0
cd src\main\webpack\app
call npx webpack --mode development
cd %wd%
call mvn clean -Dmaven.compiler.showDeprecation -Dmaven.compiler.showWarnings spring-boot:run
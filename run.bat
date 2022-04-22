set wd=%~dp0
cd src\main\webpack\app
call npx webpack --mode development
cd %wd%
call mvn clean spring-boot:run
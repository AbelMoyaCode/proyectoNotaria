@echo off
echo ============================================
echo RESET PASSWORD POSTGRESQL
echo ============================================
echo.
echo Este script cambiara la contraseña del usuario postgres a: abel444
echo.
pause

REM Cambiar autenticación temporalmente
echo Modificando configuracion de autenticacion...
powershell -Command "(Get-Content 'C:\Program Files\PostgreSQL\18\data\pg_hba.conf') -replace 'host    all             all             127.0.0.1/32            scram-sha-256', 'host    all             all             127.0.0.1/32            trust' | Set-Content 'C:\Program Files\PostgreSQL\18\data\pg_hba.conf.temp'"
move /Y "C:\Program Files\PostgreSQL\18\data\pg_hba.conf.temp" "C:\Program Files\PostgreSQL\18\data\pg_hba.conf"

REM Reiniciar PostgreSQL
echo Reiniciando servicio PostgreSQL...
net stop postgresql-x64-18
timeout /t 2
net start postgresql-x64-18
timeout /t 3

REM Cambiar contraseña
echo Cambiando contraseña...
"C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -c "ALTER USER postgres WITH PASSWORD 'abel444';"

REM Restaurar autenticación
echo Restaurando configuracion original...
powershell -Command "(Get-Content 'C:\Program Files\PostgreSQL\18\data\pg_hba.conf') -replace 'host    all             all             127.0.0.1/32            trust', 'host    all             all             127.0.0.1/32            scram-sha-256' | Set-Content 'C:\Program Files\PostgreSQL\18\data\pg_hba.conf.temp'"
move /Y "C:\Program Files\PostgreSQL\18\data\pg_hba.conf.temp" "C:\Program Files\PostgreSQL\18\data\pg_hba.conf"

REM Reiniciar PostgreSQL nuevamente
echo Reiniciando servicio PostgreSQL nuevamente...
net stop postgresql-x64-18
timeout /t 2
net start postgresql-x64-18

echo.
echo ============================================
echo CONTRASEÑA CAMBIADA EXITOSAMENTE A: abel444
echo ============================================
pause


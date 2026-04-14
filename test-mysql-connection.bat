@echo off
echo 正在测试 MySQL 连接...
echo 请输入 MySQL root 密码：
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "SELECT 'Connection successful!' AS Status;"
pause


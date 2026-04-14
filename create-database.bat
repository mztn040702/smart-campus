@echo off
echo 正在创建数据库 campus_system...
echo 请输入 MySQL root 密码：
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "CREATE DATABASE IF NOT EXISTS campus_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if %errorlevel% == 0 (
    echo 数据库创建成功！
) else (
    echo 数据库创建失败，请检查密码是否正确。
)
pause


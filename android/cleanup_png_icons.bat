@echo off
echo Cleaning up duplicate PNG icon files...

del /F /Q "app\src\main\res\mipmap-hdpi\ic_launcher.png" 2>nul
del /F /Q "app\src\main\res\mipmap-mdpi\ic_launcher.png" 2>nul
del /F /Q "app\src\main\res\mipmap-xhdpi\ic_launcher.png" 2>nul
del /F /Q "app\src\main\res\mipmap-xxhdpi\ic_launcher.png" 2>nul
del /F /Q "app\src\main\res\mipmap-xxxhdpi\ic_launcher.png" 2>nul

echo PNG files deleted. Only XML icon files remain.
echo.
pause

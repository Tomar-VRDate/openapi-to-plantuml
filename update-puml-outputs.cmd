xcopy ".\target\outputs\*.*" ".\src\test\resources\outputs\"  /K /D /H /Y
xcopy ".\target\demo-outputs\demo\*.*" ".\src\test\resources\demo-outputs\"  /K /D /H /Y
xcopy ".\target\demo-outputs\*.puml*" ".\src\test\resources\outputs\"  /K /D /H /Y
@echo off
"C:\\Users\\Administrator\\AppData\\Local\\Android\\SDK\\cmake\\3.22.1\\bin\\cmake.exe" ^
  "-HE:\\programing\\Android\\2023\\KModbus\\app" ^
  "-DCMAKE_SYSTEM_NAME=Android" ^
  "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON" ^
  "-DCMAKE_SYSTEM_VERSION=24" ^
  "-DANDROID_PLATFORM=android-24" ^
  "-DANDROID_ABI=armeabi-v7a" ^
  "-DCMAKE_ANDROID_ARCH_ABI=armeabi-v7a" ^
  "-DANDROID_NDK=C:\\Users\\Administrator\\AppData\\Local\\Android\\SDK\\ndk\\25.1.8937393" ^
  "-DCMAKE_ANDROID_NDK=C:\\Users\\Administrator\\AppData\\Local\\Android\\SDK\\ndk\\25.1.8937393" ^
  "-DCMAKE_TOOLCHAIN_FILE=C:\\Users\\Administrator\\AppData\\Local\\Android\\SDK\\ndk\\25.1.8937393\\build\\cmake\\android.toolchain.cmake" ^
  "-DCMAKE_MAKE_PROGRAM=C:\\Users\\Administrator\\AppData\\Local\\Android\\SDK\\cmake\\3.22.1\\bin\\ninja.exe" ^
  "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=E:\\programing\\Android\\2023\\KModbus\\app\\build\\intermediates\\cxx\\Debug\\5m5k42v4\\obj\\armeabi-v7a" ^
  "-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=E:\\programing\\Android\\2023\\KModbus\\app\\build\\intermediates\\cxx\\Debug\\5m5k42v4\\obj\\armeabi-v7a" ^
  "-DCMAKE_BUILD_TYPE=Debug" ^
  "-BE:\\programing\\Android\\2023\\KModbus\\app\\.cxx\\Debug\\5m5k42v4\\armeabi-v7a" ^
  -GNinja

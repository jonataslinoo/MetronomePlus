# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("metronomeplus-lib")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.
include_directories(src/main/cpp/)

add_library( metronomeplus-lib
        SHARED
        cpp/metronomeplus-lib.cpp
        cpp/Metronome.cpp
        cpp/Metronome.h

        #model
        cpp/model/Beat.h

        #utils
        cpp/utils/Constants.h
        cpp/utils/Logging.h

        # audio
        cpp/audio/AAssetDataSource.cpp
        cpp/audio/AAssetDataSource.h
        cpp/audio/DataSource.h
        cpp/audio/IRenderableAudio.h
        cpp/audio/Mixer.h
        cpp/audio/NDKExtractor.cpp
        cpp/audio/NDKExtractor.h
        cpp/audio/Player.cpp
        cpp/audio/Player.h
)

# Find the Oboe package
find_package (oboe REQUIRED CONFIG)

# Configure target libraries
set(TARGET_LIBS log android oboe::oboe GLESv2)

# Debug message
MESSAGE(STATUS "Using NDK media extractor")

# Add source file for NDKExtractor
target_sources(metronomeplus-lib PRIVATE cpp/audio/NDKExtractor.cpp)

# Add mediandk to the target libraries
set(TARGET_LIBS ${TARGET_LIBS} mediandk)

# Link the libraries to the target
target_link_libraries(metronomeplus-lib ${TARGET_LIBS})

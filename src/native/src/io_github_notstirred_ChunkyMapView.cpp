#include <vector>
#include <iostream>
#include "io_github_notstirred_ChunkyMapView.h"

#include "mapView.h"

JNIEXPORT void JNICALL Java_io_github_notstirred_ChunkyMapView_setup(JNIEnv* env, jobject thisObject) {
    setup();
}

jfieldID xId = nullptr;
jfieldID zId = nullptr;
jfieldID levelId = nullptr;

JNIEXPORT jboolean JNICALL Java_io_github_notstirred_ChunkyMapView_render(JNIEnv* env, jobject thisObject, jfloat x1, jfloat z1, jfloat x2, jfloat z2, jobjectArray positionsArray) {
    jsize planesSize = env->GetArrayLength(positionsArray);

    auto positions = std::vector<TilePos>();

    if(xId == nullptr && planesSize > 0) {
        jobject plane = env->GetObjectArrayElement(positionsArray, 0);
        jclass clazz = env->GetObjectClass(plane);
        xId = env->GetFieldID(clazz, "x", "I");
        zId = env->GetFieldID(clazz, "z", "I");
        levelId = env->GetFieldID(clazz, "level", "I");
    }
    if(xId != nullptr) {
        for (jsize i = 0; i < planesSize; ++i) {
            jobject jplane = env->GetObjectArrayElement(positionsArray, i);

            auto tilePos = TilePos(
                    env->GetIntField(jplane, xId),
                    env->GetIntField(jplane, zId),
                    env->GetIntField(jplane, levelId)
            );

            positions.emplace_back(tilePos);
        }
    }

    return render((float)x1, (float)z1, (float)x2, (float)z2, positions);
}

JNIEXPORT jfloat JNICALL Java_io_github_notstirred_ChunkyMapView_getViewX(JNIEnv* env, jobject thisObject) {
    return viewPosX();
}

JNIEXPORT jfloat JNICALL Java_io_github_notstirred_ChunkyMapView_getViewZ(JNIEnv* env, jobject thisObject) {
    return viewPosZ();
}

JNIEXPORT jfloat JNICALL Java_io_github_notstirred_ChunkyMapView_getViewSizeX(JNIEnv* env, jobject thisObject) {
    return viewSizeX();
}

JNIEXPORT jfloat JNICALL Java_io_github_notstirred_ChunkyMapView_getViewSizeZ(JNIEnv* env, jobject thisObject) {
    return viewSizeZ();
}
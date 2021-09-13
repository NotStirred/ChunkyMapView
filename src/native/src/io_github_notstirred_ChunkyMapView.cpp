#include <vector>
#include <iostream>
#include "io_github_notstirred_ChunkyMapView.h"

#include "mapView.h"

JNIEXPORT void JNICALL Java_io_github_notstirred_ChunkyMapView_setup(JNIEnv* env, jobject thisObject) {
    setup();
}

jfieldID xId = nullptr;
jfieldID zId = nullptr;
jfieldID scaleId = nullptr;

JNIEXPORT jboolean JNICALL Java_io_github_notstirred_ChunkyMapView_render(JNIEnv* env, jobject thisObject, jfloat x1, jfloat z1, jfloat x2, jfloat z2, jobjectArray planeArray) {
    jsize planesSize = env->GetArrayLength(planeArray);

    auto planes = std::vector<Plane>();

    if(xId == nullptr && planesSize > 0) {
        jobject plane = env->GetObjectArrayElement(planeArray, 0);
        jclass clazz = env->GetObjectClass(plane);
        xId = env->GetFieldID(clazz, "x", "I");
        zId = env->GetFieldID(clazz, "z", "I");
        scaleId = env->GetFieldID(clazz, "scale", "I");
    }
    if(xId != nullptr) {
        for (jsize i = 0; i < planesSize; ++i) {
            jobject jplane = env->GetObjectArrayElement(planeArray, i);

            auto plane = Plane(
                    env->GetIntField(jplane, xId),
                    env->GetIntField(jplane, zId),
                    env->GetIntField(jplane, scaleId)
            );

            planes.emplace_back(plane);
        }
    }

    return render((float)x1, (float)z1, (float)x2, (float)z2, planes);
}
#ifndef TEST_OPENGL_MAPVIEW_H
#define TEST_OPENGL_MAPVIEW_H

struct Plane {
    int x;
    int z;
    int scale;

    Plane(const int _x, const int _z, const int _scale) : x(_x), z(_z), scale(_scale) { }
    Plane() : x(0), z(0), scale(0) { }
};

void setup();

bool render(float x1, float z1, float x2, float z2, const std::vector<Plane>& planes);

#endif //TEST_OPENGL_MAPVIEW_H

#ifndef TEST_OPENGL_MAPVIEW_H
#define TEST_OPENGL_MAPVIEW_H

struct TilePos {
    int x;
    int z;
    int level;

    TilePos(const int _x, const int _z, const int _scale) : x(_x), z(_z), level(_scale) { }
    TilePos() : x(0), z(0), level(0) { }
};

void setup();

bool render(float x1, float z1, float x2, float z2, const std::vector<TilePos>& planes);

float viewPosX();
float viewPosZ();

float viewSizeX();
float viewSizeZ();


#endif //TEST_OPENGL_MAPVIEW_H

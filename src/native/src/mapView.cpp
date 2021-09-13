#include <GL/glew.h>
#include <GLFW/glfw3.h>
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>

#include <cstdio>
#include <cassert>
#include <ctime>
#include <cstdlib>
#include <cmath>

#include <log/logger.h>

#include <vector>
#include <iostream>
#include <deque>
#include <armadillo>

#include "mapView.h"

static GLfloat vertices[] = {
        0, 0, 0,
        1, 0, 0,
        0, 0, 1,
        1, 0, 1
};

static GLuint elements [] = {
        0, 2, 1, 3
};

const char* vertex_shader =
"#version 400\n"
"in vec3 vp;"
"uniform mat4 MVP;"
"void main(){"
    "gl_Position =  MVP * vec4(vp,1);"
"}";

const char* fragment_shader =
"#version 400\n"
"out vec4 frag_colour;"
"uniform vec4 in_colour;"
"void main() {"
"    frag_colour = in_colour;"
"}";

void glfw_error_callback(int error, const char* description) {
    log_asError((char*)"GLFW ERROR: code %i msg: %s\n", error, description);
}

// keep track of window size for things like the viewport and the mouse cursor
//int windowWidth = 1920;
//int windowHeight = 1020;
int windowWidth = 1280;
int windowHeight = 720;

void glfw_window_size_callback(GLFWwindow* window, int width, int height) {
    windowWidth = width;
    windowHeight = height;

    /* update any perspective matrices used here */
}

double previous_seconds;


void update_fps_counter(GLFWwindow* window) {
    static int frame_count;
    double current_seconds = glfwGetTime();
    double elapsed_seconds = current_seconds - previous_seconds;
    if (elapsed_seconds > 0.25) {
        previous_seconds = current_seconds;
        double fps = (double)frame_count / elapsed_seconds;
        char tmp[128];
        sprintf(tmp, "opengl @ fps: %.2f", fps);
        glfwSetWindowTitle(window, tmp);
        frame_count = 0;
    }
    frame_count++;
}

inline float magnitude(const glm::vec2& in) {
    return sqrt(in.x * in.x + in.y * in.y);
}

GLFWwindow* window;
GLuint vao;
GLuint ebo;
GLuint shader_program;

void setup() {
    std::srand(std::time(nullptr));

    assert(log_initialiseLogFile((char*)"debug.log"));
    log_asInfo((char*)"Starting GLFW %s\n", glfwGetVersionString());
    glfwSetErrorCallback(glfw_error_callback);
    if (!glfwInit()) {
        fprintf(stderr, "ERROR: Could not start GLFW3\n");
        log_asError((char*)"Could not start GLFW3\n");
        return;
    }

    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);

    window = glfwCreateWindow(windowWidth, windowHeight, "Chunky Map View Test", nullptr, nullptr);
    if (!window) {
        fprintf(stderr, "ERROR: could not open window with GLFW3\n");
        glfwTerminate();
        return;
    }
    glfwSetWindowSizeCallback(window, glfw_window_size_callback);

    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);

    // start GLEW extension handler
    glewExperimental = GL_TRUE;
    glewInit();

    // get version info
    const GLubyte* renderer = glGetString(GL_RENDERER); // get renderer string
    const GLubyte* version = glGetString(GL_VERSION); // version as a string
    log_asInfo((char*)"GPU: %s | Supports GL %s\n", renderer, version);

    glEnable(GL_CULL_FACE);
    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LESS);

    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_BLEND);

    GLuint vbo = 0;
    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, 4*3*sizeof(GLfloat), vertices, GL_STATIC_DRAW);

    ebo = 0;
    glGenBuffers(1, &ebo);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, 4*sizeof(GLuint), elements, GL_STATIC_DRAW);

    vao = 0;
    glGenVertexArrays(1, &vao);
    glBindVertexArray(vao);
    glEnableVertexAttribArray(0);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 0, nullptr);

    GLuint vs = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vs, 1, &vertex_shader, nullptr);
    glCompileShader(vs);
    GLuint fs = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fs, 1, &fragment_shader, nullptr);
    glCompileShader(fs);

    shader_program = glCreateProgram();
    glAttachShader(shader_program, fs);
    glAttachShader(shader_program, vs);
    glLinkProgram(shader_program);

    glfwSetCursorPos(window, windowWidth/2.0, windowHeight/2.0);
}

glm::vec3 camera_pos(0, 1, 0);

double lastTime = glfwGetTime();

double xpos, ypos;

double horizontalAngle = 0;
double verticalAngle = 0;

double speed = 100.0;

bool grabMouse = true;
bool grabMousePressedLastFrame = false;

glm::vec3 colorsByScale[] {
    { 1, 0, 0 },
    { 0, 1, 0 },
    { 0, 0, 1 },
    { 1, 1, 0 },
    { 1, 0, 1 },
    { 0, 1, 1 },
    { 0.5, 0.5, 0.5 },
    { 1, 1, 1 },
};

bool render(float x1, float z1, float x2, float z2, const std::vector<Plane>& planes) {
    if(!glfwWindowShouldClose(window)) {
        double currentTime = glfwGetTime();
        double deltaTime = glm::min(currentTime - lastTime, 0.1);
        update_fps_counter(window);

        if(grabMouse) {
            glfwGetCursorPos(window, &xpos, &ypos);
            glfwSetCursorPos(window, windowWidth/2.0, windowHeight/2.0);
            horizontalAngle += deltaTime * (windowWidth / 2.0 - xpos);
            verticalAngle += deltaTime * (windowHeight / 2.0 - ypos);
            if(horizontalAngle > 2*M_PI) {
                horizontalAngle -= 2*M_PI;
            } else if(horizontalAngle < 0) {
                horizontalAngle += 2*M_PI;
            }
            if(verticalAngle > M_PI/2-0.0001) {
                verticalAngle = M_PI/2-0.0001;
            } else if(verticalAngle < -M_PI/2+0.0001) {
                verticalAngle = -M_PI/2+0.0001;
            }
        }

        glm::vec3 direction(
                cos(verticalAngle) * sin(horizontalAngle),
                sin(verticalAngle),
                cos(verticalAngle) * cos(horizontalAngle)
                );
        glm::vec3 right = glm::vec3(
                sin(horizontalAngle - 3.14f/2.0f),
                0,
                cos(horizontalAngle - 3.14f/2.0f)
                );
        glm::vec3 up = glm::cross( right, direction );

        //update
        {
            if(grabMouse) {
                double speedDelta = speed * deltaTime;
                if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
                    camera_pos += glm::vec3(direction.x*speedDelta, direction.y*speedDelta, direction.z*speedDelta);
                }
                if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
                    camera_pos += glm::vec3(-right.x*speedDelta, -right.y*speedDelta, -right.z*speedDelta);
                }
                if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
                    camera_pos += glm::vec3(-direction.x*speedDelta, -direction.y*speedDelta, -direction.z*speedDelta);
                }
                if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
                    camera_pos += glm::vec3(right.x*speedDelta, right.y*speedDelta, right.z*speedDelta);
                }
                if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
                    camera_pos += glm::vec3(up.x*speedDelta, up.y*speedDelta, up.z*speedDelta);
                }
                if(glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) {
                    camera_pos += glm::vec3(-up.x*speedDelta, -up.y*speedDelta, -up.z*speedDelta);
                }
                if(glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS) {
                    std::cout << "PID" << ::getpid() << " (PPID: " << ::getppid() << ")" << std::endl;
                }
            }
            if(glfwGetKey(window, GLFW_KEY_U) == GLFW_PRESS) {
                if(!grabMousePressedLastFrame) {
                    grabMousePressedLastFrame = true;
                    if(grabMouse)
                        grabMouse = !grabMouse;
                    else {
                        glfwSetCursorPos(window, windowWidth/2.0, windowHeight/2.0);
                        grabMouse = !grabMouse;
                    }
                }
            } else {
                grabMousePressedLastFrame = false;
            }
        }

        //draw
        {
            glm::mat4 proj = glm::perspective(glm::radians(70.0f), (float) windowWidth / (float)windowHeight, 0.1f, 10000.0f);

            glm::mat4 view = glm::lookAt(
                    camera_pos,
                    camera_pos + direction,
                    glm::vec3(0,1,0)
                    );

            glm::mat4 vp = proj * view;
            glm::mat4 mvp;

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glViewport(0, 0, windowWidth, windowHeight);
            glUseProgram(shader_program);

            GLint matName = glGetUniformLocation(shader_program, "MVP");
            GLint in_colourName = glGetUniformLocation(shader_program, "in_colour");

            glBindVertexArray(vao);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

            {
                mvp = vp * glm::scale(glm::translate(glm::mat4(1.0f), glm::vec3(x1, 1, z1)), glm::vec3(x2, 1, z2));
                glUniformMatrix4fv(matName, 1, GL_FALSE, &mvp[0][0]);

                glUniform4f(in_colourName, 1, 1, 1, 0.25);
                glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_INT, nullptr);
            }

            for (const auto &plane : planes) {
                mvp = vp * glm::scale(glm::translate(glm::mat4(1.0f), glm::vec3(plane.x, -plane.scale/10.f, plane.z)), glm::vec3(plane.scale*0.9f));
                glUniformMatrix4fv(matName, 1, GL_FALSE, &mvp[0][0]);

                auto color = colorsByScale[((int)floor(log2(plane.scale))) & 7];
                glUniform4f(in_colourName, color.r, color.g, color.b, 1);
                glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_INT, nullptr);
            }
        }

        glfwPollEvents();

        glfwSwapBuffers(window);

        if (GLFW_PRESS == glfwGetKey(window, GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(window, 1);
        }

        lastTime = currentTime;

        return true;
    }

    // close GL context and any other GLFW resources
    glfwTerminate();
    return false;
}

